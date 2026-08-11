
        package com.bookstore.controller;

import com.bookstore.client.InventoryClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryClient inventoryClient;


    @Test
    @DisplayName("Unauthenticated user cannot create loan")
    void whenNotAuthenticated_thenForbidden() throws Exception {

        mockMvc.perform(
                        post("/api/v1/loans")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "memberId": 1,
                                    "dueDate": "2026-12-31",
                                    "items": [
                                        {
                                            "bookId": 1,
                                            "copyId": 1
                                        }
                                    ]
                                }
                                """)
                )
                .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("Authenticated USER can create loan")
    void whenAuthenticatedAsUser_thenPassesSecurity() throws Exception {

        when(inventoryClient.checkAvailability(1L, 1L))
                .thenReturn(true);

        mockMvc.perform(
                        post("/api/v1/loans")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "memberId": 1,
                                    "dueDate": "2026-12-31",
                                    "items": [
                                        {
                                            "bookId": 1,
                                            "copyId": 1
                                        }
                                    ]
                                }
                                """)
                )
                .andExpect(status().is2xxSuccessful());
    }
}

