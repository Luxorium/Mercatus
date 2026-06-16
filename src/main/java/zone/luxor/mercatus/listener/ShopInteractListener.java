package zone.luxor.mercatus.listener;

import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import zone.luxor.mercatus.MercatusPlugin;
import zone.luxor.mercatus.model.BlockLocation;
import zone.luxor.mercatus.model.Shop;
import zone.luxor.mercatus.model.ShopMode;
import zone.luxor.mercatus.service.ShopService;
import zone.luxor.mercatus.util.ItemKey;

public final class ShopInteractListener implements Listener {
    private final MercatusPlugin plugin;
    private final ShopService shopService;

    public ShopInteractListener(MercatusPlugin plugin, ShopService shopService) {
        this.plugin = plugin;
        this.shopService = shopService;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }
        Block block = event.getClickedBlock();
        if (!(block.getState() instanceof Chest)) {
            return;
        }
        Player player = event.getPlayer();
        Location bukkitLocation = block.getLocation();
        BlockLocation location = BlockLocation.from(bukkitLocation);
        shopService.findShop(location).thenAccept(result -> {
            if (!result.success()) {
                return;
            }
            plugin.getServer().getRegionScheduler().run(plugin, bukkitLocation, task -> handleShopClick(player, block, result.value()));
        });
    }

    private void handleShopClick(Player player, Block block, Shop shop) {
        if (!(block.getState() instanceof Chest chest)) {
            player.sendMessage("This shop chest no longer exists.");
            return;
        }
        if (shop.mode() == ShopMode.BUY) {
            handleBuyShop(player, chest, shop, block);
            return;
        }
        Inventory chestInventory = chest.getBlockInventory();
        int slot = firstMatchingSlot(chestInventory, shop.itemKey());
        if (slot < 0) {
            player.sendMessage("This shop is out of stock.");
            return;
        }
        Location shopLocation = block.getLocation();
        shopService.completeSale(shop, player.getUniqueId(), 1)
                .thenAccept(result -> plugin.getServer().getRegionScheduler().run(plugin, shopLocation, task -> {
                    if (!result.success()) {
                        player.sendMessage(result.message());
                        return;
                    }
                    ItemStack stack = chestInventory.getItem(slot);
                    if (stack == null || stack.getType() == Material.AIR || !ItemKey.from(stack).equals(shop.itemKey())) {
                        player.sendMessage("This shop is out of stock.");
                        return;
                    }
                    ItemStack purchased = stack.clone();
                    purchased.setAmount(1);
                    stack.setAmount(stack.getAmount() - 1);
                    chestInventory.setItem(slot, stack.getAmount() <= 0 ? null : stack);
                    player.getInventory().addItem(purchased).values()
                            .forEach(overflow -> player.getWorld().dropItemNaturally(player.getLocation(), overflow));
                    player.sendMessage("Purchased 1 " + shop.itemKey() + " for " + shop.priceMinor() + ".");
                }));
    }

    private void handleBuyShop(Player player, Chest chest, Shop shop, Block block) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType() == Material.AIR || !ItemKey.from(hand).equals(shop.itemKey())) {
            player.sendMessage("Hold the item this shop is buying.");
            return;
        }
        if (chest.getBlockInventory().firstEmpty() < 0) {
            player.sendMessage("This buy shop has no storage space.");
            return;
        }
        Location shopLocation = block.getLocation();
        shopService.completeBuyOrder(shop, player.getUniqueId(), 1)
                .thenAccept(result -> plugin.getServer().getRegionScheduler().run(plugin, shopLocation, task -> {
                    if (!result.success()) {
                        player.sendMessage(result.message());
                        return;
                    }
                    ItemStack current = player.getInventory().getItemInMainHand();
                    if (current.getType() == Material.AIR || !ItemKey.from(current).equals(shop.itemKey())) {
                        player.sendMessage("You are no longer holding the item this shop is buying.");
                        return;
                    }
                    ItemStack sold = current.clone();
                    sold.setAmount(1);
                    current.setAmount(current.getAmount() - 1);
                    player.getInventory().setItemInMainHand(current.getAmount() <= 0 ? null : current);
                    chest.getBlockInventory().addItem(sold)
                            .values()
                            .forEach(overflow -> player.getWorld().dropItemNaturally(player.getLocation(), overflow));
                    player.sendMessage("Sold 1 " + shop.itemKey() + " for " + shop.priceMinor() + ".");
                }));
    }

    private static int firstMatchingSlot(Inventory inventory, String itemKey) {
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack != null && stack.getType() != Material.AIR && ItemKey.from(stack).equals(itemKey)) {
                return slot;
            }
        }
        return -1;
    }
}
