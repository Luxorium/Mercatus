package dev.luxorium.mercatus.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import dev.luxorium.mercatus.MercatusPlugin;

public final class MercatusCommand implements CommandExecutor {
    private final MercatusPlugin plugin;

    public MercatusCommand(MercatusPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        sender.sendMessage("Mercatus " + plugin.getPluginMeta().getVersion()
                + " | payment=" + plugin.shopService().paymentProvider().name()
                + " | Folia-native player shops");
        return true;
    }
}
