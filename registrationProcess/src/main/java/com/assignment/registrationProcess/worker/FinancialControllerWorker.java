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
public class FinancialControllerWorker {

    private final List<String> actions = Arrays.asList("Approve", "Return");

    private final AtomicInteger counter = new AtomicInteger(0);

    @JobWorker(type = "financial-controller-decision", autoComplete = true)
    public void handleFinancialDecision(final JobClient client, final ActivatedJob job) {

        int index = counter.getAndIncrement() % actions.size();
        String fcDecision = actions.get(index);

        client.newCompleteCommand(job.getKey())
                .variables(Map.of("fcDecision", fcDecision))
                .send()
                .join();

        System.out.println("FinancialControllerWorker: Decision taken = " + fcDecision);
    }
}
