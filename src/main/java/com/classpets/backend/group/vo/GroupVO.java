package com.classpets.backend.group.vo;

import lombok.Data;
import java.util.List;

@Data
public class GroupVO {
    private Long id;
    private String name;
    private String icon;
    private String note;
    private Integer points;
    private Integer memberTotalPoints; // Sum of all member total_points
    private List<MemberSummary> members;

    @Data
    public static class MemberSummary {
        private Long id;
        private String name;
        private String avatarImage; // First 100 chars or null
    }
}
