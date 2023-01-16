package com.nike.ncp.scheduler.controller;

import com.nike.ncp.scheduler.controller.annotation.PermissionLimit;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HeathCheckController {

    @GetMapping("/healthcheck")
    @PermissionLimit(limit = false)
    public ResponseEntity<Void> healthCheck() {
        return ResponseEntity.ok().build();
    }
}
