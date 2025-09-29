package com.assignment.registrationProcess.controller;

import io.camunda.zeebe.client.ZeebeClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class RegistrationController {

    private final ZeebeClient zeebeClient;

    @Autowired
    public RegistrationController(ZeebeClient zeebeClient) {
        this.zeebeClient = zeebeClient;
    }

    @PostMapping("/registration/activate")
    public ResponseEntity<Map<String, Object>> activateRegistration(@RequestBody Map<String, Object> request) {
        System.out.println("Activate Registration API called with: " + request);

        Map<String, Object> response = Map.of(
                "message", "Registration activated successfully",
                "requestId", request.get("requestId"),
                "status", "activated"
        );

        return ResponseEntity.ok(response);
    }

    
}
