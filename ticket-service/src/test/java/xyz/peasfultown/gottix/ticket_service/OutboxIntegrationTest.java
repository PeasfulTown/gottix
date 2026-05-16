package xyz.peasfultown.gottix.ticket_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import xyz.peasfultown.gottix.ticket_service.entity.OutboxEntity;
import xyz.peasfultown.gottix.ticket_service.repository.OutboxRepository;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@DisplayName("Outbox")
@Transactional
public class OutboxIntegrationTest extends BaseIntegrationTest {

    private static final String TICKETS_BASE_URL = "/api/v1/tickets";
    private static final String TICKETS_ID_PATH = "/api/v1/tickets/{ticketId}";
    private static final String TICKETS_COMMENT_PATH = "/api/v1/tickets/{ticketId}/comments";

    @Autowired
    private OutboxRepository outboxRepo;

    @Nested
    @DisplayName("Ticket CRUD")
    class AddTicket {

        @Test
        @DisplayName("customer add ticket - 201 saves to outbox")
        void customer_createTicket_onSuccess_savesToOutbox() throws Exception {
            mockMvc.perform(withCustomer(post(TICKETS_BASE_URL))
                    .content(defaultTicketBody()));
            List<OutboxEntity> oes = outboxRepo.findAll();
            assertThat(oes).hasSize(1);
        }

        @Test
        @DisplayName("customer update ticket - 200 saves to outbox")
        void customer_updateTicket_onSuccess_savesToOutbox() throws Exception {
            String id = extractId(mockMvc.perform(withCustomer(post(TICKETS_BASE_URL))
                    .content(defaultTicketBody())));
            mockMvc.perform(withCustomer(patch(TICKETS_ID_PATH, id))
                    .content(toJson(Map.of("title", "A new ticket title"))));
            assertThat(outboxRepo.findAll()).hasSize(2);
        }

        @Test
        @DisplayName("customer delete ticket - 204 saves to outbox")
        void customer_deleteTicket_onSuccess_savesToOutbox() throws Exception {
            String id = extractId(mockMvc.perform(withCustomer(post(TICKETS_BASE_URL))
                    .content(defaultTicketBody())));
            mockMvc.perform(withCustomer(delete(TICKETS_ID_PATH, id)));
            assertThat(outboxRepo.findAll()).hasSize(2);
        }

        @Test
        @DisplayName("admin creates ticket - 201 saves to outbox")
        void admin_createTicket_onSuccess_savesToOutbox() throws Exception {
            mockMvc.perform(withAdmin(post(TICKETS_BASE_URL))
                    .content(defaultTicketBody()));
            assertThat(outboxRepo.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("admin updates ticket - 200 saves to outbox")
        void admin_updateTicket_onSuccess_savesToOutbox() throws Exception {
            String id = extractId(mockMvc.perform(withAdmin(post(TICKETS_BASE_URL))
                    .content(defaultTicketBody())));
            mockMvc.perform(withAdmin(patch(TICKETS_ID_PATH, id)
                    .content(toJson(Map.of("title", "New ticket title")))));
            assertThat(outboxRepo.findAll()).hasSize(2);
        }

        @Test
        @DisplayName("admin creates ticket - 201 saves to outbox")
        void admin_deleteTicket_onSuccess_savesToOutbox() throws Exception {
            String id = extractId(mockMvc.perform(withAdmin(post(TICKETS_BASE_URL))
                    .content(defaultTicketBody())));
            mockMvc.perform(withAdmin(delete(TICKETS_ID_PATH, id)
                    .content(toJson(Map.of("title", "New ticket title")))));
            assertThat(outboxRepo.findAll()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Ticket workflow")
    class TicketWorkflow {
        private String ticketId;
        private String ticketId2;

        @BeforeEach
        void setup() throws Exception {
            ticketId = extractId(mockMvc.perform(withCustomer(post(TICKETS_BASE_URL)
                    .content(defaultTicketBody()))));

            ticketId2 = extractId(mockMvc.perform(withCustomer2(post(TICKETS_BASE_URL))
                    .content(createTicketBody("Customer 2 ticket",
                            "Customer 2 ticket description",
                            "LOW",
                            CUSTOMER_ID_2))));
            outboxRepo.deleteAll(); // clear outbox
        }

        // ===========================================================
        // AGENT
        // ===========================================================

        @Test
        @DisplayName("agent assigns ticket - 204 saves to outbox")
        void agent_assignsTicket_onSuccess_savesToOutbox() throws Exception {
            mockMvc.perform(withAgent(patch(TICKETS_ID_PATH + "/assign", ticketId)
                    .content(toJson(Map.of("agentId", AGENT_ID)))));
            assertThat(outboxRepo.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("agent updates ticket status - 204 saves to outbox")
        void agent_updatesTicketStatus_onSuccess_savesToOutbox() throws Exception {
            mockMvc.perform(withAgent(patch(TICKETS_ID_PATH + "/status", ticketId)
                    .content(toJson(Map.of("status", "IN_PROGRESS")))));
            assertThat(outboxRepo.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("agent updates ticket status - 400 don't save to outbox")
        void agent_updatesTicketStatus_onFailure_notSaveToOutbox() throws Exception {
            mockMvc.perform(withAgent(patch(TICKETS_ID_PATH + "/status", ticketId)
                    .content(toJson(Map.of("status", "INVALID_STATUS")))));
            assertThat(outboxRepo.findAll()).hasSize(0);
        }

        @Test
        @DisplayName("agent updates ticket priority - 204 saves to outbox")
        void agent_updatesTicketPriority_onSuccess_savesToOutbox() throws Exception {
            mockMvc.perform(withAgent(patch(TICKETS_ID_PATH + "/priority", ticketId)
                    .content(toJson(Map.of("priority", "HIGH")))));
            assertThat(outboxRepo.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("agent updates ticket priority - 400 don't save to outbox")
        void agent_updatesTicketPriority_onFailure_savesToOutbox() throws Exception {
            mockMvc.perform(withAgent(patch(TICKETS_ID_PATH + "/priority", ticketId)
                    .content(toJson(Map.of("priority", "INVALID_PRIORITY")))));
            assertThat(outboxRepo.findAll()).hasSize(0);
        }

        @Test
        @DisplayName("agent reopens ticket - 204 saves to outbox")
        void agent_reopenTicket_onSuccess_savesToOutbox() throws Exception {
            mockMvc.perform(withAdmin(patch(TICKETS_ID_PATH + "/status", ticketId)
                    .content(toJson(Map.of("status", "CLOSED")))));
            mockMvc.perform(withAgent(post(TICKETS_ID_PATH + "/reopen", ticketId)
                    .content(toJson(Map.of("reason", "ticket reopen reason")))));

            assertThat(outboxRepo.findAll()).hasSize(2);
        }

        @Test
        @DisplayName("agent reopens ticket - 400 don't save to outbox")
        void agent_reopenTicket_onFailure_notSaveToOutbox() throws Exception {
            mockMvc.perform(withAgent(patch(TICKETS_ID_PATH + "/reopen", ticketId)
                    .content(toJson(Map.of("reason", "ticket reopen reason")))));

            assertThat(outboxRepo.findAll()).hasSize(0);
        }

        // ============================================================
        // ADMIN
        // ============================================================

        @Test
        @DisplayName("admin assigns ticket - 204 saves to outbox")
        void admin_assignsTicket_onSuccess_savesToOutbox() throws Exception {
            mockMvc.perform(withAdmin(patch(TICKETS_ID_PATH + "/assign", ticketId)
                    .content(toJson(Map.of("adminId", ADMIN_ID)))));
            assertThat(outboxRepo.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("admin updates ticket status - 204 saves to outbox")
        void admin_updatesTicketStatus_onSuccess_savesToOutbox() throws Exception {
            mockMvc.perform(withAdmin(patch(TICKETS_ID_PATH + "/status", ticketId)
                    .content(toJson(Map.of("status", "IN_PROGRESS")))));
            assertThat(outboxRepo.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("admin updates ticket status - 400 don't save to outbox")
        void admin_updatesTicketStatus_onFailure_notSaveToOutbox() throws Exception {
            mockMvc.perform(withAdmin(patch(TICKETS_ID_PATH + "/status", ticketId)
                    .content(toJson(Map.of("status", "INVALID_STATUS")))));
            assertThat(outboxRepo.findAll()).hasSize(0);
        }

        @Test
        @DisplayName("admin updates ticket priority - 204 saves to outbox")
        void admin_updatesTicketPriority_onSuccess_savesToOutbox() throws Exception {
            mockMvc.perform(withAdmin(patch(TICKETS_ID_PATH + "/priority", ticketId)
                    .content(toJson(Map.of("priority", "HIGH")))));
            assertThat(outboxRepo.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("admin updates ticket priority - 400 don't save to outbox")
        void admin_updatesTicketPriority_onFailure_savesToOutbox() throws Exception {
            mockMvc.perform(withAdmin(patch(TICKETS_ID_PATH + "/priority", ticketId)
                    .content(toJson(Map.of("priority", "INVALID_PRIORITY")))));
            assertThat(outboxRepo.findAll()).hasSize(0);
        }

        @Test
        @DisplayName("admin reopens ticket - 204 saves to outbox")
        void admin_reopenTicket_onSuccess_savesToOutbox() throws Exception {
            mockMvc.perform(withAdmin(patch(TICKETS_ID_PATH + "/status", ticketId)
                    .content(toJson(Map.of("status", "CLOSED")))));
            mockMvc.perform(withAdmin(post(TICKETS_ID_PATH + "/reopen", ticketId)));

            assertThat(outboxRepo.findAll()).hasSize(2);
        }

        @Test
        @DisplayName("admin reopens ticket - 404 don't save to outbox")
        void admin_reopenTicket_onFailure_notSaveToOutbox() throws Exception {
            mockMvc.perform(withAdmin(patch(TICKETS_ID_PATH + "/reopen", ticketId)));

            assertThat(outboxRepo.findAll()).hasSize(0);
        }

        // ============================================================
        // COMMENTS ADD
        // ============================================================

        @Test
        @DisplayName("customer adds comment on own post - 201 saves to outbox")
        void customer_createsComment_onSuccess_savesToOutbox() throws Exception {
            mockMvc.perform(withCustomer(post(TICKETS_COMMENT_PATH, ticketId))
                    .content(toJson(Map.of("body", "test comment from customer"))));

            assertThat(outboxRepo.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("customer adds comment on other customer's ticket - 403 don't save to outbox")
        void customer_createsComment_onFailure_noSaveToOutbox() throws Exception {
            mockMvc.perform(withCustomer(post(TICKETS_COMMENT_PATH, ticketId2))
                    .content(toJson(Map.of("body", "test comment from customer"))));

            assertThat(outboxRepo.findAll()).hasSize(0);
        }

        @Test
        @DisplayName("agent adds comment to ticket - 201 saves to outbox")
        void agent_createsComment_onSuccess_savesToOutbox() throws Exception {
            mockMvc.perform(withAgent(post(TICKETS_COMMENT_PATH, ticketId))
                    .content(toJson(Map.of("body", "test comment from agent"))));

            assertThat(outboxRepo.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("admin adds comment to ticket - 201 saves to outbox")
        void admin_createsComment_onSuccess_savesToOutbox() throws Exception {
            mockMvc.perform(withAdmin(post(TICKETS_COMMENT_PATH, ticketId))
                    .content(toJson(Map.of("body", "test comment from admin"))));

            assertThat(outboxRepo.findAll()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Comments")
    class Comments {
        private static final String TICKETS_COMMENT_ID_PATH = TICKETS_COMMENT_PATH + "/{commentId}";

        String ticketId;
        String ticketId2;
        String commentId;
        String commentId2;
        String agentCommentId;
        String adminCommentId;

        @BeforeEach
        void setup() throws Exception {
            ticketId = extractId(mockMvc.perform(withCustomer(post(TICKETS_BASE_URL))
                    .content(defaultTicketBody())));
            ticketId2 = extractId(mockMvc.perform(withCustomer2(post(TICKETS_BASE_URL))
                    .content(createTicketBody("customer 2 ticket", "ticket 2 description", "MEDIUM", CUSTOMER_ID_2))));

            commentId = extractId(mockMvc.perform(withCustomer(post(TICKETS_COMMENT_PATH, ticketId))
                    .content(toJson(Map.of("body", "comment from customer")))));
            commentId2 = extractId(mockMvc.perform(withCustomer2(post(TICKETS_COMMENT_PATH, ticketId2))
                    .content(toJson(Map.of("body", "comment from customer 2")))));

            agentCommentId = extractId(mockMvc.perform(withAgent(post(TICKETS_COMMENT_PATH, ticketId)
                    .content(toJson(Map.of("body", "comment from agent"))))));
            adminCommentId = extractId(mockMvc.perform(withAdmin(post(TICKETS_COMMENT_PATH, ticketId))
                    .content(toJson(Map.of("body", "comment from admin")))));
            outboxRepo.deleteAll();
        }

        @Test
        @DisplayName("customer edits own comment - 204 saves to outbox")
        void customer_editComment_onSuccess_savesToOutbox() throws Exception {
            mockMvc.perform(withCustomer(patch(TICKETS_COMMENT_ID_PATH, ticketId, commentId))
                    .content(toJson(Map.of("body", "new comment body from customer"))));

            assertThat(outboxRepo.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("customer deletes own comment - 204 saves to outbox")
        void customer_deleteComment_onSuccess_savesToOutbox() throws Exception {
            mockMvc.perform(withCustomer(delete(TICKETS_COMMENT_ID_PATH, ticketId, commentId)));

            assertThat(outboxRepo.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("customer edits other customer's comment - 403 don't save to outbox")
        void customer_editComment_onFailure_noSaveToOutbox() throws Exception {
            mockMvc.perform(withCustomer(patch(TICKETS_COMMENT_ID_PATH, ticketId2, commentId2))
                    .content(toJson(Map.of("body", "new comment body from customer"))));

            assertThat(outboxRepo.findAll()).hasSize(0);
        }

        @Test
        @DisplayName("customer deletes other customer's comment - 403 don't save to outbox")
        void customer_deleteComment_onFailure_noSaveToOutbox() throws Exception {
            mockMvc.perform(withCustomer(delete(TICKETS_COMMENT_ID_PATH, ticketId2, commentId2)));

            assertThat(outboxRepo.findAll()).hasSize(0);
        }

        @Test
        @DisplayName("agent edits own comment - 204 saves to outbox")
        void agent_editComment_onSuccess_savesToOutbox() throws Exception {
            mockMvc.perform(withAgent(patch(TICKETS_COMMENT_ID_PATH, ticketId, agentCommentId)
                    .content(toJson(Map.of("body", "updated agent comment")))));

            assertThat(outboxRepo.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("agent deletes own comment - 204 saves to outbox")
        void agent_deleteComment_onSuccess_savesToOutbox() throws Exception {
            mockMvc.perform(withAgent(delete(TICKETS_COMMENT_ID_PATH, ticketId, agentCommentId)));

            assertThat(outboxRepo.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("agent edits customer's comment - 403 don't save to outbox")
        void agent_editComment_onFailure_noSaveToOutbox() throws Exception {
            mockMvc.perform(withAgent(patch(TICKETS_COMMENT_ID_PATH, ticketId, commentId)
                    .content(toJson(Map.of("body", "updated customer comment")))));

            assertThat(outboxRepo.findAll()).hasSize(0);
        }

        @Test
        @DisplayName("agent deletes customer's comment - 403 don't save to outbox")
        void agent_deleteComment_onFailure_noSaveToOutbox() throws Exception {
            mockMvc.perform(withAgent(delete(TICKETS_COMMENT_ID_PATH, ticketId, commentId)));

            assertThat(outboxRepo.findAll()).hasSize(0);
        }

        @Test
        @DisplayName("admin edits own comment - 204 save to outbox")
        void admin_editComment_onSuccess_savesToOutbox() throws Exception {
            mockMvc.perform(withAdmin(patch(TICKETS_COMMENT_ID_PATH, ticketId, adminCommentId)
                    .content(toJson(Map.of("body", "updated admin comment")))));

            assertThat(outboxRepo.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("admin edits customer's comment - 403 don't save to outbox")
        void admin_editComment_onFailure_savesToOutbox() throws Exception {
            mockMvc.perform(withAdmin(patch(TICKETS_COMMENT_ID_PATH, ticketId, commentId)
                    .content(toJson(Map.of("body", "updated customer comment")))));

            assertThat(outboxRepo.findAll()).hasSize(0);
        }

        @Test
        @DisplayName("admin deletes customer's comment - 204 saves to outbox")
        void admin_deleteComment_onSuccess_savesToOutbox() throws Exception {
            mockMvc.perform(withAdmin(delete(TICKETS_COMMENT_ID_PATH, ticketId, commentId)));

            assertThat(outboxRepo.findAll()).hasSize(1);
        }

    }
}