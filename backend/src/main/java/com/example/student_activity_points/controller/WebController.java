package com.example.student_activity_points.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class WebController {

    private static final Logger log = LoggerFactory.getLogger(WebController.class);

    @RequestMapping(value = "/{path:[^\\.]*}")
    public String redirect() {
        log.debug("Forwarding request to index.html for SPA routing");
        return "forward:/index.html";
    }
}