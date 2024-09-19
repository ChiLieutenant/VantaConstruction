package com.chilieutenant.construction;

import de.leonhard.storage.Json;
import it.unimi.dsi.fastutil.Hash;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.sign.Side;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildConstruct {

    private HashMap<Block, BlockData> blocks = new HashMap<>();
    private HashMap<Block, SignData> blockSign = new HashMap<>();
    private Json data;
    private int id;
    public static List<BuildConstruct> constructs = new ArrayList<>();

    public BuildConstruct() {}

    public BuildConstruct(int id){
        // Create a new BuildConstruct
        this.id = id;
        this.data = new Json(String.valueOf(id), "plugins/VantaConstruction/saves");
    }

    public BuildConstruct(HashMap<Block, BlockData> blocks, HashMap<Block, SignData> blockSign){
        // Create a new BuildConstruct
        this.blocks = blocks;
        this.blockSign = blockSign;
        this.id = Utils.createID();
        this.data = new Json(String.valueOf(id), "plugins/VantaConstruction/saves");
        constructs.add(this);
    }

    public HashMap<Block, BlockData> getBlocks() {
        return blocks;
    }

    // Setter for blocks (needed for deserialization)
    public void setBlocks(HashMap<Block, BlockData> blocks) {
        this.blocks = blocks;
    }



    public void update(){
        if(blocks.isEmpty()){
            constructs.remove(this);
            if(data.getFile().exists()) data.getFile().delete();
            return;
        }
        if(getLowestBlocks().isEmpty()) return;

        Block block = getRandomBlockFromLowest();
        block.getWorld().playSound(block.getLocation(), blocks.get(block).getSoundGroup().getPlaceSound(), 1, 1);
        placeBlock(block);
    }

    public Block getRandomBlockFromLowest(){
        List<Block> lowestBlocks = getLowestBlocks();
        return lowestBlocks.get((int) (Math.random() * lowestBlocks.size()));
    }

    public int getLowestY(){
        Map.Entry<Block,BlockData> entry = blocks.entrySet().iterator().next();
        int lowest = entry.getKey().getY();
        for(Block block : blocks.keySet()){
            if(block.getY() < lowest){
                lowest = block.getY();
            }
        }
        return lowest;
    }

    public List<Block> getLowestBlocks(){
        int lowest = getLowestY();
        List<Block> lowestBlocks = new ArrayList<>();
        for(Block block : blocks.keySet()){
            if(block.getY() == lowest){
                lowestBlocks.add(block);
            }
        }
        return lowestBlocks;
    }

    public void placeBlock(Block block){
        block.setBlockData(blocks.get(block));
        if(blockSign.containsKey(block)){
            Sign sign = (Sign) block.getState();
            sign.getSide(Side.FRONT).setLine(0, blockSign.get(block).getFrontLines()[0]);
            sign.getSide(Side.FRONT).setLine(1, blockSign.get(block).getFrontLines()[1]);
            sign.getSide(Side.FRONT).setLine(2, blockSign.get(block).getFrontLines()[2]);
            sign.getSide(Side.FRONT).setLine(3, blockSign.get(block).getFrontLines()[3]);
            sign.getSide(Side.BACK).setLine(0, blockSign.get(block).getBackLines()[0]);
            sign.getSide(Side.BACK).setLine(1, blockSign.get(block).getBackLines()[1]);
            sign.getSide(Side.BACK).setLine(2, blockSign.get(block).getBackLines()[2]);
            sign.getSide(Side.BACK).setLine(3, blockSign.get(block).getBackLines()[3]);
            sign.update();
        }
        blocks.remove(block);
    }

    public void saveBlockData() {
        for (Map.Entry<Block, BlockData> entry : blocks.entrySet()) {
            Block block = entry.getKey();
            BlockData blockData = entry.getValue();

            String key = block.getWorld().getName() + "," + block.getX() + "," + block.getY() + "," + block.getZ();
            data.set(key + ".data", blockData.getAsString());
            if(blockSign.containsKey(block)){
                data.set(key + ".signData.frontLines", blockSign.get(block).getFrontLines());
                data.set(key + ".signData.backLines", blockSign.get(block).getBackLines());
            }
        }
    }

    public void loadBlockData() {
        blocks.clear();

        for (String key : data.singleLayerKeySet()) {
            String[] parts = key.split(",");
            if (parts.length != 4) continue;

            String worldName = parts[0];
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int z = Integer.parseInt(parts[3]);

            Block block = Bukkit.getWorld(worldName).getBlockAt(x, y, z);
            String bdata = data.getString(key + ".data");
            BlockData blockData = Bukkit.createBlockData(bdata);;
            blocks.put(block, blockData);
            if(data.contains(key + ".signData.frontLines")){
                SignData signData = new SignData((String[]) data.get(key + ".signData.frontLines"), (String[]) data.get(key + ".signData.backLines"));
                blockSign.put(block, signData);
            }
        }
        constructs.add(this);
    }

    public static void saveAll(){
        if(constructs.isEmpty()) return;
        for(BuildConstruct construct : constructs){
            construct.saveBlockData();
        }
    }

    public static void loadAll(){
        constructs.clear();
        if(Utils.getAllBuilds().isEmpty()) return;
        for(String key : Utils.getAllBuilds()){
            BuildConstruct construct = new BuildConstruct(Integer.parseInt(key));
            construct.loadBlockData();
        }
    }
}
