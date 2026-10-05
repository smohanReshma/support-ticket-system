package org.c2.supportticketsystem.repository;

import org.c2.supportticketsystem.model.Ticket;
import org.c2.supportticketsystem.model.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository for Ticket entity.
 */
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // Find tickets by status
    List<Ticket> findByStatus(Status status, Sort sort);

    // Search by title or description (case-insensitive)
    @Query("SELECT t FROM Ticket t WHERE " +
           "LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Ticket> search(@Param("keyword") String keyword, Sort sort);

    // Find by status with search
    @Query("SELECT t FROM Ticket t WHERE " +
           "t.status = :status AND (" +
           "LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Ticket> findByStatusAndSearch(@Param("status") Status status,
                                        @Param("keyword") String keyword,
                                        Sort sort);

    // Pagination support
    Page<Ticket> findAll(Pageable pageable);
}
