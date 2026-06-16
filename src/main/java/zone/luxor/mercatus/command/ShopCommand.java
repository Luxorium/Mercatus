package zone.luxor.mercatus.command;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zone.luxor.mercatus.MercatusPlugin;
import zone.luxor.mercatus.model.BlockLocation;
import zone.luxor.mercatus.model.Shop;
import zone.luxor.mercatus.model.ShopMode;
import zone.luxor.mercatus.service.ShopResult;
import zone.luxor.mercatus.service.ShopService;
import zone.luxor.mercatus.util.ItemKey;

public final class ShopCommand implements CommandExecutor, TabCompleter {
    private final MercatusPlugin plugin;
    private final ShopService shopService;

    public ShopCommand(MercatusPlugin plugin, ShopService shopService) {
        this.plugin = plugin;
        this.shopService = shopService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (args.length == 0) {
            sender.sendMessage("/shop create <sell|buy> <price>, /shop info, /shop remove, /shop list, /shop limits");
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            return reload(sender);
        }
        if (args[0].equalsIgnoreCase("admin")) {
            return admin(sender, args);
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use shop commands.");
            return true;
        }
        return switch (args[0].toLowerCase(java.util.Locale.ROOT)) {
            case "create" -> create(player, args);
            case "remove" -> remove(player);
            case "info" -> info(player);
            case "list" -> list(player);
            case "limits" -> limits(player);
            default -> {
                sender.sendMessage("Unknown shop command.");
                yield true;
            }
        };
    }

    private boolean create(Player player, String[] args) {
        if (!player.hasPermission("mercatus.shop.create")) {
            player.sendMessage("You do not have permission.");
            return true;
        }
        if (args.length < 4) {
            player.sendMessage("Usage: /shop create <sell|buy> <price>");
            return true;
        }
        ShopMode mode = ShopMode.parse(args[2]).orElse(null);
        if (mode == null) {
            player.sendMessage("Mode must be buy or sell.");
            return true;
        }
        long price;
        try {
            price = Long.parseLong(args[3]);
        } catch (NumberFormatException exception) {
            player.sendMessage("Price must be a whole minor-unit amount.");
            return true;
        }
        Block block = player.getTargetBlockExact(6);
        if (block == null || !(block.getState() instanceof Chest)) {
            player.sendMessage("Look at a chest to create a shop.");
            return true;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            player.sendMessage("Hold the shop item in your main hand.");
            return true;
        }
        shopService.createShop(
                player.getUniqueId(),
                player.getName(),
                BlockLocation.from(block.getLocation()),
                mode,
                ItemKey.from(item),
                price
        ).thenAccept(result -> send(player, result.message()));
        return true;
    }

    private boolean remove(Player player) {
        if (!player.hasPermission("mercatus.shop.remove.own")) {
            player.sendMessage("You do not have permission.");
            return true;
        }
        Block block = player.getTargetBlockExact(6);
        if (block == null) {
            player.sendMessage("Look at a shop chest.");
            return true;
        }
        shopService.removeShop(player.getUniqueId(), BlockLocation.from(block.getLocation()), false)
                .thenAccept(result -> send(player, result.message()));
        return true;
    }

    private boolean info(Player player) {
        if (!player.hasPermission("mercatus.shop.info")) {
            player.sendMessage("You do not have permission.");
            return true;
        }
        Block block = player.getTargetBlockExact(6);
        if (block == null) {
            player.sendMessage("Look at a shop chest.");
            return true;
        }
        shopService.findShop(BlockLocation.from(block.getLocation()))
                .thenAccept(result -> send(player, describe(result)));
        return true;
    }

    private boolean list(Player player) {
        if (!player.hasPermission("mercatus.shop.list")) {
            player.sendMessage("You do not have permission.");
            return true;
        }
        shopService.listShops(player.getUniqueId()).thenAccept(shops -> send(player, summarize(shops)));
        return true;
    }

    private boolean limits(Player player) {
        shopService.listShops(player.getUniqueId()).thenAccept(shops -> send(player,
                "Shops: " + shops.size() + "/" + shopService.config().maxShopsPerPlayer()));
        return true;
    }

    private boolean reload(CommandSender sender) {
        if (!sender.hasPermission("mercatus.admin.reload")) {
            sender.sendMessage("You do not have permission.");
            return true;
        }
        plugin.reloadMercatus();
        sender.sendMessage("Mercatus reloaded.");
        return true;
    }

    private boolean admin(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /shop admin <info|remove|list>");
            return true;
        }
        if (args[1].equalsIgnoreCase("list")) {
            if (!sender.hasPermission("mercatus.admin.list")) {
                sender.sendMessage("You do not have permission.");
                return true;
            }
            if (args.length >= 3) {
                shopService.listShopsByOwnerName(args[2]).thenAccept(shops -> sender.sendMessage(summarize(shops)));
            } else {
                shopService.listAllShops(50).thenAccept(shops -> sender.sendMessage(summarize(shops)));
            }
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Admin info/remove require an in-game target chest.");
            return true;
        }
        Block block = player.getTargetBlockExact(6);
        if (block == null) {
            sender.sendMessage("Look at a shop chest.");
            return true;
        }
        if (args[1].equalsIgnoreCase("info")) {
            if (!sender.hasPermission("mercatus.admin.info")) {
                sender.sendMessage("You do not have permission.");
                return true;
            }
            shopService.findShop(BlockLocation.from(block.getLocation())).thenAccept(result -> send(player, describe(result)));
            return true;
        }
        if (args[1].equalsIgnoreCase("remove")) {
            if (!sender.hasPermission("mercatus.admin.remove")) {
                sender.sendMessage("You do not have permission.");
                return true;
            }
            shopService.removeShop(player.getUniqueId(), BlockLocation.from(block.getLocation()), true)
                    .thenAccept(result -> send(player, result.message()));
            return true;
        }
        sender.sendMessage("Unknown admin shop command.");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (args.length == 1) {
            return List.of("create", "remove", "info", "list", "limits", "reload", "admin");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
            return List.of("sell", "buy");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("admin")) {
            return List.of("info", "remove", "list");
        }
        return List.of();
    }

    private void send(Player player, String message) {
        player.getScheduler().run(plugin, task -> player.sendMessage(message), null);
    }

    private static String describe(ShopResult<Shop> result) {
        if (!result.success()) {
            return result.message();
        }
        Shop shop = result.value();
        return "Shop #" + shop.id() + " " + shop.mode() + " " + shop.itemKey()
                + " price=" + shop.priceMinor() + " owner=" + shop.ownerName()
                + " at " + shop.location().display();
    }

    private static String summarize(List<Shop> shops) {
        if (shops.isEmpty()) {
            return "No shops found.";
        }
        List<String> lines = new ArrayList<>();
        lines.add("Shops (" + shops.size() + "):");
        for (Shop shop : shops.stream().limit(10).toList()) {
            lines.add("#" + shop.id() + " " + shop.mode() + " " + shop.itemKey()
                    + " " + shop.priceMinor() + " at " + shop.location().display());
        }
        return String.join("\n", lines);
    }
}
