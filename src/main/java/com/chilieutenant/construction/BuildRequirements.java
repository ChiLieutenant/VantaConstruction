package com.chilieutenant.construction;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildRequirements {
    private static final String FILE_NAME = "build_requirements.yml";
    private static File file;
    private static FileConfiguration config;

    public static void init(Main plugin) {
        file = new File(plugin.getDataFolder(), FILE_NAME);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create build_requirements.yml");
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public static void addRequirements(String buildName, List<ItemStack> items) {
        List<Map<String, Object>> serializedItems = new ArrayList<>();
        for (ItemStack item : items) {
            serializedItems.add(item.serialize());
        }
        config.set(buildName, serializedItems);
        saveConfig();
    }

    public static List<ItemStack> getRequirements(String buildName) {
        List<Map<?, ?>> serializedItems = config.getMapList(buildName);
        List<ItemStack> items = new ArrayList<>();
        for (Map<?, ?> serializedItem : serializedItems) {
            items.add(ItemStack.deserialize((Map<String, Object>) serializedItem));
        }
        return items;
    }

    public static Map<String, List<ItemStack>> getAllRequirements() {
        Map<String, List<ItemStack>> allRequirements = new HashMap<>();
        for (String key : config.getKeys(false)) {
            allRequirements.put(key, getRequirements(key));
        }
        return allRequirements;
    }

    private static void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
