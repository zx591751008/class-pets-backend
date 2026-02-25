package com.classpets.backend.data.vo;

import java.util.ArrayList;
import java.util.List;

public class StudentImportCommitResultVO {
    private Integer total;
    private Integer created;
    private Integer updated;
    private Integer failed;
    private List<RowErrorVO> errors;

    public StudentImportCommitResultVO() {
        this.errors = new ArrayList<>();
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getCreated() {
        return created;
    }

    public void setCreated(Integer created) {
        this.created = created;
    }

    public Integer getUpdated() {
        return updated;
    }

    public void setUpdated(Integer updated) {
        this.updated = updated;
    }

    public Integer getFailed() {
        return failed;
    }

    public void setFailed(Integer failed) {
        this.failed = failed;
    }

    public List<RowErrorVO> getErrors() {
        return errors;
    }

    public void setErrors(List<RowErrorVO> errors) {
        this.errors = errors;
    }

    public static class RowErrorVO {
        private Integer lineNo;
        private String studentNo;
        private String message;

        public RowErrorVO() {
        }

        public RowErrorVO(Integer lineNo, String studentNo, String message) {
            this.lineNo = lineNo;
            this.studentNo = studentNo;
            this.message = message;
        }

        public Integer getLineNo() {
            return lineNo;
        }

        public void setLineNo(Integer lineNo) {
            this.lineNo = lineNo;
        }

        public String getStudentNo() {
            return studentNo;
        }

        public void setStudentNo(String studentNo) {
            this.studentNo = studentNo;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
