package com.yxmax.vaultBot.Listener;

import com.yxmax.vaultBot.DataBases.DataBases;
import com.yxmax.vaultBot.Enums.MsgEnums;
import com.yxmax.vaultBot.Util.InvHolder.CheckVaultHolder;
import com.yxmax.vaultBot.Util.InvHolder.VaultHolder;
import com.yxmax.vaultBot.Util.Object.PlayerVault;
import com.yxmax.vaultBot.Util.Util;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

import static com.yxmax.vaultBot.Listener.PickupListener.isEmpty;
import static com.yxmax.vaultBot.Util.Util.*;
import static com.yxmax.vaultBot.VaultBot.*;

public class VaultListener implements Listener {


    @EventHandler
    public void onVauleDragEvent(InventoryDragEvent event) {
        if(event.getInventory().getHolder() instanceof VaultHolder) {
            if(event.getRawSlots().stream().min(Integer::compareTo).get() <= 35) {
                Player player = (Player) event.getWhoClicked();
                if(!PlaceEvent){
                    event.setCancelled(true);
                }
                if(!player.hasPermission("vaultbot.inventory.place")){
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onVaultClickEvent(InventoryClickEvent event){
        if(event.getInventory().getHolder() instanceof VaultHolder){
            Player player = (Player) event.getWhoClicked();
            switch(event.getAction()){
                case HOTBAR_SWAP:
                case HOTBAR_MOVE_AND_READD:
                    event.setCancelled(true);
                    break;

                case PLACE_ALL:
                case PLACE_ONE:
                case PLACE_SOME:
                case NOTHING:
                    if(event.getRawSlot() < event.getInventory().getSize()){
                        if(!PlaceEvent){
                            event.setCancelled(true);
                            break;
                        }
                        if(!player.hasPermission("vaultbot.inventory.place")){
                            event.setCancelled(true);
                            break;
                        }
                    }
                    return;

                case PICKUP_ALL:
                case PICKUP_ONE:
                case PICKUP_SOME:
                case PICKUP_HALF:
                case DROP_ALL_CURSOR:
                case DROP_ALL_SLOT:
                case DROP_ONE_CURSOR:
                case DROP_ONE_SLOT:
                    if(event.getRawSlot() < event.getInventory().getSize()){
                        if(!PickupEvent){
                            event.setCancelled(true);
                            break;
                        }
                        if(!player.hasPermission("vaultbot.inventory.pickup")){
                            event.setCancelled(true);
                            break;
                        }
                    }
                    return;

                case MOVE_TO_OTHER_INVENTORY:
                    if(event.getRawSlot() < event.getInventory().getSize()){
                        if(!PickupEvent){
                            event.setCancelled(true);
                            break;
                        }
                        if(!player.hasPermission("vaultbot.inventory.pickup")){
                            event.setCancelled(true);
                            break;
                        }
                    } else if(event.getRawSlot() >= event.getInventory().getSize()){
                        if(!PlaceEvent){
                            event.setCancelled(true);
                            break;
                        }
                        if(!player.hasPermission("vaultbot.inventory.place")){
                            event.setCancelled(true);
                            break;
                        }
                    }
                    return;

                case SWAP_WITH_CURSOR:
                case COLLECT_TO_CURSOR:
                case CLONE_STACK:
                    if(!PlaceEvent || !PickupEvent){
                        event.setCancelled(true);
                        break;
                    }
                    if(!player.hasPermission("vaultbot.inventory.place")){
                        event.setCancelled(true);
                        break;
                    }
                    if(!player.hasPermission("vaultbot.inventory.pickup")){
                        event.setCancelled(true);
                        break;
                    }
            }
        }
    }

    @EventHandler
    public void onSaveVaultTempEvent(InventoryClickEvent event){
        if (event.getInventory().getHolder() instanceof VaultHolder){
            Player player = (Player) event.getWhoClicked();
            VaultTemp.put(player.getUniqueId(),event.getInventory());
        }
    }

    @EventHandler
    public void onVaultCloseEvent(InventoryCloseEvent event){
        if (event.getInventory().getHolder() instanceof VaultHolder){
            Player player = (Player) event.getPlayer();
            isExitInventory.put(player.getUniqueId(),true);
            PlayerVault target = VaultMap.get(player.getUniqueId());
            target.setInventory(event.getInventory());
            return;
        }
        if (event.getInventory().getHolder() instanceof CheckVaultHolder){
            Player player = (Player) event.getPlayer();
            String[] target_detail = CheckPlayerTemp.get(player).split(":");
            if(target_detail[1].equals("map")){
                PlayerVault vault = VaultMap.get(UUID.fromString(target_detail[0]));
                vault.setInventory(event.getInventory());
                CheckPlayerTemp.remove(player);
                return;
            }
            if(target_detail[1].equals("database")){
                DataBases.updateInventory(target_detail[0],Util.inventoryToBase64(event.getInventory()));
                CheckPlayerTemp.remove(player);
                return;
            }
        }
    }

    @EventHandler
    public void onVaultBlackListEvent(InventoryClickEvent event){
        if (event.getInventory().getHolder() instanceof VaultHolder){
            if(event.getRawSlot() >= event.getInventory().getSize()){
                Player player = (Player) event.getWhoClicked();
                if(event.getCurrentItem() == null){
                    return;
                }
                if(Blacklist_Material.contains(event.getCurrentItem().getType().toString())){
                    event.setCancelled(true);
                    event.setResult(Event.Result.DENY);
                    Util.sendMessages(player, MsgEnums.Blacklist_notice);
                    return;
                }
                ItemStack item = event.getCurrentItem();
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
                for(String each_lore : Blacklist_Lore){
                    if(isEmpty(each_lore)){
                        continue;
                    }
                    if(lore_list.stream().anyMatch(str -> str.contains(each_lore))){
                        event.setCancelled(true);
                        event.setResult(Event.Result.DENY);
                        Util.sendMessages(player,MsgEnums.Blacklist_notice);
                        return;
                    }
                }
            }
        }
    }
}
