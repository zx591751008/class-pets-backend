package com.classpets.backend.controller;

import com.classpets.backend.common.ApiResponse;
import com.classpets.backend.common.BizException;
import com.classpets.backend.entity.RedemptionRecord;
import com.classpets.backend.entity.StoreItem;
import com.classpets.backend.store.service.StoreService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    /**
     * List items for a class
     */
    @GetMapping("/classes/{classId}/store/items")
    public ApiResponse<List<StoreItem>> listItems(@PathVariable Long classId) {
        return ApiResponse.ok(storeService.getItemsByClass(classId));
    }

    /**
     * Create item
     */
    @PostMapping("/classes/{classId}/store/items")
    public ApiResponse<StoreItem> createItem(@PathVariable Long classId, @RequestBody StoreItem item) {
        item.setClassId(classId);
        // Default values
        if (item.getIsActive() == null)
            item.setIsActive(true);
        if (item.getStock() == null)
            item.setStock(-1); // Infinite default

        return ApiResponse.ok(storeService.createItem(item));
    }

    /**
     * Update item
     */
    @PutMapping("/store/items/{itemId}")
    public ApiResponse<StoreItem> updateItem(@PathVariable Long itemId, @RequestBody StoreItem item) {
        return ApiResponse.ok(storeService.updateItem(itemId, item));
    }

    /**
     * Toggle shelf status (active/inactive)
     */
    @PatchMapping("/store/items/{itemId}/active")
    public ApiResponse<StoreItem> setItemActive(@PathVariable Long itemId, @RequestBody Map<String, Boolean> request) {
        Boolean isActive = request.get("isActive");
        if (isActive == null) {
            throw new BizException(40001, "缺少 isActive 参数");
        }
        return ApiResponse.ok(storeService.setItemActive(itemId, isActive));
    }

    /**
     * Delete item (Soft delete)
     */
    @DeleteMapping("/store/items/{itemId}")
    public ApiResponse<Void> deleteItem(@PathVariable Long itemId) {
        storeService.deleteItem(itemId);
        return ApiResponse.ok();
    }

    /**
     * Redeem item (Student action)
     * Body: { studentId: 123, itemId: 456 }
     */
    @PostMapping("/store/redeem")
    public ApiResponse<RedemptionRecord> redeemItem(@RequestBody Map<String, Long> request) {
        Long studentId = request.get("studentId");
        Long itemId = request.get("itemId");

        if (studentId == null || itemId == null) {
            throw new BizException(40001, "缺少 studentId 或 itemId");
        }

        RedemptionRecord record = storeService.redeemItem(studentId, itemId);
        return ApiResponse.ok(record);
    }

    @PostMapping("/store/redemptions/{recordId}/undo")
    public ApiResponse<RedemptionRecord> undoRedemption(@PathVariable Long recordId) {
        return ApiResponse.ok(storeService.undoRedemption(recordId));
    }
}
