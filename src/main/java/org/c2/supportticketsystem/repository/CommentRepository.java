package org.c2.supportticketsystem.repository;

import org.c2.supportticketsystem.model.Comment;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for Comment entity.
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Find comments by ticket ID
    List<Comment> findByTicketId(Long ticketId, Sort sort);

    // Count comments by ticket ID
    long countByTicketId(Long ticketId);
}
