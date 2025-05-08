package dev.project.scholar_ai.supabase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import dev.project.scholar_ai.config.SupabaseConfig;
import dev.project.scholar_ai.dto.user.UserUpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class SupabaseAdminClient {
    private final Logger log = LoggerFactory.getLogger(SupabaseAdminClient.class);
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public SupabaseAdminClient(SupabaseConfig supabaseConfig) {
        String baseUrl = String.format("https://%s.supabase.co/auth/v1/admin", supabaseConfig.getProjectId());
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("apikey", supabaseConfig.getServiceRoleKey())
                .defaultHeader("Authorization", "Bearer " + supabaseConfig.getServiceRoleKey())
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public void createUser(String email, String password) {
        ObjectNode userData = objectMapper.createObjectNode();
        userData.set("email", TextNode.valueOf(email));
        userData.set("password", TextNode.valueOf(password));
        userData.set("email_confirm", BooleanNode.TRUE);
        userData.set("user_metadata", objectMapper.createObjectNode());
        userData.set("app_metadata", objectMapper.createObjectNode());

        userData.set("role", TextNode.valueOf("authenticated"));
        userData.set("aud", TextNode.valueOf("authenticated"));
        userData.set("is_super_admin", BooleanNode.FALSE);
        userData.set("is_sso_user", BooleanNode.FALSE);
        userData.set("is_anonymous", BooleanNode.FALSE);

        webClient
                .post()
                .uri("/users")
                .bodyValue(userData)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(error -> log.error("Error creating user: {}", error.getMessage(), error))
                .subscribe(response -> log.info("User created: {}", response));
    }

    public void updateUser(String userId, UserUpdateRequest updateRequest) {
        ObjectNode updateData = objectMapper.createObjectNode();

        if (updateRequest.getEmail() != null) {
            updateData.set("email", TextNode.valueOf(updateRequest.getEmail()));
        }
        if (updateRequest.getPassword() != null) {
            updateData.set("password", TextNode.valueOf(updateRequest.getPassword()));
        }
        if (updateRequest.getPhone() != null) {
            updateData.set("phone", TextNode.valueOf(updateRequest.getPhone()));
        }
        if (updateRequest.getUserMetadata() != null) {
            updateData.set("user_metadata", updateRequest.getUserMetadata());
        }

        webClient
                .put()
                .uri("/users/" + userId)
                .bodyValue(updateData)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(error -> log.error("Error updating user: {}", error.getMessage(), error))
                .subscribe(response -> log.info("User updated: {}", response));
    }
}
