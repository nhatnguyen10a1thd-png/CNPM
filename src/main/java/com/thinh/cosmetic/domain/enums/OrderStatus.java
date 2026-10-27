package com.thinh.cosmetic.domain.enums;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PREPARING,
    SHIPPING,
    COMPLETED,
    DELIVERED,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus next) {
        if (next == null) {
            return false;
        }
        if (this == next) {
            return true;
        }
        return switch (this) {
            case PENDING -> next == CONFIRMED || next == CANCELLED;
            case CONFIRMED -> next == PREPARING || next == CANCELLED;
            case PREPARING -> next == SHIPPING || next == CANCELLED;
            case SHIPPING -> next == COMPLETED || next == DELIVERED;
            case COMPLETED, DELIVERED, CANCELLED -> false;
        };
    }
}
