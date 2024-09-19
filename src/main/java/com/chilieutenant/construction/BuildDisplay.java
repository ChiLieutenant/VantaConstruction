package com.chilieutenant.construction;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BuildDisplay {

    private List<BuildBlock> blocks = new ArrayList<>();
    private Player player;
    private long time;
    private int rotation = 0;
    private boolean isRemoved = false;
    private String buildName;
    private boolean isReady = false;

    public static List<BuildDisplay> displays = new ArrayList<>();

    public BuildDisplay(Player player, List<BuildBlock> blocks, String buildName){
        // Create a new BuildDisplay
        this.blocks = blocks;
        this.player = player;
        this.time = System.currentTimeMillis();
        this.buildName = buildName;
        displays.add(this);
    }

    public void rotate90Degrees() {
        rotation = (rotation + 90) % 360;
        for (BuildBlock block : blocks) {
            block.rotate90Degrees();
        }
    }


    public void update(){
        // Update the display
        boolean isAir = true;

        if(!player.isOnline() && player.isDead()){
            isRemoved = true;
        }

        if(isRemoved){
            displays.remove(this);
            return;
        }

        if(System.currentTimeMillis() - time > 30000){
            displays.remove(this);
            for(BuildBlock block : blocks){
                block.removeDisplay();
            }
        }

        if(isReady || isRemoved) return;
        for (BuildBlock block : blocks) {
            block.moveDisplay(Utils.getTargetedLocation(player, 10).getBlock().getRelative(BlockFace.DOWN, Utils.getYOffset(buildName)).getLocation());
            if(block.getDisplayLocation().getBlock().getType() != Material.AIR && block.getDisplayLocation().getY() - getLowestY() >= Utils.getYOffset(buildName)){
                isAir = false;
            }
        }

        for(BuildBlock block : blocks){
            if(isAir){
                block.changeColor(Color.LIME);
            }else{
                block.changeColor(Color.RED);
            }
        }


    }

    public Player getPlayer() {
        return player;
    }

    public void createConstruction(){
        // Create a new Construction
        boolean isAir = true;
        for (BuildBlock block : blocks) {
            if(block.getDisplayLocation().getBlock().getType() != Material.AIR && block.getDisplayLocation().getY() - getLowestY() >= Utils.getYOffset(buildName)){
                isAir = false;
            }
        }

        if(!isAir){
            player.sendMessage("§cYou can't place the building here.");
            return;
        }

        // Check if the player has permission to build in this region using WorldGuard
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(player.getWorld()));
        Location loc = blocks.get(0).getDisplayLocation();
        ApplicableRegionSet set = regions.getApplicableRegions(BukkitAdapter.asBlockVector(loc));
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        if (!set.testState(localPlayer, Flags.BUILD)) {
            player.sendMessage("§cYou don't have permission to build in this area.");
            return;
        }

        List<ItemStack> requirements = BuildRequirements.getRequirements(buildName);
        if (Utils.hasRequiredItems(player, requirements)) {
            if(isReady){
                HashMap<Block, BlockData> blockConstruct = new HashMap<>();
                HashMap<Block, SignData> blockSign = new HashMap<>();
                for (BuildBlock block : blocks) {
                    blockConstruct.put(block.getDisplayLocation().getBlock(), block.getBlockData());
                    if(block.getBlockSave().isSign()){
                        blockSign.put(block.getDisplayLocation().getBlock(), block.getBlockSave().getSignData());
                    }
                    block.removeDisplay();
                }
                removeRequiredItems(player, requirements);
                new BuildConstruct(blockConstruct, blockSign);
                isRemoved = true;
            }else{
                for (BuildBlock block : blocks) {
                    block.setGlowing(false);
                }
                isReady = true;
                player.sendTitle("§7Are you sure about the building placement?", "§aLeft-click to build.", 10, 70, 20);
            }
        } else {
            player.sendMessage("§cYou don't have enough items to build this structure.");
        }
    }

    private void removeRequiredItems(Player player, List<ItemStack> requirements) {
        for (ItemStack required : requirements) {
            player.getInventory().removeItem(required);
        }
    }

    public int getLowestY(){
        // Get the lowest Y value
        int lowest = 256;
        for (BuildBlock block : blocks) {
            if(block.getDisplayLocation().getBlockY() < lowest){
                lowest = block.getDisplayLocation().getBlockY();
            }
        }
        return lowest;
    }

    public boolean isRemoved() {
        return isRemoved;
    }

    public void remove(){
        // Remove the BuildDisplay
        isRemoved = true;
        for (BuildBlock block : blocks) {
            block.removeDisplay();
        }
    }
}
