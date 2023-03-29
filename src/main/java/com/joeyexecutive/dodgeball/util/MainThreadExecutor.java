package com.joeyexecutive.dodgeball.util;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

/**
 * Executor which runs tasks on the Bukkit main thread, used in our CompleteableFutures
 */
public final class MainThreadExecutor implements Executor {

    public static final MainThreadExecutor MAIN_THREAD_EXECUTOR = new MainThreadExecutor();

    private final int tickDelay;

    private MainThreadExecutor() {
        this.tickDelay = 0;
    }

    private MainThreadExecutor(int tickDelay) {
        this.tickDelay = tickDelay;
    }

    public static MainThreadExecutor newDelayedExecutor(int tickDelay) {
        return new MainThreadExecutor(tickDelay);
    }

    @Override
    public void execute(@NotNull Runnable runnable) {
        if (Bukkit.isPrimaryThread()) {
            runnable.run();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        Runnable task = () -> {
            runnable.run();
            latch.countDown();
        };

        if (tickDelay == 0) {
            BukkitTasks.sync(task);
        } else {
            BukkitTasks.syncLater(tickDelay, task);
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace(); // if this happens, something is very very very very wrong
        }
    }
}
