package dev.luxorium.mercatus.economy;

import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

public final class PaymentProviders {
    private PaymentProviders() {
    }

    public static PaymentProvider resolve(Server server, boolean aureusEnabled, boolean requireForTransactions, Logger logger) {
        if (!aureusEnabled) {
            return fallback(requireForTransactions);
        }
        Plugin aureus = server.getPluginManager().getPlugin("Aureus");
        if (aureus == null || !aureus.isEnabled()) {
            return fallback(requireForTransactions);
        }
        return AureusPaymentProvider.create(aureus, logger);
    }

    private static PaymentProvider fallback(boolean requireForTransactions) {
        return requireForTransactions ? new NoopPaymentProvider() : new StandalonePaymentProvider();
    }
}
