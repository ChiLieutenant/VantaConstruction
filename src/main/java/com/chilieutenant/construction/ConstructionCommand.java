package com.chilieutenant.construction;

import com.sk89q.worldedit.regions.Region;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ConstructionCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            Player player = (Player) sender;

            if(!player.hasPermission("construction.use")){
                player.sendMessage("§cYou don't have permission to use this command.");
                return false;
            }

            if(args.length == 0){
                //UI aç
                BuildRequirementGUI.openGUI(player);
                return true;
            }

            if(args.length == 2){
                if(args[0].equalsIgnoreCase("save")){
                    if(!player.hasPermission("construction.save")){
                        player.sendMessage("§cYou don't have permission to use this command.");
                        return false;
                    }
                    Region selection = Utils.getSelection(player);
                    if(selection == null){
                        player.sendMessage("§cYou need to select an area first.");
                        return false;
                    }
                    try {
                        Utils.saveBuild(player, args[1], selection);
                        player.sendMessage("§aStructure successfully saved: " + args[1]);
                    } catch (Exception e) {
                        player.sendMessage("§cAn error occurred. Check the console.");
                        e.printStackTrace();
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("setrequirements")) {
                    if(!player.hasPermission("construction.requirements")){
                        player.sendMessage("§cYou don't have permission to use this command.");
                        return false;
                    }
                    String buildName = args[1];
                    List<ItemStack> requirements = new ArrayList<>();
                    for (ItemStack item : player.getInventory().getContents()) {
                        if (item != null && item.getType() != Material.AIR) {
                            requirements.add(item.clone());
                        }
                    }
                    BuildRequirements.addRequirements(buildName, requirements);
                    player.sendMessage("§aRequired items added for this structure: §2" + buildName);
                    return true;
                }

            }else if (args.length == 3){
                if (args[0].equalsIgnoreCase("offset")) {
                    if(!player.hasPermission("construction.offset")){
                        player.sendMessage("§cYou don't have permission to use this command.");
                        return false;
                    }
                    int offset = Integer.parseInt(args[2]);
                    Utils.setYOffset(args[1], offset);
                    player.sendMessage("§aOffset set: §2" + args[1] + ": " + offset);
                    return true;
                }
            }

        }


        return false;
    }
}
