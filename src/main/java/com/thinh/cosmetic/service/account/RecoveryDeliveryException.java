package com.thinh.cosmetic.service.account;

public class RecoveryDeliveryException extends RuntimeException {
    public RecoveryDeliveryException() {
        super("Recovery delivery is currently unavailable");
    }
}
