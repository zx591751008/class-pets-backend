package com.classpets.backend.data.dto;

import javax.validation.constraints.NotEmpty;
import java.util.List;

public class StudentImportCommitRequest {

    @NotEmpty
    private List<StudentImportRowDTO> rows;

    public List<StudentImportRowDTO> getRows() {
        return rows;
    }

    public void setRows(List<StudentImportRowDTO> rows) {
        this.rows = rows;
    }
}
