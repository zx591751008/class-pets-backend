package com.classpets.backend.data.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classpets.backend.classinfo.service.ClassInfoService;
import com.classpets.backend.common.BizException;
import com.classpets.backend.data.dto.StudentImportCommitRequest;
import com.classpets.backend.data.dto.StudentImportRowDTO;
import com.classpets.backend.data.vo.StudentImportCommitResultVO;
import com.classpets.backend.data.vo.StudentImportPreviewVO;
import com.classpets.backend.entity.GroupInfo;
import com.classpets.backend.entity.Student;
import com.classpets.backend.growth.service.GrowthConfigService;
import com.classpets.backend.mapper.GroupInfoMapper;
import com.classpets.backend.mapper.StudentMapper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StudentImportService {

    private static final int MAX_STUDENTS_PER_CLASS = 80;

    private final ClassInfoService classInfoService;
    private final StudentMapper studentMapper;
    private final GroupInfoMapper groupInfoMapper;
    private final GrowthConfigService growthConfigService;

    public StudentImportService(ClassInfoService classInfoService,
            StudentMapper studentMapper,
            GroupInfoMapper groupInfoMapper,
            GrowthConfigService growthConfigService) {
        this.classInfoService = classInfoService;
        this.studentMapper = studentMapper;
        this.groupInfoMapper = groupInfoMapper;
        this.growthConfigService = growthConfigService;
    }

    public StudentImportPreviewVO preview(Long classId, MultipartFile file) {
        ensureClassOwnership(classId);
        if (file == null || file.isEmpty()) {
            throw new BizException(40001, "请上传CSV文件");
        }

        List<StudentImportRowDTO> rows = parseRows(classId, file);
        int valid = 0;
        for (StudentImportRowDTO row : rows) {
            if (trim(row.getError()).isEmpty()) {
                valid++;
            }
        }

        StudentImportPreviewVO vo = new StudentImportPreviewVO();
        vo.setRows(rows);
        vo.setTotalRows(rows.size());
        vo.setValidRows(valid);
        vo.setErrorRows(rows.size() - valid);
        return vo;
    }

    public StudentImportCommitResultVO commit(Long classId, StudentImportCommitRequest request) {
        ensureClassOwnership(classId);
        if (request == null || request.getRows() == null || request.getRows().isEmpty()) {
            throw new BizException(40001, "导入数据不能为空");
        }

        List<Student> existing = studentMapper.selectList(new LambdaQueryWrapper<Student>()
                .eq(Student::getClassId, classId));
        Map<String, Student> byNo = new HashMap<>();
        for (Student student : existing) {
            String no = trim(student.getStudentNo());
            if (!no.isEmpty()) {
                byNo.put(no, student);
            }
        }

        int existingCount = existing.size();
        int createdInRequest = 0;

        List<GroupInfo> groups = groupInfoMapper.selectList(new LambdaQueryWrapper<GroupInfo>()
                .eq(GroupInfo::getClassId, classId));
        Map<String, GroupInfo> groupByName = new LinkedHashMap<>();
        for (GroupInfo g : groups) {
            groupByName.put(trim(g.getName()), g);
        }

        int created = 0;
        int updated = 0;
        int failed = 0;
        List<StudentImportCommitResultVO.RowErrorVO> errors = new ArrayList<>();

        for (StudentImportRowDTO row : request.getRows()) {
            try {
                String rowErr = validateRow(row, classId);
                if (!trim(rowErr).isEmpty()) {
                    throw new BizException(40001, rowErr);
                }

                String studentNo = trim(row.getStudentNo());
                Student student = studentNo.isEmpty() ? null : byNo.get(studentNo);
                if (student == null) {
                    if (existingCount + createdInRequest >= MAX_STUDENTS_PER_CLASS) {
                        throw new BizException(40001, "班级人数已达上限（最多80人）");
                    }
                    student = new Student();
                    student.setClassId(classId);
                    student.setStudentNo(studentNo.isEmpty() ? null : studentNo);
                    student.setTotalPoints(0);
                    student.setRedeemPoints(0);
                    student.setExp(0);
                    student.setLevel(1);
                    fillStudentFields(student, row, classId, groupByName);
                    studentMapper.insert(student);
                    if (!studentNo.isEmpty()) {
                        byNo.put(studentNo, student);
                    }
                    created++;
                    createdInRequest++;
                } else {
                    fillStudentFields(student, row, classId, groupByName);
                    studentMapper.updateById(student);
                    updated++;
                }
            } catch (Exception ex) {
                failed++;
                String msg = ex instanceof BizException ? ex.getMessage() : "导入失败";
                errors.add(new StudentImportCommitResultVO.RowErrorVO(row.getLineNo(), row.getStudentNo(), msg));
            }
        }

        StudentImportCommitResultVO result = new StudentImportCommitResultVO();
        result.setTotal(request.getRows().size());
        result.setCreated(created);
        result.setUpdated(updated);
        result.setFailed(failed);
        result.setErrors(errors);
        return result;
    }

    public byte[] template(Long classId) {
        ensureClassOwnership(classId);
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("students");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("姓名");
            header.createCell(1).setCellValue("学号");
            header.createCell(2).setCellValue("性别");
            header.createCell(3).setCellValue("分组");
            header.createCell(4).setCellValue("初始积分（可不填）");

            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("李小红");
            row1.createCell(1).setCellValue("02");
            row1.createCell(2).setCellValue("女");
            row1.createCell(3).setCellValue("火箭队");
            row1.createCell(4).setCellValue(56);

            Row row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue("张小明");
            row2.createCell(1).setCellValue("");
            row2.createCell(2).setCellValue("男");
            row2.createCell(3).setCellValue("火箭队");
            row2.createCell(4).setCellValue("");

            for (int i = 0; i <= 4; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new BizException(50001, "生成模板失败");
        }
    }

    public byte[] exportErrorRowsXlsx(Long classId, List<StudentImportRowDTO> rows) {
        ensureClassOwnership(classId);
        if (rows == null || rows.isEmpty()) {
            throw new BizException(40001, "没有可导出的失败行");
        }

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("failed_rows");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("行号");
            header.createCell(1).setCellValue("姓名");
            header.createCell(2).setCellValue("学号");
            header.createCell(3).setCellValue("性别");
            header.createCell(4).setCellValue("分组");
            header.createCell(5).setCellValue("初始积分");
            header.createCell(6).setCellValue("错误原因");
            header.createCell(7).setCellValue("修复建议");

            int rowIndex = 1;
            for (StudentImportRowDTO item : rows) {
                String message = trim(item.getError());
                if (message.isEmpty()) {
                    message = validateRow(item, classId);
                }
                if (message.isEmpty()) {
                    continue;
                }

                Row dataRow = sheet.createRow(rowIndex++);
                dataRow.createCell(0).setCellValue(item.getLineNo() == null ? 0 : item.getLineNo());
                dataRow.createCell(1).setCellValue(trim(item.getName()));
                dataRow.createCell(2).setCellValue(trim(item.getStudentNo()));
                dataRow.createCell(3).setCellValue(trim(item.getGender()));
                dataRow.createCell(4).setCellValue(trim(item.getGroupName()));
                dataRow.createCell(5).setCellValue(trim(item.getPoints()));
                dataRow.createCell(6).setCellValue(message);
                dataRow.createCell(7).setCellValue(buildErrorSuggestion(message));
            }

            if (rowIndex == 1) {
                throw new BizException(40001, "没有可导出的失败行");
            }

            for (int i = 0; i <= 7; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BizException(50001, "导出失败行失败");
        }
    }

    private List<StudentImportRowDTO> parseRows(Long classId, MultipartFile file) {
        String name = trim(file.getOriginalFilename()).toLowerCase();
        if (name.endsWith(".xlsx") || name.endsWith(".xls")) {
            return parseExcelRows(classId, file);
        }
        return parseCsvRows(classId, file);
    }

    private List<StudentImportRowDTO> parseCsvRows(Long classId, MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new BizException(40001, "CSV文件为空");
            }
            headerLine = removeBom(headerLine);
            List<String> headers = parseCsvLine(headerLine);
            Map<String, Integer> index = buildHeaderIndex(headers);
            if (!index.containsKey("name") || !index.containsKey("gender")) {
                throw new BizException(40001, "CSV表头缺少必填列：姓名, 性别");
            }

            List<StudentImportRowDTO> rows = new ArrayList<>();
            String line;
            int lineNo = 1;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (trim(line).isEmpty()) {
                    continue;
                }
                List<String> values = parseCsvLine(line);
                StudentImportRowDTO row = new StudentImportRowDTO();
                row.setLineNo(lineNo);
                row.setName(getValue(values, index.get("name")));
                row.setStudentNo(getValue(values, index.get("studentNo")));
                row.setGender(normalizeGender(getValue(values, index.get("gender"))));
                row.setGroupName(getValue(values, index.get("groupName")));
                row.setPoints(getValue(values, index.get("points")));

                row.setError(validateRow(row, classId));
                rows.add(row);
            }
            return rows;
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BizException(40001, "解析CSV失败，请检查文件编码与格式");
        }
    }

    private List<StudentImportRowDTO> parseExcelRows(Long classId, MultipartFile file) {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                throw new BizException(40001, "Excel文件为空");
            }
            DataFormatter formatter = new DataFormatter();
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                throw new BizException(40001, "Excel文件缺少表头");
            }

            List<String> headers = new ArrayList<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                headers.add(trim(formatter.formatCellValue(headerRow.getCell(i))));
            }

            Map<String, Integer> index = buildHeaderIndex(headers);
            if (!index.containsKey("name") || !index.containsKey("gender")) {
                throw new BizException(40001, "Excel表头缺少必填列：姓名, 性别");
            }

            List<StudentImportRowDTO> rows = new ArrayList<>();
            int start = sheet.getFirstRowNum() + 1;
            int end = sheet.getLastRowNum();
            for (int i = start; i <= end; i++) {
                Row rowObj = sheet.getRow(i);
                if (rowObj == null) {
                    continue;
                }
                StudentImportRowDTO row = new StudentImportRowDTO();
                row.setLineNo(i + 1);
                row.setName(readCell(rowObj, index.get("name"), formatter));
                row.setStudentNo(readCell(rowObj, index.get("studentNo"), formatter));
                row.setGender(normalizeGender(readCell(rowObj, index.get("gender"), formatter)));
                row.setGroupName(readCell(rowObj, index.get("groupName"), formatter));
                row.setPoints(readCell(rowObj, index.get("points"), formatter));

                if (trim(row.getName()).isEmpty() && trim(row.getStudentNo()).isEmpty() && trim(row.getGender()).isEmpty()
                        && trim(row.getGroupName()).isEmpty() && trim(row.getPoints()).isEmpty()) {
                    continue;
                }

                row.setError(validateRow(row, classId));
                rows.add(row);
            }
            return rows;
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BizException(40001, "解析Excel失败，请检查文件格式");
        }
    }

    private String readCell(Row row, Integer idx, DataFormatter formatter) {
        if (idx == null || idx < 0) {
            return "";
        }
        Cell cell = row.getCell(idx);
        if (cell == null) {
            return "";
        }
        return trim(formatter.formatCellValue(cell));
    }

    private String validateRow(StudentImportRowDTO row, Long classIdForPetCheck) {
        String name = trim(row.getName());
        String gender = trim(row.getGender());
        String pointsRaw = trim(row.getPoints());

        if (name.isEmpty()) {
            return "姓名不能为空";
        }
        if (!("男".equals(gender) || "女".equals(gender))) {
            return "性别必须为 男/女";
        }
        if (!pointsRaw.isEmpty()) {
            try {
                Integer.parseInt(pointsRaw);
            } catch (Exception ex) {
                return "初始积分必须是整数（可留空）";
            }
        }

        return "";
    }

    private void fillStudentFields(Student student, StudentImportRowDTO row, Long classId, Map<String, GroupInfo> groupByName) {
        student.setName(trim(row.getName()));
        student.setGender(trim(row.getGender()));
        String studentNo = trim(row.getStudentNo());
        if (!studentNo.isEmpty()) {
            student.setStudentNo(studentNo);
        }
        student.setUpdateTime(System.currentTimeMillis());

        Integer importPoints = parsePoints(row.getPoints());
        if (importPoints != null) {
            int total = importPoints;
            int redeem = Math.max(0, importPoints);
            double ratio = growthConfigService.resolveForClass(classId).getExpGainRatio();
            int exp = (int) Math.round(Math.max(0, importPoints) * ratio);
            int level = resolveLevelByExp(classId, exp);
            student.setTotalPoints(total);
            student.setRedeemPoints(redeem);
            student.setExp(exp);
            student.setLevel(level);
        }

        String groupName = trim(row.getGroupName());
        if (!groupName.isEmpty()) {
            GroupInfo group = groupByName.get(groupName);
            if (group == null) {
                group = new GroupInfo();
                group.setClassId(classId);
                group.setName(groupName);
                group.setIcon("👥");
                group.setPoints(0);
                groupInfoMapper.insert(group);
                groupByName.put(groupName, group);
            }
            student.setGroupId(group.getId());
        }

    }

    private Map<String, Integer> buildHeaderIndex(List<String> headers) {
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            String key = trim(headers.get(i));
            if ("姓名".equals(key) || "name".equalsIgnoreCase(key)) {
                index.put("name", i);
            } else if ("学号".equals(key) || "studentNo".equalsIgnoreCase(key) || "student_no".equalsIgnoreCase(key)) {
                index.put("studentNo", i);
            } else if ("性别".equals(key) || "gender".equalsIgnoreCase(key)) {
                index.put("gender", i);
            } else if ("分组".equals(key) || "group".equalsIgnoreCase(key) || "groupName".equalsIgnoreCase(key)) {
                index.put("groupName", i);
            } else if ("初始积分（可不填）".equals(key) || "初始积分".equals(key) || "积分".equals(key)
                    || "points".equalsIgnoreCase(key) || "total".equalsIgnoreCase(key)
                    || "initialPoints".equalsIgnoreCase(key) || "initial_points".equalsIgnoreCase(key)) {
                index.put("points", i);
            }
        }
        return index;
    }

    private Integer parsePoints(String raw) {
        String v = trim(raw);
        if (v.isEmpty()) {
            return null;
        }
        return Integer.parseInt(v);
    }

    private int resolveLevelByExp(Long classId, int exp) {
        int safeExp = Math.max(0, exp);
        List<Integer> levels = growthConfigService.resolveForClass(classId).getLevelThresholds();
        if (levels == null || levels.isEmpty()) {
            return 1;
        }
        int level = 1;
        for (int i = 0; i < levels.size(); i++) {
            if (safeExp >= levels.get(i)) {
                level = i + 1;
            } else {
                break;
            }
        }
        return Math.min(level, levels.size());
    }

    private List<String> parseCsvLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
                continue;
            }
            if (c == ',' && !inQuotes) {
                out.add(current.toString().trim());
                current.setLength(0);
                continue;
            }
            current.append(c);
        }
        out.add(current.toString().trim());
        return out;
    }

    private String removeBom(String text) {
        if (text != null && !text.isEmpty() && text.charAt(0) == '\uFEFF') {
            return text.substring(1);
        }
        return text;
    }

    private String normalizeGender(String raw) {
        String v = trim(raw);
        if ("男".equals(v) || "m".equalsIgnoreCase(v) || "male".equalsIgnoreCase(v)) {
            return "男";
        }
        if ("女".equals(v) || "f".equalsIgnoreCase(v) || "female".equalsIgnoreCase(v)) {
            return "女";
        }
        return v;
    }

    private String getValue(List<String> values, Integer idx) {
        if (idx == null || idx < 0 || idx >= values.size()) {
            return "";
        }
        return trim(values.get(idx));
    }

    private String buildErrorSuggestion(String message) {
        String safeMessage = trim(message);
        if (safeMessage.contains("姓名")) {
            return "请补充姓名，不能为空";
        }
        if (safeMessage.contains("性别")) {
            return "性别仅支持 男 或 女";
        }
        if (safeMessage.contains("积分")) {
            return "初始积分可留空；若填写请使用整数，如 0、10、-5";
        }
        return "请根据错误原因修正后再次导入";
    }

    private void ensureClassOwnership(Long classId) {
        if (!classInfoService.isOwnedByCurrentTeacher(classId)) {
            throw new BizException(40301, "无权访问此班级");
        }
    }

    private String trim(String text) {
        return text == null ? "" : text.trim();
    }
}
