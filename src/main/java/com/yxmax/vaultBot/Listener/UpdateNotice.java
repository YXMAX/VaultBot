package com.yxmax.vaultBot.Listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static com.yxmax.vaultBot.Util.Util.*;

public class UpdateNotice implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(player.isOp()){
            if(getUpdateBool() && has_update){
                player.sendMessage(color(PREFIX + "&bThere is a newer version (" + newer_version + ") available:"));
                player.sendMessage(color("&e https://www.spigotmc.org/resources/vaultbot.120750/"));
            }
        }
    }
}
