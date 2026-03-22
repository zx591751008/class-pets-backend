package com.classpets.backend.lottery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classpets.backend.classinfo.service.ClassInfoService;
import com.classpets.backend.common.BizException;
import com.classpets.backend.entity.ClassConfig;
import com.classpets.backend.entity.InventoryUseRecord;
import com.classpets.backend.entity.LotteryDrawRecord;
import com.classpets.backend.entity.Student;
import com.classpets.backend.entity.StudentEvent;
import com.classpets.backend.entity.StudentInventory;
import com.classpets.backend.entity.StudentPetGallery;
import com.classpets.backend.growth.service.GrowthConfigService;
import com.classpets.backend.history.service.HistoryUndoService;
import com.classpets.backend.lottery.dto.InventoryUseRequest;
import com.classpets.backend.lottery.dto.LotteryPrizeConfigItemDTO;
import com.classpets.backend.lottery.vo.DeductibleEventVO;
import com.classpets.backend.lottery.vo.LotteryDrawResultVO;
import com.classpets.backend.lottery.vo.LotteryPrizeVO;
import com.classpets.backend.lottery.vo.LotteryRecordVO;
import com.classpets.backend.lottery.vo.StudentInventoryVO;
import com.classpets.backend.mapper.ClassConfigMapper;
import com.classpets.backend.mapper.InventoryUseRecordMapper;
import com.classpets.backend.mapper.LotteryDrawRecordMapper;
import com.classpets.backend.mapper.StudentEventMapper;
import com.classpets.backend.mapper.StudentInventoryMapper;
import com.classpets.backend.mapper.StudentMapper;
import com.classpets.backend.mapper.StudentPetGalleryMapper;
import com.classpets.backend.student.service.AbandonedPetService;
import com.classpets.backend.sync.SyncEventService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class LotteryService {

    private static final int SINGLE_DRAW_COST = 20;
    private static final String ITEM_STATUS_ACTIVE = "ACTIVE";
    private static final String ITEM_SHIELD = "SHIELD";
    private static final String LOTTERY_PRIZES_KEY = "lotteryPrizes";
    private static final String LOTTERY_SINGLE_DRAW_COST_KEY = "lotterySingleDrawCost";
    private static final Set<String> EDITABLE_PRIZE_CODES = new HashSet<>(Arrays.asList("CLEAN_PASS", "SONG_CARD", "SPEAK_STICK"));
    private static final TypeReference<Map<String, PrizeConfigEntry>> PRIZE_CONFIG_TYPE = new TypeReference<Map<String, PrizeConfigEntry>>() {
    };

    private final ClassInfoService classInfoService;
    private final ClassConfigMapper classConfigMapper;
    private final StudentMapper studentMapper;
    private final StudentEventMapper studentEventMapper;
    private final LotteryDrawRecordMapper lotteryDrawRecordMapper;
    private final StudentInventoryMapper studentInventoryMapper;
    private final InventoryUseRecordMapper inventoryUseRecordMapper;
    private final HistoryUndoService historyUndoService;
    private final SyncEventService syncEventService;
    private final GrowthConfigService growthConfigService;
    private final StudentPetGalleryMapper studentPetGalleryMapper;
    private final AbandonedPetService abandonedPetService;
    private final ObjectMapper objectMapper;

    public LotteryService(ClassInfoService classInfoService,
            ClassConfigMapper classConfigMapper,
            StudentMapper studentMapper,
            StudentEventMapper studentEventMapper,
            LotteryDrawRecordMapper lotteryDrawRecordMapper,
            StudentInventoryMapper studentInventoryMapper,
            InventoryUseRecordMapper inventoryUseRecordMapper,
            HistoryUndoService historyUndoService,
            SyncEventService syncEventService,
            GrowthConfigService growthConfigService,
            StudentPetGalleryMapper studentPetGalleryMapper,
            AbandonedPetService abandonedPetService,
            ObjectMapper objectMapper) {
        this.classInfoService = classInfoService;
        this.classConfigMapper = classConfigMapper;
        this.studentMapper = studentMapper;
        this.studentEventMapper = studentEventMapper;
        this.lotteryDrawRecordMapper = lotteryDrawRecordMapper;
        this.studentInventoryMapper = studentInventoryMapper;
        this.inventoryUseRecordMapper = inventoryUseRecordMapper;
        this.historyUndoService = historyUndoService;
        this.syncEventService = syncEventService;
        this.growthConfigService = growthConfigService;
        this.studentPetGalleryMapper = studentPetGalleryMapper;
        this.abandonedPetService = abandonedPetService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public LotteryDrawResultVO drawSingle(Long classId, Long studentId) {
        ensureClassOwnership(classId);
        Student student = mustGetStudent(studentId);
        if (!classId.equals(student.getClassId())) {
            throw new BizException(40001, "学生不属于当前班级");
        }

        int redeem = safe(student.getRedeemPoints());
        int singleDrawCost = getSingleDrawCost(classId);
        if (redeem < singleDrawCost) {
            throw new BizException(40002, "余额不足，需要 " + singleDrawCost + " 积分");
        }

        Prize prize = randomPrize(classId);
        long now = System.currentTimeMillis();

        student.setRedeemPoints(redeem - singleDrawCost);

        StudentEvent costEvent = new StudentEvent();
        costEvent.setClassId(classId);
        costEvent.setStudentId(studentId);
        costEvent.setReason("九宫格抽奖单抽");
        costEvent.setChangeValue(0);
        costEvent.setRedeemChange(-singleDrawCost);
        costEvent.setExpChange(0);
        costEvent.setTimestamp(now);
        costEvent.setRevoked(0);
        studentEventMapper.insert(costEvent);

        Integer rewardRedeem = 0;
        Long inventoryId = null;
        String inventoryItemCode = null;
        String inventoryItemName = null;

        if (prize.itemCode != null && !prize.itemCode.isEmpty()) {
            StudentInventory inventory = studentInventoryMapper.selectOne(new LambdaQueryWrapper<StudentInventory>()
                    .eq(StudentInventory::getStudentId, studentId)
                    .eq(StudentInventory::getItemCode, prize.itemCode)
                    .last("limit 1"));
            if (inventory == null) {
                inventory = new StudentInventory();
                inventory.setClassId(classId);
                inventory.setStudentId(studentId);
                inventory.setItemCode(prize.itemCode);
                inventory.setItemName(prize.name);
                inventory.setRarity(prize.rarity);
                inventory.setQuantity(1);
                inventory.setStatus(ITEM_STATUS_ACTIVE);
                inventory.setCreateTime(now);
                inventory.setUpdateTime(now);
                studentInventoryMapper.insert(inventory);
            } else {
                inventory.setQuantity(Math.max(0, safe(inventory.getQuantity())) + 1);
                inventory.setItemName(prize.name);
                inventory.setRarity(prize.rarity);
                inventory.setStatus(ITEM_STATUS_ACTIVE);
                inventory.setUpdateTime(now);
                studentInventoryMapper.updateById(inventory);
            }
            inventoryId = inventory.getId();
            inventoryItemCode = inventory.getItemCode();
            inventoryItemName = inventory.getItemName();
        }

        student.setUpdateTime(now);
        studentMapper.updateById(student);

        LotteryDrawRecord drawRecord = new LotteryDrawRecord();
        drawRecord.setClassId(classId);
        drawRecord.setStudentId(studentId);
        drawRecord.setPrizeCode(prize.code);
        drawRecord.setPrizeName(prize.name);
        drawRecord.setRarity(prize.rarity);
        drawRecord.setCostRedeem(singleDrawCost);
        drawRecord.setRewardRedeem(rewardRedeem);
        drawRecord.setInventoryItemCode(inventoryItemCode);
        drawRecord.setInventoryItemName(inventoryItemName);
        drawRecord.setCreateTime(now);
        lotteryDrawRecordMapper.insert(drawRecord);

        syncEventService.publishClassChange(classId, "lottery_draw", studentId);

        LotteryDrawResultVO vo = new LotteryDrawResultVO();
        vo.setDrawId(drawRecord.getId());
        vo.setPrizeCode(prize.code);
        vo.setPrizeName(prize.name);
        vo.setRarity(prize.rarity);
        vo.setIcon(prize.icon);
        vo.setCostRedeem(singleDrawCost);
        vo.setRewardRedeem(rewardRedeem);
        vo.setAddedToInventory(inventoryId != null);
        vo.setInventoryId(inventoryId);
        vo.setInventoryItemCode(inventoryItemCode);
        vo.setInventoryItemName(inventoryItemName);
        vo.setStudentRedeem(safe(student.getRedeemPoints()));
        return vo;
    }

    public List<LotteryRecordVO> listClassRecords(Long classId) {
        ensureClassOwnership(classId);
        List<LotteryDrawRecord> list = lotteryDrawRecordMapper.selectList(new LambdaQueryWrapper<LotteryDrawRecord>()
                .eq(LotteryDrawRecord::getClassId, classId)
                .orderByDesc(LotteryDrawRecord::getCreateTime)
                .last("limit 100"));
        List<Long> studentIds = list.stream().map(LotteryDrawRecord::getStudentId).distinct().collect(Collectors.toList());
        Map<Long, String> studentNameMap = new HashMap<>();
        if (!studentIds.isEmpty()) {
            studentMapper.selectBatchIds(studentIds)
                    .forEach(student -> studentNameMap.put(student.getId(), student.getName()));
        }
        return list.stream().map(row -> toRecordVO(row, studentNameMap.get(row.getStudentId()))).collect(Collectors.toList());
    }

    public List<LotteryRecordVO> listRecords(Long studentId) {
        Student student = mustGetStudent(studentId);
        ensureClassOwnership(student.getClassId());
        return lotteryDrawRecordMapper.selectList(new LambdaQueryWrapper<LotteryDrawRecord>()
                .eq(LotteryDrawRecord::getStudentId, studentId)
                .orderByDesc(LotteryDrawRecord::getCreateTime)
                .last("limit 100")).stream().map(row -> toRecordVO(row, student.getName())).collect(Collectors.toList());
    }

    public List<LotteryPrizeVO> listClassPrizes(Long classId) {
        ensureClassOwnership(classId);
        List<Prize> effectivePrizes = getEffectivePrizes(classId);
        int singleDrawCost = getSingleDrawCost(classId);
        return effectivePrizes.stream().map(prize -> toPrizeVO(prize, singleDrawCost)).collect(Collectors.toList());
    }

    @Transactional
    public List<LotteryPrizeVO> updateClassPrizes(Long classId, List<LotteryPrizeConfigItemDTO> items, Integer singleDrawCost) {
        ensureClassOwnership(classId);
        Map<String, PrizeConfigEntry> overrides = loadPrizeConfig(classId);

        if (items != null) {
            for (LotteryPrizeConfigItemDTO item : items) {
                if (item == null) {
                    continue;
                }
                String code = trim(item.getCode()).toUpperCase();
                if (!EDITABLE_PRIZE_CODES.contains(code)) {
                    throw new BizException(40001, "仅允许编辑虚拟奖品");
                }
                PrizeConfigEntry entry = overrides.getOrDefault(code, new PrizeConfigEntry());
                entry.name = sanitizePrizeName(item.getName());
                entry.icon = sanitizePrizeIcon(item.getIcon());
                entry.note = sanitizePrizeNote(item.getNote());
                entry.enabled = true;
                overrides.put(code, entry);
            }
        }

        savePrizeConfig(classId, overrides, singleDrawCost);
        return listClassPrizes(classId);
    }

    public List<StudentInventoryVO> listInventory(Long studentId) {
        Student student = mustGetStudent(studentId);
        ensureClassOwnership(student.getClassId());
        return studentInventoryMapper.selectList(new LambdaQueryWrapper<StudentInventory>()
                .eq(StudentInventory::getStudentId, studentId)
                .eq(StudentInventory::getStatus, ITEM_STATUS_ACTIVE)
                .gt(StudentInventory::getQuantity, 0)
                .orderByDesc(StudentInventory::getUpdateTime))
                .stream().map(this::toInventoryVO).collect(Collectors.toList());
    }

    public List<DeductibleEventVO> listDeductibleEvents(Long studentId) {
        Student student = mustGetStudent(studentId);
        ensureClassOwnership(student.getClassId());
        return studentEventMapper.selectList(new LambdaQueryWrapper<StudentEvent>()
                .eq(StudentEvent::getClassId, student.getClassId())
                .eq(StudentEvent::getStudentId, studentId)
                .eq(StudentEvent::getRevoked, 0)
                .lt(StudentEvent::getChangeValue, 0)
                .orderByDesc(StudentEvent::getTimestamp)
                .last("limit 100")).stream().map(this::toDeductibleEventVO).collect(Collectors.toList());
    }

    @Transactional
    public StudentInventoryVO useInventory(Long studentId, Long inventoryId, InventoryUseRequest request) {
        Student student = mustGetStudent(studentId);
        ensureClassOwnership(student.getClassId());

        StudentInventory inventory = studentInventoryMapper.selectById(inventoryId);
        if (inventory == null || !studentId.equals(inventory.getStudentId())) {
            throw new BizException(40401, "道具不存在");
        }
        if (safe(inventory.getQuantity()) <= 0) {
            throw new BizException(40001, "道具数量不足");
        }

        Long targetEventId = request == null ? null : request.getTargetEventId();
        if (ITEM_SHIELD.equals(inventory.getItemCode())) {
            if (targetEventId == null) {
                throw new BizException(40001, "请先选择要抵扣的扣分记录");
            }
            StudentEvent event = studentEventMapper.selectById(targetEventId);
            if (event == null || !student.getClassId().equals(event.getClassId()) || !studentId.equals(event.getStudentId())) {
                throw new BizException(40401, "目标扣分记录不存在");
            }
            if (event.getRevoked() != null && event.getRevoked() == 1) {
                throw new BizException(40001, "该扣分记录已撤销");
            }
            if (safe(event.getChangeValue()) >= 0) {
                throw new BizException(40001, "仅支持抵扣扣分记录");
            }
            historyUndoService.undoByEvent(student.getClassId(), targetEventId);
        } else if ("CANDY".equals(inventory.getItemCode())) {
            applyExpReward(student, 25, "使用道具：魔法糖果");
        } else if ("MEAT".equals(inventory.getItemCode())) {
            applyExpReward(student, 70, "使用道具：超级烤肉");
        } else if ("BIG_BURST".equals(inventory.getItemCode())) {
            applyExpReward(student, 200, "使用道具：经验大爆雪");
            int reward = 100;
            student.setRedeemPoints(safe(student.getRedeemPoints()) + reward);
            student.setUpdateTime(System.currentTimeMillis());
            studentMapper.updateById(student);
            StudentEvent rewardEvent = new StudentEvent();
            rewardEvent.setClassId(student.getClassId());
            rewardEvent.setStudentId(studentId);
            rewardEvent.setReason("使用道具：经验大爆雪（余额奖励）");
            rewardEvent.setChangeValue(0);
            rewardEvent.setRedeemChange(reward);
            rewardEvent.setExpChange(0);
            rewardEvent.setTimestamp(System.currentTimeMillis());
            rewardEvent.setRevoked(0);
            studentEventMapper.insert(rewardEvent);
        } else if ("AMNESIA".equals(inventory.getItemCode())) {
            student.setPetId("");
            student.setExp(0);
            student.setLevel(1);
            student.setTitle(generateTitle(1, student.getClassId()));
            student.setUpdateTime(System.currentTimeMillis());
            studentMapper.updateById(student);
            StudentEvent event = new StudentEvent();
            event.setClassId(student.getClassId());
            event.setStudentId(studentId);
            event.setReason("使用道具：遗忘果实");
            event.setChangeValue(0);
            event.setRedeemChange(0);
            event.setExpChange(0);
            event.setNote("noUndo=1");
            event.setTimestamp(System.currentTimeMillis());
            event.setRevoked(0);
            studentEventMapper.insert(event);
        } else if ("BALANCE_BAG".equals(inventory.getItemCode())) {
            int reward = 10;
            student.setRedeemPoints(safe(student.getRedeemPoints()) + reward);
            student.setUpdateTime(System.currentTimeMillis());
            studentMapper.updateById(student);

            StudentEvent rewardEvent = new StudentEvent();
            rewardEvent.setClassId(student.getClassId());
            rewardEvent.setStudentId(studentId);
            rewardEvent.setReason("使用道具：余额福袋");
            rewardEvent.setChangeValue(0);
            rewardEvent.setRedeemChange(reward);
            rewardEvent.setExpChange(0);
            rewardEvent.setTimestamp(System.currentTimeMillis());
            rewardEvent.setRevoked(0);
            studentEventMapper.insert(rewardEvent);
        } else if ("CLEAN_PASS".equals(inventory.getItemCode())
                || "SONG_CARD".equals(inventory.getItemCode())
                || "SPEAK_STICK".equals(inventory.getItemCode())) {
            StudentEvent virtualUseEvent = new StudentEvent();
            virtualUseEvent.setClassId(student.getClassId());
            virtualUseEvent.setStudentId(studentId);
            virtualUseEvent.setReason("使用道具：" + trim(inventory.getItemName()));
            virtualUseEvent.setChangeValue(0);
            virtualUseEvent.setRedeemChange(0);
            virtualUseEvent.setExpChange(0);
            virtualUseEvent.setNote("noUndo=1");
            virtualUseEvent.setTimestamp(System.currentTimeMillis());
            virtualUseEvent.setRevoked(0);
            studentEventMapper.insert(virtualUseEvent);
        }

        long now = System.currentTimeMillis();
        int nextQuantity = Math.max(0, safe(inventory.getQuantity()) - 1);
        inventory.setQuantity(nextQuantity);
        inventory.setUpdateTime(now);
        if (nextQuantity == 0) {
            inventory.setStatus("USED");
        }
        studentInventoryMapper.updateById(inventory);

        InventoryUseRecord useRecord = new InventoryUseRecord();
        useRecord.setClassId(student.getClassId());
        useRecord.setStudentId(studentId);
        useRecord.setInventoryId(inventory.getId());
        useRecord.setItemCode(inventory.getItemCode());
        useRecord.setItemName(inventory.getItemName());
        useRecord.setTargetEventId(targetEventId);
        useRecord.setNote(request == null ? null : request.getNote());
        useRecord.setCreateTime(now);
        inventoryUseRecordMapper.insert(useRecord);

        syncEventService.publishClassChange(student.getClassId(), "inventory_use", studentId);
        return toInventoryVO(inventory);
    }

    private void applyExpReward(Student student, int expGain, String reason) {
        int beforeLevel = safe(student.getLevel());
        String beforePetId = trim(student.getPetId());
        int exp = Math.max(0, safe(student.getExp()) + Math.max(0, expGain));
        int level = snapshotLevelByExp(student.getClassId(), exp);
        student.setExp(exp);
        student.setLevel(level);
        student.setTitle(generateTitle(level, student.getClassId()));
        handleAutoUnlockAndReset(student, beforeLevel, beforePetId);
        student.setUpdateTime(System.currentTimeMillis());
        studentMapper.updateById(student);

        StudentEvent event = new StudentEvent();
        event.setClassId(student.getClassId());
        event.setStudentId(student.getId());
        event.setReason(reason + " (EXP+" + expGain + ")");
        event.setChangeValue(0);
        event.setRedeemChange(0);
        event.setExpChange(expGain);
        event.setTimestamp(System.currentTimeMillis());
        event.setRevoked(0);
        studentEventMapper.insert(event);
    }

    private void ensureClassOwnership(Long classId) {
        if (!classInfoService.isOwnedByCurrentTeacher(classId)) {
            throw new BizException(40301, "无权访问此班级");
        }
    }

    private Student mustGetStudent(Long studentId) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BizException(40401, "学生不存在");
        }
        return student;
    }

    private int safe(Integer value) {
        return value == null ? 0 : value;
    }

    private int snapshotLevelByExp(Long classId, int exp) {
        int safeExp = Math.max(0, exp);
        List<Integer> levels = growthConfigService.resolveForClass(classId).getLevelThresholds();
        int level = 1;
        for (int i = 0; i < levels.size(); i++) {
            if (safeExp >= levels.get(i)) {
                level = i + 1;
            } else {
                break;
            }
        }
        return Math.max(1, Math.min(level, levels.size()));
    }

    private int getMaxLevel(Long classId) {
        List<Integer> levels = growthConfigService.resolveForClass(classId).getLevelThresholds();
        return Math.min(levels.size(), 10);
    }

    private void handleAutoUnlockAndReset(Student student, int beforeLevel, String beforePetId) {
        String petId = trim(beforePetId);
        if (petId.isEmpty()) {
            return;
        }
        int maxLevel = getMaxLevel(student.getClassId());
        int afterLevel = safe(student.getLevel());
        boolean reachMaxNow = beforeLevel < maxLevel && afterLevel >= maxLevel;
        if (!reachMaxNow) {
            return;
        }
        recordGalleryUnlock(student.getId(), student.getClassId(), petId, afterLevel);
        abandonedPetService.abandonPet(student.getClassId(), student.getId(), petId);
        student.setPetId("");
        student.setExp(0);
        student.setLevel(1);
        student.setTitle(generateTitle(1, student.getClassId()));
    }

    private void recordGalleryUnlock(Long studentId, Long classId, String petId, int currentLevel) {
        if (studentId == null || classId == null || petId == null || petId.trim().isEmpty()) {
            return;
        }
        int maxLevel = getMaxLevel(classId);
        if (currentLevel < maxLevel) {
            return;
        }
        boolean exists = studentPetGalleryMapper.selectCount(new LambdaQueryWrapper<StudentPetGallery>()
                .eq(StudentPetGallery::getStudentId, studentId)
                .eq(StudentPetGallery::getPetRouteId, petId)) > 0;
        if (exists) {
            return;
        }

        String petName = "未知宠物";
        com.classpets.backend.growth.vo.GrowthConfigVO configVO = growthConfigService.getForClass(classId);
        if (configVO != null && configVO.getPetRoutes() != null) {
            for (com.classpets.backend.growth.vo.GrowthConfigVO.PetRouteVO route : configVO.getPetRoutes()) {
                if (petId.equals(route.getId())) {
                    petName = route.getName();
                    break;
                }
            }
        }

        StudentPetGallery gallery = new StudentPetGallery();
        gallery.setClassId(classId);
        gallery.setStudentId(studentId);
        gallery.setPetRouteId(petId);
        gallery.setPetName(petName);
        gallery.setUnlockTime(System.currentTimeMillis());
        studentPetGalleryMapper.insert(gallery);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String generateTitle(int level, Long classId) {
        try {
            GrowthConfigService.ResolvedGrowthConfig config = growthConfigService.resolveForClass(classId);
            int stage1Max = config.getStage1MaxLevel();
            int stage2Max = config.getStage2MaxLevel();
            if (level <= stage1Max) {
                return "灵兽";
            }
            if (level <= stage2Max) {
                return "神兽";
            }
            return "传说神兽";
        } catch (Exception ex) {
            if (level <= 3) {
                return "灵兽";
            }
            if (level <= 6) {
                return "神兽";
            }
            return "传说神兽";
        }
    }

    private LotteryRecordVO toRecordVO(LotteryDrawRecord row, String studentName) {
        LotteryRecordVO vo = new LotteryRecordVO();
        vo.setId(row.getId());
        vo.setStudentId(row.getStudentId());
        vo.setStudentName(studentName == null ? "未知学生" : studentName);
        vo.setPrizeCode(row.getPrizeCode());
        vo.setPrizeName(row.getPrizeName());
        vo.setRarity(row.getRarity());
        vo.setCostRedeem(row.getCostRedeem());
        vo.setRewardRedeem(row.getRewardRedeem());
        vo.setTimestamp(row.getCreateTime());
        return vo;
    }

    private StudentInventoryVO toInventoryVO(StudentInventory row) {
        StudentInventoryVO vo = new StudentInventoryVO();
        vo.setId(row.getId());
        vo.setStudentId(row.getStudentId());
        vo.setItemCode(row.getItemCode());
        vo.setItemName(row.getItemName());
        vo.setRarity(row.getRarity());
        vo.setQuantity(safe(row.getQuantity()));
        return vo;
    }

    private DeductibleEventVO toDeductibleEventVO(StudentEvent row) {
        DeductibleEventVO vo = new DeductibleEventVO();
        vo.setId(row.getId());
        vo.setReason(row.getReason());
        vo.setChangeValue(row.getChangeValue());
        vo.setTimestamp(row.getTimestamp());
        return vo;
    }

    private Prize randomPrize(Long classId) {
        List<Prize> prizes = getEffectivePrizes(classId);
        if (prizes.isEmpty()) {
            prizes = PRIZES;
        }
        int total = prizes.stream().mapToInt(p -> p.weight).sum();
        int hit = ThreadLocalRandom.current().nextInt(total);
        int sum = 0;
        for (Prize prize : prizes) {
            sum += prize.weight;
            if (hit < sum) {
                return prize;
            }
        }
        return prizes.get(prizes.size() - 1);
    }

    private List<Prize> getEffectivePrizes(Long classId) {
        Map<String, PrizeConfigEntry> overrides = loadPrizeConfig(classId);
        List<Prize> effective = new java.util.ArrayList<>();
        for (Prize base : PRIZES) {
            PrizeConfigEntry override = overrides.get(base.code);
            String name = base.name;
            String icon = base.icon;
            String note = base.note;
            boolean enabled = true;

            if (override != null && base.editable) {
                if (!trim(override.name).isEmpty()) {
                    name = trim(override.name);
                }
                if (!trim(override.icon).isEmpty()) {
                    icon = trim(override.icon);
                }
                if (!trim(override.note).isEmpty()) {
                    note = trim(override.note);
                }
                enabled = true;
            }

            if (!enabled) {
                continue;
            }

            effective.add(new Prize(base.code, name, base.rarity, icon, base.weight, base.itemCode, base.redeemReward, note, base.editable));
        }
        return effective;
    }

    private LotteryPrizeVO toPrizeVO(Prize prize, int singleDrawCost) {
        LotteryPrizeVO vo = new LotteryPrizeVO();
        vo.setCode(prize.code);
        vo.setName(prize.name);
        vo.setRarity(prize.rarity);
        vo.setIcon(prize.icon);
        vo.setNote(prize.note);
        vo.setWeight(prize.weight);
        vo.setSingleDrawCost(singleDrawCost);
        vo.setEnabled(true);
        vo.setEditable(prize.editable);
        return vo;
    }

    private int getSingleDrawCost(Long classId) {
        ClassConfig config = classConfigMapper.selectOne(new LambdaQueryWrapper<ClassConfig>()
                .eq(ClassConfig::getClassId, classId)
                .last("LIMIT 1"));
        if (config == null || trim(config.getPets()).isEmpty()) {
            return SINGLE_DRAW_COST;
        }
        try {
            JsonNode root = objectMapper.readTree(config.getPets());
            if (root == null || !root.isObject()) {
                return SINGLE_DRAW_COST;
            }
            JsonNode node = root.get(LOTTERY_SINGLE_DRAW_COST_KEY);
            if (node == null || node.isNull()) {
                return SINGLE_DRAW_COST;
            }
            return sanitizeSingleDrawCost(node.asInt(SINGLE_DRAW_COST));
        } catch (Exception ex) {
            return SINGLE_DRAW_COST;
        }
    }

    private Map<String, PrizeConfigEntry> loadPrizeConfig(Long classId) {
        ClassConfig config = classConfigMapper.selectOne(new LambdaQueryWrapper<ClassConfig>()
                .eq(ClassConfig::getClassId, classId)
                .last("LIMIT 1"));
        if (config == null || trim(config.getPets()).isEmpty()) {
            return new HashMap<>();
        }
        try {
            JsonNode root = objectMapper.readTree(config.getPets());
            if (root == null || !root.isObject()) {
                return new HashMap<>();
            }
            JsonNode node = root.get(LOTTERY_PRIZES_KEY);
            if (node == null || node.isNull() || !node.isObject()) {
                return new HashMap<>();
            }
            Map<String, PrizeConfigEntry> parsed = objectMapper.convertValue(node, PRIZE_CONFIG_TYPE);
            return parsed == null ? new HashMap<>() : parsed;
        } catch (Exception ex) {
            return new HashMap<>();
        }
    }

    private void savePrizeConfig(Long classId, Map<String, PrizeConfigEntry> overrides, Integer singleDrawCost) {
        ClassConfig config = classConfigMapper.selectOne(new LambdaQueryWrapper<ClassConfig>()
                .eq(ClassConfig::getClassId, classId)
                .last("LIMIT 1"));
        if (config == null) {
            config = new ClassConfig();
            config.setClassId(classId);
        }

        Map<String, Object> petsPayload = new LinkedHashMap<>();
        String petsJson = trim(config.getPets());
        if (!petsJson.isEmpty()) {
            try {
                JsonNode root = objectMapper.readTree(petsJson);
                if (root != null && root.isObject()) {
                    java.util.Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> entry = fields.next();
                        if (LOTTERY_PRIZES_KEY.equals(entry.getKey()) || LOTTERY_SINGLE_DRAW_COST_KEY.equals(entry.getKey())) {
                            continue;
                        }
                        petsPayload.put(entry.getKey(), objectMapper.convertValue(entry.getValue(), Object.class));
                    }
                }
            } catch (Exception ignore) {
                // keep empty payload
            }
        }

        Map<String, PrizeConfigEntry> sanitized = new HashMap<>();
        for (String code : EDITABLE_PRIZE_CODES) {
            PrizeConfigEntry source = overrides.get(code);
            if (source == null) {
                continue;
            }
            PrizeConfigEntry entry = new PrizeConfigEntry();
            entry.name = sanitizePrizeName(source.name);
            entry.icon = sanitizePrizeIcon(source.icon);
            entry.note = sanitizePrizeNote(source.note);
            entry.enabled = true;
            sanitized.put(code, entry);
        }

        petsPayload.put(LOTTERY_PRIZES_KEY, sanitized);
        petsPayload.put(LOTTERY_SINGLE_DRAW_COST_KEY, sanitizeSingleDrawCost(singleDrawCost));
        try {
            config.setPets(objectMapper.writeValueAsString(petsPayload));
        } catch (Exception ex) {
            throw new BizException(50001, "保存抽奖奖品配置失败");
        }

        if (config.getId() == null) {
            classConfigMapper.insert(config);
        } else {
            classConfigMapper.updateById(config);
        }
    }

    private String sanitizePrizeName(String value) {
        String v = trim(value);
        if (v.length() > 20) {
            v = v.substring(0, 20);
        }
        return v;
    }

    private String sanitizePrizeIcon(String value) {
        String v = trim(value);
        if (v.length() > 8) {
            v = v.substring(0, 8);
        }
        return v;
    }

    private String sanitizePrizeNote(String value) {
        String v = trim(value);
        if (v.length() > 64) {
            v = v.substring(0, 64);
        }
        return v;
    }

    private int sanitizeSingleDrawCost(Integer value) {
        int v = value == null ? SINGLE_DRAW_COST : value.intValue();
        if (v < 1) {
            return 1;
        }
        if (v > 999) {
            return 999;
        }
        return v;
    }

    private static final List<Prize> PRIZES = Arrays.asList(
            new Prize("CANDY", "魔法糖果", "common", "🍬", 24, "CANDY", 0, "宠物经验 +25", false),
            new Prize("BALANCE_BAG", "余额福袋", "common", "💰", 21, "BALANCE_BAG", 0, "余额 +10", false),
            new Prize("CLEAN_PASS", "劳动豁免券", "common", "🧹", 18, "CLEAN_PASS", 0, "抵扣一次值日", true),
            new Prize("MEAT", "超级烤肉", "rare", "🍗", 12, "MEAT", 0, "宠物经验 +70", false),
            new Prize("SHIELD", "免死金牌", "rare", "🛡️", 10, ITEM_SHIELD, 0, "抵扣一次扣分", false),
            new Prize("SONG_CARD", "讲台点歌卡", "rare", "🎵", 8, "SONG_CARD", 0, "课间点歌一次", true),
            new Prize("AMNESIA", "遗忘果实", "epic", "🍎", 4, "AMNESIA", 0, "宠物重选路线道具", false),
            new Prize("SPEAK_STICK", "发言指定棒", "epic", "👑", 2, "SPEAK_STICK", 0, "指定同学代答一次", true),
            new Prize("BIG_BURST", "经验大爆雪", "epic", "💥", 1, "BIG_BURST", 0, "EXP+200 / 余额+100", false));

    private static class Prize {
        private final String code;
        private final String name;
        private final String rarity;
        private final String icon;
        private final int weight;
        private final String itemCode;
        private final int redeemReward;
        private final String note;
        private final boolean editable;

        private Prize(String code, String name, String rarity, String icon, int weight, String itemCode, int redeemReward,
                String note, boolean editable) {
            this.code = code;
            this.name = name;
            this.rarity = rarity;
            this.icon = icon;
            this.weight = weight;
            this.itemCode = itemCode;
            this.redeemReward = redeemReward;
            this.note = note;
            this.editable = editable;
        }
    }

    private static class PrizeConfigEntry {
        public String name;
        public String icon;
        public String note;
        public Boolean enabled;
    }
}
