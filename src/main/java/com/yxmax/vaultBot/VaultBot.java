package com.yxmax.vaultBot;

import com.yxmax.vaultBot.Command.CommandExecute;
import com.yxmax.vaultBot.DataBases.DataBases;
import com.yxmax.vaultBot.Listener.PickupListener;
import com.yxmax.vaultBot.Listener.PlayerJoinListener;
import com.yxmax.vaultBot.Listener.UpdateNotice;
import com.yxmax.vaultBot.Listener.VaultListener;
import com.yxmax.vaultBot.Metric.Metrics;
import com.yxmax.vaultBot.Scheduler.UploadVaultScheduler;
import com.yxmax.vaultBot.Util.Object.PlayerVault;
import com.yxmax.vaultBot.Util.Util;
import net.jpountz.lz4.LZ4Factory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import space.arim.morepaperlib.MorePaperLib;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import static com.yxmax.vaultBot.Util.Util.checkUpdate;

public final class VaultBot extends JavaPlugin {

    public static VaultBot plugin;

    public static LZ4Factory factory;

    public static MorePaperLib FoliaLib;

    public static boolean isFolia = false;

    public static HashMap<UUID, PlayerVault> VaultMap = new HashMap<>();

    public static HashSet<String> LoreSet = new HashSet<>();

    public static HashSet<String> Blacklist_Material = new HashSet<>();

    public static HashSet<String> Blacklist_Lore = new HashSet<>();

    public static HashMap<UUID, Inventory> VaultTemp = new HashMap<>();

    public static HashMap<UUID, Boolean> isExitInventory = new HashMap<>();

    public static HashMap<String, File> LocaleMap = new HashMap<>();

    public static FileConfiguration LocaleConfig;

    public static boolean isMySQL = false;

    public static HashMap<Player,String> CheckPlayerTemp = new HashMap<>();

    public static boolean OpenSoundBool;

    public static String OpenSound = "UI_BUTTON_CLICK";

    public static float OpenSoundVolume = 1;

    public static float OpenSoundPitch = 1;

    public static boolean PickupSoundBool;

    public static String PickupSound = "ENTITY_PLAYER_LEVELUP";

    public static float PickupSoundVolume = 1;

    public static float PickupSoundPitch = 1;

    public static boolean PlaceEvent = false;

    public static boolean PickupEvent = false;

    @Override
    public void onEnable() {
        plugin = this;
        new Metrics(this, 23886);
        saveDefaultConfig();
        initCommand();
        initListeners();
        initLz4Factory();
        initMorePaperLib();
        Util.loadPrefix();
        Util.Update_Config();
        Util.DetectServerVersion();
        DataBases.detectConnection();
        Util.loadLoreSet();
        Util.loadBlacklist();
        Util.loadLocale();
        Util.loadSound();
        Util.loadPlace();
        Util.loadPickup();
        UploadVaultScheduler.start();
        PluginStartup();
        checkUpdate();
    }

    @Override
    public void onDisable() {
        DataBases.uploadVault(true);
    }

    protected void initMorePaperLib(){
        FoliaLib = new MorePaperLib(plugin);
        isFolia = FoliaLib.scheduling().isUsingFolia();
    }

    protected void initLz4Factory() {
        factory = LZ4Factory.fastestInstance();
    }

    protected void initCommand() {
        Bukkit.getPluginCommand("VaultBot").setExecutor(new CommandExecute());
    }

    protected void initListeners() {
        getServer().getPluginManager().registerEvents((Listener)new VaultListener(), (Plugin)this);
        getServer().getPluginManager().registerEvents((Listener)new PlayerJoinListener(), (Plugin)this);
        getServer().getPluginManager().registerEvents((Listener)new PickupListener(), (Plugin)this);
        getServer().getPluginManager().registerEvents((Listener)new UpdateNotice(), (Plugin)this);
    }

    public static void PluginStartup() {
        if(Util.getLanguages().equals("zh-CN")){
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "[VaultBot] 插件启动成功..");
            if(!isMySQL){
                Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "[VaultBot] 成功连接本地数据库");
            }
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "[VaultBot] 插件版本: v" + plugin.getDescription().getVersion());
        } else {
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "[VaultBot] Plugin enabled..");
            if(!isMySQL){
                Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "[VaultBot] Connect to local database successfully.");
            }
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "[VaultBot] Plugin version: v"  + plugin.getDescription().getVersion());
        }
        plugin.saveConfig();
    }

    public static void PluginDisabled() {
        if(Util.getLanguages().equals("zh-CN")){
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GRAY + "[VaultBot] " + ChatColor.YELLOW + "插件卸载中...");
            if(isMySQL){
                Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GRAY + "[VaultBot] " + ChatColor.YELLOW + "已断开与MySQL数据库连接");
            } else {
                Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GRAY + "[VaultBot] " + ChatColor.YELLOW + "已断开与本地数据库连接");
            }
        } else {
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GRAY + "[VaultBot] " + ChatColor.YELLOW + "Plugin disabled...");
            if(isMySQL){
                Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GRAY + "[VaultBot] " + ChatColor.YELLOW + "Disconnecting the MySQL databases...");
            } else {
                Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GRAY + "[VaultBot] " + ChatColor.YELLOW + "Disconnecting the local databases...");
            }
        }
        plugin.saveConfig();
    }
}
