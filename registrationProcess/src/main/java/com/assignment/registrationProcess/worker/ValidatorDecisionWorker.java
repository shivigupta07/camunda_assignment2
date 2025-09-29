package com.assignment.registrationProcess.worker;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ValidatorDecisionWorker {

    private final List<String> actions = Arrays.asList(
            "Approve",
            "Raise Inspection",
            "Reject",
            "Request Clarification"
    );

    private final AtomicInteger counter = new AtomicInteger(0);

    @JobWorker(type = "validator-decision", autoComplete = true)
    public void handleValidatorDecision(final JobClient client, final ActivatedJob job) {

        int index = counter.getAndIncrement() % actions.size();
        String validatorDecision = actions.get(index);

        client.newCompleteCommand(job.getKey())
                .variables(Map.of("validatorDecision", validatorDecision))
                .send()
                .join();

        System.out.println("ValidatorDecisionWorker: Decision taken = " + validatorDecision);
    }
}
