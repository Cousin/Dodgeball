package com.joeyexecutive.dodgeball.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Utils for scheduling bukkit tasks
 */
public final class BukkitTasks {

    private static Plugin plugin;

    public static void init(Plugin suppliedPlugin) {
        plugin = suppliedPlugin;
    }

    private BukkitTasks() {}

    /**
     * @return an already canceled empty BukkitTask
     */
    public static BukkitTask canceledTask() {
        BukkitTask bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {

            }
        }.runTask(plugin);
        bukkitTask.cancel();
        return bukkitTask;
    }

    /**
     * Count down from the start number of seconds until zero
     * @param start The seconds to start the countdown from
     * @param consumer A consumer to call with the number of seconds left every second
     * @return The created {@link BukkitTask}
     */
    public static BukkitTask secondsCountdown(int start, Consumer<Integer> consumer) {
        return secondsCountdown(plugin, start, 20L, consumer);
    }

    /**
     * Count down from the start number of seconds until zero
     * @param plugin The plugin to run the task under
     * @param start The seconds to start the countdown from
     * @param delay The delay between each 'second'
     * @param consumer A consumer to call with the number of seconds left every second
     * @return The created {@link BukkitTask}
     */
    public static BukkitTask secondsCountdown(Plugin plugin, int start, long delay, Consumer<Integer> consumer) {
        final AtomicInteger secondsLeft = new AtomicInteger(start);
        return new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    consumer.accept(secondsLeft.getAndDecrement());
                } finally {
                    if (secondsLeft.get() == -1) {
                        cancel();
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, delay);
    }

    /**
     * Run a sync task
     * @param runnable What to run
     * @return The created {@link BukkitTask}
     */
    public static BukkitTask sync(Runnable runnable) {
        return sync(plugin, runnable);
    }

    /**
     * Run a sync task
     * @param plugin The plugin to run the task under
     * @param runnable What to run
     * @return The created {@link BukkitTask}
     */
    public static BukkitTask sync(Plugin plugin, Runnable runnable) {
        return Bukkit.getScheduler().runTask(plugin, runnable);
    }

    /**
     * Run a sync task on a timer with a 0L wait
     * @param runnable What to run
     * @param period The time between executions
     * @return The created {@link BukkitTask}
     */
    public static BukkitTask syncTimer(long period, Runnable runnable) {
        return syncTimer(plugin, period, runnable);
    }

    /**
     * Run a sync task on a timer with a 0L wait
     * @param plugin The plugin to run the task under
     * @param runnable What to run
     * @param period The time between executions
     * @return The created {@link BukkitTask}
     */
    public static BukkitTask syncTimer(Plugin plugin, long period, Runnable runnable) {
        return syncTimer(plugin, 0L, period, runnable);
    }

    /**
     * Run a sync task on a timer
     * @param runnable What to run
     * @param wait How long to wait before starting
     * @param period The time between executions
     * @return The created {@link BukkitTask}
     */
    public static BukkitTask syncTimer(long wait, long period, Runnable runnable) {
        return syncTimer(plugin, wait, period, runnable);
    }

    /**
     * Run a sync task on a timer
     * @param plugin The plugin to run the task under
     * @param runnable What to run
     * @param wait How long to wait before starting
     * @param period The time between executions
     * @return The created {@link BukkitTask}
     */
    public static BukkitTask syncTimer(Plugin plugin, long wait, long period, Runnable runnable) {
        return Bukkit.getScheduler().runTaskTimer(plugin, runnable, wait, period);
    }

    /**
     * Run a sync task later
     * @param runnable What to run
     * @param wait How long to wait before execution
     * @return The created {@link BukkitTask}
     */
    public static BukkitTask syncLater(long wait, Runnable runnable) {
        return syncLater(plugin, wait, runnable);
    }

    /**
     * Run a sync task later
     * @param plugin The plugin to run the task under
     * @param runnable What to run
     * @param wait How long to wait before execution
     * @return The created {@link BukkitTask}
     */
    public static BukkitTask syncLater(Plugin plugin, long wait, Runnable runnable) {
        return Bukkit.getScheduler().runTaskLater(plugin, runnable, wait);
    }

    /**
     * Run an async task
     * @param runnable What to run
     * @return The created {@link BukkitTask}
     */
    public static BukkitTask async(Runnable runnable) {
        return async(plugin, runnable);
    }

    /**
     * Run an async task
     * @param plugin The plugin to run the task under
     * @param runnable What to run
     * @return The created {@link BukkitTask}
     */
    public static BukkitTask async(Plugin plugin, Runnable runnable) {
        return Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    /**
     * Run an async task on a timer
     * @param runnable What to run
     * @param period The time between executions
     * @return The created {@link BukkitTask}
     */
    public static BukkitTask asyncTimer(long period, Runnable runnable) {
        return asyncTimer(plugin, period, runnable);
    }

    /**
     * Run an async task on a timer
     * @param plugin The plugin to run the task under
     * @param runnable What to run
     * @param period The time between executions
     * @return The created {@link BukkitTask}
     */
    public static BukkitTask asyncTimer(Plugin plugin, long period, Runnable runnable) {
        return asyncTimer(plugin, 0L, period, runnable);
    }

    /**
     * Run an async task on a timer
     * @param runnable What to run
     * @param wait How long to wait before starting
     * @param period The time between executions
     * @return The created {@link BukkitTask}
     */
    public static BukkitTask asyncTimer(long wait, long period, Runnable runnable) {
        return asyncTimer(plugin, wait, period, runnable);
    }

    /**
     * Run an async task on a timer
     * @param plugin The plugin to run the task under
     * @param runnable What to run
     * @param wait How long to wait before starting
     * @param period The time between executions
     * @return The created {@link BukkitTask}
     */
    public static BukkitTask asyncTimer(Plugin plugin, long wait, long period, Runnable runnable) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, wait, period);
    }

    /**
     * Run an async task later
     * @param runnable What to run
     * @param wait How long to wait before execution
     * @return The created {@link BukkitTask}
     */
    public static BukkitTask asyncLater(long wait, Runnable runnable) {
        return asyncLater(plugin, wait, runnable);
    }

    /**
     * Run an async task later
     * @param plugin The plugin to run the task under
     * @param runnable What to run
     * @param wait How long to wait before execution
     * @return The created {@link BukkitTask}
     */
    public static BukkitTask asyncLater(Plugin plugin, long wait, Runnable runnable) {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, wait);
    }
}
