package com.chilieutenant.construction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.type.Sign;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import org.joml.Vector3f;
import org.joml.Quaternionf;
import java.util.ArrayList;
import java.util.List;

public class BuildBlock {

    private Location origin;
    private double x;
    private double y;
    private double z;
    private BlockData blockData;
    private int entityId;
    private int rotation = 0;
    private Player player;
    private BlockDisplay blockDisplay;
    private BlockSave blockSave;

    public BuildBlock(Player player, Location origin, BlockSave bs) {
        this.origin = origin;
        this.player = player;
        this.x = bs.getX();
        this.y = bs.getY();
        this.z = bs.getZ();
        this.blockData = Bukkit.createBlockData(bs.getBlockData());
        this.blockSave = bs;
    }

    public void rotate90Degrees() {
        rotation = (rotation + 90) % 360;
        double temp = x;
        x = -z;
        z = temp;

        this.blockData = rotateBlockData(this.blockData);
        moveDisplay(origin);
    }

    public void createDisplay() {
        Location loc = origin.clone().subtract(x, y, z);
        // Create a new block display entity
        blockDisplay = (BlockDisplay) loc.getWorld().spawnEntity(loc, EntityType.BLOCK_DISPLAY);
        this.entityId = blockDisplay.getEntityId();

        // Set the block data for the display
        blockDisplay.setBlock(this.blockData);
        blockDisplay.setGlowing(true);
        // Set additional properties if needed
        blockDisplay.setTransformation(new Transformation(
            new Vector3f(0, 0, 0),  // translation
            new Quaternionf(),     // left rotation
            new Vector3f(1, 1, 1), // scale
            new Quaternionf()      // right rotation
        ));

        // Make the display visible only to the player
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer != this.player) {
                onlinePlayer.hideEntity(Main.plugin, blockDisplay);
            }
        }


        
    }


    public BlockSave getBlockSave() {
        return blockSave;
    }


    public void moveDisplay(Location newOrigin) {
        Location loc = newOrigin.clone().subtract(x, y, z);

        blockDisplay.teleport(loc);

        this.origin = newOrigin;
    }


    public void removeDisplay() {
        blockDisplay.remove();
    }


    public void setGlowing(boolean glowing) {
        blockDisplay.setGlowing(glowing);
    }

    public void changeColor(Color color) {
        blockDisplay.setGlowColorOverride(color);
    }
    public Location getDisplayLocation() {
        return origin.clone().subtract(x, y, z);
    }

    public BlockData getBlockData() {
        return blockData;
    }

    private BlockData rotateBlockData(BlockData data) {
        BlockData rotatedData = Bukkit.createBlockData(data.getAsString());
        if (rotatedData instanceof Directional) {
            Directional directional = (Directional) rotatedData;
            BlockFace face = directional.getFacing();
            directional.setFacing(rotateBlockFace(face));
        } else if (rotatedData instanceof Rotatable) {
            Rotatable rotatable = (Rotatable) rotatedData;
            BlockFace face = rotatable.getRotation();
            rotatable.setRotation(rotateBlockFace(face));
        }

        // Special handling for stairs
        if (rotatedData instanceof Stairs) {
            Stairs stairs = (Stairs) rotatedData;
            stairs.setShape(rotateStairShape(stairs.getShape()));
        }

        return rotatedData;
    }

    private BlockFace rotateBlockFace(BlockFace face) {
        switch (face) {
            case NORTH: return BlockFace.EAST;
            case EAST: return BlockFace.SOUTH;
            case SOUTH: return BlockFace.WEST;
            case WEST: return BlockFace.NORTH;
            default: return face;
        }
    }

    private Stairs.Shape rotateStairShape(Stairs.Shape shape) {
        switch (shape) {
            case INNER_LEFT: return Stairs.Shape.INNER_RIGHT;
            case INNER_RIGHT: return Stairs.Shape.INNER_LEFT;
            case OUTER_LEFT: return Stairs.Shape.OUTER_RIGHT;
            case OUTER_RIGHT: return Stairs.Shape.OUTER_LEFT;
            default: return shape;
        }
    }


}
