package com.classpets.backend.student.vo;

import java.util.ArrayList;
import java.util.List;

public class BatchAdoptResultVO {
    private Boolean dryRun;
    private Integer totalStudents;
    private Integer assignableCount;
    private Integer assignedCount;
    private Integer skippedCount;
    private List<ItemVO> items;

    public BatchAdoptResultVO() {
        this.items = new ArrayList<>();
    }

    public Boolean getDryRun() {
        return dryRun;
    }

    public void setDryRun(Boolean dryRun) {
        this.dryRun = dryRun;
    }

    public Integer getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(Integer totalStudents) {
        this.totalStudents = totalStudents;
    }

    public Integer getAssignableCount() {
        return assignableCount;
    }

    public void setAssignableCount(Integer assignableCount) {
        this.assignableCount = assignableCount;
    }

    public Integer getAssignedCount() {
        return assignedCount;
    }

    public void setAssignedCount(Integer assignedCount) {
        this.assignedCount = assignedCount;
    }

    public Integer getSkippedCount() {
        return skippedCount;
    }

    public void setSkippedCount(Integer skippedCount) {
        this.skippedCount = skippedCount;
    }

    public List<ItemVO> getItems() {
        return items;
    }

    public void setItems(List<ItemVO> items) {
        this.items = items;
    }

    public static class ItemVO {
        private Long studentId;
        private String studentName;
        private String studentNo;
        private String petId;
        private String petName;
        private String status;
        private String reason;

        public Long getStudentId() {
            return studentId;
        }

        public void setStudentId(Long studentId) {
            this.studentId = studentId;
        }

        public String getStudentName() {
            return studentName;
        }

        public void setStudentName(String studentName) {
            this.studentName = studentName;
        }

        public String getStudentNo() {
            return studentNo;
        }

        public void setStudentNo(String studentNo) {
            this.studentNo = studentNo;
        }

        public String getPetId() {
            return petId;
        }

        public void setPetId(String petId) {
            this.petId = petId;
        }

        public String getPetName() {
            return petName;
        }

        public void setPetName(String petName) {
            this.petName = petName;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
