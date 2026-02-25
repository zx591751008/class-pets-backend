package com.classpets.backend.sync;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SyncEventServiceTest {

    @Test
    void subscribeAndPublishShouldNotThrow() {
        SyncEventService service = new SyncEventService();

        SseEmitter emitter = service.subscribe(10L);
        assertNotNull(emitter);

        assertDoesNotThrow(() -> service.publishClassChange(10L, "student_updated", 99L));
        assertDoesNotThrow(service::heartbeat);

        emitter.complete();
    }
}
