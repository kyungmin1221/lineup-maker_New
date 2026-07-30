package org.example.lineupmaker_be.web.dto.lineup;

import org.example.lineupmaker_be.domain.model.lineup.CommentJson;

public record CommentResponse(
    String name,
    String text,
    long createdAt

) {
    public static CommentResponse from(CommentJson comment) {
        return new CommentResponse(
            comment.name(),
            comment.text(),
            comment.createdAt()
        );
    }
}