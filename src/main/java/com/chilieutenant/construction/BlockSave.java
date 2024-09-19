package com.chilieutenant.construction;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.sign.Side;

public class BlockSave {

    private double x;
    private double y;
    private double z;
    private String blockData;
    private SignData signData;

    public BlockSave(double x, double y, double z, String blockData) {
        // Create a new BlockSave
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockData = blockData;

    }

    public BlockSave(double x, double y, double z, String blockData, Block block) {
        // Create a new BlockSave
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockData = blockData;
        if(block.getType().toString().contains("SIGN")){
            Sign sign = (Sign) block.getState();
            this.signData = new SignData(sign.getSide(Side.FRONT).getLines(), sign.getSide(Side.BACK).getLines());
        }
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public String getBlockData() {
        return blockData;
    }

    public boolean isSign(){
        BlockData bd = Bukkit.createBlockData(blockData);
        if(bd.getMaterial().toString().contains("SIGN")){
            return true;
        }
        return false;
    }

    public SignData getSignData() {
        return signData;
    }

    public void setSignData(String[] frontLines, String[] backLines) {
        this.signData = new SignData(frontLines, backLines);
    }
}
