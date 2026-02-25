package com.classpets.backend.leaderboard.vo;

import lombok.Data;

@Data
public class RankVO {
    private Long studentId;
    private String name;
    private String avatar;
    private String avatarImage; // For custom avatar URL
    private Integer score;
    private Integer rank;
    private Integer level;
    private String title;
    private String groupName;
}
