package com.classpets.backend.lottery.controller;

import com.classpets.backend.common.ApiResponse;
import com.classpets.backend.lottery.dto.InventoryUseRequest;
import com.classpets.backend.lottery.dto.LotteryDrawRequest;
import com.classpets.backend.lottery.dto.LotteryPrizeConfigUpdateRequest;
import com.classpets.backend.lottery.service.LotteryService;
import com.classpets.backend.lottery.vo.DeductibleEventVO;
import com.classpets.backend.lottery.vo.LotteryDrawResultVO;
import com.classpets.backend.lottery.vo.LotteryPrizeVO;
import com.classpets.backend.lottery.vo.LotteryRecordVO;
import com.classpets.backend.lottery.vo.StudentInventoryVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class LotteryController {

    private final LotteryService lotteryService;

    public LotteryController(LotteryService lotteryService) {
        this.lotteryService = lotteryService;
    }

    @PostMapping("/classes/{classId}/lottery/draw/single")
    public ApiResponse<LotteryDrawResultVO> drawSingle(@PathVariable Long classId,
            @Validated @RequestBody LotteryDrawRequest request) {
        return ApiResponse.ok(lotteryService.drawSingle(classId, request.getStudentId()));
    }

    @GetMapping("/classes/{classId}/lottery-records")
    public ApiResponse<List<LotteryRecordVO>> listClassLotteryRecords(@PathVariable Long classId) {
        return ApiResponse.ok(lotteryService.listClassRecords(classId));
    }

    @GetMapping("/classes/{classId}/lottery/prizes")
    public ApiResponse<List<LotteryPrizeVO>> listClassLotteryPrizes(@PathVariable Long classId) {
        return ApiResponse.ok(lotteryService.listClassPrizes(classId));
    }

    @PutMapping("/classes/{classId}/lottery/prizes")
    public ApiResponse<List<LotteryPrizeVO>> updateClassLotteryPrizes(
            @PathVariable Long classId,
            @RequestBody(required = false) LotteryPrizeConfigUpdateRequest request) {
        return ApiResponse.ok(lotteryService.updateClassPrizes(
                classId,
                request == null ? null : request.getItems(),
                request == null ? null : request.getSingleDrawCost()));
    }

    @GetMapping("/students/{studentId}/lottery-records")
    public ApiResponse<List<LotteryRecordVO>> listStudentLotteryRecords(@PathVariable Long studentId) {
        return ApiResponse.ok(lotteryService.listRecords(studentId));
    }

    @GetMapping("/students/{studentId}/inventory")
    public ApiResponse<List<StudentInventoryVO>> listStudentInventory(@PathVariable Long studentId) {
        return ApiResponse.ok(lotteryService.listInventory(studentId));
    }

    @GetMapping("/students/{studentId}/deductible-events")
    public ApiResponse<List<DeductibleEventVO>> listDeductibleEvents(@PathVariable Long studentId) {
        return ApiResponse.ok(lotteryService.listDeductibleEvents(studentId));
    }

    @PostMapping("/students/{studentId}/inventory/{inventoryId}/use")
    public ApiResponse<StudentInventoryVO> useInventory(@PathVariable Long studentId,
            @PathVariable Long inventoryId,
            @RequestBody(required = false) InventoryUseRequest request) {
        return ApiResponse.ok(lotteryService.useInventory(studentId, inventoryId, request));
    }
}
