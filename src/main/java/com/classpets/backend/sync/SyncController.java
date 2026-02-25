package com.classpets.backend.sync;

import com.classpets.backend.classinfo.service.ClassInfoService;
import com.classpets.backend.common.BizException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1/classes/{classId}/events")
public class SyncController {

    private final ClassInfoService classInfoService;
    private final SyncEventService syncEventService;

    public SyncController(ClassInfoService classInfoService, SyncEventService syncEventService) {
        this.classInfoService = classInfoService;
        this.syncEventService = syncEventService;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable Long classId, HttpServletResponse response) {
        if (!classInfoService.isOwnedByCurrentTeacher(classId)) {
            throw new BizException(40301, "无权访问此班级");
        }
        response.setHeader("Cache-Control", "no-cache, no-transform");
        response.setHeader("X-Accel-Buffering", "no");
        response.setHeader("Connection", "keep-alive");
        return syncEventService.subscribe(classId);
    }
}
