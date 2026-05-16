package xyz.peasfultown.gottix.ticket_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Comments")
@Transactional
public class CommentIntegrationTest extends BaseIntegrationTest {

    private static final String TICKETS = "/api/v1/tickets";

    private String ticketId;

    @BeforeEach
    void createTicket() throws Exception {
        ticketId = extractId(mockMvc.perform(
                withCustomer(post(TICKETS)).content(defaultTicketBody())));
    }

    private String commentsPath() {
        return TICKETS + "/" + ticketId + "/comments";
    }

    private String commentBody(String body) throws Exception {
        return toJson(Map.of("body", body));
    }

    // ================================================================
    // POST /api/v1/tickets/{ticketId}/comments
    // ================================================================

    @Nested
    @DisplayName("POST /{ticketId}/comments - add comment")
    class AddComment {

        @Test
        @DisplayName("agent adds comment - 201 with comment response")
        void agent_addComment_returns201() throws Exception {
            mockMvc.perform(withAgent(post(commentsPath()))
                            .content(commentBody("Have you tried clearing your browser cache?")))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.body").value("Have you tried clearing your browser cache?"))
                    .andExpect(jsonPath("$.authorId").value(AGENT_ID))
                    .andExpect(jsonPath("$.createdAt").isNotEmpty());
        }

        @Test
        @DisplayName("customer adds comment to own ticket - 201")
        void customer_addCommentToOwnTicket_returns201() throws Exception {
            mockMvc.perform(withCustomer(post(commentsPath()))
                            .content(commentBody("I already tried that, still not working")))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.authorId").value(CUSTOMER_ID));
        }

        @Test
        @DisplayName("admin adds comment - 201")
        void admin_addComment_returns201() throws Exception {
            mockMvc.perform(withAdmin(post(commentsPath()))
                            .content(commentBody("Escalating to tier 2 support")))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("customer adds comment to another customer's ticket - 403")
        void customer_addCommentToOtherTicket_returns403() throws Exception {
            mockMvc.perform(withCustomer2(post(commentsPath()))
                            .content(commentBody("This should not work")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("comment is embedded in ticket response after creation")
        void addComment_appearsInTicketResponse() throws Exception {
            mockMvc.perform(withAgent(post(commentsPath()))
                    .content(commentBody("First comment")));

            mockMvc.perform(withAgent(get(TICKETS + "/{id}", ticketId)))
                    .andExpect(jsonPath("$.comments", hasSize(greaterThanOrEqualTo(1))))
                    .andExpect(jsonPath("$.comments[0].body").value("First comment"));
        }

        @Test
        @DisplayName("multiple comments appear in order on ticket")
        void addMultipleComments_appearsInOrder() throws Exception {
            mockMvc.perform(withAgent(post(commentsPath()))
                    .content(commentBody("First agent response")));
            mockMvc.perform(withCustomer(post(commentsPath()))
                    .content(commentBody("Customer reply")));
            mockMvc.perform(withAgent(post(commentsPath()))
                    .content(commentBody("Second agent response")));

            mockMvc.perform(withAgent(get(TICKETS + "/{id}", ticketId)))
                    .andExpect(jsonPath("$.comments", hasSize(3)));
        }

        @Test
        @DisplayName("add comment with blank body - 400")
        void addComment_blankBody_returns400() throws Exception {
            mockMvc.perform(withAgent(post(commentsPath()))
                            .content(toJson(Map.of("body", "  "))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("add comment with missing body - 400")
        void addComment_missingBody_returns400() throws Exception {
            mockMvc.perform(withAgent(post(commentsPath()))
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("add comment to non-existent ticket - 404")
        void addComment_nonExistentTicket_returns404() throws Exception {
            mockMvc.perform(withAgent(post(TICKETS + "/00000000-0000-0000-0000-00000000000f/comments"))
                            .content(commentBody("Comment on nothing")))
                    .andExpect(status().isNotFound());
        }
    }

    // ================================================================
    // PATCH /api/v1/tickets/{ticketId}/comments/{commentId}
    // ================================================================

    @Nested
    @DisplayName("PATCH /{ticketId}/comments/{commentId} - edit comment")
    class EditComment {

        private String commentId;

        @BeforeEach
        void addComment() throws Exception {
            commentId = extractId(mockMvc.perform(
                    withAgent(post(commentsPath()))
                            .content(commentBody("Original comment body"))));
        }

        @Test
        @DisplayName("author edits own comment - 200 with updated body")
        void author_editOwnComment_returns200() throws Exception {
            mockMvc.perform(withAgent(patch(commentsPath() + "/{cid}", commentId))
                            .content(commentBody("Updated comment body")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.body").value("Updated comment body"))
                    .andExpect(jsonPath("$.updatedAt").isNotEmpty());
        }

        @Test
        @DisplayName("admin edits any comment - 200")
        void admin_editAnyComment_returns200() throws Exception {
            mockMvc.perform(withAdmin(patch(commentsPath() + "/{cid}", commentId))
                            .content(commentBody("Admin edited this")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.body").value("Admin edited this"));
        }

        @Test
        @DisplayName("different agent edits another agent's comment - 403")
        void differentAgent_editOtherComment_returns403() throws Exception {
            mockMvc.perform(withAgent2(patch(commentsPath() + "/{cid}", commentId))
                            .content(commentBody("Trying to edit")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("customer edits agent's comment - 403")
        void customer_editAgentComment_returns403() throws Exception {
            mockMvc.perform(withCustomer(patch(commentsPath() + "/{cid}", commentId))
                            .content(commentBody("Trying to edit")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("edit with blank body - 400")
        void editComment_blankBody_returns400() throws Exception {
            mockMvc.perform(withAgent(patch(commentsPath() + "/{cid}", commentId))
                            .content(toJson(Map.of("body", "  "))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("edit non-existent comment - 404")
        void editComment_notFound_returns404() throws Exception {
            mockMvc.perform(withAgent(patch(commentsPath() + "/{cid}", "00000000-0000-0000-0000-00000000000f"))
                            .content(commentBody("Editing nothing")))
                    .andExpect(status().isNotFound());
        }
    }

    // ================================================================
    // DELETE /api/v1/tickets/{ticketId}/comments/{commentId}
    // ================================================================

    @Nested
    @DisplayName("DELETE /{ticketId}/comments/{commentId} - delete comment")
    class DeleteComment {

        private String agentCommentId;
        private String customerCommentId;

        @BeforeEach
        void addComments() throws Exception {
            agentCommentId = extractId(mockMvc.perform(
                    withAgent(post(commentsPath()))
                            .content(commentBody("Agent's comment"))));

            customerCommentId = extractId(mockMvc.perform(
                    withCustomer(post(commentsPath()))
                            .content(commentBody("Customer's comment"))));
        }

        @Test
        @DisplayName("agent deletes own comment - 204")
        void agent_deleteOwnComment_returns204() throws Exception {
            mockMvc.perform(withAgent(delete(commentsPath() + "/{cid}", agentCommentId)))
                    .andExpect(status().isNoContent());

            // Verify it's gone from the ticket
            mockMvc.perform(withAgent(get(TICKETS + "/{id}", ticketId)))
                    .andExpect(jsonPath("$.comments[*].id",
                            not(hasItem(agentCommentId))));
        }

        @Test
        @DisplayName("customer deletes own comment - 204")
        void customer_deleteOwnComment_returns204() throws Exception {
            mockMvc.perform(withCustomer(delete(commentsPath() + "/{cid}", customerCommentId)))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("admin deletes any comment - 204")
        void admin_deleteAnyComment_returns204() throws Exception {
            mockMvc.perform(withAdmin(delete(commentsPath() + "/{cid}", agentCommentId)))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("customer deletes agent's comment - 403")
        void customer_deleteAgentComment_returns403() throws Exception {
            mockMvc.perform(withCustomer(delete(commentsPath() + "/{cid}", agentCommentId)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("agent deletes customer's comment - 403")
        void agent_deleteCustomerComment_returns403() throws Exception {
            mockMvc.perform(withAgent(delete(commentsPath() + "/{cid}", customerCommentId)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("delete non-existent comment - 404")
        void deleteComment_notFound_returns404() throws Exception {
            mockMvc.perform(withAdmin(delete(commentsPath() + "/{cid}", "00000000-0000-0000-0000-00000000000f")))
                    .andExpect(status().isNotFound());
        }
    }
}
