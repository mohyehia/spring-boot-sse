package com.mohyehia.sse.service;

import com.mohyehia.sse.model.Notification;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
public class EmitterService {
    List<SseEmitter> emitters = new ArrayList<>();

    public void addEmitter(SseEmitter emitter){
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitters.add(emitter);
    }

    public void pushNotification(String username, String name, String message){
        log.info("pushing {} notification for user {}", message, username);
        List<SseEmitter> deadEmitters = new ArrayList<>();
        Notification payload = Notification
                .builder()
                .from(name)
                .message(message)
                .build();
        emitters.forEach(emitter -> {
            try {
                emitter.send(
                        SseEmitter
                        .event()
                        .name(username)
                        .data(payload)
                );
            } catch (IOException e){
                e.printStackTrace();
                deadEmitters.add(emitter);
            }
        });
        emitters.removeAll(deadEmitters);
    }
}
