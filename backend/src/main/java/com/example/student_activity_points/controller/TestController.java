package com.example.student_activity_points.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/test")
    public String test() {
        return "CORS is working!";
    }
    @GetMapping("/test-error")
        public void triggerError() {
    throw new RuntimeException("This internal error should not be seen by the client");
    }
}
