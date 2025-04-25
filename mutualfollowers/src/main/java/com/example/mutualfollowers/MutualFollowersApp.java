package com.example.mutualfollowers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@SpringBootApplication
public class MutualFollowersApp {
    public static void main(String[] args) {
        SpringApplication.run(MutualFollowersApp.class, args);
    }
}

@Component
class WebhookRunner implements CommandLineRunner {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void run(String... args) {
        String initUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook";
        Map<String, String> requestBody = Map.of(
                "name", "John Doe",
                "regNo", "REG12347",
                "email", "john@example.com"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<InitResponse> response = restTemplate.postForEntity(initUrl, entity, InitResponse.class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            InitResponse body = response.getBody();
            String webhook = body.getWebhook().trim();
            String token = body.getAccessToken().trim();
            List<User> users = body.getData().getUsers();

            List<List<Integer>> outcome = findMutualFollowers(users);

            Map<String, Object> finalPayload = new HashMap<>();
            finalPayload.put("regNo", "REG12347");
            finalPayload.put("outcome", outcome);

            HttpHeaders outHeaders = new HttpHeaders();
            outHeaders.setContentType(MediaType.APPLICATION_JSON);
            outHeaders.set("Authorization", token);
            HttpEntity<Map<String, Object>> outEntity = new HttpEntity<>(finalPayload, outHeaders);

            int retries = 0;
            while (retries < 4) {
                try {
                    ResponseEntity<String> webhookResponse = restTemplate.postForEntity(webhook, outEntity, String.class);
                    if (webhookResponse.getStatusCode().is2xxSuccessful()) {
                        System.out.println("Successfully posted outcome.");
                        break;
                    }
                } catch (Exception e) {
                    System.err.println("Post attempt failed, retrying..." + (retries + 1));
                }
                retries++;
            }
        }
    }

    private List<List<Integer>> findMutualFollowers(List<User> users) {
        Map<Integer, Set<Integer>> followMap = new HashMap<>();
        for (User user : users) {
            followMap.put(user.getId(), new HashSet<>(user.getFollows()));
        }

        Set<String> seenPairs = new HashSet<>();
        List<List<Integer>> mutuals = new ArrayList<>();

        for (User user : users) {
            int userId = user.getId();
            for (Integer followedId : user.getFollows()) {
                if (followMap.containsKey(followedId) && followMap.get(followedId).contains(userId)) {
                    int min = Math.min(userId, followedId);
                    int max = Math.max(userId, followedId);
                    String pairKey = min + ":" + max;
                    if (!seenPairs.contains(pairKey)) {
                        mutuals.add(List.of(min, max));
                        seenPairs.add(pairKey);
                    }
                }
            }
        }
        return mutuals;
    }
}

@Data
class InitResponse {
    private String webhook;

    @JsonProperty("accessToken")
    private String accessToken;

    private InitData data;
}

@Data
class InitData {
    @JsonProperty("users")
    private List<User> users;
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class User {
    private int id;
    private String name;

    @JsonProperty("follows ")
    private List<Integer> follows;
}
