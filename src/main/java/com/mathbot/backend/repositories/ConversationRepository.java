package com.mathbot.backend.repositories;

import com.mathbot.backend.models.entity.Conversation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);

    Optional<Conversation> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    Optional<Conversation> findFirstByUserIdOrderByUpdatedAtDesc(Long userId);
}
