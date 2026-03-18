package com.classpets.backend.history.vo;

import java.util.List;

public class HistoryPageVO {

    private List<HistoryLogVO> records;
    private long total;
    private int page;
    private int size;

    public List<HistoryLogVO> getRecords() {
        return records;
    }

    public void setRecords(List<HistoryLogVO> records) {
        this.records = records;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
