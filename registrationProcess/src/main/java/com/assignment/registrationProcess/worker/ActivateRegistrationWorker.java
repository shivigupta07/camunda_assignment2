package com.assignment.registrationProcess.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class ActivateRegistrationWorker {

    private final ZeebeClient zeebeClient;
    private final RestTemplate restTemplate;

    public ActivateRegistrationWorker(ZeebeClient zeebeClient) {
        this.zeebeClient = zeebeClient;
        this.restTemplate = new RestTemplate();
    }

    @PostConstruct
    public void subscribeWorker() {
        zeebeClient
                .newWorker()
                .jobType("activate-registration")   //BPMN service task job type
                .handler(new JobHandler() {
                    @Override
                    public void handle(JobClient client, ActivatedJob job) {
                        try {
                            Map<String, Object> variables = job.getVariablesAsMap();

                            String requestId = (String) variables.get("requestId");
                            String applicantName = (String) variables.get("applicantName");
                            String wage = (String) variables.get("wage");
                            String date = (String) variables.get("date");
                            String indicator = (String) variables.get("indicator");

                            // API request body
                            Map<String, Object> requestBody = Map.of(
                                    "requestId", requestId,
                                    "applicantName", applicantName,
                                    "wage", wage,
                                    "date", date,
                                    "indicator", indicator
                            );

                            // REST API call
                            String apiUrl = "http://localhost:8080/api/registration/activate";
                            HttpHeaders headers = new HttpHeaders();
                            headers.setContentType(MediaType.APPLICATION_JSON);

                            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

                            ResponseEntity<String> response =
                                    restTemplate.postForEntity(apiUrl, request, String.class);

                            
                            client.newCompleteCommand(job.getKey())
                                    .variables(Map.of("apiResponse", response.getBody(), "status", "success"))
                                    .send()
                                    .join();

                            System.out.println("Registration activated for: " + applicantName);

                        } catch (Exception e) {
                            client.newFailCommand(job.getKey())
                                    .retries(job.getRetries() - 1)
                                    .errorMessage(e.getMessage())
                                    .send()
                                    .join();

                            System.err.println("Error in ActivateRegistrationWorker: " + e.getMessage());
                        }
                    }
                })
                .name("activate-registration-worker")
                .open();
    }
}
