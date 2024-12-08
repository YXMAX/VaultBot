package com.yxmax.vaultBot.DataBases;

import com.yxmax.vaultBot.Scheduler.MySQLScheduler;
import com.yxmax.vaultBot.Util.Object.PlayerVault;
import com.yxmax.vaultBot.Util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

import java.sql.*;

import static com.yxmax.vaultBot.VaultBot.*;

public class DataBases {

    public static void detectConnection(){
        if(plugin.getConfig().getBoolean("DataBases.MySQL")){
            isMySQL = true;
            con = getMySQLConnection();
            MysqlcreateTable();
            MySQLScheduler.start();
        } else {
            try {
                con = getConnection();
                createTable();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Connection con;

    public static Connection getConnection() throws Exception {
        SQLiteConfig config = new SQLiteConfig();
        config.setSharedCache(true);
        config.enableRecursiveTriggers(true);
        SQLiteDataSource ds = new SQLiteDataSource(config);
        String url = System.getProperty("user.dir");
        ds.setUrl("jdbc:sqlite:"+url+"/plugins/VaultBot/"+"Database.db");
        return ds.getConnection();
    }

    public static String allowPublicKeyRetrieval(){
        Boolean publickey = plugin.getConfig().getBoolean("DataBases.allowPublicKeyRetrieval");
        return publickey.toString();
    }

    public static String useSSL(){
        Boolean ssl = plugin.getConfig().getBoolean("DataBases.useSSL");
        return ssl.toString();
    }

    public static Connection getMySQLConnection() {
        String host = plugin.getConfig().getString("DataBases.host");
        String port = plugin.getConfig().getString("DataBases.port");
        String user = plugin.getConfig().getString("DataBases.user");
        String password = plugin.getConfig().getString("DataBases.password");
        String database = plugin.getConfig().getString("DataBases.database");
        try {
            Class.forName("com.mysql.jdbc.Driver");

            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?" + "allowPublicKeyRetrieval=" + allowPublicKeyRetrieval() + "&useSSL=" + useSSL();
            Connection connection = DriverManager.getConnection(url, user, password);
            if(Util.getLanguages().equals("zh-CN")){
                Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GRAY + "[VaultBot] " + ChatColor.YELLOW + "成功连接MySQL数据库");
            } else {
                Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GRAY + "[VaultBot] " + ChatColor.YELLOW + "Connect to MySQL database successfully.");
            }
            return connection;
        } catch (ClassNotFoundException | SQLException e) {
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GRAY + "[VaultBot] " + ChatColor.YELLOW + "Failed to connect to database. Check MySQL is enabled");
            e.printStackTrace();
            return null;
        }
    }

    public static void createTable(){
        try {
            String sql = "CREATE TABLE IF NOT EXISTS vaultbot_data (\n"
                    + "	uuid string,\n"
                    + "	inventory string\n"
                    + ");";
            Statement stat = null;
            stat = con.createStatement();
            stat.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void MysqlcreateTable(){
        try {
            String sql = "CREATE TABLE IF NOT EXISTS vaultbot_data"
                    + "("
                    + "uuid VARCHAR(255),"
                    + "inventory LONGTEXT"
                    + ");";
            Statement stat = null;
            stat = con.createStatement();
            stat.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void insert(String uuid, String inventory){
        try {
            String sql = "insert into vaultbot_data (uuid, inventory) values(?,?)";
            PreparedStatement pst = null;
            pst = con.prepareStatement(sql);
            pst.setString(1, uuid);
            pst.setString(2, inventory);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateInventory(String uuid, String inventory){
        String sql = "update vaultbot_data set inventory=? where uuid=?";
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(sql);
            pst.setString(1, inventory);
            pst.setString(2, uuid);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getInventory(String uuid){
        try {
            String invcode = null;
            String sql = "select inventory from vaultbot_data where uuid = ?";
            PreparedStatement pst = null;
            pst = con.prepareStatement(sql);
            pst.setString(1, uuid);
            ResultSet rs = pst.executeQuery();
            while(rs.next()){
                String invbase = rs.getString("inventory");
                invcode = invbase;
            }
            rs.close();
            pst.close();
            return invcode;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void uploadVault(boolean shutdown){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(shutdown){
                        if(Util.getLanguages().equals("zh-CN")){
                            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GRAY + "[VaultBot] " + ChatColor.YELLOW + "数据同步中..");
                        } else {
                            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GRAY + "[VaultBot] " + ChatColor.YELLOW + "Save vault temp..");
                        }
                    }
                    String sql = "update vaultbot_data set inventory = ? where uuid = ?";
                    PreparedStatement pst = null;
                    pst = con.prepareStatement(sql);
                    for(PlayerVault vault : VaultMap.values()){
                        pst.setString(1, Util.inventoryToBase64(vault.getInventory()));
                        pst.setString(2,vault.getUuid());
                        pst.addBatch();
                    }
                    con.setAutoCommit(false);
                    pst.executeBatch();
                    con.commit();
                    con.setAutoCommit(true);
                    pst.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                if(shutdown){
                    try {
                        con.close();
                        PluginDisabled();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    plugin = null;
                }
            }
        });
        thread.start();
    }

    public static boolean has(String uuid){
        try {
            String sql = "select uuid from vaultbot_data where uuid = ?";
            PreparedStatement pst = null;
            pst = con.prepareStatement(sql);
            pst.setString(1, uuid);
            ResultSet rs = pst.executeQuery();
            if(rs.next()){
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
