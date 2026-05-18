package xyz.peasfultown.gottix.search_service.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TicketStatus {
    OPENED, IN_PROGRESS, RESOLVED, CLOSED;

    @JsonValue
    @Override
    public String toString() {
        return this.name();
    }
}
