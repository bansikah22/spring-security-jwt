package com.bansikah.secureportal.admin.web;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    @GetMapping
    Map<String, String> dashboard() {
        return Map.of("status", "Administrative API access granted");
    }
}