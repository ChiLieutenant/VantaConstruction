package com.chilieutenant.construction;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.World;
import de.leonhard.storage.Json;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class Utils {

    public static Clipboard createConstruction(String schem) throws IOException {
        // Create construction
        Clipboard clipboard;
        File file = new File("plugins/VantaConstruction/schematics/" + schem + ".schem");

        ClipboardFormat format = ClipboardFormats.findByFile(file);
        assert format != null;
        try (ClipboardReader reader = format.getReader(Files.newInputStream(file.toPath()))) {
            clipboard = reader.read();
        }
        return clipboard;
    }

    public static Region getSelection(Player player) {
        com.sk89q.worldedit.entity.Player actor = BukkitAdapter.adapt(player);
        SessionManager manager = WorldEdit.getInstance().getSessionManager();
        LocalSession localSession = manager.get(actor);
        Region region; // declare the region variable
        World selectionWorld = localSession.getSelectionWorld();
        try {
            if (selectionWorld == null) throw new IncompleteRegionException();
            region = localSession.getSelection(selectionWorld);
            return region;
        } catch (IncompleteRegionException ex) {
            actor.printError(TextComponent.of("Please make a region selection first."));
            return null;
        }
    }

    public static void saveBuild(Player player, String name, Region region) throws IOException {
        // Save the build
        Json data = new Json(name, "plugins/VantaConstruction/builds");

        List<BlockSave> blocks = new ArrayList<>();

        Location loc = new Location(player.getWorld(), region.getCenter().x(), region.getMinimumPoint().y(), region.getCenter().z()).getBlock().getLocation();
        for (BlockVector3 blockVector3 : region) {
            Block block = player.getWorld().getBlockAt(blockVector3.x(), blockVector3.y(), blockVector3.z());
            if (!block.getType().isAir()) {
                Location subLoc = loc.clone().subtract(block.getLocation()).getBlock().getLocation();
                BlockSave bs = new BlockSave(subLoc.getX(), subLoc.getY(), subLoc.getZ(), block.getBlockData().getAsString(), block);
                blocks.add(bs);
            }
        }

        Main.plugin.getServer().getScheduler().runTaskAsynchronously(Main.plugin, () -> {
            for (int i = 0; i < blocks.size(); i++) {
                data.set(i + ".x", blocks.get(i).getX());
                data.set(i + ".y", blocks.get(i).getY());
                data.set(i + ".z", blocks.get(i).getZ());
                data.set(i + ".blockData", blocks.get(i).getBlockData());

                if(blocks.get(i).isSign()){
                    data.set(i + ".signData.frontLines", blocks.get(i).getSignData().getFrontLines());
                    data.set(i + ".signData.backLines", blocks.get(i).getSignData().getBackLines());
                }
            }
        });

    }

    public static void previewBuild(Player player, String name) {
        // Preview the build

        if(Utils.getBuildDisplayByPlayer(player) != null){
            player.sendMessage("§cZaten bir yapı önizlemesi açık.");
            return;
        }
        player.sendMessage("§aÖn gösterim yapılıyor: §2" + name);

        Json data = new Json(name, "plugins/VantaConstruction/builds");

        List<BlockSave> blocks = new ArrayList<>();
        for (String key : data.singleLayerKeySet()) {
            double x = data.getDouble(key + ".x");
            double y = data.getDouble(key + ".y");
            double z = data.getDouble(key + ".z");
            String blockData = data.getString(key + ".blockData");
            BlockSave bs = new BlockSave(x, y, z, blockData);
            if (bs.isSign()) {
                Object[] fL = data.getList(key + ".signData.frontLines").toArray();
                Object[] bL = data.getList(key + ".signData.backLines").toArray();
                String[] frontLines = Arrays.copyOf(fL, fL.length, String[].class);
                String[] backLines = Arrays.copyOf(bL, bL.length, String[].class);
                bs.setSignData(frontLines, backLines);
            }

            blocks.add(bs);
        }

        List<BuildBlock> buildBlocks = new ArrayList<>();
        for (BlockSave bs : blocks) {
            BuildBlock bd = new BuildBlock(player, getTargetedLocation(player, 10), bs);
            bd.createDisplay();
            buildBlocks.add(bd);
        }

        BuildDisplay display = new BuildDisplay(player, buildBlocks, name);
    }


    public static Location getTargetedLocation(final Player player, final double range, final boolean ignoreTempBlocks, final boolean checkDiagonals, final Material... nonOpaque2) {
        final Location origin = player.getEyeLocation();
        final Vector direction = origin.getDirection();

        final HashSet<Material> trans = new HashSet<Material>();
        trans.add(Material.AIR);
        trans.add(Material.CAVE_AIR);
        trans.add(Material.VOID_AIR);

        if (nonOpaque2 != null) {
            for (final Material material : nonOpaque2) {
                trans.add(material);
            }
        }

        final Location location = origin.clone();
        final Vector vec = direction.normalize().multiply(0.2);

        for (double i = 0; i < range; i += 0.2) {
            location.add(vec);

            if (checkDiagonals && checkDiagonalWall(location, vec)) {
                location.subtract(vec);
                break;
            }

            final Block block = location.getBlock();

            if (trans.contains(block.getType())) {
                continue;
            } else {
                location.subtract(vec);
                break;
            }
        }

        return location;
    }


    public static Location getTargetedLocation(final Player player, final double range, final boolean ignoreTempBlocks, final Material... nonOpaque2) {
        return getTargetedLocation(player, range, ignoreTempBlocks, true, nonOpaque2);
    }

    public static Location getTargetedLocation(final Player player, final double range, final Material... nonOpaque2) {
        return getTargetedLocation(player, range, false, nonOpaque2);
    }

    public static Location getTargetedLocation(final Player player, final int range) {
        return getTargetedLocation(player, range, false);
    }

    public static boolean checkDiagonalWall(final Location location, final Vector direction) {
        final boolean[] xyzsolid = {false, false, false};
        for (int i = 0; i < 3; i++) {
            double value;
            if (i == 0) {
                value = direction.getX();
            } else if (i == 1) {
                value = direction.getY();
            } else {
                value = direction.getZ();
            }
            final BlockFace face = getBlockFaceFromValue(i, value);
            if (face == null) {
                continue;
            }
            xyzsolid[i] = location.getBlock().getRelative(face).getType().isSolid();
        }
        final boolean a = xyzsolid[0] && xyzsolid[2];
        final boolean b = xyzsolid[0] && xyzsolid[1];
        final boolean c = xyzsolid[1] && xyzsolid[2];
        return (a || b || c || (a && b));
    }

    public static BlockFace getBlockFaceFromValue(final int xyz, final double value) {
        switch (xyz) {
            case 0:
                if (value > 0) {
                    return BlockFace.EAST;
                } else if (value < 0) {
                    return BlockFace.WEST;
                } else {
                    return BlockFace.SELF;
                }
            case 1:
                if (value > 0) {
                    return BlockFace.UP;
                } else if (value < 0) {
                    return BlockFace.DOWN;
                } else {
                    return BlockFace.SELF;
                }
            case 2:
                if (value > 0) {
                    return BlockFace.SOUTH;
                } else if (value < 0) {
                    return BlockFace.NORTH;
                } else {
                    return BlockFace.SELF;
                }
            default:
                return null;
        }
    }

    public static BuildDisplay getBuildDisplayByPlayer(Player player) {
        for (BuildDisplay display : BuildDisplay.displays) {
            if (display.getPlayer().equals(player)) {
                return display;
            }
        }
        return null;
    }

    public static Location rotateAroundPoint(Location locationToRotate, Location center, double angleInDegrees) {
        // Convert angle to radians
        double angleInRadians = Math.toRadians(angleInDegrees);

        // Translate the location to rotate so that the center point is at the origin
        Vector v = locationToRotate.toVector().subtract(center.toVector());

        // Perform the rotation
        double cosTheta = Math.cos(angleInRadians);
        double sinTheta = Math.sin(angleInRadians);
        double x = v.getX() * cosTheta - v.getZ() * sinTheta;
        double z = v.getX() * sinTheta + v.getZ() * cosTheta;

        // Translate back
        Vector rotated = new Vector(x, v.getY(), z).add(center.toVector());

        // Create and return the new rotated location
        return rotated.toLocation(locationToRotate.getWorld());
    }

    static boolean hasRequiredItems(Player player, List<ItemStack> requirements) {
        for (ItemStack required : requirements) {
            if (!player.getInventory().containsAtLeast(required, required.getAmount())) {
                return false;
            }
        }
        return true;
    }

    public static void setYOffset(String build, int offset) {
        Json data = new Json(build, "plugins/VantaConstruction/offsets");
        data.set("yOffset", offset);
    }

    public static int getYOffset(String build) {
        Json data = new Json(build, "plugins/VantaConstruction/offsets");
        return data.getInt("yOffset");
    }

    public static List<String> getAllBuilds() {
        List<String> builds = new ArrayList<>();
        File folder = new File("plugins/VantaConstruction/saves");
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    builds.add(file.getName().replace(".json", ""));
                }
            }
        }
        return builds;
    }

    public static int createID() {
        int i = 0;
        while (true) {
            if (new File("plugins/VantaConstruction/saves/" + i + ".json").exists()) {
                i++;
            } else {
                return i;
            }
        }
    }
}
