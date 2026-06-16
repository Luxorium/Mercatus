package dev.luxorium.mercatus.economy;

import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

public final class PaymentProviders {
    private PaymentProviders() {
    }

    public static PaymentProvider resolve(Server server, boolean aureusEnabled, Logger logger) {
        if (!aureusEnabled) {
            return new NoopPaymentProvider();
        }
        Plugin aureus = server.getPluginManager().getPlugin("Aureus");
        if (aureus == null || !aureus.isEnabled()) {
            return new NoopPaymentProvider();
        }
        return AureusPaymentProvider.create(aureus, logger);
    }
}
