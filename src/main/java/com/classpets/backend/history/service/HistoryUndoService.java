package com.classpets.backend.history.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classpets.backend.classinfo.service.ClassInfoService;
import com.classpets.backend.common.BizException;
import com.classpets.backend.entity.RedemptionRecord;
import com.classpets.backend.entity.Student;
import com.classpets.backend.entity.StudentEvent;
import com.classpets.backend.growth.service.GrowthConfigService;
import com.classpets.backend.mapper.RedemptionRecordMapper;
import com.classpets.backend.mapper.StudentEventMapper;
import com.classpets.backend.mapper.StudentMapper;
import com.classpets.backend.sync.SyncEventService;
import com.classpets.backend.store.service.StoreService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HistoryUndoService {

    private final StudentEventMapper studentEventMapper;
    private final StudentMapper studentMapper;
    private final ClassInfoService classInfoService;
    private final StoreService storeService;
    private final RedemptionRecordMapper redemptionRecordMapper;
    private final SyncEventService syncEventService;
    private final GrowthConfigService growthConfigService;

    public HistoryUndoService(StudentEventMapper studentEventMapper,
            StudentMapper studentMapper,
            ClassInfoService classInfoService,
            StoreService storeService,
            RedemptionRecordMapper redemptionRecordMapper,
            SyncEventService syncEventService,
            GrowthConfigService growthConfigService) {
        this.studentEventMapper = studentEventMapper;
        this.studentMapper = studentMapper;
        this.classInfoService = classInfoService;
        this.storeService = storeService;
        this.redemptionRecordMapper = redemptionRecordMapper;
        this.syncEventService = syncEventService;
        this.growthConfigService = growthConfigService;
    }

    @Transactional
    public void undoByEvent(Long classId, Long eventId) {
        if (!classInfoService.isOwnedByCurrentTeacher(classId)) {
            throw new BizException(40301, "无权访问此班级");
        }

        StudentEvent event = studentEventMapper.selectById(eventId);
        if (event == null || !classId.equals(event.getClassId())) {
            throw new BizException(40401, "记录不存在");
        }
        if (event.getRevoked() != null && event.getRevoked() == 1) {
            throw new BizException(40001, "该记录已撤销");
        }
        if (event.getNote() != null && (event.getNote().contains("undoFromEventId=")
                || event.getNote().contains("undoRedemptionRecordId="))) {
            throw new BizException(40001, "撤销操作记录不支持再次撤销");
        }
        if ((event.getReason() != null && event.getReason().contains("遗忘果实"))
                || (event.getNote() != null && event.getNote().contains("noUndo=1"))) {
            throw new BizException(40001, "该记录不可撤销");
        }

        Long redemptionRecordId = parseLongFromNote(event.getNote(), "redeemRecordId=");
        if (redemptionRecordId != null) {
            storeService.undoRedemption(redemptionRecordId);
            return;
        }

        if (event.getReason() != null && event.getReason().startsWith("兑换商品:")) {
            RedemptionRecord matched = redemptionRecordMapper.selectOne(new LambdaQueryWrapper<RedemptionRecord>()
                    .eq(RedemptionRecord::getClassId, classId)
                    .eq(RedemptionRecord::getStudentId, event.getStudentId())
                    .eq(RedemptionRecord::getStatus, "COMPLETED")
                    .eq(RedemptionRecord::getItemName, event.getReason().replace("兑换商品:", "").trim())
                    .eq(RedemptionRecord::getCost, Math.abs(event.getRedeemChange() == null ? 0 : event.getRedeemChange()))
                    .orderByDesc(RedemptionRecord::getCreateTime)
                    .last("limit 1"));
            if (matched != null) {
                storeService.undoRedemption(matched.getId());
                return;
            }
            throw new BizException(40001, "该兑换记录较旧，暂不可从此处撤销，请在兑换模块操作");
        }

        Student student = studentMapper.selectById(event.getStudentId());
        if (student == null || !classId.equals(student.getClassId())) {
            throw new BizException(40401, "学生不存在");
        }

        int changeValue = event.getChangeValue() == null ? 0 : event.getChangeValue();
        int redeemChange = event.getRedeemChange() == null ? 0 : event.getRedeemChange();
        int expChange = event.getExpChange() == null ? 0 : event.getExpChange();
        boolean expExact = event.getNote() != null && event.getNote().contains("expExact=1");

        student.setTotalPoints((student.getTotalPoints() == null ? 0 : student.getTotalPoints()) - changeValue);
        int nextRedeem = (student.getRedeemPoints() == null ? 0 : student.getRedeemPoints()) - redeemChange;
        student.setRedeemPoints(Math.max(0, nextRedeem));
        int currentExp = student.getExp() == null ? 0 : student.getExp();
        int nextExp;
        if (expExact || expChange != 0) {
            nextExp = Math.max(0, currentExp - expChange);
        } else if (changeValue != 0) {
            int undoChangeValue = -changeValue;
            nextExp = Math.max(0, currentExp + calcExpDeltaFromChange(undoChangeValue, resolveExpGainRatio(classId)));
        } else {
            nextExp = currentExp;
        }
        student.setExp(nextExp);
        int level = snapshotLevelByExp(classId, nextExp);
        student.setLevel(level);
        student.setTitle(generateTitle(level, classId));
        student.setUpdateTime(System.currentTimeMillis());
        studentMapper.updateById(student);

        event.setRevoked(1);
        studentEventMapper.updateById(event);

        syncEventService.publishClassChange(classId, "history_undo", event.getStudentId());
    }

    private double resolveExpGainRatio(Long classId) {
        GrowthConfigService.ResolvedGrowthConfig config = growthConfigService.resolveForClass(classId);
        double ratio = config.getExpGainRatio() == null ? 1.0 : config.getExpGainRatio();
        return ratio <= 0 ? 1.0 : ratio;
    }

    private int calcExpDeltaFromChange(int changeValue, double ratio) {
        if (changeValue == 0) {
            return 0;
        }
        int abs = Math.abs(changeValue);
        int gain = (int) Math.round(abs * ratio);
        gain = Math.max(1, gain);
        return changeValue > 0 ? gain : -gain;
    }

    private int snapshotLevelByExp(Long classId, int exp) {
        int safeExp = Math.max(0, exp);
        java.util.List<Integer> levels = growthConfigService.resolveForClass(classId).getLevelThresholds();
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

    private Long parseLongFromNote(String note, String key) {
        if (note == null || key == null || key.isEmpty()) {
            return null;
        }
        int idx = note.indexOf(key);
        if (idx < 0) {
            return null;
        }
        int start = idx + key.length();
        int end = start;
        while (end < note.length() && Character.isDigit(note.charAt(end))) {
            end++;
        }
        if (end <= start) {
            return null;
        }
        try {
            return Long.parseLong(note.substring(start, end));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
