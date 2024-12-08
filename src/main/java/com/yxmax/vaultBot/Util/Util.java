package com.yxmax.vaultBot.Util;

import com.yxmax.vaultBot.Enums.MsgEnums;
import com.yxmax.vaultBot.Scheduler.UpdateChecker;
import com.yxmax.vaultBot.Util.InvHolder.CheckVaultHolder;
import com.yxmax.vaultBot.Util.InvHolder.VaultHolder;
import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;
import net.jpountz.lz4.LZ4Compressor;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.yxmax.vaultBot.VaultBot.*;
import static org.bukkit.Bukkit.getServer;

public class Util {

    public static boolean isBelow116 = false;

    public static boolean isBelow113 = false;

    public static boolean is113 = false;

    static final String ConsolePrefix = "&7[VaultBot] ";

    public static String PREFIX;

    public static void loadPrefix(){
        PREFIX = plugin.getConfig().getString("Prefix");
    }

    public static String getMessages(MsgEnums enums){
        return LocaleConfig.getString("Messages." + enums.name());
    }

    public static String getMessages(MsgEnums enums, String replacement, String value){
        return LocaleConfig.getString("Messages." + enums.name()).replaceAll(replacement,value);
    }

    public static void sendMessages(CommandSender commandSender,MsgEnums enums,boolean only_player){
        if(commandSender instanceof Player){
            Player player = (Player) commandSender;
            player.sendMessage(color(PREFIX + Util.getMessages(enums)));
        } else if(!only_player){
            Bukkit.getConsoleSender().sendMessage(color(ConsolePrefix + Util.getMessages(enums)));
        } else {
            Bukkit.getConsoleSender().sendMessage(color(ConsolePrefix + Util.getMessages(MsgEnums.Only_player)));
        }
    }

    public static void sendMessages(Player player,MsgEnums enums){
        player.sendMessage(color(PREFIX + Util.getMessages(enums)));
    }

    public static void sendMessages(Player player,MsgEnums enums,String replacement,String value){
        player.sendMessage(color(PREFIX + Util.getMessages(enums).replaceAll(replacement,value)));
    }

    public static boolean isConsole(CommandSender commandSender){
        return !(commandSender instanceof Player);
    }

    public static String color(String message) {
        if(!isBelow116 || !isBelow113 || !is113) {
            Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
            Matcher matcher = pattern.matcher(message);
            while (matcher.find()) {
                String hexCode = message.substring(matcher.start(), matcher.end());
                String replaceSharp = hexCode.replace('#', 'x');

                char[] ch = replaceSharp.toCharArray();
                StringBuilder builder = new StringBuilder("");
                for (char c : ch) {
                    builder.append("&" + c);
                }

                message = message.replace(hexCode, builder.toString());
                matcher = pattern.matcher(message);
            }
            return ChatColor.translateAlternateColorCodes('&', message);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String compress(String str) throws IOException {
        byte[] decoded = Base64.getDecoder().decode(str);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        LZ4Compressor compressor = factory.fastCompressor();
        LZ4BlockOutputStream compressedOutput = new LZ4BlockOutputStream(out,2048,compressor);
        compressedOutput.write(decoded);
        compressedOutput.close();
        byte[] bytes = out.toByteArray();
        String encode = Base64.getEncoder().encodeToString(bytes);
        return encode;
    }

    public static String inventoryToBase64(Inventory inventory) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeInt(inventory.getSize());
            for (int i = 0; i < inventory.getSize(); i++) {
                dataOutput.writeObject(inventory.getItem(i));
            }
            dataOutput.close();
            String pre_encode = Base64.getEncoder().encodeToString(outputStream.toByteArray());
            String encode = compress(pre_encode);
            return encode;
            //Converts the inventory and its contents to base64, This also saves item meta-data and inventory type
        } catch (Exception e) {
            throw new IllegalStateException("Could not convert inventory to base64.", e);
        }
    }

    public static Inventory inventoryFromBase64(String data) {
        try {
            byte[] decoded = Base64.getDecoder().decode(data);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(decoded);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            LZ4BlockInputStream lz4Input = new LZ4BlockInputStream(inputStream, factory.fastDecompressor());
            byte[] buffer = new byte[1024];
            int count;
            while ((count = lz4Input.read(buffer)) != -1) {
                outputStream.write(buffer, 0, count);
            }
            ByteArrayInputStream inventoryInput = new ByteArrayInputStream(outputStream.toByteArray());
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inventoryInput);
            Inventory inventory = Bukkit.getServer().createInventory(new VaultHolder(), dataInput.readInt(), color(getVaultTitle()));
            // Read the serialized inventory
            for (int i = 0; i < inventory.getSize(); i++){
                ItemStack item = null;
                try {
                    item = (ItemStack) dataInput.readObject();
                } catch (IllegalArgumentException | IOException e){
                    Bukkit.getConsoleSender().sendMessage(color(PREFIX + " Convert an item failed! Set type Material.AIR to fix it! (may some module items disappeared! check your server module loader!)"));
                    continue;
                }
                if(item == null){
                    continue;
                }
                if(item.getType() == Material.AIR){
                    continue;
                }
                inventory.setItem(i, item);
            }
            dataInput.close();
            return inventory;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to decode the class type.", e);
        } catch (IOException e) {
            throw new RuntimeException("Unable to convert Inventory to Base64.", e);
        }
    }

    public static Inventory inventoryFromBase64_Check(String data) {
        try {
            byte[] decoded = Base64.getDecoder().decode(data);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(decoded);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            LZ4BlockInputStream lz4Input = new LZ4BlockInputStream(inputStream, factory.fastDecompressor());
            byte[] buffer = new byte[1024];
            int count;
            while ((count = lz4Input.read(buffer)) != -1) {
                outputStream.write(buffer, 0, count);
            }
            ByteArrayInputStream inventoryInput = new ByteArrayInputStream(outputStream.toByteArray());
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inventoryInput);
            Inventory inventory = Bukkit.getServer().createInventory(new CheckVaultHolder(), dataInput.readInt(), color(getVaultTitle()));
            // Read the serialized inventory
            for (int i = 0; i < inventory.getSize(); i++){
                ItemStack item = null;
                try {
                    item = (ItemStack) dataInput.readObject();
                } catch (IllegalArgumentException | IOException e){
                    Bukkit.getConsoleSender().sendMessage(color(PREFIX + " Convert an item failed! Set type Material.AIR to fix it! (may some module items disappeared! check your server module loader!)"));
                    continue;
                }
                if(item == null){
                    continue;
                }
                if(item.getType() == Material.AIR){
                    continue;
                }
                inventory.setItem(i, item);
            }
            dataInput.close();
            return inventory;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to decode the class type.", e);
        } catch (IOException e) {
            throw new RuntimeException("Unable to convert Inventory to Base64.", e);
        }
    }

    public static void DetectServerVersion(){
        String version = getServer().getClass().getPackage().getName();
        if(version.contains("v1_12") || version.contains("v1_11") || version.contains("v1_10") || version.contains("v1_9") || version.contains("v1_8") || version.contains("v1_7") || version.contains("v1_6")){
            isBelow113 = true;
        } else if(version.contains("v1_13")){
            is113 = true;
        }
    }

    public static String getVaultTitle(){
        return plugin.getConfig().getString("Inventory.title");
    }

    public static String getBasicEmptyVault(){
        Inventory inventory = Bukkit.createInventory(new VaultHolder(),36,color(getVaultTitle()));
        return inventoryToBase64(inventory);
    }

    public static void loadLoreSet(){
        List<String> list = plugin.getConfig().getStringList("Inventory.detected_lore");
        if(list.isEmpty()){
            LoreSet = new HashSet<>();
            return;
        }
        LoreSet = new HashSet<>(list);
    }

    public static void loadBlacklist(){
        List<String> list = plugin.getConfig().getStringList("Inventory.blacklist.material");
        Blacklist_Material = new HashSet<>(list);

        List<String> list_lore = plugin.getConfig().getStringList("Inventory.blacklist.lore");
        Blacklist_Lore = new HashSet<>(list_lore);
    }

    public static void loadLocale() {
        File CN_File = new File(plugin.getDataFolder() + "/locale/", "zh-CN.yml");
        File US_File = new File(plugin.getDataFolder() + "/locale/", "en-US.yml");
        if (!CN_File.exists()) {
            CN_File.getParentFile().mkdirs();
            plugin.saveResource("locale/zh-CN.yml", false);
            detectClientLanguage();
        }
        if (!US_File.exists()) {
            US_File.getParentFile().mkdirs();
            plugin.saveResource("locale/en-US.yml", false);
        }

        String path = plugin.getDataFolder().getAbsolutePath() + "\\locale";
        File file = new File(path);
        HashMap<String,File> temp_map = new HashMap<>();
        File[] array = file.listFiles();

        for (int i = 0; i < array.length; i++) {
            if (array[i].isFile()) {
                temp_map.put(array[i].getName(),array[i].getAbsoluteFile());
            }
        }
        LocaleMap = temp_map;
        reloadLocaleConfig();
    }

    public static String getLanguages(){
        return plugin.getConfig().getString("Language");
    }

    public static void detectClientLanguage(){
        Locale locale = Locale.getDefault();
        String client_language = locale.toLanguageTag();
        if(!client_language.equalsIgnoreCase("zh-CN")){
            plugin.getConfig().set("Language","en-US");
            plugin.saveConfig();
        }
    }

    public static void reloadLocaleConfig(){
        String file_lang = getLanguages();
        if(LocaleMap.containsKey(file_lang + ".yml")){
            File file = LocaleMap.get(file_lang + ".yml");
            LocaleConfig = new YamlConfiguration();
            try {
                LocaleConfig.load(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InvalidConfigurationException e) {
                throw new RuntimeException(e);
            }
        } else {
            File us_file = LocaleMap.get("en-US.yml");
            LocaleConfig = new YamlConfiguration();
            try {
                LocaleConfig.load(us_file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InvalidConfigurationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void loadSound(){
        OpenSoundBool = plugin.getConfig().getBoolean("Inventory.open_sound.enabled");
        String[] sound_detail = plugin.getConfig().getString("Inventory.open_sound.value").split(":");
        OpenSound = sound_detail[0];
        OpenSoundVolume = Float.parseFloat(sound_detail[1]);
        OpenSoundPitch = Float.parseFloat(sound_detail[2]);

        PickupSoundBool = plugin.getConfig().getBoolean("Inventory.pickup_sound.enabled");
        String[] Pickup_detail = plugin.getConfig().getString("Inventory.pickup_sound.value").split(":");
        PickupSound = Pickup_detail[0];
        PickupSoundVolume = Float.parseFloat(Pickup_detail[1]);
        PickupSoundPitch = Float.parseFloat(Pickup_detail[2]);
    }

    public static void playOpenSound(Player player){
        if(OpenSoundBool){
            player.playSound(player.getLocation(),Sound.valueOf(OpenSound),OpenSoundVolume,OpenSoundPitch);
        }
    }

    public static void playPickupSound(Player player){
        if(PickupSoundBool){
            player.playSound(player.getLocation(),Sound.valueOf(PickupSound),PickupSoundVolume,PickupSoundPitch);
        }
    }

    public static String checkItemName(ItemStack itemStack){
        if(itemStack.hasItemMeta()){
            if(itemStack.getItemMeta().hasDisplayName()){
                return itemStack.getItemMeta().getDisplayName();
            } else {
                return itemStack.getType().toString();
            }
        } else {
            return itemStack.getType().toString();
        }
    }

    public static void Update_Config(){
        boolean change = false;
        Configuration defaults = null;
        File config = new File(plugin.getDataFolder(),"/config.yml");
        FileConfiguration configuration = new YamlConfiguration();
        try {
            configuration.load(config);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
        try {
            InputStream resource = plugin.getResource("config.yml");
            File temp = new File(plugin.getDataFolder(),"/config.tmp");
            temp.createNewFile();
            FileUtils.copyInputStreamToFile(resource,temp);
            defaults = YamlConfiguration.loadConfiguration(temp);
            temp.delete();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (String defaultKey : defaults.getKeys(true)) {
            if (!configuration.contains(defaultKey)) {
                configuration.set(defaultKey, defaults.get(defaultKey));
                change = true;
            }
        }
        if (change) {
            try {
                configuration.save(config);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void loadPlace(){
        PlaceEvent = plugin.getConfig().getBoolean("Inventory.event.place");
    }

    public static void loadPickup(){
        PickupEvent = plugin.getConfig().getBoolean("Inventory.event.pickup");
    }

    public static boolean getUpdateBool(){
        return plugin.getConfig().getBoolean("Check-update");
    }

    public static boolean has_update = false;

    public static String newer_version;

    public static void checkUpdate(){
        if(!getUpdateBool()){
            return;
        }
        new UpdateChecker(plugin, 120750).getVersion(version -> {
            if (plugin.getDescription().getVersion().equals(version.replaceAll("v",""))) {
                Bukkit.getConsoleSender().sendMessage(color("&e" + PREFIX + "No new update available"));
            } else {
                has_update = true;
                newer_version = version;
                Bukkit.getConsoleSender().sendMessage(color("&e" + PREFIX + "An function update for VaultBot (" + version + ") is available at:"));
                Bukkit.getConsoleSender().sendMessage(color("&e" + PREFIX + "https://www.spigotmc.org/resources/vaultbot.120750/"));
            }
        });
    }
}
