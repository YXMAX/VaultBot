package com.yxmax.vaultBot.Listener;

import com.yxmax.vaultBot.DataBases.DataBases;
import com.yxmax.vaultBot.Enums.MsgEnums;
import com.yxmax.vaultBot.Scheduler.CheckViewScheduler;
import com.yxmax.vaultBot.Util.Object.PlayerVault;
import com.yxmax.vaultBot.Util.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static com.yxmax.vaultBot.Util.Util.checkItemName;
import static com.yxmax.vaultBot.Util.Util.inventoryFromBase64;
import static com.yxmax.vaultBot.VaultBot.*;
import static com.yxmax.vaultBot.VaultBot.VaultMap;

public class PickupListener implements Listener {

    @EventHandler
    public void onPlayerPickupItemEvent(EntityPickupItemEvent event){
        if(event.getEntityType() == EntityType.PLAYER){
            Player player = (Player) event.getEntity();
            ItemStack item = event.getItem().getItemStack();
            if(!item.hasItemMeta()){
                return;
            }
            if(!item.getItemMeta().hasLore()){
                return;
            }
            List<String> lore_list = item.getItemMeta().getLore();
            if(lore_list.isEmpty()){
                return;
            }
            for(String each_lore : LoreSet){
                if(isEmpty(each_lore)){
                    continue;
                }
                if(lore_list.stream().anyMatch(str -> str.contains(each_lore))){
                    event.setCancelled(true);
                    event.getItem().remove();
                    if(!VaultMap.containsKey(player.getUniqueId())){
                        Inventory inventory = inventoryFromBase64(DataBases.getInventory(player.getUniqueId().toString()));
                        VaultMap.put(player.getUniqueId(),new PlayerVault(player.getUniqueId().toString(),inventory));
                    }
                    PlayerVault vault = VaultMap.get(player.getUniqueId());
                    vault.getInventory().addItem(item);
                    Util.sendMessages(player,MsgEnums.Pickup_success,"%item%",checkItemName(item));
                    Util.playPickupSound(player);
                    return;
                }
            }
        }
    }


    public static boolean isEmpty(String str) {
        if(str != null && !str.replace(" ", "").equals("")){
            return false;
        }
        else {
            return true;
        }
    }
}
