package com.classpets.backend.controller;

import com.classpets.backend.common.ApiResponse;
import com.classpets.backend.data.dto.ExportBundleRequest;
import com.classpets.backend.data.dto.RuleImportCommitRequest;
import com.classpets.backend.data.dto.StudentImportCommitRequest;
import com.classpets.backend.data.service.DataExportService;
import com.classpets.backend.data.service.RuleImportService;
import com.classpets.backend.data.service.StudentImportService;
import com.classpets.backend.data.vo.RuleImportCommitResultVO;
import com.classpets.backend.data.vo.RuleImportPreviewVO;
import com.classpets.backend.data.vo.StudentImportCommitResultVO;
import com.classpets.backend.data.vo.StudentImportPreviewVO;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/classes/{classId}/data")
public class DataController {

    private final DataExportService dataExportService;
    private final StudentImportService studentImportService;
    private final RuleImportService ruleImportService;

    public DataController(DataExportService dataExportService,
            StudentImportService studentImportService,
            RuleImportService ruleImportService) {
        this.dataExportService = dataExportService;
        this.studentImportService = studentImportService;
        this.ruleImportService = ruleImportService;
    }

    @GetMapping("/export/students")
    public ResponseEntity<byte[]> exportStudents(@PathVariable Long classId) {
        byte[] body = dataExportService.exportStudentsCsv(classId);
        return csvResponse("students.csv", body);
    }

    @GetMapping("/export/rules")
    public ResponseEntity<byte[]> exportRules(@PathVariable Long classId) {
        byte[] body = dataExportService.exportRulesCsv(classId);
        return csvResponse("rules.csv", body);
    }

    @GetMapping("/export/events")
    public ResponseEntity<byte[]> exportEvents(@PathVariable Long classId,
            @RequestParam(required = false) Long from,
            @RequestParam(required = false) Long to) {
        byte[] body = dataExportService.exportStudentEventsCsv(classId, from, to);
        return csvResponse("student_events.csv", body);
    }

    @GetMapping("/export/groups")
    public ResponseEntity<byte[]> exportGroups(@PathVariable Long classId) {
        byte[] body = dataExportService.exportGroupsCsv(classId);
        return csvResponse("groups.csv", body);
    }

    @GetMapping("/export/leaderboard")
    public ResponseEntity<byte[]> exportLeaderboard(@PathVariable Long classId,
            @RequestParam(defaultValue = "TOTAL") String type,
            @RequestParam(required = false) String category) {
        byte[] body = dataExportService.exportLeaderboardXlsx(classId, type, category);
        String safeType = (type == null ? "TOTAL" : type.toUpperCase()).replaceAll("[^A-Z_0-9-]", "_");
        StringBuilder filename = new StringBuilder("排行榜_").append(safeType);
        if (category != null && !category.trim().isEmpty()) {
            String safeCategory = category.trim().replaceAll("[\\\\/:*?\"<>|]", "_");
            filename.append('_').append(safeCategory);
        }
        filename.append(".xlsx");
        return xlsxResponse(filename.toString(), body);
    }

    @PostMapping("/export/bundle")
    public ResponseEntity<byte[]> exportBundle(@PathVariable Long classId,
            @RequestBody ExportBundleRequest request) {
        byte[] body = dataExportService.exportBundleXlsx(classId, request.getTypes(), request.getFrom(), request.getTo());
        return xlsxResponse("数据导出.xlsx", body);
    }

    @GetMapping("/export/redemptions")
    public ResponseEntity<byte[]> exportRedemptions(@PathVariable Long classId) {
        byte[] body = dataExportService.exportRedemptionsCsv(classId);
        return csvResponse("redemptions.csv", body);
    }

    @PostMapping("/import/students/preview")
    public ApiResponse<StudentImportPreviewVO> previewImportStudents(@PathVariable Long classId,
            @RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(studentImportService.preview(classId, file));
    }

    @PostMapping("/import/students/commit")
    public ApiResponse<StudentImportCommitResultVO> commitImportStudents(@PathVariable Long classId,
            @Valid @RequestBody StudentImportCommitRequest request) {
        return ApiResponse.ok(studentImportService.commit(classId, request));
    }

    @PostMapping("/import/students/errors-export")
    public ResponseEntity<byte[]> exportStudentImportErrors(@PathVariable Long classId,
            @Valid @RequestBody StudentImportCommitRequest request) {
        byte[] body = studentImportService.exportErrorRowsXlsx(classId, request.getRows());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(
                ContentDisposition.attachment().filename("students_import_failed_rows.xlsx").build());
        return ResponseEntity.ok().headers(headers).body(body);
    }

    @GetMapping("/import/students/template")
    public ResponseEntity<byte[]> downloadStudentImportTemplate(@PathVariable Long classId) {
        byte[] body = studentImportService.template(classId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(
                ContentDisposition.attachment().filename("students_import_template.xlsx").build());
        return ResponseEntity.ok().headers(headers).body(body);
    }

    @PostMapping("/import/rules/preview")
    public ApiResponse<RuleImportPreviewVO> previewImportRules(@PathVariable Long classId,
            @RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(ruleImportService.preview(classId, file));
    }

    @PostMapping("/import/rules/commit")
    public ApiResponse<RuleImportCommitResultVO> commitImportRules(@PathVariable Long classId,
            @Valid @RequestBody RuleImportCommitRequest request) {
        return ApiResponse.ok(ruleImportService.commit(classId, request));
    }

    @PostMapping("/import/rules/errors-export")
    public ResponseEntity<byte[]> exportRuleImportErrors(@PathVariable Long classId,
            @Valid @RequestBody RuleImportCommitRequest request) {
        byte[] body = ruleImportService.exportErrorRowsXlsx(classId, request.getRows());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(
                ContentDisposition.attachment().filename("rules_import_failed_rows.xlsx").build());
        return ResponseEntity.ok().headers(headers).body(body);
    }

    @GetMapping("/import/rules/template")
    public ResponseEntity<byte[]> downloadRuleImportTemplate(@PathVariable Long classId) {
        byte[] body = ruleImportService.template(classId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.attachment().filename("rules_import_template.xlsx").build());
        return ResponseEntity.ok().headers(headers).body(body);
    }

    private ResponseEntity<byte[]> csvResponse(String filename, byte[] body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv"));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build());
        return ResponseEntity.ok().headers(headers).body(body);
    }

    private ResponseEntity<byte[]> xlsxResponse(String filename, byte[] body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build());
        return ResponseEntity.ok().headers(headers).body(body);
    }
}
