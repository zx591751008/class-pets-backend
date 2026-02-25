package com.classpets.backend.data.dto;

import javax.validation.constraints.NotEmpty;
import java.util.List;

public class RuleImportCommitRequest {

    @NotEmpty
    private List<RuleImportRowDTO> rows;

    private String conflictPolicy;

    public List<RuleImportRowDTO> getRows() {
        return rows;
    }

    public void setRows(List<RuleImportRowDTO> rows) {
        this.rows = rows;
    }

    public String getConflictPolicy() {
        return conflictPolicy;
    }

    public void setConflictPolicy(String conflictPolicy) {
        this.conflictPolicy = conflictPolicy;
    }
}
