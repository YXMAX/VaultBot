package com.yxmax.vaultBot.Scheduler;

import com.yxmax.vaultBot.DataBases.DataBases;

import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.yxmax.vaultBot.DataBases.DataBases.con;

public class UploadVaultScheduler implements Runnable {

    public static void start(){
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
        executor.scheduleWithFixedDelay(
                new UploadVaultScheduler(),
                0,
                180,
                TimeUnit.SECONDS);
    }


    @Override
    public void run() {
        DataBases.uploadVault(false);
    }
}