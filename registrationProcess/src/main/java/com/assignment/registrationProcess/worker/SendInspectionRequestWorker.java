package com.assignment.registrationProcess.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobWorker;
import io.camunda.zeebe.client.ZeebeClient;
import jakarta.annotation.PostConstruct;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;


import java.util.HashMap;
import java.util.Map;

@Component
public class SendInspectionRequestWorker {

    private final JmsTemplate jmsTemplate;
    private final ZeebeClient zeebeClient;
    private final ObjectMapper objectMapper;  // Jackson ObjectMapper for JSON
    @Value("${app.jms.request-queue}")
    private String requestQueue;
    

    public SendInspectionRequestWorker(JmsTemplate jmsTemplate, ZeebeClient zeebeClient) {
        this.jmsTemplate = jmsTemplate;
        this.zeebeClient = zeebeClient;
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void registerWorker() {
        JobWorker worker = zeebeClient
                .newWorker()
                .jobType("send-inspection-request") // BPMN service task type
                .handler(this::handleJob)
                .open();

        System.out.println("Worker registered for send-inspection-request");
    }

    private void handleJob(JobClient client, ActivatedJob job) {
        try {
            Map<String, Object> vars = job.getVariablesAsMap();

            // Extract variables from Zeebe job
            String requestId = (String) vars.get("requestId");  // make sure workflow variable is "requestId"
            String customerEmail = (String) vars.get("customerEmail");

            if (requestId == null) requestId = "UNKNOWN";
            if (customerEmail == null) customerEmail = "UNKNOWN";

            // Create JSON payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("requestId", requestId);
            payload.put("customerEmail", customerEmail);

            String jsonMessage = objectMapper.writeValueAsString(payload);

            // Send JSON message to JMS queue
            jmsTemplate.convertAndSend(requestQueue, jsonMessage);

            System.out.println("JMS Message sent: " + jsonMessage);

            // Complete the Zeebe job
            client.newCompleteCommand(job.getKey()).send().join();

        } catch (Exception e) {
            e.printStackTrace();
            client.newFailCommand(job.getKey())
                    .retries(job.getRetries() - 1)
                    .errorMessage(e.getMessage())
                    .send()
                    .join();
        }
    }
}
