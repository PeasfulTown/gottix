package xyz.peasfultown.gottix.ticket_service.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OutboxComment {
    private String id;
    private String body;
    private String authorId;
}
