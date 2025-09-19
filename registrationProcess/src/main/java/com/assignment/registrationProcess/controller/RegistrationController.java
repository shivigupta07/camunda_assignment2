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

    // Start a new BPMN process
    @PostMapping("/start-process")
    public String startProcess(@RequestBody Map<String, Object> variables) {
        try {
            zeebeClient
                .newCreateInstanceCommand()
                .bpmnProcessId("registration_process") // Replace with your actual BPMN process ID
                .latestVersion()
                .variables(variables)
                .send()
                .join();

            return "Process started successfully!";
        } catch (Exception e) {
            return "Failed to start process: " + e.getMessage();
        }
    }
    
    // Mock API for Activate Registration (Worker will call this)
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
