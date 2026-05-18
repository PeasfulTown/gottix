package xyz.peasfultown.gottix.ticket_service.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class CommentChangeEvent {
    private String ticketId;
    private String id;
    private String body;
    private String authorId;
}
