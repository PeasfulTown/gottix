package xyz.peasfultown.gottix.search_service.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class CommentChangeEvent {
    private String ticketId;
    private String id;
    private String body;
    private String authorId;
    private Instant updatedAt;
}
