package com.yxmax.vaultBot.Util.Object;

import org.bukkit.inventory.Inventory;

public class PlayerVault {

    String uuid;

    Inventory inventory;

    public PlayerVault(String uuid, Inventory inventory){
        this.uuid = uuid;
        this.inventory = inventory;
    }

    public String getUuid() {
        return uuid;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
