package org.example.lineupmaker_be.domain.model.lineup;


// 댓글 하나
public record CommentJson(
        String name,
        String text,
        long createdAt
) {
}
