package com.classpets.backend.data.vo;

import com.classpets.backend.data.dto.StudentImportRowDTO;

import java.util.ArrayList;
import java.util.List;

public class StudentImportPreviewVO {
    private Integer totalRows;
    private Integer validRows;
    private Integer errorRows;
    private List<StudentImportRowDTO> rows;

    public StudentImportPreviewVO() {
        this.rows = new ArrayList<>();
    }

    public Integer getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(Integer totalRows) {
        this.totalRows = totalRows;
    }

    public Integer getValidRows() {
        return validRows;
    }

    public void setValidRows(Integer validRows) {
        this.validRows = validRows;
    }

    public Integer getErrorRows() {
        return errorRows;
    }

    public void setErrorRows(Integer errorRows) {
        this.errorRows = errorRows;
    }

    public List<StudentImportRowDTO> getRows() {
        return rows;
    }

    public void setRows(List<StudentImportRowDTO> rows) {
        this.rows = rows;
    }
}
