package com.araw.araw.application.service.result;

public record ParticipantAccountProvisionResult(boolean accountCreated,
                                                String email,
                                                String temporaryPassword) {

    public static ParticipantAccountProvisionResult notCreated() {
        return new ParticipantAccountProvisionResult(false, null, null);
    }

    public static ParticipantAccountProvisionResult existing(String email) {
        return new ParticipantAccountProvisionResult(false, email, null);
    }

    public static ParticipantAccountProvisionResult created(String email, String temporaryPassword) {
        return new ParticipantAccountProvisionResult(true, email, temporaryPassword);
    }
}
