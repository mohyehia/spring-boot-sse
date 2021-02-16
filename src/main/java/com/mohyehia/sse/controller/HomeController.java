package com.mohyehia.sse.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
@Log4j2
public class HomeController {
    public Map<String, SseEmitter> emitters = new HashMap<>();

    @GetMapping("/home")
    public String viewHomePage(@AuthenticationPrincipal Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        return "home";
    }

    @GetMapping("/admin")
    public String viewAdminPage() {
        return "admin";
    }

    @PostMapping("/notify")
    @ResponseBody
    public String sendEvent(@RequestParam("username") String username,
                            @RequestParam("content") String content) {
        log.info("submitted username =>{}, content =>{}", username, content);
        for (Map.Entry<String, SseEmitter> entry : emitters.entrySet()) {
            try {
                if (entry.getKey().equals(username)) {
                    log.info("sending to =>{}", username);
                    entry.getValue().send(SseEmitter.event().name("latestNews").data(content));
                }
            } catch (IOException e) {
                e.printStackTrace();
                emitters.remove(username);
            }
        }
        return "success";
    }

    @PostMapping("/notify-all")
    @ResponseBody
    public String notifyAllUsers(@RequestParam("content") String content) {
        log.info("submitted content to notify all users =>{}", content);
        for (Map.Entry<String, SseEmitter> entry : emitters.entrySet()) {
            try {
                entry.getValue().send(SseEmitter.event().name("latestNews").data(content));
            } catch (IOException e) {
                e.printStackTrace();
                emitters.remove(entry.getKey());
            }
        }
        log.info("content sent successfully to all users!");
        return "success";
    }

    @GetMapping("subscribe/{userId}")
    @ResponseBody
    public SseEmitter subscribe(@PathVariable("userId") String userId) throws IOException {
        log.info("subscribing for user {} ......", userId);
        SseEmitter sseEmitter = new SseEmitter(Long.MAX_VALUE);
        sseEmitter.send(SseEmitter.event().name("INIT"));
        if (emitters.get(userId) != null) {
            emitters.remove(userId);
        }
        emitters.put(userId, sseEmitter);
        sseEmitter.onCompletion(() -> emitters.remove(userId));
        sseEmitter.onError((e) -> {
            e.printStackTrace();
            emitters.remove(userId);
        });
        log.info("user {} subscribed successfully", userId);
        return sseEmitter;
    }
}
