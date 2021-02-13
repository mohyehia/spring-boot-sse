package com.mohyehia.sse.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@Log4j2
@CrossOrigin("*")
public class NotificationController {

    public Map<String, SseEmitter> emitters = new HashMap<>();

    @GetMapping("/subscribe/{userId}")
    public SseEmitter subscribe(@PathVariable("userId") String userId) throws IOException {
        log.info("subscribing for user {} ......", userId);
        SseEmitter sseEmitter = new SseEmitter(24 * 60 * 60 * 1000L);
        sseEmitter.send(SseEmitter.event().name("INIT"));
        emitters.put(userId, sseEmitter);
        sseEmitter.onCompletion(() -> emitters.remove(userId));
        sseEmitter.onTimeout(() -> emitters.remove(userId));
        sseEmitter.onError((e) -> {
            e.printStackTrace();
            emitters.remove(userId);
        });
        log.info("user {} subscribed successfully", userId);
        return sseEmitter;
    }

    @GetMapping("/send/{userId}/{content}")
    public void sendEvent(@PathVariable("userId") String userId, @PathVariable String content){
        for(Map.Entry<String, SseEmitter> entry : emitters.entrySet()){
            try {
                if(entry.getKey().equals(userId)){
                    entry.getValue().send(SseEmitter.event().name("latestNews").data(content));
                }
            } catch (IOException e){
                e.printStackTrace();
                emitters.remove(userId);
            }
        }
    }

//    @PostMapping("notification/{username}")
//    public ResponseEntity<?> send(@PathVariable("username") String username, @RequestBody NotificationRequest request){
//        log.info("submitted username =>{}, from =>{}, message =>{}", username, request.getFrom(), request.getMessage());
//        emitterService.pushNotification(username, request.getFrom(), request.getMessage());
//        return new ResponseEntity<>("message pushed to user => " + username, HttpStatus.OK);
//    }
}
