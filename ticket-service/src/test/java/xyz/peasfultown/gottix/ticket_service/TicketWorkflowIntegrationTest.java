package xyz.peasfultown.gottix.ticket_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Ticket workflow")
public class TicketWorkflowIntegrationTest extends BaseIntegrationTest {

    private static final String BASE_URL = "/api/v1/tickets";

    private String ticketId;

    @BeforeEach
    void createTicket() throws Exception {
        ticketId = extractId(mockMvc.perform(
                withCustomer(post(BASE_URL)).content(defaultTicketBody())));
    }

    // ================================================================
    // PATCH /api/v1/tickets/{ticketId}/status
    // ================================================================

    @Nested
    @DisplayName("PATCH /{ticketId}/status - update status")
    class UpdateStatus {

        @Test
        @DisplayName("agent transitions OPEN -> IN_PROGRESS - 204")
        void agent_openToInProgress_returns204() throws Exception {
            mockMvc.perform(withAgent(patch(BASE_URL + "/{id}/status", ticketId))
                            .content(toJson(Map.of("status", "IN_PROGRESS"))))
                    .andExpect(status().isNoContent());

            mockMvc.perform(withAgent(get(BASE_URL + "/{id}", ticketId)))
                    .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        }

        @Test
        @DisplayName("agent transitions IN_PROGRESS -> RESOLVED - 204")
        void agent_inProgressToResolved_returns204() throws Exception {
            // First move to IN_PROGRESS
            mockMvc.perform(withAgent(patch(BASE_URL + "/{id}/status", ticketId))
                    .content(toJson(Map.of("status", "IN_PROGRESS"))));

            mockMvc.perform(withAgent(patch(BASE_URL + "/{id}/status", ticketId))
                            .content(toJson(Map.of("status", "RESOLVED"))))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("admin transitions RESOLVED -> CLOSED - 204")
        void admin_resolvedToClosed_returns204() throws Exception {
            mockMvc.perform(withAgent(patch(BASE_URL + "/{id}/status", ticketId))
                    .content(toJson(Map.of("status", "IN_PROGRESS"))));
            mockMvc.perform(withAgent(patch(BASE_URL + "/{id}/status", ticketId))
                    .content(toJson(Map.of("status", "RESOLVED"))));

            mockMvc.perform(withAdmin(patch(BASE_URL + "/{id}/status", ticketId))
                            .content(toJson(Map.of("status", "CLOSED"))))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("customer updates status - 403")
        void customer_updateStatus_returns403() throws Exception {
            mockMvc.perform(withCustomer(patch(BASE_URL + "/{id}/status", ticketId))
                            .content(toJson(Map.of("status", "IN_PROGRESS"))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("update status with invalid value - 400")
        void updateStatus_invalidValue_returns400() throws Exception {
            mockMvc.perform(withAgent(patch(BASE_URL + "/{id}/status", ticketId))
                            .content(toJson(Map.of("status", "FLYING"))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("update status on non-existent ticket - 404")
        void updateStatus_notFound_returns404() throws Exception {
            mockMvc.perform(withAgent(patch(BASE_URL + "/{id}/status", "00000000-0000-0000-0000-00000000000f"))
                            .content(toJson(Map.of("status", "IN_PROGRESS"))))
                    .andExpect(status().isNotFound());
        }
    }

    // ================================================================
    // PATCH /api/v1/tickets/{ticketId}/assign
    // ================================================================

    @Nested
    @DisplayName("PATCH /{ticketId}/assign - assign to agent")
    class AssignTicket {

        @Test
        @DisplayName("admin assigns ticket to agent - 204")
        void admin_assignTicket_returns204() throws Exception {
            mockMvc.perform(withAdmin(patch(BASE_URL + "/{id}/assign", ticketId))
                            .content(toJson(Map.of("agentId", AGENT_ID))))
                    .andExpect(status().isNoContent());

            mockMvc.perform(withAdmin(get(BASE_URL + "/{id}", ticketId)))
                    .andExpect(jsonPath("$.assignedAgentId").value(AGENT_ID));
        }

        @Test
        @DisplayName("agent self-assigns ticket - 204")
        void agent_selfAssign_returns204() throws Exception {
            mockMvc.perform(withAgent(patch(BASE_URL + "/{id}/assign", ticketId))
                            .content(toJson(Map.of("agentId", AGENT_ID))))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("agent reassigns to another agent - 204")
        void agent_reassignToAnotherAgent_returns204() throws Exception {
            mockMvc.perform(withAgent(patch(BASE_URL + "/{id}/assign", ticketId))
                    .content(toJson(Map.of("agentId", AGENT_ID))));

            mockMvc.perform(withAgent(patch(BASE_URL + "/{id}/assign", ticketId))
                            .content(toJson(Map.of("agentId", AGENT_ID_2))))
                    .andExpect(status().isNoContent());

            mockMvc.perform(withAdmin(get(BASE_URL + "/{id}", ticketId)))
                    .andExpect(jsonPath("$.assignedAgentId").value(AGENT_ID_2));
        }

        @Test
        @DisplayName("unassign ticket by passing null agentId - 204")
        void admin_unassignTicket_returns204() throws Exception {
            mockMvc.perform(withAdmin(patch(BASE_URL + "/{id}/assign", ticketId))
                    .content(toJson(Map.of("agentId", AGENT_ID))));

            // Unassign
            mockMvc.perform(withAdmin(patch(BASE_URL + "/{id}/assign", ticketId))
                            .content("{\"agentId\": null}"))
                    .andExpect(status().isNoContent());

            mockMvc.perform(withAdmin(get(BASE_URL + "/{id}", ticketId)))
                    .andExpect(jsonPath("$.assignedAgentId").isEmpty());
        }

        @Test
        @DisplayName("customer assigns ticket - 403")
        void customer_assignTicket_returns403() throws Exception {
            mockMvc.perform(withCustomer(patch(BASE_URL + "/{id}/assign", ticketId))
                            .content(toJson(Map.of("agentId", AGENT_ID))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("assign non-existent ticket - 404")
        void assignTicket_notFound_returns404() throws Exception {
            mockMvc.perform(withAdmin(patch(BASE_URL + "/{id}/assign", "00000000-0000-0000-0000-00000000000f"))
                            .content(toJson(Map.of("agentId", AGENT_ID))))
                    .andExpect(status().isNotFound());
        }
    }

    // ================================================================
    // PATCH /api/v1/tickets/{ticketId}/priority
    // ================================================================

    @Nested
    @DisplayName("PATCH /{ticketId}/priority - update priority")
    class UpdatePriority {

        @Test
        @DisplayName("agent updates priority to HIGH - 204")
        void agent_updatePriorityHigh_returns204() throws Exception {
            mockMvc.perform(withAgent(patch(BASE_URL + "/{id}/priority", ticketId))
                            .content(toJson(Map.of("priority", "HIGH"))))
                    .andExpect(status().isNoContent());

            mockMvc.perform(withAgent(get(BASE_URL + "/{id}", ticketId)))
                    .andExpect(jsonPath("$.priority").value("HIGH"));
        }

        @Test
        @DisplayName("customer updates own ticket priority - 204")
        void customer_updateOwnTicketPriority_returns204() throws Exception {
            mockMvc.perform(withCustomer(patch(BASE_URL + "/{id}/priority", ticketId))
                            .content(toJson(Map.of("priority", "HIGH"))))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("customer updates another customer's ticket priority - 403")
        void customer_updateOtherPriority_returns403() throws Exception {
            mockMvc.perform(withCustomer2(patch(BASE_URL + "/{id}/priority", ticketId))
                            .content(toJson(Map.of("priority", "HIGH"))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("update with invalid priority value - 400")
        void updatePriority_invalidValue_returns400() throws Exception {
            mockMvc.perform(withAgent(patch(BASE_URL + "/{id}/priority", ticketId))
                            .content(toJson(Map.of("priority", "ULTRA"))))
                    .andExpect(status().isBadRequest());
        }
    }

    // ================================================================
    // POST /api/v1/tickets/{ticketId}/reopen
    // ================================================================

    @Nested
    @DisplayName("POST /{ticketId}/reopen - reopen closed ticket")
    class ReopenTicket {

        private String closedTicketId;

        @BeforeEach
        void closeTicket() throws Exception {
            closedTicketId = extractId(mockMvc.perform(
                    withCustomer(post(BASE_URL)).content(defaultTicketBody())));

            // Walk through to CLOSED
            mockMvc.perform(withAgent(patch(BASE_URL + "/{id}/status", closedTicketId))
                    .content(toJson(Map.of("status", "IN_PROGRESS"))));
            mockMvc.perform(withAgent(patch(BASE_URL + "/{id}/status", closedTicketId))
                    .content(toJson(Map.of("status", "RESOLVED"))));
            mockMvc.perform(withAdmin(patch(BASE_URL + "/{id}/status", closedTicketId))
                    .content(toJson(Map.of("status", "CLOSED"))));
        }

        @Test
        @DisplayName("customer reopens own closed ticket - 204 and status is OPEN")
        void customer_reopenOwnTicket_returns204() throws Exception {
            mockMvc.perform(withCustomer(post(BASE_URL + "/{id}/reopen", closedTicketId))
                            .content(toJson(Map.of("reason", "Issue is still happening"))))
                    .andExpect(status().isNoContent());

            mockMvc.perform(withAgent(get(BASE_URL + "/{id}", closedTicketId)))
                    .andExpect(jsonPath("$.status").value("OPENED"));
        }

        @Test
        @DisplayName("agent reopens ticket without reason - 204")
        void agent_reopenWithoutReason_returns204() throws Exception {
            mockMvc.perform(withAgent(post(BASE_URL + "/{id}/reopen", closedTicketId))
                            .content("{}"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("reopen an already open ticket - 400")
        void reopenOpenTicket_returns422() throws Exception {
            mockMvc.perform(withCustomer(post(BASE_URL + "/{id}/reopen", ticketId))
                            .content(toJson(Map.of("reason", "Reopening open ticket"))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("customer reopens another customer's ticket - 403")
        void customer_reopenOtherTicket_returns403() throws Exception {
            mockMvc.perform(withCustomer2(post(BASE_URL + "/{id}/reopen", closedTicketId))
                            .content(toJson(Map.of("reason", "Should not work"))))
                    .andExpect(status().isForbidden());
        }
    }
}
