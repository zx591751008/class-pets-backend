package com.classpets.backend.student.vo;

import java.util.ArrayList;
import java.util.List;

public class BatchScoreResultVO {
    private Integer total;
    private Integer successCount;
    private Integer failedCount;
    private List<Long> successIds;
    private List<BatchFailureVO> failures;

    public BatchScoreResultVO() {
        this.successIds = new ArrayList<>();
        this.failures = new ArrayList<>();
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(Integer failedCount) {
        this.failedCount = failedCount;
    }

    public List<Long> getSuccessIds() {
        return successIds;
    }

    public void setSuccessIds(List<Long> successIds) {
        this.successIds = successIds;
    }

    public List<BatchFailureVO> getFailures() {
        return failures;
    }

    public void setFailures(List<BatchFailureVO> failures) {
        this.failures = failures;
    }

    public static class BatchFailureVO {
        private Long studentId;
        private String message;

        public BatchFailureVO() {
        }

        public BatchFailureVO(Long studentId, String message) {
            this.studentId = studentId;
            this.message = message;
        }

        public Long getStudentId() {
            return studentId;
        }

        public void setStudentId(Long studentId) {
            this.studentId = studentId;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
