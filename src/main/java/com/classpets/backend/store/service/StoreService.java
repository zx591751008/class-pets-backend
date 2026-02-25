package com.classpets.backend.store.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.classpets.backend.entity.StoreItem;
import com.classpets.backend.entity.RedemptionRecord;

import java.util.List;

public interface StoreService extends IService<StoreItem> {

    /**
     * List store items for a class (including inactive)
     */
    List<StoreItem> getItemsByClass(Long classId);

    /**
     * Create a new store item
     */
    StoreItem createItem(StoreItem item);

    /**
     * Update an item
     */
    StoreItem updateItem(Long itemId, StoreItem item);

    /**
     * Toggle item active status (shelve/unshelve)
     */
    StoreItem setItemActive(Long itemId, Boolean isActive);

    /**
     * Soft delete an item (set is_deleted = 1)
     */
    void deleteItem(Long itemId);

    /**
     * Process redemption (Transactional)
     * 
     * @return The created redemption record
     */
    RedemptionRecord redeemItem(Long studentId, Long itemId);

    /**
     * Undo a completed redemption record.
     */
    RedemptionRecord undoRedemption(Long recordId);
}
