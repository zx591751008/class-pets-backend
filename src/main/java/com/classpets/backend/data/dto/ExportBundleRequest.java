package com.classpets.backend.data.dto;

import java.util.List;

public class ExportBundleRequest {
    private List<String> types;
    private Long from;
    private Long to;

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public Long getFrom() {
        return from;
    }

    public void setFrom(Long from) {
        this.from = from;
    }

    public Long getTo() {
        return to;
    }

    public void setTo(Long to) {
        this.to = to;
    }
}
