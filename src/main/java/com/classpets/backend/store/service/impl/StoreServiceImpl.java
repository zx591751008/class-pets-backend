package com.classpets.backend.store.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.classpets.backend.classinfo.service.ClassInfoService;
import com.classpets.backend.common.BizException;
import com.classpets.backend.entity.RedemptionRecord;
import com.classpets.backend.entity.StoreItem;
import com.classpets.backend.entity.Student;
import com.classpets.backend.entity.StudentEvent;
import com.classpets.backend.mapper.RedemptionRecordMapper;
import com.classpets.backend.mapper.StoreItemMapper;
import com.classpets.backend.mapper.StudentEventMapper;
import com.classpets.backend.mapper.StudentMapper;
import com.classpets.backend.sync.SyncEventService;
import com.classpets.backend.store.service.StoreService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StoreServiceImpl extends ServiceImpl<StoreItemMapper, StoreItem> implements StoreService {

    private final StudentMapper studentMapper;
    private final RedemptionRecordMapper redemptionRecordMapper;
    private final ClassInfoService classInfoService;
    private final StudentEventMapper studentEventMapper;
    private final SyncEventService syncEventService;

    public StoreServiceImpl(StudentMapper studentMapper,
            RedemptionRecordMapper redemptionRecordMapper,
            ClassInfoService classInfoService,
            StudentEventMapper studentEventMapper,
            SyncEventService syncEventService) {
        this.studentMapper = studentMapper;
        this.redemptionRecordMapper = redemptionRecordMapper;
        this.classInfoService = classInfoService;
        this.studentEventMapper = studentEventMapper;
        this.syncEventService = syncEventService;
    }

    @Override
    public List<StoreItem> getItemsByClass(Long classId) {
        ensureClassOwnership(classId);
        return this.list(new LambdaQueryWrapper<StoreItem>()
                .eq(StoreItem::getClassId, classId)
                .eq(StoreItem::getIsDeleted, false)
                .orderByDesc(StoreItem::getCreateTime));
    }

    @Override
    public StoreItem createItem(StoreItem item) {
        if (item == null || item.getClassId() == null) {
            throw new BizException(40001, "classId 不能为空");
        }
        ensureClassOwnership(item.getClassId());

        if (item.getCost() == null || item.getCost() < 0) {
            throw new BizException(40001, "商品积分不能小于 0");
        }
        this.save(item);
        return item;
    }

    @Override
    public StoreItem updateItem(Long itemId, StoreItem itemDetails) {
        StoreItem existing = this.getById(itemId);
        if (existing == null) {
            throw new BizException(40401, "商品不存在");
        }
        ensureClassOwnership(existing.getClassId());

        // Update fields
        if (itemDetails.getName() != null)
            existing.setName(itemDetails.getName());
        if (itemDetails.getDescription() != null)
            existing.setDescription(itemDetails.getDescription());
        if (itemDetails.getCost() != null)
            existing.setCost(itemDetails.getCost());
        if (itemDetails.getStock() != null)
            existing.setStock(itemDetails.getStock());
        if (itemDetails.getIcon() != null)
            existing.setIcon(itemDetails.getIcon());
        if (itemDetails.getIsActive() != null)
            existing.setIsActive(itemDetails.getIsActive());

        this.updateById(existing);
        return existing;
    }

    @Override
    public StoreItem setItemActive(Long itemId, Boolean isActive) {
        StoreItem existing = this.getById(itemId);
        if (existing == null) {
            throw new BizException(40401, "商品不存在");
        }
        ensureClassOwnership(existing.getClassId());
        existing.setIsActive(Boolean.TRUE.equals(isActive));
        boolean ok = this.updateById(existing);
        if (!ok) {
            throw new BizException(50001, "更新商品状态失败");
        }
        return existing;
    }

    @Override
    public void deleteItem(Long itemId) {
        StoreItem item = this.getById(itemId);
        if (item == null) {
            throw new BizException(40401, "商品不存在");
        }

        ensureClassOwnership(item.getClassId());
        boolean ok = this.removeById(itemId);
        if (!ok) {
            throw new BizException(50001, "删除商品失败");
        }
    }

    @Transactional
    @Override
    public RedemptionRecord redeemItem(Long studentId, Long itemId) {
        // 1. Get Item (Locking ideally, but for MVP simple read is okay, or use
        // optimisitic locking if high concurrency expected)
        StoreItem item = this.getById(itemId);
        if (item == null || item.getIsDeleted() || !item.getIsActive()) {
            throw new BizException(40401, "商品不可用");
        }
        ensureClassOwnership(item.getClassId());

        // Check stock
        if (item.getStock() != -1 && item.getStock() <= 0) {
            throw new BizException(40001, "商品库存不足");
        }

        // 2. Get Student
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BizException(40401, "学生不存在");
        }

        if (!item.getClassId().equals(student.getClassId())) {
            throw new BizException(40001, "商品与学生不属于同一班级");
        }

        // 3. Check Balance
        if (student.getRedeemPoints() == null || student.getRedeemPoints() < item.getCost()) {
            throw new BizException(40001, "积分余额不足");
        }

        // 4. Deduct Points
        student.setRedeemPoints(student.getRedeemPoints() - item.getCost());
        student.setUpdateTime(System.currentTimeMillis());
        studentMapper.updateById(student);

        // 5. Decrement Stock
        if (item.getStock() != -1) {
            item.setStock(item.getStock() - 1);
            this.updateById(item);
        }

        // 6. Create Record
        RedemptionRecord record = new RedemptionRecord();
        record.setClassId(item.getClassId());
        record.setStudentId(studentId);
        record.setItemId(itemId);
        record.setItemName(item.getName());
        record.setCost(item.getCost());
        record.setStatus("COMPLETED"); // Auto-complete for now

        redemptionRecordMapper.insert(record);

        // 7. Write student event so redemption appears in history
        StudentEvent event = new StudentEvent();
        event.setClassId(item.getClassId());
        event.setStudentId(studentId);
        event.setReason("兑换商品: " + item.getName());
        event.setChangeValue(0);
        event.setRedeemChange(-item.getCost());
        event.setNote("redeemRecordId=" + record.getId() + ";itemId=" + itemId);
        event.setTimestamp(System.currentTimeMillis());
        event.setRevoked(0);
        studentEventMapper.insert(event);

        syncEventService.publishClassChange(item.getClassId(), "store_redeem", studentId);

        return record;
    }

    @Transactional
    @Override
    public RedemptionRecord undoRedemption(Long recordId) {
        RedemptionRecord record = redemptionRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BizException(40401, "兑换记录不存在");
        }
        ensureClassOwnership(record.getClassId());

        if (!"COMPLETED".equalsIgnoreCase(record.getStatus())) {
            throw new BizException(40001, "该兑换记录已撤销或不可撤销");
        }

        Student student = studentMapper.selectById(record.getStudentId());
        if (student == null || !record.getClassId().equals(student.getClassId())) {
            throw new BizException(40401, "学生不存在或不属于当前班级");
        }

        StoreItem item = this.getById(record.getItemId());

        int refundCost = Math.max(0, record.getCost() == null ? 0 : record.getCost());
        student.setRedeemPoints((student.getRedeemPoints() == null ? 0 : student.getRedeemPoints()) + refundCost);
        student.setUpdateTime(System.currentTimeMillis());
        studentMapper.updateById(student);

        if (item != null && !Boolean.TRUE.equals(item.getIsDeleted()) && item.getStock() != null && item.getStock() != -1) {
            item.setStock(item.getStock() + 1);
            this.updateById(item);
        }

        record.setStatus("REFUNDED");
        redemptionRecordMapper.updateById(record);

        StudentEvent redeemEvent = studentEventMapper.selectOne(new LambdaQueryWrapper<StudentEvent>()
                .eq(StudentEvent::getClassId, record.getClassId())
                .eq(StudentEvent::getStudentId, record.getStudentId())
                .eq(StudentEvent::getRevoked, 0)
                .like(StudentEvent::getNote, "redeemRecordId=" + record.getId())
                .orderByDesc(StudentEvent::getId)
                .last("limit 1"));

        if (redeemEvent != null) {
            redeemEvent.setRevoked(1);
            studentEventMapper.updateById(redeemEvent);
        }

        StudentEvent undoEvent = new StudentEvent();
        undoEvent.setClassId(record.getClassId());
        undoEvent.setStudentId(record.getStudentId());
        undoEvent.setReason("撤销兑换: " + (record.getItemName() == null ? "商品" : record.getItemName()));
        undoEvent.setChangeValue(0);
        undoEvent.setRedeemChange(refundCost);
        undoEvent.setNote("undoRedemptionRecordId=" + record.getId());
        undoEvent.setTimestamp(System.currentTimeMillis());
        undoEvent.setRevoked(0);
        studentEventMapper.insert(undoEvent);

        syncEventService.publishClassChange(record.getClassId(), "store_redeem_undo", record.getStudentId());

        return record;
    }

    private void ensureClassOwnership(Long classId) {
        if (!classInfoService.isOwnedByCurrentTeacher(classId)) {
            throw new BizException(40301, "无权访问此班级");
        }
    }
}
