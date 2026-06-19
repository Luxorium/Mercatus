package dev.luxorium.mercatus.economy;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class StandalonePaymentProvider implements PaymentProvider {
    @Override
    public String name() {
        return "standalone";
    }

    @Override
    public boolean available() {
        return false;
    }

    @Override
    public CompletableFuture<PaymentResult> transfer(UUID sourceUuid, UUID targetUuid, long amountMinor, String reason) {
        if (amountMinor < 0L) {
            return CompletableFuture.completedFuture(PaymentResult.failure("Amount must not be negative."));
        }
        return CompletableFuture.completedFuture(PaymentResult.ok());
    }

    @Override
    public CompletableFuture<PaymentResult> withdraw(UUID sourceUuid, long amountMinor, String reason) {
        if (amountMinor < 0L) {
            return CompletableFuture.completedFuture(PaymentResult.failure("Amount must not be negative."));
        }
        return CompletableFuture.completedFuture(PaymentResult.ok());
    }
}
