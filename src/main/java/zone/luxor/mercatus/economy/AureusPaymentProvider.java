package zone.luxor.mercatus.economy;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.plugin.Plugin;

public final class AureusPaymentProvider implements PaymentProvider {
    private final Object economyService;
    private final Method transferMethod;
    private final Method withdrawMethod;
    private final Logger logger;

    private AureusPaymentProvider(Object economyService, Method transferMethod, Method withdrawMethod, Logger logger) {
        this.economyService = economyService;
        this.transferMethod = transferMethod;
        this.withdrawMethod = withdrawMethod;
        this.logger = logger;
    }

    public static PaymentProvider create(Plugin aureusPlugin, Logger logger) {
        try {
            Method serviceMethod = aureusPlugin.getClass().getMethod("economyService");
            Object service = serviceMethod.invoke(aureusPlugin);
            Method transfer = service.getClass().getMethod("transfer", UUID.class, UUID.class, long.class, String.class);
            Method withdraw = service.getClass().getMethod("withdraw", UUID.class, long.class, Class.forName("zone.luxor.aureus.model.TransactionType"), String.class, UUID.class);
            return new AureusPaymentProvider(service, transfer, withdraw, logger);
        } catch (ReflectiveOperationException exception) {
            logger.log(Level.WARNING, "Aureus is installed but its economy API could not be linked.", exception);
            return new NoopPaymentProvider();
        }
    }

    @Override
    public String name() {
        return "Aureus";
    }

    @Override
    public boolean available() {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<PaymentResult> transfer(UUID sourceUuid, UUID targetUuid, long amountMinor, String reason) {
        if (amountMinor <= 0L) {
            return CompletableFuture.completedFuture(PaymentResult.failure("Amount must be positive."));
        }
        try {
            CompletableFuture<Object> future = (CompletableFuture<Object>) transferMethod.invoke(economyService, sourceUuid, targetUuid, amountMinor, reason);
            return future.thenApply(AureusPaymentProvider::fromAureusResult);
        } catch (ReflectiveOperationException exception) {
            logger.log(Level.WARNING, "Aureus transfer failed before completion.", exception);
            return CompletableFuture.completedFuture(PaymentResult.failure("Aureus transfer failed."));
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public CompletableFuture<PaymentResult> withdraw(UUID sourceUuid, long amountMinor, String reason) {
        if (amountMinor == 0L) {
            return CompletableFuture.completedFuture(PaymentResult.ok());
        }
        if (amountMinor < 0L) {
            return CompletableFuture.completedFuture(PaymentResult.failure("Amount must not be negative."));
        }
        try {
            Class enumType = Class.forName("zone.luxor.aureus.model.TransactionType");
            Object type = Enum.valueOf(enumType, "ADMIN_TAKE");
            CompletableFuture<Object> future = (CompletableFuture<Object>) withdrawMethod.invoke(economyService, sourceUuid, amountMinor, type, reason, null);
            return future.thenApply(AureusPaymentProvider::fromAureusResult);
        } catch (IllegalArgumentException exception) {
            return transfer(sourceUuid, sourceUuid, amountMinor, reason)
                    .thenApply(result -> PaymentResult.failure("Aureus does not expose a shop fee transaction type."));
        } catch (ReflectiveOperationException exception) {
            logger.log(Level.WARNING, "Aureus withdraw failed before completion.", exception);
            return CompletableFuture.completedFuture(PaymentResult.failure("Aureus withdraw failed."));
        }
    }

    private static PaymentResult fromAureusResult(Object result) {
        try {
            boolean success = (boolean) result.getClass().getMethod("success").invoke(result);
            if (success) {
                return PaymentResult.ok();
            }
            String message = (String) result.getClass().getMethod("message").invoke(result);
            return PaymentResult.failure(message == null || message.isBlank() ? "Aureus payment failed." : message);
        } catch (ReflectiveOperationException exception) {
            return PaymentResult.failure("Aureus returned an unreadable payment result.");
        }
    }
}
