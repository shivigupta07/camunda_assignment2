package com.assignment.registrationProcess.listener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RegistrationJmsListener {

    private final ZeebeClient zeebeClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public RegistrationJmsListener(ZeebeClient zeebeClient, ObjectMapper objectMapper) {
        this.zeebeClient = zeebeClient;
        this.objectMapper = objectMapper;
    }

    @JmsListener(destination = "${app.jms.registration-queue}")
    public void listen(String messageJson) {
        try {

            Map<String, Object> message = objectMapper.readValue(
                    messageJson, new TypeReference<Map<String, Object>>() {}
            );

            System.out.println("Received JMS message: " + message);

            zeebeClient
                .newCreateInstanceCommand()
                .bpmnProcessId("registration_process") // BPMN process ID
                .latestVersion()
                .variables(message)
                .send()
                .join();

            System.out.println("Process started for requestId: " + message.get("requestId"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
