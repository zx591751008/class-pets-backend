package com.classpets.backend.growth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classpets.backend.classinfo.service.ClassInfoService;
import com.classpets.backend.common.BizException;
import com.classpets.backend.entity.ClassConfig;
import com.classpets.backend.growth.GrowthProperties;
import com.classpets.backend.growth.dto.GrowthConfigUpdateRequest;
import com.classpets.backend.growth.vo.GrowthConfigVO;
import com.classpets.backend.mapper.ClassConfigMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GrowthConfigService {
    private static final TypeReference<List<Integer>> INTEGER_LIST_TYPE = new TypeReference<List<Integer>>() {
    };

    private final ClassConfigMapper classConfigMapper;
    private final ClassInfoService classInfoService;
    private final GrowthProperties growthProperties;
    private final ObjectMapper objectMapper;

    public GrowthConfigService(ClassConfigMapper classConfigMapper,
            ClassInfoService classInfoService,
            GrowthProperties growthProperties,
            ObjectMapper objectMapper) {
        this.classConfigMapper = classConfigMapper;
        this.classInfoService = classInfoService;
        this.growthProperties = growthProperties;
        this.objectMapper = objectMapper;
    }

    public GrowthConfigVO getForClass(Long classId) {
        ensureClassOwnership(classId);
        ResolvedGrowthConfig resolved = resolveForClass(classId);
        return toVO(resolved);
    }

    public GrowthConfigVO updateForClass(Long classId, GrowthConfigUpdateRequest request) {
        ensureClassOwnership(classId);
        if (request == null) {
            throw new BizException(40001, "请求不能为空");
        }
        ResolvedGrowthConfig current = resolveForClass(classId);

        List<LevelItem> levelItems;
        if (request.getLevelItems() != null && !request.getLevelItems().isEmpty()) {
            levelItems = sanitizeLevelItems(fromRequestLevelItems(request.getLevelItems()));
        } else if (request.getLevelThresholds() != null && !request.getLevelThresholds().isEmpty()) {
            levelItems = sanitizeLevelItems(levelItemsFromThresholds(request.getLevelThresholds()));
        } else {
            levelItems = current.getLevelItems();
        }

        int overflowStep = request.getOverflowStep() == null
                ? current.getOverflowStep()
                : sanitizeOverflowStep(request.getOverflowStep());
        double expGainRatio = request.getExpGainRatio() == null
                ? current.getExpGainRatio()
                : sanitizeExpGainRatio(request.getExpGainRatio());

        EvolutionConfig evolution = request.getEvolution() == null
                ? current.getEvolution()
                : sanitizeEvolution(fromRequestEvolution(request.getEvolution()));

        List<PetRoute> petRoutes = request.getPetRoutes() == null
                ? current.getPetRoutes()
                : sanitizePetRoutes(fromRequestPetRoutes(request.getPetRoutes()));

        ClassConfig config = classConfigMapper.selectOne(new LambdaQueryWrapper<ClassConfig>()
                .eq(ClassConfig::getClassId, classId)
                .last("LIMIT 1"));
        if (config == null) {
            config = new ClassConfig();
            config.setClassId(classId);
        }

        config.setLevels(toLevelsJson(levelItems, overflowStep, expGainRatio));
        config.setEvolution(toEvolutionJson(evolution));
        config.setPets(toPetsJson(petRoutes));

        if (config.getId() == null) {
            classConfigMapper.insert(config);
        } else {
            classConfigMapper.updateById(config);
        }

        ResolvedGrowthConfig resolved = new ResolvedGrowthConfig(levelItems, overflowStep, expGainRatio, evolution,
                petRoutes,
                true);
        return toVO(resolved);
    }

    public ResolvedGrowthConfig resolveForClass(Long classId) {
        ResolvedGrowthConfig defaults = getDefaults();
        ClassConfig config = classConfigMapper.selectOne(new LambdaQueryWrapper<ClassConfig>()
                .eq(ClassConfig::getClassId, classId)
                .last("LIMIT 1"));
        if (config == null) {
            return defaults;
        }

        boolean customized = !isBlank(config.getLevels()) || !isBlank(config.getEvolution())
                || !isBlank(config.getPets());
        if (!customized) {
            return defaults;
        }

        List<LevelItem> levelItems = defaults.getLevelItems();
        int overflowStep = defaults.getOverflowStep();
        double expGainRatio = defaults.getExpGainRatio();
        EvolutionConfig evolution = defaults.getEvolution();
        List<PetRoute> petRoutes = defaults.getPetRoutes();

        ParsedLevels parsedLevels = parseLevels(config.getLevels(), defaults.getLevelItems(),
                defaults.getOverflowStep(),
                defaults.getExpGainRatio());
        levelItems = parsedLevels.levelItems;
        overflowStep = parsedLevels.overflowStep;
        expGainRatio = parsedLevels.expGainRatio;
        evolution = parseEvolution(config.getEvolution(), defaults.getEvolution());
        petRoutes = parsePetRoutes(config.getPets(), defaults.getPetRoutes());

        // --- Filter out legacy/deprecated pets ---
        if (petRoutes != null) {
            petRoutes.removeIf(r -> "dragon".equals(r.id)
                    || "phoenix".equals(r.id)
                    || "sys_lava_golem".equals(r.id)
                    || "sys_lucky_koi".equals(r.id)
                    || "sys_storm_eagle".equals(r.id));
        }

        // --- Merge System Pets from Defaults ---
        // Automatically inject any system pet (sys_*) from defaults that is missing in
        // current config
        for (PetRoute defRoute : defaults.getPetRoutes()) {
            if (defRoute.id != null && defRoute.id.startsWith("sys_")) {
                boolean exists = false;
                if (petRoutes != null) {
                    for (PetRoute r : petRoutes) {
                        if (defRoute.id.equals(r.id)) {
                            exists = true;
                            break;
                        }
                    }
                }
                if (!exists) {
                    if (petRoutes == null) {
                        petRoutes = new ArrayList<>();
                    }
                    petRoutes.add(defRoute);
                }
            }
        }

        return new ResolvedGrowthConfig(levelItems, overflowStep, expGainRatio, evolution, petRoutes, true);
    }

    public String resolvePetRouteId(Long classId, String routeKey) {
        String key = trim(routeKey);
        if (key.isEmpty()) {
            return "";
        }
        ResolvedGrowthConfig config = resolveForClass(classId);
        List<PetRoute> routes = config.getPetRoutes();
        if (routes == null || routes.isEmpty()) {
            return "";
        }
        for (PetRoute route : routes) {
            if (route == null || !Boolean.TRUE.equals(route.enabled)) {
                continue;
            }
            if (key.equalsIgnoreCase(trim(route.id)) || key.equalsIgnoreCase(trim(route.name))) {
                return trim(route.id);
            }
        }
        return "";
    }

    private ParsedLevels parseLevels(String levelsJson, List<LevelItem> defaultLevelItems, int defaultOverflowStep,
            double defaultExpGainRatio) {
        if (isBlank(levelsJson)) {
            return new ParsedLevels(defaultLevelItems, defaultOverflowStep, defaultExpGainRatio);
        }
        try {
            JsonNode node = objectMapper.readTree(levelsJson);
            if (node == null || node.isNull()) {
                return new ParsedLevels(defaultLevelItems, defaultOverflowStep, defaultExpGainRatio);
            }

            if (node.isArray()) {
                List<LevelItem> items = sanitizeLevelItems(
                        levelItemsFromThresholds(objectMapper.convertValue(node, INTEGER_LIST_TYPE)));
                return new ParsedLevels(items, defaultOverflowStep, defaultExpGainRatio);
            }

            JsonNode itemsNode = node.get("items");
            List<LevelItem> items = defaultLevelItems;
            if (itemsNode != null && itemsNode.isArray() && itemsNode.size() > 0) {
                items = sanitizeLevelItems(readLevelItems(itemsNode));
            } else {
                JsonNode thresholdsNode = node.get("levelThresholds");
                if (thresholdsNode == null || thresholdsNode.isNull()) {
                    thresholdsNode = node.get("levels");
                }
                if (thresholdsNode == null || thresholdsNode.isNull()) {
                    thresholdsNode = node.get("thresholds");
                }
                if (thresholdsNode != null && thresholdsNode.isArray()) {
                    items = sanitizeLevelItems(
                            levelItemsFromThresholds(objectMapper.convertValue(thresholdsNode, INTEGER_LIST_TYPE)));
                }
            }

            JsonNode overflowNode = node.get("overflowStep");
            if (overflowNode == null || overflowNode.isNull()) {
                overflowNode = node.get("overflow_step");
            }
            int overflowStep = overflowNode == null || overflowNode.isNull()
                    ? defaultOverflowStep
                    : sanitizeOverflowStep(overflowNode.asInt(defaultOverflowStep));
            JsonNode ratioNode = node.get("expGainRatio");
            if (ratioNode == null || ratioNode.isNull()) {
                ratioNode = node.get("exp_gain_ratio");
            }
            double expGainRatio = ratioNode == null || ratioNode.isNull()
                    ? defaultExpGainRatio
                    : sanitizeExpGainRatio(ratioNode.asDouble(defaultExpGainRatio));

            return new ParsedLevels(items, overflowStep, expGainRatio);
        } catch (Exception ex) {
            return new ParsedLevels(defaultLevelItems, defaultOverflowStep, defaultExpGainRatio);
        }
    }

    private EvolutionConfig parseEvolution(String evolutionJson, EvolutionConfig defaults) {
        if (isBlank(evolutionJson)) {
            return defaults;
        }
        try {
            JsonNode node = objectMapper.readTree(evolutionJson);
            if (node == null || node.isNull()) {
                return defaults;
            }
            int stage1 = safePositive(node.path("stage1MaxLevel").asInt(defaults.stage1MaxLevel),
                    defaults.stage1MaxLevel);
            int stage2 = safePositive(node.path("stage2MaxLevel").asInt(defaults.stage2MaxLevel),
                    defaults.stage2MaxLevel);
            int stage3 = safePositive(node.path("stage3StartLevel").asInt(defaults.stage3StartLevel),
                    defaults.stage3StartLevel);
            return sanitizeEvolution(new EvolutionConfig(stage1, stage2, stage3));
        } catch (Exception ex) {
            return defaults;
        }
    }

    private List<PetRoute> parsePetRoutes(String petsJson, List<PetRoute> defaults) {
        if (isBlank(petsJson)) {
            return defaults;
        }
        try {
            JsonNode node = objectMapper.readTree(petsJson);
            if (node == null || node.isNull()) {
                return defaults;
            }
            JsonNode routesNode = node.isArray() ? node : node.get("routes");
            if (routesNode == null || !routesNode.isArray()) {
                return defaults;
            }
            List<PetRoute> routes = readPetRoutes(routesNode);
            return sanitizePetRoutes(routes);
        } catch (Exception ex) {
            return defaults;
        }
    }

    private String toLevelsJson(List<LevelItem> levelItems, int overflowStep, double expGainRatio) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("items", toLevelItemPayload(levelItems));
        payload.put("levelThresholds", levelItemsToThresholds(levelItems));
        payload.put("overflowStep", overflowStep);
        payload.put("expGainRatio", expGainRatio);
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            throw new BizException(50001, "保存成长配置失败");
        }
    }

    private String toEvolutionJson(EvolutionConfig evolution) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("stage1MaxLevel", evolution.stage1MaxLevel);
        payload.put("stage2MaxLevel", evolution.stage2MaxLevel);
        payload.put("stage3StartLevel", evolution.stage3StartLevel);
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            throw new BizException(50001, "保存进化配置失败");
        }
    }

    private String toPetsJson(List<PetRoute> petRoutes) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("routes", toPetRoutePayload(petRoutes));
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            throw new BizException(50001, "保存宠物路线失败");
        }
    }

    private GrowthConfigVO toVO(ResolvedGrowthConfig resolved) {
        GrowthConfigVO vo = new GrowthConfigVO();
        vo.setLevelItems(toLevelItemVO(resolved.getLevelItems()));
        vo.setLevelThresholds(resolved.getLevelThresholds());
        vo.setOverflowStep(resolved.getOverflowStep());
        vo.setExpGainRatio(resolved.getExpGainRatio());
        vo.setEvolution(toEvolutionVO(resolved.getEvolution()));
        vo.setPetRoutes(toPetRouteVO(resolved.getPetRoutes()));
        vo.setCustomized(resolved.getCustomized());
        return vo;
    }

    private ResolvedGrowthConfig getDefaults() {
        List<Integer> thresholds = growthProperties.getLevelThresholds();
        if (thresholds == null || thresholds.isEmpty()) {
            thresholds = new ArrayList<>();
            thresholds.add(0);
            thresholds.add(100);
            thresholds.add(250);
        }
        int overflowStep = growthProperties.getOverflowStep() == null || growthProperties.getOverflowStep() <= 0
                ? 800
                : growthProperties.getOverflowStep();
        double expGainRatio = sanitizeExpGainRatio(growthProperties.getExpGainRatio());
        List<LevelItem> levelItems = sanitizeLevelItems(levelItemsFromThresholds(thresholds));
        EvolutionConfig evolution = new EvolutionConfig(3, 6, 7);
        List<PetRoute> routes = new ArrayList<>();

        // --- System Pets (Hardcoded) ---
        // Legacy "dragon" and "phoenix" removed per user request

        // 3. Thunder Wolf (System Locked)
        List<PetStage> wolfStages = new ArrayList<>();
        wolfStages.add(new PetStage(1, "/uploads/system/thunder_wolf/1.png"));
        wolfStages.add(new PetStage(2, "/uploads/system/thunder_wolf/2.png"));
        wolfStages.add(new PetStage(3, "/uploads/system/thunder_wolf/3.png"));
        routes.add(new PetRoute("sys_thunder_wolf", "雷霆狼", true, wolfStages));

        // 4. Golden Light Dragon (System Locked)
        List<PetStage> dragonStages = new ArrayList<>();
        dragonStages.add(new PetStage(1, "/uploads/system/golden_dragon/1.png"));
        dragonStages.add(new PetStage(2, "/uploads/system/golden_dragon/2.png"));
        dragonStages.add(new PetStage(3, "/uploads/system/golden_dragon/3.png"));
        routes.add(new PetRoute("sys_golden_dragon", "金光龙", true, dragonStages));

        // --- Batch Added System Pets (10 Types) ---
        addSystemPet(routes, "sys_flame_lion", "烈焰狮王", "flame_lion");
        addSystemPet(routes, "sys_mech_rex", "机甲暴龙", "mech_rex");
        addSystemPet(routes, "sys_nimble_mouse", "灵机鼠", "nimble_mouse");
        addSystemPet(routes, "sys_frost_unicorn", "寒冰独角兽", "frost_unicorn");
        addSystemPet(routes, "sys_starry_whale", "星空鲸", "starry_whale");
        addSystemPet(routes, "sys_moon_deer", "月光灵鹿", "moon_deer");
        addSystemPet(routes, "sys_sakura_fox", "樱花九尾狐", "sakura_fox");
        addSystemPet(routes, "sys_mountain_ox", "撼山牛", "mountain_ox");
        addSystemPet(routes, "sys_kungfu_panda", "功夫熊猫", "kungfu_panda");
        addSystemPet(routes, "sys_flame_tiger", "烈焰虎", "flame_tiger");
        addSystemPet(routes, "sys_ice_crystal_fox", "冰晶狐", "ice_crystal_fox");
        addSystemPet(routes, "sys_frost_dragon_horse", "寒冰龙马", "frost_dragon_horse");
        addSystemPet(routes, "sys_shadow_lion", "影爪狮", "shadow_lion");
        addSystemPet(routes, "sys_flame_kirin", "炎狱麒麟", "flame_kirin");
        addSystemPet(routes, "sys_polar_bear", "极地熊", "polar_bear");
        addSystemPet(routes, "sys_frost_monn_hound", "霜月犬", "frost_monn_hound");
        addSystemPet(routes, "sys_nebula_fox", "星幻狐", "nebula_fox");
        addSystemPet(routes, "sys_amethyst_kirin", "紫晶麒麟", "amethyst_kirin");
        addSystemPet(routes, "sys_azure_mammoth", "苍火猛犸", "azure_mammoth");
        addSystemPet(routes, "sys_frost_dew_fox", "寒霜妖狐", "frost_dew_fox");
        addSystemPet(routes, "sys_rock_crystal_lion", "岩晶狮", "rock_crystal_lion");

        return new ResolvedGrowthConfig(levelItems, overflowStep, expGainRatio, evolution, routes, false);
    }

    private void addSystemPet(List<PetRoute> routes, String id, String name, String folder) {
        List<PetStage> stages = new ArrayList<>();
        stages.add(new PetStage(1, "/uploads/system/" + folder + "/1.png"));
        stages.add(new PetStage(2, "/uploads/system/" + folder + "/2.png"));
        stages.add(new PetStage(3, "/uploads/system/" + folder + "/3.png"));
        routes.add(new PetRoute(id, sanitizePetRouteName(name), true, stages));
    }

    private List<LevelItem> sanitizeLevelItems(List<LevelItem> input) {
        if (input == null || input.isEmpty()) {
            throw new BizException(40001, "等级阈值不能为空");
        }
        List<LevelItem> cleaned = new ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            LevelItem item = input.get(i);
            if (item == null || item.threshold == null) {
                throw new BizException(40001, "等级阈值不能包含空值");
            }
            if (item.threshold < 0) {
                throw new BizException(40001, "等级阈值必须大于等于 0");
            }
            String name = trim(item.name);
            if (name.isEmpty()) {
                name = "等级" + (i + 1);
            }
            cleaned.add(new LevelItem(i + 1, name, item.threshold));
        }

        if (cleaned.get(0).threshold != 0) {
            throw new BizException(40001, "第一个等级阈值必须为 0");
        }
        for (int i = 1; i < cleaned.size(); i++) {
            if (cleaned.get(i).threshold <= cleaned.get(i - 1).threshold) {
                throw new BizException(40001, "等级阈值必须严格递增");
            }
        }
        return cleaned;
    }

    private int sanitizeOverflowStep(Integer value) {
        if (value == null || value <= 0) {
            throw new BizException(40001, "溢出步长必须大于 0");
        }
        return value;
    }

    private double sanitizeExpGainRatio(Double value) {
        if (value == null || value <= 0) {
            return 1.0;
        }
        return value;
    }

    private EvolutionConfig sanitizeEvolution(EvolutionConfig input) {
        if (input == null) {
            throw new BizException(40001, "进化配置不能为空");
        }
        int stage1 = safePositive(input.stage1MaxLevel, 3);
        int stage2 = safePositive(input.stage2MaxLevel, 6);
        int stage3 = safePositive(input.stage3StartLevel, 7);
        int maxLevel = 10;
        if (stage1 < 1) stage1 = 1;
        if (stage2 <= stage1) stage2 = stage1 + 1;
        if (stage2 >= maxLevel) stage2 = maxLevel - 1;
        if (stage3 <= stage2) stage3 = stage2 + 1;
        if (!(stage1 < stage2 && stage2 < stage3)) {
            throw new BizException(40001, "进化阶段等级必须满足 阶段1 < 阶段2 < 阶段3，且阶段2必须小于10");
        }
        return new EvolutionConfig(stage1, stage2, stage3);
    }

    private List<PetRoute> sanitizePetRoutes(List<PetRoute> routes) {
        List<PetRoute> safeRoutes = new ArrayList<>();
        if (routes == null) {
            return safeRoutes;
        }
        for (int i = 0; i < routes.size(); i++) {
            PetRoute route = routes.get(i);
            if (route == null) {
                continue;
            }
            String id = trim(route.id);
            if (id.isEmpty()) {
                id = "route_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            }
            String name = sanitizePetRouteName(route.name);
            if (name.isEmpty()) {
                name = "路线" + (i + 1);
            }
            boolean enabled = route.enabled == null || route.enabled;
            List<PetStage> stages = normalizeStages(route.stages);
            safeRoutes.add(new PetRoute(id, name, enabled, stages));
        }
        return safeRoutes;
    }

    private void ensureClassOwnership(Long classId) {
        if (!classInfoService.isOwnedByCurrentTeacher(classId)) {
            throw new BizException(40301, "无权访问此班级");
        }
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    private String trim(String text) {
        return text == null ? "" : text.trim();
    }

    private String sanitizePetRouteName(String text) {
        String name = trim(text);
        if (name.isEmpty()) {
            return "";
        }
        name = name.replace("(系统限定)", "")
                .replace("（系统限定）", "")
                .replace("系统限定", "")
                .replace("宠物限定", "");
        return trim(name);
    }

    private int safePositive(Integer value, int fallback) {
        if (value == null || value <= 0) {
            return fallback;
        }
        return value;
    }

    private List<LevelItem> levelItemsFromThresholds(List<Integer> thresholds) {
        List<LevelItem> items = new ArrayList<>();
        if (thresholds == null) {
            return items;
        }
        for (int i = 0; i < thresholds.size(); i++) {
            items.add(new LevelItem(i + 1, "等级" + (i + 1), thresholds.get(i)));
        }
        return items;
    }

    private List<Integer> levelItemsToThresholds(List<LevelItem> levelItems) {
        List<Integer> thresholds = new ArrayList<>();
        for (LevelItem item : levelItems) {
            thresholds.add(item.threshold);
        }
        return thresholds;
    }

    private List<LevelItem> fromRequestLevelItems(List<GrowthConfigUpdateRequest.LevelItemDTO> levelItems) {
        List<LevelItem> list = new ArrayList<>();
        for (GrowthConfigUpdateRequest.LevelItemDTO item : levelItems) {
            if (item == null) {
                continue;
            }
            list.add(new LevelItem(item.getLevel(), item.getName(), item.getThreshold()));
        }
        return list;
    }

    private EvolutionConfig fromRequestEvolution(GrowthConfigUpdateRequest.EvolutionConfigDTO dto) {
        return new EvolutionConfig(dto.getStage1MaxLevel(), dto.getStage2MaxLevel(), dto.getStage3StartLevel());
    }

    private List<PetRoute> fromRequestPetRoutes(List<GrowthConfigUpdateRequest.PetRouteDTO> dtos) {
        List<PetRoute> routes = new ArrayList<>();
        for (GrowthConfigUpdateRequest.PetRouteDTO dto : dtos) {
            if (dto == null) {
                continue;
            }
            List<PetStage> stages = new ArrayList<>();
            if (dto.getStages() != null) {
                for (GrowthConfigUpdateRequest.PetStageDTO stageDTO : dto.getStages()) {
                    if (stageDTO == null) {
                        continue;
                    }
                    stages.add(new PetStage(stageDTO.getStage(), stageDTO.getImage()));
                }
            }
            routes.add(new PetRoute(dto.getId(), dto.getName(), dto.getEnabled(), stages));
        }
        return routes;
    }

    private List<LevelItem> readLevelItems(JsonNode itemsNode) {
        List<LevelItem> items = new ArrayList<>();
        for (int i = 0; i < itemsNode.size(); i++) {
            JsonNode node = itemsNode.get(i);
            Integer level = node.path("level").asInt(i + 1);
            String name = node.path("name").asText("");
            Integer threshold = node.path("threshold").asInt(Integer.MIN_VALUE);
            if (threshold == Integer.MIN_VALUE) {
                continue;
            }
            items.add(new LevelItem(level, name, threshold));
        }
        return items;
    }

    private List<PetRoute> readPetRoutes(JsonNode routesNode) {
        List<PetRoute> routes = new ArrayList<>();
        for (int i = 0; i < routesNode.size(); i++) {
            JsonNode routeNode = routesNode.get(i);
            String id = routeNode.path("id").asText("");
            String name = sanitizePetRouteName(routeNode.path("name").asText(""));
            Boolean enabled = routeNode.path("enabled").isMissingNode() ? true
                    : routeNode.path("enabled").asBoolean(true);
            List<PetStage> stages = new ArrayList<>();
            JsonNode stagesNode = routeNode.path("stages");
            if (stagesNode.isArray()) {
                for (int j = 0; j < stagesNode.size(); j++) {
                    JsonNode stageNode = stagesNode.get(j);
                    int stageNo = stageNode.path("stage").asInt(j + 1);
                    String image = stageNode.path("image").asText("");
                    stages.add(new PetStage(stageNo, image));
                }
            }
            routes.add(new PetRoute(id, name, enabled, stages));
        }
        return routes;
    }

    private List<PetStage> normalizeStages(List<PetStage> stages) {
        List<PetStage> sorted = new ArrayList<>();
        if (stages != null) {
            sorted.addAll(stages);
            sorted.sort(Comparator.comparingInt(it -> it.stage == null ? Integer.MAX_VALUE : it.stage));
        }
        Map<Integer, String> imageMap = new LinkedHashMap<>();
        for (PetStage stage : sorted) {
            int stageNo = stage.stage == null ? 0 : stage.stage;
            if (stageNo >= 1 && stageNo <= 3) {
                imageMap.put(stageNo, trim(stage.image));
            }
        }
        List<PetStage> normalized = new ArrayList<>();
        for (int stageNo = 1; stageNo <= 3; stageNo++) {
            normalized.add(new PetStage(stageNo, imageMap.getOrDefault(stageNo, "")));
        }
        return normalized;
    }

    private List<Map<String, Object>> toLevelItemPayload(List<LevelItem> levelItems) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (LevelItem item : levelItems) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("level", item.level);
            row.put("name", item.name);
            row.put("threshold", item.threshold);
            list.add(row);
        }
        return list;
    }

    private List<Map<String, Object>> toPetRoutePayload(List<PetRoute> petRoutes) {
        List<Map<String, Object>> routes = new ArrayList<>();
        for (PetRoute route : petRoutes) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", route.id);
            row.put("name", route.name);
            row.put("enabled", route.enabled);
            List<Map<String, Object>> stages = new ArrayList<>();
            for (PetStage stage : route.stages) {
                Map<String, Object> stageRow = new LinkedHashMap<>();
                stageRow.put("stage", stage.stage);
                stageRow.put("image", stage.image);
                stages.add(stageRow);
            }
            row.put("stages", stages);
            routes.add(row);
        }
        return routes;
    }

    private List<GrowthConfigVO.LevelItemVO> toLevelItemVO(List<LevelItem> levelItems) {
        List<GrowthConfigVO.LevelItemVO> result = new ArrayList<>();
        for (LevelItem item : levelItems) {
            GrowthConfigVO.LevelItemVO vo = new GrowthConfigVO.LevelItemVO();
            vo.setLevel(item.level);
            vo.setName(item.name);
            vo.setThreshold(item.threshold);
            result.add(vo);
        }
        return result;
    }

    private GrowthConfigVO.EvolutionConfigVO toEvolutionVO(EvolutionConfig evolution) {
        GrowthConfigVO.EvolutionConfigVO vo = new GrowthConfigVO.EvolutionConfigVO();
        vo.setStage1MaxLevel(evolution.stage1MaxLevel);
        vo.setStage2MaxLevel(evolution.stage2MaxLevel);
        vo.setStage3StartLevel(evolution.stage3StartLevel);
        return vo;
    }

    private List<GrowthConfigVO.PetRouteVO> toPetRouteVO(List<PetRoute> petRoutes) {
        List<GrowthConfigVO.PetRouteVO> result = new ArrayList<>();
        for (PetRoute route : petRoutes) {
            GrowthConfigVO.PetRouteVO vo = new GrowthConfigVO.PetRouteVO();
            vo.setId(route.id);
            vo.setName(route.name);
            vo.setEnabled(route.enabled);
            List<GrowthConfigVO.PetStageVO> stages = new ArrayList<>();
            for (PetStage stage : route.stages) {
                GrowthConfigVO.PetStageVO stageVO = new GrowthConfigVO.PetStageVO();
                stageVO.setStage(stage.stage);
                stageVO.setImage(stage.image);
                stages.add(stageVO);
            }
            vo.setStages(stages);
            result.add(vo);
        }
        return result;
    }

    public static class ResolvedGrowthConfig {
        private final List<LevelItem> levelItems;
        private final Integer overflowStep;
        private final Double expGainRatio;
        private final EvolutionConfig evolution;
        private final List<PetRoute> petRoutes;
        private final Boolean customized;

        public ResolvedGrowthConfig(List<LevelItem> levelItems,
                Integer overflowStep,
                Double expGainRatio,
                EvolutionConfig evolution,
                List<PetRoute> petRoutes,
                Boolean customized) {
            this.levelItems = levelItems;
            this.overflowStep = overflowStep;
            this.expGainRatio = expGainRatio;
            this.evolution = evolution;
            this.petRoutes = petRoutes;
            this.customized = customized;
        }

        public List<LevelItem> getLevelItems() {
            return levelItems;
        }

        public List<Integer> getLevelThresholds() {
            List<Integer> thresholds = new ArrayList<>();
            for (LevelItem item : levelItems) {
                thresholds.add(item.threshold);
            }
            return thresholds;
        }

        public Integer getOverflowStep() {
            return overflowStep;
        }

        public Double getExpGainRatio() {
            return expGainRatio;
        }

        public EvolutionConfig getEvolution() {
            return evolution;
        }

        public int getStage1MaxLevel() {
            return evolution != null && evolution.stage1MaxLevel != null ? evolution.stage1MaxLevel : 3;
        }

        public int getStage2MaxLevel() {
            return evolution != null && evolution.stage2MaxLevel != null ? evolution.stage2MaxLevel : 6;
        }

        public List<PetRoute> getPetRoutes() {
            return petRoutes;
        }

        public String getDefaultPetRouteId() {
            if (petRoutes == null || petRoutes.isEmpty()) {
                return "";
            }
            for (PetRoute route : petRoutes) {
                if (route != null && Boolean.TRUE.equals(route.enabled) && !isBlank(route.id)) {
                    return route.id;
                }
            }
            PetRoute first = petRoutes.get(0);
            return first == null || isBlank(first.id) ? "" : first.id;
        }

        public Boolean getCustomized() {
            return customized;
        }

        private boolean isBlank(String text) {
            return text == null || text.trim().isEmpty();
        }
    }

    private static class ParsedLevels {
        private final List<LevelItem> levelItems;
        private final int overflowStep;
        private final double expGainRatio;

        private ParsedLevels(List<LevelItem> levelItems, int overflowStep, double expGainRatio) {
            this.levelItems = levelItems;
            this.overflowStep = overflowStep;
            this.expGainRatio = expGainRatio;
        }
    }

    private static class LevelItem {
        private final Integer level;
        private final String name;
        private final Integer threshold;

        private LevelItem(Integer level, String name, Integer threshold) {
            this.level = level;
            this.name = name;
            this.threshold = threshold;
        }
    }

    private static class EvolutionConfig {
        private final Integer stage1MaxLevel;
        private final Integer stage2MaxLevel;
        private final Integer stage3StartLevel;

        private EvolutionConfig(Integer stage1MaxLevel, Integer stage2MaxLevel, Integer stage3StartLevel) {
            this.stage1MaxLevel = stage1MaxLevel;
            this.stage2MaxLevel = stage2MaxLevel;
            this.stage3StartLevel = stage3StartLevel;
        }
    }

    private static class PetRoute {
        private final String id;
        private final String name;
        private final Boolean enabled;
        private final List<PetStage> stages;

        private PetRoute(String id, String name, Boolean enabled, List<PetStage> stages) {
            this.id = id;
            this.name = name;
            this.enabled = enabled;
            this.stages = stages;
        }
    }

    private static class PetStage {
        private final Integer stage;
        private final String image;

        private PetStage(Integer stage, String image) {
            this.stage = stage;
            this.image = image;
        }
    }
}
