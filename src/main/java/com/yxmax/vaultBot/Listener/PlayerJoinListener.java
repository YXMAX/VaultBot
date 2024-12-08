package com.yxmax.vaultBot.Listener;

import com.yxmax.vaultBot.DataBases.DataBases;
import com.yxmax.vaultBot.Scheduler.PlayerJoinScheduler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = (Player) event.getPlayer();
        PlayerJoinScheduler.detectInsert(player);
    }
}
