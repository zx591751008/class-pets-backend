package com.classpets.backend.data.vo;

import java.util.ArrayList;
import java.util.List;

public class RuleImportCommitResultVO {
    private Integer total;
    private Integer created;
    private Integer updated;
    private Integer skipped;
    private Integer failed;
    private List<RowErrorVO> errors;

    public RuleImportCommitResultVO() {
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

    public Integer getSkipped() {
        return skipped;
    }

    public void setSkipped(Integer skipped) {
        this.skipped = skipped;
    }

    public List<RowErrorVO> getErrors() {
        return errors;
    }

    public void setErrors(List<RowErrorVO> errors) {
        this.errors = errors;
    }

    public static class RowErrorVO {
        private Integer lineNo;
        private String content;
        private String message;

        public RowErrorVO() {
        }

        public RowErrorVO(Integer lineNo, String content, String message) {
            this.lineNo = lineNo;
            this.content = content;
            this.message = message;
        }

        public Integer getLineNo() {
            return lineNo;
        }

        public void setLineNo(Integer lineNo) {
            this.lineNo = lineNo;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
