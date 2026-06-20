package com.campusmart.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DemoDataInitializer implements CommandLineRunner {

    private final DemoDataService demoDataService;

    @Override
    public void run(String... args) throws Exception {
        boolean force = "true".equalsIgnoreCase(System.getenv("DEMO_SEED_FORCE"));
        String result = demoDataService.seed(force);
        System.out.println(result);
    }
}
