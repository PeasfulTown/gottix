package xyz.peasfultown.gottix.search_service.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TicketPriority {
    LOW, MEDIUM, HIGH;

    @JsonValue
    @Override
    public String toString() {
        return this.name();
    }
}
