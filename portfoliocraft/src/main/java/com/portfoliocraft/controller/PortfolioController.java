package com.portfoliocraft.controller;

import com.portfoliocraft.dto.GenerateRequest;
import com.portfoliocraft.service.PortfolioService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/")
    public String home() {
        return "forward:/home.html";
    }

    @GetMapping("/builder")
    public String builder() {
        return "forward:/builder.html";
    }

    @PostMapping("/generate")
    @ResponseBody
    public ResponseEntity<Map<String, String>> generate(@RequestBody GenerateRequest request) {
        if (request == null || request.userData() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "No user data received"));
        }
        String html = portfolioService.generate(request.userData());
        return ResponseEntity.ok(Map.of("html", html));
    }
}
