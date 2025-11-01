package com.araw.araw.application.service;

import com.araw.araw.application.service.result.ParticipantAccountProvisionResult;
import com.araw.araw.domain.application.entity.Application;
import com.araw.araw.domain.participant.enitity.Participant;
import com.araw.araw.domain.participant.repository.ParticipantRepository;
import com.araw.araw.domain.participantaccount.entity.ParticipantAccount;
import com.araw.araw.domain.participantaccount.repository.ParticipantAccountRepository;
import com.araw.shared.exception.DomainValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ParticipantAccountService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789@#$%".toCharArray();

    private final ParticipantAccountRepository accountRepository;
    private final ParticipantRepository participantRepository;
    private final PasswordEncoder passwordEncoder;

    public ParticipantAccountProvisionResult provisionForApplication(Application application) {
        Participant participant = resolveParticipant(application);
        if (participant == null) {
            return ParticipantAccountProvisionResult.notCreated();
        }

        String email = resolveAccountEmail(application, participant);
        if (email == null || email.isBlank()) {
            return ParticipantAccountProvisionResult.notCreated();
        }

        ParticipantAccount account = accountRepository.findByParticipantId(participant.getId())
                .orElseGet(() -> accountRepository.findByEmailIgnoreCase(email).orElse(null));

        if (account != null) {
            boolean changed = false;
            if (account.getParticipant() == null) {
                account.setParticipant(participant);
                changed = true;
            }
            if (!account.getEmail().equalsIgnoreCase(email)) {
                validateEmailAvailability(email, account.getId());
                account.setEmail(email.toLowerCase(Locale.ROOT));
                changed = true;
            }
            if (changed) {
                accountRepository.save(account);
            }
            return ParticipantAccountProvisionResult.existing(account.getEmail());
        }

        validateEmailAvailability(email, null);

        String temporaryPassword = generateTemporaryPassword();
        ParticipantAccount created = ParticipantAccount.builder()
                .participant(participant)
                .email(email.toLowerCase(Locale.ROOT))
                .passwordHash(passwordEncoder.encode(temporaryPassword))
                .requiresPasswordReset(true)
                .temporaryPassword(null)
                .build();

        accountRepository.save(created);
        return ParticipantAccountProvisionResult.created(created.getEmail(), temporaryPassword);
    }

    private Participant resolveParticipant(Application application) {
        if (application.getParticipant() != null) {
            return application.getParticipant();
        }
        if (application.getEmail() != null && !application.getEmail().isBlank()) {
            return participantRepository.findByContactInfoEmailIgnoreCase(application.getEmail().trim())
                    .orElse(null);
        }
        return null;
    }

    private String resolveAccountEmail(Application application, Participant participant) {
        if (participant.getContactInfo() != null && participant.getContactInfo().getEmail() != null
                && !participant.getContactInfo().getEmail().isBlank()) {
            return participant.getContactInfo().getEmail().trim();
        }
        if (application.getEmail() != null && !application.getEmail().isBlank()) {
            return application.getEmail().trim();
        }
        return null;
    }

    private void validateEmailAvailability(String email, java.util.UUID ignoreId) {
        Optional<ParticipantAccount> existing = accountRepository.findByEmailIgnoreCase(email);
        if (existing.isPresent() && (ignoreId == null || !existing.get().getId().equals(ignoreId))) {
            throw new DomainValidationException("An account already exists for email " + email);
        }
    }

    private String generateTemporaryPassword() {
        int length = 14;
        char[] buffer = new char[length];
        for (int i = 0; i < length; i++) {
            buffer[i] = PASSWORD_CHARS[RANDOM.nextInt(PASSWORD_CHARS.length)];
        }
        return new String(buffer);
    }
}
