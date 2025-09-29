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

    
    private static int committeeIndex = 0;
    private static final Map<String, Integer> memberIndexes = new ConcurrentHashMap<>();

    @Value("${camunda.identity.token-url}")
    private String serverUrl;

    @Value("${camunda.identity.realm}")
    private String realm;

    private final WebClient webClient = WebClient.create();

    private static final List<String> COMMITTEES = Arrays.asList("C1", "C2");

    @JobWorker(type = "assign-committee")
    public void handle(JobClient client, ActivatedJob job) {
        try {
            String selectedCommittee = COMMITTEES.get(committeeIndex % COMMITTEES.size());
            committeeIndex++;
            System.out.println("Selected Committee: " + selectedCommittee);

            String token = getAccessToken();

            String committeeGroupId = getGroupId(selectedCommittee, token);
            if (committeeGroupId == null) {
                throw new RuntimeException("Committee group not found: " + selectedCommittee);
            }

            List<Map<String, Object>> childrenGroups = getChildrenGroups(committeeGroupId, token);

            Map<String, String> selectedMembers = new HashMap<>();
            for (Map<String, Object> child : childrenGroups) {
                String groupName = (String) child.get("name");
                String groupId = (String) child.get("id");

                String member = pickMemberFromGroup(groupName, groupId, token);
                if (member == null) {
                    member = "NO_MEMBER_FOUND";
                }

                selectedMembers.put(groupName, member);
                System.out.println(groupName + " Member: " + member);
            }

            Map<String, Object> vars = new HashMap<>();
            vars.put("selectedCommittee", selectedCommittee);
            vars.putAll(selectedMembers);

            client.newCompleteCommand(job.getKey())
                    .variables(vars)
                    .send()
                    .join();

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

    private List<Map<String, Object>> getChildrenGroups(String parentGroupId, String token) {
        String url = serverUrl + "/admin/realms/" + realm + "/groups/" + parentGroupId + "/children";

        Mono<List<Map<String, Object>>> response = webClient.get()
                .uri(url)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

        return response.block();
    }

    private String pickMemberFromGroup(String groupName, String groupId, String token) {
        List<String> members = getMembers(groupId, token);

        if (members.isEmpty()) return null;

        int lastIndex = memberIndexes.getOrDefault(groupId, -1);

        int nextIndex = (lastIndex + 1) % members.size();

        String selectedMember = members.get(nextIndex);

        memberIndexes.put(groupId, nextIndex);

        System.out.println("Group: " + groupName + " | Members: " + members + " | Selected: " + selectedMember + " | Next Index: " + nextIndex);

        return selectedMember;
    }


    private List<String> getMembers(String groupId, String token) {
        String url = serverUrl + "/admin/realms/" + realm + "/groups/" + groupId + "/members";

        Mono<List<Map<String, Object>>> response = webClient.get()
                .uri(url)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

        List<Map<String, Object>> memberList = response.block();
        if (memberList == null) return Collections.emptyList();

        return memberList.stream().map(m -> (String) m.get("username")).toList();
    }
}
