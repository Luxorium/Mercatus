package dev.luxorium.mercatus;

import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import dev.luxorium.mercatus.command.MercatusCommand;
import dev.luxorium.mercatus.command.ShopCommand;
import dev.luxorium.mercatus.config.MercatusConfig;
import dev.luxorium.mercatus.economy.PaymentProvider;
import dev.luxorium.mercatus.economy.PaymentProviders;
import dev.luxorium.mercatus.listener.ShopInteractListener;
import dev.luxorium.mercatus.service.ShopPolicy;
import dev.luxorium.mercatus.service.ShopService;
import dev.luxorium.mercatus.storage.MercatusStorage;

public final class MercatusPlugin extends JavaPlugin {
    private MercatusStorage storage;
    private ShopService shopService;
    private MercatusConfig mercatusConfig;
    private Executor asyncExecutor;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadMercatusConfig();

        asyncExecutor = task -> getServer().getAsyncScheduler().runNow(this, scheduledTask -> task.run());
        Path databasePath = getDataFolder().toPath().resolve(mercatusConfig.storageFile());
        storage = new MercatusStorage(databasePath);
        PaymentProvider paymentProvider = PaymentProviders.resolve(getServer(), mercatusConfig.aureusEnabled(), getLogger());
        shopService = new ShopService(storage, asyncExecutor, new ShopPolicy(), mercatusConfig, paymentProvider, getLogger());

        asyncExecutor.execute(() -> {
            try {
                storage.initialize();
                getLogger().info("SQLite storage initialized with WAL mode.");
            } catch (Exception exception) {
                getLogger().log(Level.SEVERE, "Failed to initialize Mercatus storage.", exception);
                getServer().getGlobalRegionScheduler().run(this, task -> getServer().getPluginManager().disablePlugin(this));
            }
        });

        registerCommands();
        getServer().getPluginManager().registerEvents(new ShopInteractListener(this, shopService), this);
        getLogger().info("Mercatus enabled with payment provider: " + paymentProvider.name());
    }

    @Override
    public void onDisable() {
        if (storage != null) {
            storage.close();
        }
    }

    public ShopService shopService() {
        return shopService;
    }

    public MercatusConfig mercatusConfig() {
        return mercatusConfig;
    }

    public void reloadMercatus() {
        reloadConfig();
        reloadMercatusConfig();
        if (shopService != null) {
            PaymentProvider paymentProvider = PaymentProviders.resolve(getServer(), mercatusConfig.aureusEnabled(), getLogger());
            shopService.updateConfig(mercatusConfig, paymentProvider);
        }
    }

    private void reloadMercatusConfig() {
        mercatusConfig = MercatusConfig.from(getConfig());
    }

    private void registerCommands() {
        setExecutor("mercatus", new MercatusCommand(this));
        ShopCommand shopCommand = new ShopCommand(this, shopService);
        PluginCommand command = Objects.requireNonNull(getCommand("shop"), "Missing command: shop");
        command.setExecutor(shopCommand);
        command.setTabCompleter(shopCommand);
    }

    private void setExecutor(String commandName, org.bukkit.command.CommandExecutor executor) {
        PluginCommand command = Objects.requireNonNull(getCommand(commandName), "Missing command: " + commandName);
        command.setExecutor(executor);
    }
}
