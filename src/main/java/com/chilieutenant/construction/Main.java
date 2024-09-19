package com.chilieutenant.construction;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.google.gson.*;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BlockState;
import de.leonhard.storage.Json;
import de.leonhard.storage.internal.FlatFile;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin implements Listener {

    public static Main plugin;
    public static ProtocolManager protocolManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        protocolManager = ProtocolLibrary.getProtocolManager();
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new BuildRequirementGUI(), this);
        getServer().getScheduler().runTaskTimer(this, () -> {
            List<BuildDisplay> displaysCopy = new ArrayList<>(BuildDisplay.displays);
            for (BuildDisplay display : displaysCopy) {
                if (BuildDisplay.displays.contains(display)) {
                    display.update();
                }
            }
        }, 0, 1);

        getServer().getScheduler().runTaskTimer(this, () -> {
            List<BuildConstruct> constructsCopy = new ArrayList<>(BuildConstruct.constructs);
            for (BuildConstruct buildConstruct : constructsCopy) {
                buildConstruct.update();
            }
        }, 0, 5);

        getCommand("construction").setExecutor(new ConstructionCommand());
        BuildRequirements.init(this);
        BuildConstruct.loadAll();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        BuildConstruct.saveAll();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            BuildDisplay bd = Utils.getBuildDisplayByPlayer(player);
            if (bd != null) {
                if(player.isSneaking()){
                    bd.remove();
                }else{
                    bd.createConstruction();
                }
            }
        }
    }

    @EventHandler
    public void onPlayerChangeSlot(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if(event.getPreviousSlot() != event.getNewSlot()){
            BuildDisplay bd = Utils.getBuildDisplayByPlayer(player);
            if (bd != null) {
                event.setCancelled(true);
                bd.rotate90Degrees();
            }
        }
    }
}
