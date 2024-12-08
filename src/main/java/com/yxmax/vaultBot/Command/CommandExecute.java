package com.yxmax.vaultBot.Command;

import com.yxmax.vaultBot.DataBases.DataBases;
import com.yxmax.vaultBot.Enums.MsgEnums;
import com.yxmax.vaultBot.Scheduler.CheckViewScheduler;
import com.yxmax.vaultBot.Util.Object.PlayerVault;
import com.yxmax.vaultBot.Util.Util;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.util.StringUtil;

import java.util.*;

import static com.yxmax.vaultBot.Util.Util.*;
import static com.yxmax.vaultBot.VaultBot.*;

public class CommandExecute implements CommandExecutor , TabExecutor {
    private List<String> Commands = new ArrayList<>(Arrays.asList("inventory","reload", "check"));

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        if(args.length == 1 && args[0].equalsIgnoreCase("inventory")){
            if(Util.isConsole(commandSender)){
                Util.sendMessages(commandSender, MsgEnums.Only_player,true);
                return true;
            }
            Player player = (Player) commandSender;
            if(player.hasPermission("vaultbot.command.inventory")){
                if(!VaultMap.containsKey(player.getUniqueId())){
                    Inventory inventory = inventoryFromBase64(DataBases.getInventory(player.getUniqueId().toString()));
                    VaultMap.put(player.getUniqueId(),new PlayerVault(player.getUniqueId().toString(),inventory));
                    player.openInventory(inventory);
                    CheckViewScheduler.checkPlayerView(player);
                    Util.sendMessages(player,MsgEnums.Open_inventory);
                    Util.playOpenSound(player);
                    return true;
                }
                Inventory inventory = VaultMap.get(player.getUniqueId()).getInventory();
                player.openInventory(inventory);
                CheckViewScheduler.checkPlayerView(player);
                Util.sendMessages(player,MsgEnums.Open_inventory);
                Util.playOpenSound(player);
                return true;
            } else {
                Util.sendMessages(player,MsgEnums.No_permission);
                return true;
            }
        }

        if(args.length == 1 && args[0].equalsIgnoreCase("reload")){
            if(commandSender instanceof Player){
                Player player = (Player) commandSender;
                if(!player.hasPermission("vaultbot.command.reload")){
                    sendMessages(player,MsgEnums.No_permission);
                    return true;
                }
            }
            plugin.reloadConfig();
            Util.Update_Config();
            Util.loadLoreSet();
            Util.loadBlacklist();
            Util.loadPrefix();
            Util.loadLocale();
            Util.loadSound();
            Util.loadPlace();
            Util.loadPickup();
            sendMessages(commandSender,MsgEnums.Reload,false);
            return true;
        }

        if(args.length >= 1 && args[0].equalsIgnoreCase("check")){
            if(Util.isConsole(commandSender)){
                Util.sendMessages(commandSender,MsgEnums.Only_player,true);
                return true;
            }
            Player player = (Player) commandSender;
            if(args.length == 1){
                Util.sendMessages(player,MsgEnums.Check_player_null);
                return true;
            }
            String target = args[1];
            OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(target);
            if(VaultMap.containsKey(offlineTarget.getUniqueId())){
                PlayerVault vault = VaultMap.get(offlineTarget.getUniqueId());
                player.openInventory(vault.getInventory());
                CheckPlayerTemp.put(player,offlineTarget.getUniqueId().toString() + ":" + "map");
                Util.sendMessages(player,MsgEnums.Check_success,"%check_player%",target);
                return true;
            } else {
                String target_string = DataBases.getInventory(offlineTarget.getUniqueId().toString());
                if(target_string == null){
                    Util.sendMessages(player,MsgEnums.Check_player_error);
                    return true;
                }
                Inventory target_vault = inventoryFromBase64_Check(target_string);
                player.openInventory(target_vault);
                CheckPlayerTemp.put(player,offlineTarget.getUniqueId().toString() + ":" + "database");
                Util.sendMessages(player,MsgEnums.Check_success,"%check_player%",target);
                return true;
            }
        }

        Util.sendMessages(commandSender,MsgEnums.Unknown_command,false);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        List<String> list = new ArrayList<>();
        switch(args.length){

            case 1:
                for (String cmd : Commands) {
                    if (!commandSender.hasPermission("vaultbot.command." + cmd)) continue;

                    list.add(cmd);
                }
                return StringUtil.copyPartialMatches(args[0].toLowerCase(), list, new ArrayList<String>());

            case 2:

                if (!commandSender.hasPermission("vaultbot.command.check")) return Collections.emptyList();

                Bukkit.getOnlinePlayers().forEach(player -> {list.add(player.getName());});
                return StringUtil.copyPartialMatches(args[1].toLowerCase(), list, new ArrayList<String>());
        }
        return Collections.emptyList();
    }
}
