package com.assignment.registrationProcess.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

@Component
public class InspectionResponseListener {

    private final ZeebeClient zeebeClient;
    private final ObjectMapper objectMapper;

    public InspectionResponseListener(ZeebeClient zeebeClient) {
        this.zeebeClient = zeebeClient;
        this.objectMapper = new ObjectMapper();
    }
    @Value("${app.jms.response-queue}")
    private String responseQueue;

    @JmsListener(destination = "${app.jms.response-queue}")
    public void onMessage(String msg) {
        System.out.println("Got response: " + msg);

        try {
            // Parse JSON message
            JsonNode jsonNode = objectMapper.readTree(msg);
            String requestId = jsonNode.get("requestId").asText();
            boolean violation = jsonNode.get("violation").asBoolean();
            System.out.println("Parsed inspectionId (correlation key): " + requestId);
            System.out.println("Parsed violation: " + violation);

            // Publish message to Zeebe BPMN
            zeebeClient.newPublishMessageCommand()
                .messageName("inspectionResponse")   // BPMN message name
                .correlationKey(requestId)         // BPMN correlation key
                .variables(Map.of("violation", violation))
                .send()
                .join();

            System.out.println("Message published to Zeebe with inspectionId: " + requestId);

        } catch (Exception e) {
            System.err.println("Failed to process message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
