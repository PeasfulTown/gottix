package xyz.peasfultown.gottix.ticket_service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Ticket CRUD")
class TicketCrudIntegrationTest extends BaseIntegrationTest {
    private static final String BASE_URL = "/api/v1/tickets";

    @Nested
    @DisplayName("POST /api/v1/tickets - create ticket")
    class CreateTicket {

        @Test
        @DisplayName("customer creates ticket - 201 with full ticket response")
        void customer_createTicket_returns201() throws Exception {
            mockMvc.perform(withCustomer(post(BASE_URL))
                            .content(defaultTicketBody()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.title").value("Cannot login to my account"))
                    .andExpect(jsonPath("$.description").value(containsString("invalid credentials")))
                    .andExpect(jsonPath("$.status").value("OPEN"))
                    .andExpect(jsonPath("$.priority").value("MEDIUM"))
                    .andExpect(jsonPath("$.customerId").value(CUSTOMER_ID))
                    .andExpect(jsonPath("$.comments").isArray())
                    .andExpect(jsonPath("$.comments", hasSize(0)))
                    .andExpect(jsonPath("$.createdAt").isNotEmpty())
                    .andExpect(jsonPath("$.updatedAt").isNotEmpty());
        }

        @Test
        @DisplayName("agent creates ticket - 201")
        void agent_createTicket_returns201() throws Exception {
            mockMvc.perform(withAgent(post(BASE_URL))
                            .content(defaultTicketBody()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNotEmpty());
        }

        @Test
        @DisplayName("admin creates ticket - 201")
        void admin_createTicket_returns201() throws Exception {
            mockMvc.perform(withAdmin(post(BASE_URL))
                            .content(defaultTicketBody()))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("create with HIGH priority - priority stored correctly")
        void createTicket_highPriority_storedCorrectly() throws Exception {
            mockMvc.perform(withCustomer(post(BASE_URL))
                            .content(createTicketBody("Urgent issue", "Production is down", "HIGH", CUSTOMER_ID)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.priority").value("HIGH"));
        }

        @Test
        @DisplayName("create with missing title - 400")
        void createTicket_missingTitle_returns400() throws Exception {
            String body = toJson(Map.of("description", "Some description", "priority", "LOW"));
            mockMvc.perform(withCustomer(post(BASE_URL)).content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("create with missing description - 400")
        void createTicket_missingDescription_returns400() throws Exception {
            String body = toJson(Map.of("title", "Some title", "priority", "LOW"));
            mockMvc.perform(withCustomer(post(BASE_URL)).content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("create with blank title - 400")
        void createTicket_blankTitle_returns400() throws Exception {
            String body = toJson(Map.of("title", "  ", "description", "desc", "priority", "LOW"));
            mockMvc.perform(withCustomer(post(BASE_URL)).content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("create without auth headers - 401 or 403")
        void createTicket_noAuthHeaders_returnsUnauthorized() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                            .content(defaultTicketBody()))
                    .andExpect(status().is(anyOf(is(401), is(403))));
        }

    }
}
