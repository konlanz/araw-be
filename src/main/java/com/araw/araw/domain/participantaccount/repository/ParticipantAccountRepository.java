package com.araw.araw.domain.participantaccount.repository;

import com.araw.araw.domain.participantaccount.entity.ParticipantAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParticipantAccountRepository extends JpaRepository<ParticipantAccount, UUID> {

    Optional<ParticipantAccount> findByParticipantId(UUID participantId);

    Optional<ParticipantAccount> findByEmailIgnoreCase(String email);
}
