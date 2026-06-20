package com.campusmart.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/demo")
@RequiredArgsConstructor
public class DemoSeedController {

    private final DemoDataService demoDataService;

    @PostMapping("/seed")
    public ResponseEntity<String> seed(@RequestParam(defaultValue = "false") boolean force) {
        String result = demoDataService.seed(force);
        return ResponseEntity.ok(result);
    }
}
