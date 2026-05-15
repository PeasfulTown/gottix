package xyz.peasfultown.gottix.ticket_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Ticket list views")
public class TicketListViewIntegrationTest extends BaseIntegrationTest {

    private static final String BASE     = "/api/v1/tickets";
    private static final String ME       = "/api/v1/tickets/me";
    private static final String ASSIGNED = "/api/v1/tickets/assigned";

    // ================================================================
    // GET /api/v1/tickets/me - customer's own tickets
    // ================================================================

    @Nested
    @DisplayName("GET /api/v1/tickets/me - customer's own tickets")
    class GetMyTickets {

        @BeforeEach
        void createTickets() throws Exception {
            // CUSTOMER creates 2 tickets
            mockMvc.perform(withCustomer(post(BASE))
                    .content(createTicketBody("My first issue", "Desc one", "LOW", CUSTOMER_ID)));
            mockMvc.perform(withCustomer(post(BASE))
                    .content(createTicketBody("My second issue", "Desc two", "HIGH", CUSTOMER_ID)));

            // CUSTOMER_2 creates 1 ticket - should NOT appear in CUSTOMER's /me
            mockMvc.perform(withCustomer2(post(BASE))
                    .content(createTicketBody("Other customer issue", "Desc three", "MEDIUM", CUSTOMER_ID_2)));
        }

        @Test
        @DisplayName("customer sees only own tickets - 200")
        void customer_seesOwnTicketsOnly() throws Exception {
            mockMvc.perform(withCustomer(get(ME)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[*].customerId",
                            everyItem(equalTo(CUSTOMER_ID))));
        }

        @Test
        @DisplayName("customer does not see other customers' tickets")
        void customer_doesNotSeeOtherCustomerTickets() throws Exception {
            mockMvc.perform(withCustomer(get(ME)))
                    .andExpect(jsonPath("$.content[*].customerId",
                            not(hasItem(CUSTOMER_ID_2))));
        }

        @Test
        @DisplayName("filter by status=OPENED - only open tickets returned")
        void filterByStatus_returnsOnlyMatchingTickets() throws Exception {
            mockMvc.perform(withCustomer(get(ME).param("status", "OPENED")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].status",
                            everyItem(equalTo("OPENED"))));
        }

        @Test
        @DisplayName("filter by priority=HIGH - only high priority tickets")
        void filterByPriority_returnsOnlyHighPriority() throws Exception {
            mockMvc.perform(withCustomer(get(ME).param("priority", "HIGH")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].priority",
                            everyItem(equalTo("HIGH"))));
        }

        @Test
        @DisplayName("combined filter status + priority")
        void filterByStatusAndPriority() throws Exception {
            mockMvc.perform(withCustomer(get(ME)
                            .param("status", "OPENED")
                            .param("priority", "HIGH")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].status",
                            everyItem(equalTo("OPENED"))))
                    .andExpect(jsonPath("$.content[*].priority",
                            everyItem(equalTo("HIGH"))));
        }

        @Test
        @DisplayName("response does not contain description field - summary only")
        void response_doesNotContainDescription() throws Exception {
            mockMvc.perform(withCustomer(get(ME)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].description").doesNotExist());
        }

        @Test
        @DisplayName("response does not contain comments array - summary only")
        void response_doesNotContainComments() throws Exception {
            mockMvc.perform(withCustomer(get(ME)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].comments").doesNotExist());
        }

        @Test
        @DisplayName("pagination - pageSize=1 returns one ticket per page")
        void pagination_respectsPageSize() throws Exception {
            mockMvc.perform(withCustomer(get(ME)
                            .param("pageNumber", "0")
                            .param("pageSize",   "1")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.page.size").value(1))
                    .andExpect(jsonPath("$.page.totalPages").value(greaterThanOrEqualTo(2)));
        }

        @Test
        @DisplayName("agent accessing /me - 403")
        void agent_accessMe_returns403() throws Exception {
            mockMvc.perform(withAgent(get(ME)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("admin accessing /me - 403")
        void admin_accessMe_returns403() throws Exception {
            mockMvc.perform(withAdmin(get(ME)))
                    .andExpect(status().isForbidden());
        }
    }

    // ================================================================
    // GET /api/v1/tickets/assigned - agent's assigned tickets
    // ================================================================

    @Nested
    @DisplayName("GET /api/v1/tickets/assigned - agent's assigned tickets")
    class GetAssignedTickets {

        @BeforeEach
        void createAndAssignTickets() throws Exception {
            // Create 2 tickets and assign to AGENT
            String t1 = extractId(mockMvc.perform(withCustomer(post(BASE))
                    .content(createTicketBody("Ticket assigned to agent", "Desc", "HIGH", CUSTOMER_ID))));
            String t2 = extractId(mockMvc.perform(withCustomer(post(BASE))
                    .content(createTicketBody("Another assigned ticket", "Desc", "LOW", CUSTOMER_ID))));

            mockMvc.perform(withAdmin(patch(BASE + "/{id}/assign", t1))
                    .content(toJson(Map.of("agentId", AGENT_ID))));
            mockMvc.perform(withAdmin(patch(BASE + "/{id}/assign", t2))
                    .content(toJson(Map.of("agentId", AGENT_ID))));

            // Create 1 ticket assigned to AGENT_2 - should NOT appear for AGENT
            String t3 = extractId(mockMvc.perform(withCustomer(post(BASE))
                    .content(createTicketBody("Agent 2 ticket", "Desc", "MEDIUM", CUSTOMER_ID))));
            mockMvc.perform(withAdmin(patch(BASE + "/{id}/assign", t3))
                    .content(toJson(Map.of("agentId", AGENT_ID_2))));
        }

        @Test
        @DisplayName("agent sees only tickets assigned to them - 200")
        void agent_seesOnlyAssignedTickets() throws Exception {
            mockMvc.perform(withAgent(get(ASSIGNED)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[*].assignedAgentId",
                            everyItem(equalTo(AGENT_ID))));
        }

        @Test
        @DisplayName("agent does not see tickets assigned to other agents")
        void agent_doesNotSeeOtherAgentTickets() throws Exception {
            mockMvc.perform(withAgent(get(ASSIGNED)))
                    .andExpect(jsonPath("$.content[*].assignedAgentId",
                            not(hasItem(AGENT_ID_2))));
        }

        @Test
        @DisplayName("filter by priority=HIGH on assigned tickets")
        void filterByPriority_onAssignedTickets() throws Exception {
            mockMvc.perform(withAgent(get(ASSIGNED).param("priority", "HIGH")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].priority",
                            everyItem(equalTo("HIGH"))));
        }

        @Test
        @DisplayName("filter by status=OPENED on assigned tickets")
        void filterByStatus_onAssignedTickets() throws Exception {
            mockMvc.perform(withAgent(get(ASSIGNED).param("status", "OPENED")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].status",
                            everyItem(equalTo("OPENED"))));
        }

        @Test
        @DisplayName("response does not include description or comments")
        void response_isSummaryOnly() throws Exception {
            mockMvc.perform(withAgent(get(ASSIGNED)))
                    .andExpect(jsonPath("$.content[0].description").doesNotExist())
                    .andExpect(jsonPath("$.content[0].comments").doesNotExist());
        }

        @Test
        @DisplayName("customer accessing /assigned - 403")
        void customer_accessAssigned_returns403() throws Exception {
            mockMvc.perform(withCustomer(get(ASSIGNED)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("sort by createdAt desc - most recent first")
        void sortByCreatedAtDesc() throws Exception {
            mockMvc.perform(withAgent(get(ASSIGNED)
                            .param("sortBy",    "CREATED_AT")
                            .param("sortOrder", "DESC")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }
}
