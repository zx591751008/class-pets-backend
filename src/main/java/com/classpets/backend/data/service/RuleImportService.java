package com.classpets.backend.data.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classpets.backend.classinfo.service.ClassInfoService;
import com.classpets.backend.common.BizException;
import com.classpets.backend.data.dto.RuleImportCommitRequest;
import com.classpets.backend.data.dto.RuleImportRowDTO;
import com.classpets.backend.data.vo.RuleImportCommitResultVO;
import com.classpets.backend.data.vo.RuleImportPreviewVO;
import com.classpets.backend.entity.RuleInfo;
import com.classpets.backend.mapper.RuleInfoMapper;
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
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RuleImportService {

    private final ClassInfoService classInfoService;
    private final RuleInfoMapper ruleInfoMapper;

    public RuleImportService(ClassInfoService classInfoService, RuleInfoMapper ruleInfoMapper) {
        this.classInfoService = classInfoService;
        this.ruleInfoMapper = ruleInfoMapper;
    }

    public RuleImportPreviewVO preview(Long classId, MultipartFile file) {
        ensureClassOwnership(classId);
        if (file == null || file.isEmpty()) {
            throw new BizException(40001, "请上传规则导入文件");
        }

        List<RuleImportRowDTO> rows = parseRows(file);
        int valid = 0;
        for (RuleImportRowDTO row : rows) {
            if (trim(row.getError()).isEmpty()) {
                valid++;
            }
        }

        RuleImportPreviewVO vo = new RuleImportPreviewVO();
        vo.setRows(rows);
        vo.setTotalRows(rows.size());
        vo.setValidRows(valid);
        vo.setErrorRows(rows.size() - valid);
        return vo;
    }

    public RuleImportCommitResultVO commit(Long classId, RuleImportCommitRequest request) {
        ensureClassOwnership(classId);
        if (request == null || request.getRows() == null || request.getRows().isEmpty()) {
            throw new BizException(40001, "导入规则不能为空");
        }

        String conflictPolicy = normalizeConflictPolicy(request.getConflictPolicy());

        int created = 0;
        int updated = 0;
        int skipped = 0;
        int failed = 0;
        List<RuleImportCommitResultVO.RowErrorVO> errors = new ArrayList<>();

        for (RuleImportRowDTO row : request.getRows()) {
            try {
                String rowErr = validateRow(row);
                if (!trim(rowErr).isEmpty()) {
                    throw new BizException(40001, rowErr);
                }

                String normalizedType = normalizeType(row.getType());
                Integer normalizedTargetType = parseTargetType(row.getTargetType());
                RuleInfo existing = findByNaturalKey(classId, row.getContent(), normalizedType, normalizedTargetType);
                if (existing == null) {
                    RuleInfo rule = new RuleInfo();
                    applyRow(classId, row, rule, normalizedType, normalizedTargetType);
                    ruleInfoMapper.insert(rule);
                    created++;
                } else {
                    if ("skip".equals(conflictPolicy)) {
                        skipped++;
                    } else {
                        applyRow(classId, row, existing, normalizedType, normalizedTargetType);
                        ruleInfoMapper.updateById(existing);
                        updated++;
                    }
                }
            } catch (Exception ex) {
                failed++;
                String msg = ex instanceof BizException ? ex.getMessage() : "导入规则失败";
                errors.add(new RuleImportCommitResultVO.RowErrorVO(row.getLineNo(), row.getContent(), msg));
            }
        }

        RuleImportCommitResultVO result = new RuleImportCommitResultVO();
        result.setTotal(request.getRows().size());
        result.setCreated(created);
        result.setUpdated(updated);
        result.setSkipped(skipped);
        result.setFailed(failed);
        result.setErrors(errors);
        return result;
    }

    public byte[] template(Long classId) {
        ensureClassOwnership(classId);
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet studentSheet = workbook.createSheet("个人规则");
            fillTemplateSheet(studentSheet, "课堂积极发言", 2, "加分", "课堂表现");

            Sheet groupSheet = workbook.createSheet("小组规则");
            fillTemplateSheet(groupSheet, "小组作业未按时提交", 3, "扣分", "作业管理");

            workbook.setActiveSheet(0);

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new BizException(50001, "生成规则导入模板失败");
        }
    }

    public byte[] exportErrorRowsXlsx(Long classId, List<RuleImportRowDTO> rows) {
        ensureClassOwnership(classId);
        if (rows == null || rows.isEmpty()) {
            throw new BizException(40001, "没有可导出的失败行");
        }

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("failed_rows");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("行号");
            header.createCell(1).setCellValue("规则内容");
            header.createCell(2).setCellValue("分值");
            header.createCell(3).setCellValue("类型");
            header.createCell(4).setCellValue("作用对象");
            header.createCell(5).setCellValue("分类");
            header.createCell(6).setCellValue("错误原因");
            header.createCell(7).setCellValue("修复建议");

            int rowIndex = 1;
            for (RuleImportRowDTO item : rows) {
                String message = trim(item.getError());
                if (message.isEmpty()) {
                    message = validateRow(item);
                }
                if (message.isEmpty()) {
                    continue;
                }

                Row dataRow = sheet.createRow(rowIndex++);
                dataRow.createCell(0).setCellValue(item.getLineNo() == null ? 0 : item.getLineNo());
                dataRow.createCell(1).setCellValue(trim(item.getContent()));
                dataRow.createCell(2).setCellValue(trim(item.getPoints()));
                dataRow.createCell(3).setCellValue(trim(item.getType()));
                dataRow.createCell(4).setCellValue(trim(item.getTargetType()));
                dataRow.createCell(5).setCellValue(trim(item.getCategory()));
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

    private List<RuleImportRowDTO> parseRows(MultipartFile file) {
        String name = trim(file.getOriginalFilename()).toLowerCase();
        if (name.endsWith(".xlsx") || name.endsWith(".xls")) {
            return parseExcelRows(file);
        }
        return parseCsvRows(file);
    }

    private List<RuleImportRowDTO> parseCsvRows(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new BizException(40001, "CSV文件为空");
            }
            headerLine = removeBom(headerLine);
            List<String> headers = parseCsvLine(headerLine);
            Map<String, Integer> index = buildHeaderIndex(headers);
            if (!index.containsKey("content") || !index.containsKey("points") || !index.containsKey("type")) {
                throw new BizException(40001, "CSV表头缺少必填列：规则内容, 分值, 类型");
            }

            List<RuleImportRowDTO> rows = new ArrayList<>();
            String line;
            int lineNo = 1;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (trim(line).isEmpty()) {
                    continue;
                }
                List<String> values = parseCsvLine(line);
                RuleImportRowDTO row = new RuleImportRowDTO();
                row.setLineNo(lineNo);
                row.setContent(getValue(values, index.get("content")));
                row.setPoints(getValue(values, index.get("points")));
                row.setType(getValue(values, index.get("type")));
                row.setTargetType(getValue(values, index.get("targetType")));
                row.setCategory(getValue(values, index.get("category")));
                row.setCooldownHours(getValue(values, index.get("cooldownHours")));
                row.setStackable(getValue(values, index.get("stackable")));
                row.setError(validateRow(row));
                rows.add(row);
            }
            return rows;
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BizException(40001, "解析CSV失败，请检查文件编码与格式");
        }
    }

    private List<RuleImportRowDTO> parseExcelRows(MultipartFile file) {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            if (workbook.getNumberOfSheets() <= 0) {
                throw new BizException(40001, "Excel文件为空");
            }
            DataFormatter formatter = new DataFormatter();
            List<RuleImportRowDTO> rows = new ArrayList<>();
            boolean parsedAnySheet = false;

            for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
                Sheet sheet = workbook.getSheetAt(s);
                if (sheet == null) {
                    continue;
                }

                Row headerRow = sheet.getRow(sheet.getFirstRowNum());
                if (headerRow == null) {
                    continue;
                }

                List<String> headers = new ArrayList<>();
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    headers.add(trim(formatter.formatCellValue(headerRow.getCell(i))));
                }

                Map<String, Integer> index = buildHeaderIndex(headers);
                if (!index.containsKey("content") || !index.containsKey("points") || !index.containsKey("type")) {
                    continue;
                }
                parsedAnySheet = true;

                String defaultTargetType = defaultTargetTypeBySheetName(sheet.getSheetName());

                int start = sheet.getFirstRowNum() + 1;
                int end = sheet.getLastRowNum();
                for (int i = start; i <= end; i++) {
                    Row rowObj = sheet.getRow(i);
                    if (rowObj == null) {
                        continue;
                    }

                    RuleImportRowDTO row = new RuleImportRowDTO();
                    row.setLineNo(i + 1);
                    row.setContent(readCell(rowObj, index.get("content"), formatter));
                    row.setPoints(readCell(rowObj, index.get("points"), formatter));
                    row.setType(readCell(rowObj, index.get("type"), formatter));
                    row.setTargetType(index.containsKey("targetType")
                            ? readCell(rowObj, index.get("targetType"), formatter)
                            : defaultTargetType);
                    row.setCategory(readCell(rowObj, index.get("category"), formatter));
                    row.setCooldownHours(readCell(rowObj, index.get("cooldownHours"), formatter));
                    row.setStackable(readCell(rowObj, index.get("stackable"), formatter));

                    if (trim(row.getContent()).isEmpty() && trim(row.getPoints()).isEmpty() && trim(row.getType()).isEmpty()
                            && trim(row.getTargetType()).isEmpty() && trim(row.getCategory()).isEmpty()
                            && trim(row.getCooldownHours()).isEmpty() && trim(row.getStackable()).isEmpty()) {
                        continue;
                    }

                    row.setError(validateRow(row));
                    rows.add(row);
                }
            }

            if (!parsedAnySheet) {
                throw new BizException(40001, "Excel表头缺少必填列：规则内容, 分值, 类型");
            }
            return rows;
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BizException(40001, "解析Excel失败，请检查文件格式");
        }
    }

    private void fillTemplateSheet(Sheet sheet, String sampleContent, int samplePoints, String sampleType,
            String sampleCategory) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("规则内容");
        header.createCell(1).setCellValue("分值");
        header.createCell(2).setCellValue("类型");
        header.createCell(3).setCellValue("分类");

        Row row = sheet.createRow(1);
        row.createCell(0).setCellValue(sampleContent);
        row.createCell(1).setCellValue(samplePoints);
        row.createCell(2).setCellValue(sampleType);
        row.createCell(3).setCellValue(sampleCategory);

        for (int i = 0; i <= 3; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private String defaultTargetTypeBySheetName(String sheetName) {
        String name = trim(sheetName).toLowerCase();
        if (name.contains("小组") || name.contains("group")) {
            return "1";
        }
        if (name.contains("个人") || name.contains("学生") || name.contains("student") || name.contains("personal")) {
            return "0";
        }
        return "";
    }

    private void applyRow(Long classId, RuleImportRowDTO row, RuleInfo rule, String normalizedType, Integer normalizedTargetType) {
        rule.setClassId(classId);
        rule.setContent(trim(row.getContent()));
        rule.setPoints(Integer.parseInt(trim(row.getPoints())));
        rule.setType(normalizedType);
        rule.setTargetType(normalizedTargetType);
        rule.setCategory(defaultCategory(row.getCategory()));
        rule.setCooldownHours(parseCooldownHours(row.getCooldownHours()));
        rule.setStackable(parseStackable(row.getStackable()));
        if (rule.getEnabled() == null) {
            rule.setEnabled(1);
        }
    }

    private RuleInfo findByNaturalKey(Long classId, String content, String type, Integer targetType) {
        List<RuleInfo> rules = ruleInfoMapper.selectList(new LambdaQueryWrapper<RuleInfo>()
                .eq(RuleInfo::getClassId, classId)
                .eq(RuleInfo::getContent, trim(content))
                .eq(RuleInfo::getType, type)
                .eq(RuleInfo::getTargetType, targetType)
                .last("limit 1"));
        return rules.isEmpty() ? null : rules.get(0);
    }

    private Map<String, Integer> buildHeaderIndex(List<String> headers) {
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            String key = trim(headers.get(i));
            if ("规则内容".equals(key) || "content".equalsIgnoreCase(key)) {
                index.put("content", i);
            } else if ("分值".equals(key) || "points".equalsIgnoreCase(key)) {
                index.put("points", i);
            } else if ("类型".equals(key) || "type".equalsIgnoreCase(key)) {
                index.put("type", i);
            } else if ("作用对象".equals(key) || "targetType".equalsIgnoreCase(key) || "target".equalsIgnoreCase(key)) {
                index.put("targetType", i);
            } else if ("分类".equals(key) || "category".equalsIgnoreCase(key)) {
                index.put("category", i);
            } else if ("冷却小时".equals(key) || "cooldownHours".equalsIgnoreCase(key) || "cooldown".equalsIgnoreCase(key)) {
                index.put("cooldownHours", i);
            } else if ("可叠加".equals(key) || "stackable".equalsIgnoreCase(key)) {
                index.put("stackable", i);
            }
        }
        return index;
    }

    private String validateRow(RuleImportRowDTO row) {
        if (trim(row.getContent()).isEmpty()) {
            return "规则内容不能为空";
        }

        String points = trim(row.getPoints());
        if (points.isEmpty()) {
            return "分值不能为空";
        }
        try {
            int value = Integer.parseInt(points);
            if (value <= 0) {
                return "分值必须大于0";
            }
        } catch (Exception ex) {
            return "分值必须是整数";
        }

        String type = normalizeType(row.getType());
        if (type.isEmpty()) {
            return "类型必须为 add/deduct 或 加分/扣分";
        }

        if (parseTargetType(row.getTargetType()) == null) {
            return "作用对象必须为 个人/小组 或 0/1";
        }

        String cooldown = trim(row.getCooldownHours());
        if (!cooldown.isEmpty()) {
            try {
                BigDecimal value = new BigDecimal(cooldown);
                if (value.compareTo(BigDecimal.ZERO) < 0) {
                    return "冷却小时不能小于0";
                }
            } catch (Exception ex) {
                return "冷却小时必须是数字";
            }
        }

        String stackable = trim(row.getStackable());
        if (!stackable.isEmpty() && parseStackable(stackable) == null) {
            return "可叠加必须为 是/否 或 1/0";
        }
        return "";
    }

    private String buildErrorSuggestion(String message) {
        String safeMessage = trim(message);
        if (safeMessage.contains("规则内容")) {
            return "请填写规则内容";
        }
        if (safeMessage.contains("分值")) {
            return "分值请填写大于0的整数";
        }
        if (safeMessage.contains("类型")) {
            return "类型填写 add/deduct 或 加分/扣分";
        }
        if (safeMessage.contains("作用对象")) {
            return "作用对象填写 个人/小组 或 0/1";
        }
        if (safeMessage.contains("冷却小时")) {
            return "冷却小时需为不小于0的数字";
        }
        if (safeMessage.contains("可叠加")) {
            return "可叠加填写 是/否 或 1/0";
        }
        return "请根据错误原因修正后重新导入";
    }

    private String normalizeType(String raw) {
        String value = trim(raw).toLowerCase();
        if ("add".equals(value) || "加分".equals(trim(raw))) {
            return "add";
        }
        if ("deduct".equals(value) || "扣分".equals(trim(raw))) {
            return "deduct";
        }
        return "";
    }

    private Integer parseTargetType(String raw) {
        String value = trim(raw);
        if (value.isEmpty()) {
            return 0;
        }
        if ("0".equals(value) || "个人".equals(value) || "学生".equals(value) || "student".equalsIgnoreCase(value)) {
            return 0;
        }
        if ("1".equals(value) || "小组".equals(value) || "group".equalsIgnoreCase(value)) {
            return 1;
        }
        return null;
    }

    private BigDecimal parseCooldownHours(String raw) {
        String value = trim(raw);
        if (value.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value);
    }

    private Integer parseStackable(String raw) {
        String value = trim(raw);
        if (value.isEmpty()) {
            return 1;
        }
        if ("1".equals(value) || "是".equals(value) || "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value)) {
            return 1;
        }
        if ("0".equals(value) || "否".equals(value) || "false".equalsIgnoreCase(value) || "no".equalsIgnoreCase(value)) {
            return 0;
        }
        return null;
    }

    private String defaultCategory(String raw) {
        String value = trim(raw);
        return value.isEmpty() ? "其他" : value;
    }

    private String normalizeConflictPolicy(String raw) {
        String value = trim(raw).toLowerCase();
        if ("overwrite".equals(value) || "update".equals(value) || "replace".equals(value)) {
            return "overwrite";
        }
        return "skip";
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

    private String getValue(List<String> values, Integer idx) {
        if (idx == null || idx < 0 || idx >= values.size()) {
            return "";
        }
        return trim(values.get(idx));
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
