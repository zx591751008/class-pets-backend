package com.classpets.backend.growth;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ConfigurationProperties(prefix = "growth")
public class GrowthProperties {

    private List<Integer> levelThresholds = new ArrayList<>(
            Arrays.asList(0, 100, 250, 450, 700, 1000, 1400, 1900, 2500, 3200, 4000));

    private Integer overflowStep = 800;
    private Double expGainRatio = 10.0;

    public List<Integer> getLevelThresholds() {
        return levelThresholds;
    }

    public void setLevelThresholds(List<Integer> levelThresholds) {
        this.levelThresholds = levelThresholds;
    }

    public Integer getOverflowStep() {
        return overflowStep;
    }

    public void setOverflowStep(Integer overflowStep) {
        this.overflowStep = overflowStep;
    }

    public Double getExpGainRatio() {
        return expGainRatio;
    }

    public void setExpGainRatio(Double expGainRatio) {
        this.expGainRatio = expGainRatio;
    }
}
