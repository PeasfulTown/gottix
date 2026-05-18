package xyz.peasfultown.gottix.search_service.entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDocument {
    private String id;
    private String body;
    private String authorId;
}
