package com.assignment.registrationProcess.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AssignCommitteeWorker {

    private static String lastCommittee = "C2"; // round-robin starting point
    private static final Map<String, Integer> memberIndexes = new ConcurrentHashMap<>();

    @Value("${camunda.operate.identity.token-url}")
    private String serverUrl;

    @Value("${camunda.operate.identity.realm}")
    private String realm;

    @Value("${camunda.operate.identity.client-id}")
    private String clientId;

    @Value("${camunda.operate.identity.client-secret}")
    private String clientSecret;

    private final WebClient webClient = WebClient.create();

    private static final List<String> COMMITTEES = Arrays.asList("C1", "C2");
    private static final List<String> GROUPS = Arrays.asList("Finance", "Head", "Legal");

    @JobWorker(type = "assign-committee")
    public void handle(JobClient client, ActivatedJob job) {
        try {
            // 1️⃣ Select committee round-robin
            String selectedCommittee = "C1".equals(lastCommittee) ? "C2" : "C1";
            lastCommittee = selectedCommittee;
            System.out.println("Selected Committee: " + selectedCommittee);

            // 2️⃣ Generate fresh token using client credentials
            String token = getAccessToken();

            // 3️⃣ Pick one member from Finance, Head, Legal groups (round-robin)
            String financeMember = pickMemberFromGroup("Finance", token);
            String headMember = pickMemberFromGroup("Head", token);
            String legalMember = pickMemberFromGroup("Legal", token);

            // 4️⃣ Complete job with 4 variables
            client.newCompleteCommand(job.getKey())
                    .variables(Map.of(
                            "selectedCommittee", selectedCommittee,
                            "financeMember", financeMember,
                            "headMember", headMember,
                            "legalMember", legalMember
                    ))
                    .send()
                    .join();

            System.out.println("Finance Member: " + financeMember);
            System.out.println("Head Member: " + headMember);
            System.out.println("Legal Member: " + legalMember);

        } catch (Exception e) {
            e.printStackTrace();
            client.newFailCommand(job.getKey())
                    .retries(0)
                    .errorMessage("Failed to assign committee: " + e.getMessage())
                    .send()
                    .join();
        }
    }

    private String getAccessToken() {
        Mono<Map> response = webClient.post()
                .uri(serverUrl + "/realms/master/protocol/openid-connect/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(BodyInserters.fromFormData("grant_type", "password")
                        .with("client_id", "admin-cli")
                        .with("username", "admin")
                        .with("password", "admin"))
                .retrieve()
                .bodyToMono(Map.class);

        Map<String, Object> result = response.block();
        return (String) result.get("access_token");
    }


    private String pickMemberFromGroup(String groupName, String token) {
        String groupId = getGroupId(groupName, token);
        if (groupId == null) return null;

        List<String> members = getMembers(groupId, token);
        if (members.isEmpty()) return null;

        int idx = memberIndexes.getOrDefault(groupName, 0);
        String member = members.get(idx % members.size());
        memberIndexes.put(groupName, (idx + 1) % members.size());
        return member;
    }

    private String getGroupId(String groupName, String token) {
        List<Map<String, Object>> groups = getGroups(token);
        return groups.stream()
                .filter(g -> groupName.equals(g.get("name")))
                .findFirst()
                .map(g -> (String) g.get("id"))
                .orElse(null);
    }

    private List<Map<String, Object>> getGroups(String token) {
        String url = serverUrl + "/admin/realms/" + realm + "/groups";

        Mono<List<Map<String, Object>>> response = webClient.get()
                .uri(url)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

        return response.block();
    }

    private List<String> getMembers(String groupId, String token) {
        String url = serverUrl + "/admin/realms/" + realm + "/groups/" + groupId + "/members";

        Mono<List<Map<String, Object>>> response = webClient.get()
                .uri(url)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

        List<Map<String, Object>> memberList = response.block();
        return memberList.stream().map(m -> (String) m.get("username")).toList();
    }
}
