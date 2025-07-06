package dev.project.scholar_ai.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:testdb",
            "spring.datasource.driverClassName=org.h2.Driver",
            "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
            "spring.jpa.hibernate.ddl-auto=update"
        })
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void whenAccessPublicEndpoint_thenIsOk() throws Exception {
        mockMvc.perform(get("/api/test/test")).andExpect(status().isOk());
    }

    @Test
    void whenAccessSwaggerEndpoint_thenIsOk() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }

    @Test
    void whenAccessSecuredEndpointWithoutUser_thenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/todo/summary")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void whenAccessSecuredEndpointWithUser_thenIsOk() throws Exception {
        mockMvc.perform(get("/api/v1/todo/summary")).andExpect(status().isOk());
    }
}
