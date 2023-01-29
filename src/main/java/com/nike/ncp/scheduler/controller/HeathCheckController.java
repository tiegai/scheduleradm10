package com.nike.ncp.scheduler.controller;

import com.nike.ncp.scheduler.controller.annotation.PermissionLimit;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "scheduler admin api", tags = "scheduler admin")
@RestController
public class HeathCheckController {

    @ApiOperation("scheduler admin health check")
    @GetMapping("/healthcheck")
    @PermissionLimit(limit = false)
    public ResponseEntity<Void> healthCheck() {
        return ResponseEntity.ok().build();
    }
}
