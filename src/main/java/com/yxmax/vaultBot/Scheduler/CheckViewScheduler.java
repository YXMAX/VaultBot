package com.yxmax.vaultBot.Scheduler;

import com.yxmax.vaultBot.Util.InvHolder.VaultHolder;
import com.yxmax.vaultBot.Util.Object.PlayerVault;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import static com.yxmax.vaultBot.Util.Util.isBelow113;
import static com.yxmax.vaultBot.VaultBot.*;

public class CheckViewScheduler {

    public static void checkPlayerView(Player player){
        if(isFolia){
            isExitInventory.put(player.getUniqueId(),false);
            if(!VaultTemp.containsKey(player.getUniqueId())){
                VaultTemp.put(player.getUniqueId(),player.getOpenInventory().getTopInventory());
            }
            FoliaLib.scheduling().entitySpecificScheduler(player).runAtFixedRate(scheduledTask -> {
                if(!(player.getOpenInventory().getTopInventory().getHolder() instanceof VaultHolder)){
                    if(!isExitInventory.get(player.getUniqueId())){
                        Inventory last_inventory = VaultTemp.get(player.getUniqueId());
                        try {
                            PlayerVault target = VaultMap.get(player.getUniqueId());
                            target.setInventory(last_inventory);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        scheduledTask.cancel();
                    } else {
                        isExitInventory.remove(player.getUniqueId());
                        scheduledTask.cancel();
                    }
                }
            },null,2,6);
        } else {
            if(isBelow113){
                isExitInventory.put(player.getUniqueId(),false);
                if(!VaultTemp.containsKey(player.getUniqueId())){
                    VaultTemp.put(player.getUniqueId(),player.getOpenInventory().getTopInventory());
                }
                BukkitRunnable task = new BukkitRunnable() {
                    @Override
                    public void run() {
                        if(!(player.getOpenInventory().getTopInventory().getHolder() instanceof VaultHolder)){
                            if(!isExitInventory.get(player.getUniqueId())){
                                Inventory last_inventory = VaultTemp.get(player.getUniqueId());
                                try {
                                    PlayerVault target = VaultMap.get(player.getUniqueId());
                                    target.setInventory(last_inventory);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                                this.cancel();
                            } else {
                                isExitInventory.remove(player.getUniqueId());
                                this.cancel();
                            }
                        }
                    }
                };
                task.runTaskTimerAsynchronously(plugin,2,6);
            } else {
                isExitInventory.put(player.getUniqueId(),false);
                if(!VaultTemp.containsKey(player.getUniqueId())){
                    VaultTemp.put(player.getUniqueId(),player.getOpenInventory().getTopInventory());
                }
                Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, bukkitTask -> {
                    if(!(player.getOpenInventory().getTopInventory().getHolder() instanceof VaultHolder)){
                        if(!isExitInventory.get(player.getUniqueId())){
                            Inventory last_inventory = VaultTemp.get(player.getUniqueId());
                            try {
                                PlayerVault target = VaultMap.get(player.getUniqueId());
                                target.setInventory(last_inventory);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            bukkitTask.cancel();
                        } else {
                            isExitInventory.remove(player.getUniqueId());
                            bukkitTask.cancel();
                        }
                    }
                },2,6);
            }
        }
    }
}
