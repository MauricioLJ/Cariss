package com.mauledji.app.cariss.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
    
    @GetMapping("/")
    public String redirectIndex() {
        return "forward:/index.html";
    }
}


