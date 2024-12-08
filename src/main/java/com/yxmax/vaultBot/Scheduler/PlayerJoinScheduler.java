package com.yxmax.vaultBot.Scheduler;

import com.yxmax.vaultBot.DataBases.DataBases;
import com.yxmax.vaultBot.Util.InvHolder.VaultHolder;
import com.yxmax.vaultBot.Util.Object.PlayerVault;
import com.yxmax.vaultBot.Util.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import static com.yxmax.vaultBot.Util.Util.color;
import static com.yxmax.vaultBot.Util.Util.inventoryFromBase64;
import static com.yxmax.vaultBot.VaultBot.*;

public class PlayerJoinScheduler{

    public static void detectInsert(Player player){
        if(isFolia){
            FoliaLib.scheduling().asyncScheduler().run(new Runnable() {
                @Override
                public void run() {
                    if(!DataBases.has(player.getUniqueId().toString())){
                        DataBases.insert(player.getUniqueId().toString(), Util.getBasicEmptyVault());
                        VaultMap.put(player.getUniqueId(),new PlayerVault(player.getUniqueId().toString(), Bukkit.createInventory(new VaultHolder(),36,color(Util.getVaultTitle()))));
                    }
                    if(!VaultMap.containsKey(player.getUniqueId())){
                        Inventory inventory = inventoryFromBase64(DataBases.getInventory(player.getUniqueId().toString()));
                        VaultMap.put(player.getUniqueId(),new PlayerVault(player.getUniqueId().toString(),inventory));
                    }
                }
            });
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    if(!DataBases.has(player.getUniqueId().toString())){
                        DataBases.insert(player.getUniqueId().toString(), Util.getBasicEmptyVault());
                        VaultMap.put(player.getUniqueId(),new PlayerVault(player.getUniqueId().toString(), Bukkit.createInventory(new VaultHolder(),36,color(Util.getVaultTitle()))));
                    }
                    if(!VaultMap.containsKey(player.getUniqueId())){
                        Inventory inventory = inventoryFromBase64(DataBases.getInventory(player.getUniqueId().toString()));
                        VaultMap.put(player.getUniqueId(),new PlayerVault(player.getUniqueId().toString(),inventory));
                    }
                }
            });
        }
    }
}
