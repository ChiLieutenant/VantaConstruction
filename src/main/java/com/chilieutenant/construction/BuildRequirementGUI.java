package com.chilieutenant.construction;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class BuildRequirementGUI implements Listener {

    private static final String GUI_TITLE = "İnşa Gereksinimleri";
    private static final int GUI_SIZE = 27;
    private static final int ITEMS_PER_PAGE = 19;
    private static final Material NEXT_BUTTON = Material.ARROW;
    private static final Material PREVIOUS_BUTTON = Material.ARROW;
    private static final String NEXT_BUTTON_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19";
    private static final String PREVIOUS_BUTTON_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==";
    private static final String BACKGROUND_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzM3YjIwNTU1ZmUxNDIzNGI4ZmU1YWI1NmYxNDY5YTI2ZjY3ODM3NTcxYWI5MWNhYmQzM2RlMDQwNjNlNDRmIn19fQ==";

    public static void openGUI(Player player) {
        openGUI(player, 0);
    }

    public static void openGUI(Player player, int page) {
        Map<String, List<ItemStack>> allRequirements = BuildRequirements.getAllRequirements();
        List<Map.Entry<String, List<ItemStack>>> filteredRequirements = allRequirements.entrySet().stream()
                .filter(entry -> player.hasPermission("construction.build." + entry.getKey()))
                .collect(Collectors.toList());

        if (filteredRequirements.isEmpty()) {
            player.sendMessage("§cYou don't have permission for any buildings.");
            return;
        }

        int totalPages = (int) Math.ceil((double) filteredRequirements.size() / ITEMS_PER_PAGE);
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;

        Inventory gui = Bukkit.createInventory(null, GUI_SIZE, GUI_TITLE + " - Page " + (page + 1));

        // Set background
        ItemStack backgroundItem = createCustomTexturedItem(Material.BLACK_STAINED_GLASS_PANE, " ", BACKGROUND_TEXTURE);
        for (int i = 0; i < GUI_SIZE; i++) {
            gui.setItem(i, backgroundItem);
        }

        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, filteredRequirements.size());

        int slot = 1;
        for (int i = startIndex; i < endIndex; i++) {
            Map.Entry<String, List<ItemStack>> entry = filteredRequirements.get(i);
            String buildName = entry.getKey();
            List<ItemStack> requirements = entry.getValue();

            ItemStack buildItem = new ItemStack(Material.PAPER);
            ItemMeta meta = buildItem.getItemMeta();
            meta.setDisplayName(buildName);
            List<String> lore = new ArrayList<>();
            lore.add("§0Requirements:");
            for (ItemStack req : requirements) {
                lore.add("§f- §7" + req.getType().name() + " §8x" + req.getAmount());
            }
            meta.setLore(lore);
            buildItem.setItemMeta(meta);

            gui.setItem(slot, buildItem);
            slot++;
            if ((slot + 1) % 9 == 0) slot += 2;
        }

        // Add navigation buttons
        if (page > 0) {
            gui.setItem(18, createCustomTexturedItem(PREVIOUS_BUTTON, "§aPrevious Page", PREVIOUS_BUTTON_TEXTURE));
        }
        if (page < totalPages - 1) {
            gui.setItem(26, createCustomTexturedItem(NEXT_BUTTON, "§aNext Page", NEXT_BUTTON_TEXTURE));
        }

        player.openInventory(gui);
    }

    private static ItemStack createCustomTexturedItem(Material material, String name, String texture) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);

        if (material == Material.PLAYER_HEAD) {
            SkullMeta skullMeta = (SkullMeta) meta;
            skullMeta.setOwner("Notch"); // This is needed to trigger texture loading
            skullMeta.setCustomModelData(1); // Optional: Custom model data for resource pack textures
            item.setItemMeta(skullMeta);

            // Apply the base64 texture
            Bukkit.getUnsafe().modifyItemStack(item, "{SkullOwner:{Id:\"" + UUID.randomUUID() + "\",Properties:{textures:[{Value:\"" + texture + "\"}]}}}");
        } else {
            item.setItemMeta(meta);
        }

        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().startsWith(GUI_TITLE)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        String title = event.getView().getTitle();
        int currentPage = 0;
        if (title.contains("Page ")) {
            String[] parts = title.split("Page ");
            if (parts.length > 1) {
                try {
                    currentPage = Integer.parseInt(parts[1]) - 1;
                } catch (NumberFormatException e) {
                    // Handle parse error if necessary
                }
            }
        }

        if (event.getSlot() == 18 && clickedItem.getType() == PREVIOUS_BUTTON) {
            openGUI(player, currentPage - 1);
        } else if (event.getSlot() == 26 && clickedItem.getType() == NEXT_BUTTON) {
            openGUI(player, currentPage + 1);
        } else if (clickedItem.getType() == Material.PAPER) {
            String buildName = clickedItem.getItemMeta().getDisplayName();
            player.closeInventory();
            Utils.previewBuild(player, buildName);
        }
    }
}