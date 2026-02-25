package com.classpets.backend.growth.vo;

import java.util.List;

public class GrowthConfigVO {
    private List<LevelItemVO> levelItems;
    private List<Integer> levelThresholds;
    private Integer overflowStep;
    private Double expGainRatio;
    private EvolutionConfigVO evolution;
    private List<PetRouteVO> petRoutes;
    private Boolean customized;

    public List<LevelItemVO> getLevelItems() {
        return levelItems;
    }

    public void setLevelItems(List<LevelItemVO> levelItems) {
        this.levelItems = levelItems;
    }

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

    public EvolutionConfigVO getEvolution() {
        return evolution;
    }

    public void setEvolution(EvolutionConfigVO evolution) {
        this.evolution = evolution;
    }

    public List<PetRouteVO> getPetRoutes() {
        return petRoutes;
    }

    public void setPetRoutes(List<PetRouteVO> petRoutes) {
        this.petRoutes = petRoutes;
    }

    public Boolean getCustomized() {
        return customized;
    }

    public void setCustomized(Boolean customized) {
        this.customized = customized;
    }

    public static class LevelItemVO {
        private Integer level;
        private String name;
        private Integer threshold;

        public Integer getLevel() {
            return level;
        }

        public void setLevel(Integer level) {
            this.level = level;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getThreshold() {
            return threshold;
        }

        public void setThreshold(Integer threshold) {
            this.threshold = threshold;
        }
    }

    public static class EvolutionConfigVO {
        private Integer stage1MaxLevel;
        private Integer stage2MaxLevel;
        private Integer stage3StartLevel;

        public Integer getStage1MaxLevel() {
            return stage1MaxLevel;
        }

        public void setStage1MaxLevel(Integer stage1MaxLevel) {
            this.stage1MaxLevel = stage1MaxLevel;
        }

        public Integer getStage2MaxLevel() {
            return stage2MaxLevel;
        }

        public void setStage2MaxLevel(Integer stage2MaxLevel) {
            this.stage2MaxLevel = stage2MaxLevel;
        }

        public Integer getStage3StartLevel() {
            return stage3StartLevel;
        }

        public void setStage3StartLevel(Integer stage3StartLevel) {
            this.stage3StartLevel = stage3StartLevel;
        }
    }

    public static class PetRouteVO {
        private String id;
        private String name;
        private Boolean enabled;
        private List<PetStageVO> stages;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public List<PetStageVO> getStages() {
            return stages;
        }

        public void setStages(List<PetStageVO> stages) {
            this.stages = stages;
        }
    }

    public static class PetStageVO {
        private Integer stage;
        private String image;

        public Integer getStage() {
            return stage;
        }

        public void setStage(Integer stage) {
            this.stage = stage;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }
    }
}
