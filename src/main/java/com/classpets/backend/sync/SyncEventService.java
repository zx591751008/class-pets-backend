package com.classpets.backend.sync;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SyncEventService {

    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> classEmitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long classId) {
        SseEmitter emitter = new SseEmitter(0L);
        classEmitters.computeIfAbsent(classId, key -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(classId, emitter));
        emitter.onTimeout(() -> removeEmitter(classId, emitter));
        emitter.onError(ex -> removeEmitter(classId, emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("ok"));
        } catch (IOException ex) {
            removeEmitter(classId, emitter);
        }
        return emitter;
    }

    public void publishClassChange(Long classId, String eventType, Long entityId) {
        SyncEventPayload payload = new SyncEventPayload();
        payload.setEventType(eventType);
        payload.setClassId(classId);
        payload.setEntityId(entityId);
        payload.setTs(System.currentTimeMillis());
        broadcast(classId, "class_change", payload);
    }

    @Scheduled(fixedDelay = 25000)
    public void heartbeat() {
        for (Map.Entry<Long, CopyOnWriteArrayList<SseEmitter>> entry : classEmitters.entrySet()) {
            broadcast(entry.getKey(), "heartbeat", "ping");
        }
    }

    private void broadcast(Long classId, String eventName, Object payload) {
        List<SseEmitter> emitters = classEmitters.get(classId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(payload));
            } catch (IOException ex) {
                removeEmitter(classId, emitter);
            }
        }
    }

    private void removeEmitter(Long classId, SseEmitter emitter) {
        List<SseEmitter> emitters = classEmitters.get(classId);
        if (emitters == null) {
            return;
        }
        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            classEmitters.remove(classId);
        }
    }
}
