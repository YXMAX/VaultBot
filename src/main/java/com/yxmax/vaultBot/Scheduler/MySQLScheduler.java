package com.yxmax.vaultBot.Scheduler;

import com.yxmax.vaultBot.DataBases.DataBases;

import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.yxmax.vaultBot.DataBases.DataBases.con;

public class MySQLScheduler implements Runnable {

    public static void start(){
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleWithFixedDelay(
                new MySQLScheduler(),
                0,
                60,
                TimeUnit.SECONDS);
    }


    @Override
    public void run() {
        try {
            if (con != null && !con.isClosed()) {
                con.createStatement().execute("SELECT 1");
            }
        } catch (SQLException e) {
            con = DataBases.getMySQLConnection();
        }
    }
}