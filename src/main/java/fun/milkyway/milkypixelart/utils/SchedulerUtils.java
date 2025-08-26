package fun.milkyway.milkypixelart.utils;

import fun.milkyway.milkypixelart.MilkyPixelart;
import fun.milkyway.milkypixelart.folia.FoliaRunnable;
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static fun.milkyway.milkypixelart.MilkyPixelart.isFolia;

/**
 * Utility class for scheduling tasks in a Paper/Folia server.
 */
public abstract class SchedulerUtils {

    private static Method getRegionSchedulerMethod;
    private static Method getGlobalSchedulerMethod;

    static {
        try {
            Class<?> serverClass = Bukkit.getServer().getClass();
            getRegionSchedulerMethod = serverClass.getMethod("getRegionScheduler");
            getGlobalSchedulerMethod = serverClass.getMethod("getGlobalRegionScheduler");
        } catch (Exception ignored) {}
    }

    /**
     * Schedules a task to run later on the main server thread.
     *
     * @param loc   The location where the task should run, or null for the main thread.
     * @param task  The task to run.
     * @param delay The delay in ticks before the task runs.
     */
    public static void runTaskLater(@Nullable Location loc, @NotNull Runnable task, long delay) {
        JavaPlugin plugin = MilkyPixelart.getInstance();
        if (isFolia()) {
            try {
                if (loc != null && getRegionSchedulerMethod != null) {
                    RegionScheduler regionScheduler = (RegionScheduler) getRegionSchedulerMethod.invoke(plugin.getServer());
                    regionScheduler.runDelayed(
                            plugin,
                            loc,
                            (ScheduledTask scheduledTask) -> {
                                try {
                                    task.run();
                                } catch (Throwable t) {
                                    t.printStackTrace();
                                }
                            },
                            delay
                    );
                } else if (getGlobalSchedulerMethod != null) {
                    GlobalRegionScheduler globalScheduler = (GlobalRegionScheduler) getGlobalSchedulerMethod.invoke(plugin.getServer());
                    globalScheduler.runDelayed(
                            plugin,
                            (ScheduledTask scheduledTask) -> {
                                try {
                                    task.run();
                                } catch (Throwable t) {
                                    t.printStackTrace();
                                }
                            },
                            delay
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        plugin.getServer().getScheduler().runTaskLater(plugin, task, delay);
    }

    /**
     * Schedules a task to run repeatedly on the main server thread.
     * @param loc The location where the task should run, or null for the main thread.
     * @param runnable   The BukkitRunnable to run.
     * @param delay  The delay in ticks before the task runs.
     * @param period The period in ticks between subsequent runs of the task.
     */
    public static void runTaskTimer(@Nullable Location loc, @NotNull FoliaRunnable runnable, long delay, long period) {
        JavaPlugin plugin = MilkyPixelart.getInstance();
        if (isFolia()) {
            try {
                ScheduledTask task = null;
                if (loc != null && loc.getWorld() != null && getRegionSchedulerMethod != null) {
                    RegionScheduler regionScheduler = (RegionScheduler) getRegionSchedulerMethod.invoke(plugin.getServer());
                    task = regionScheduler.runAtFixedRate(
                            plugin,
                            loc,
                            (ScheduledTask t) -> {
                                try {
                                    runnable.run();
                                } catch (Throwable ex) {
                                    ex.printStackTrace();
                                }
                            },
                            delay,
                            period
                    );
                } else if (getGlobalSchedulerMethod != null) {
                    GlobalRegionScheduler globalScheduler = (GlobalRegionScheduler) getGlobalSchedulerMethod.invoke(plugin.getServer());
                    task = globalScheduler.runAtFixedRate(
                            plugin,
                            (ScheduledTask t) -> {
                                try {
                                    runnable.run();
                                } catch (Throwable ex) {
                                    ex.printStackTrace();
                                }
                            },
                            delay,
                            period
                    );
                }
                if (task != null) {
                    runnable.setScheduledTask(task);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        runnable.runTaskTimer(plugin, delay, period);
    }

    /**
     * Schedules a task to run later asynchronously on the main server thread.
     * @param loc The location where the task should run, or null for the main thread.
     * @param task   The task to run.
     * @param delay  The delay in ticks before the task runs.
     */
    public static void runTaskLaterAsynchronously(@Nullable Location loc, @NotNull Runnable task, long delay) {
        JavaPlugin plugin = MilkyPixelart.getInstance();
        if (isFolia()) {
            try {
                if (loc != null && getRegionSchedulerMethod != null) {
                    Method getRegionScheduler = getRegionSchedulerMethod;
                    RegionScheduler regionScheduler = (RegionScheduler) getRegionScheduler.invoke(plugin.getServer());
                    regionScheduler.runDelayed(
                            plugin,
                            loc,
                            (ScheduledTask scheduledTask) -> task.run(),
                            delay
                    );
                } else if(getGlobalSchedulerMethod != null) {
                    Method getGlobalScheduler = getGlobalSchedulerMethod;
                    GlobalRegionScheduler globalScheduler = (GlobalRegionScheduler) getGlobalScheduler.invoke(plugin.getServer());
                    globalScheduler.runDelayed(
                            plugin,
                            (ScheduledTask scheduledTask) -> task.run(),
                            delay
                    );
                }
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, task, delay);
    }

    /**
     * Schedules a task to run repeatedly on the main server thread asynchronously.
     * @param runnable The FoliaRunnable to run.
     * @param delay  The delay in ticks before the task runs.
     * @param period The period in ticks between subsequent runs of the task.
     */
    public static void runTaskTimerAsynchronously(@NotNull FoliaRunnable runnable, long delay, long period) {
        JavaPlugin plugin = MilkyPixelart.getInstance();
        if (isFolia()) {
            try {
                final AsyncScheduler asyncScheduler = plugin.getServer().getAsyncScheduler();
                class AsyncRepeatingTask {
                    void start(long initialDelay) {
                        ScheduledTask task = asyncScheduler.runDelayed(
                                plugin,
                                (ScheduledTask t) -> {
                                    try {
                                        runnable.run();
                                    } catch (Throwable ex) {
                                        ex.printStackTrace();
                                    }

                                    if (!t.isCancelled()) {
                                        start(period);
                                    }
                                },
                                initialDelay,
                                TimeUnit.MILLISECONDS
                        );
                        runnable.setScheduledTask(task);
                    }
                }

                new AsyncRepeatingTask().start(delay * 50L);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        runnable.runTaskTimerAsynchronously(plugin, delay, period);
    }


    /**
     * Runs a task asynchronously on the main server thread.
     * @param task The task to run.
     */
    public static void runTaskAsynchronously(@NotNull Runnable task) {
        JavaPlugin plugin = MilkyPixelart.getInstance();
        if (isFolia()) {
            try {
                final AsyncScheduler asyncScheduler = plugin.getServer().getAsyncScheduler();
                asyncScheduler.runNow(
                        plugin,
                        scheduledTask -> {
                            try {
                                task.run();
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                        }
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, task);
    }

    /**
     * Runs a task on the main server thread at a specific location or globally.
     * @param loc The location where the task should run, or null for the main thread.
     * @param task The task to run.
     */
    public static void runTask(@Nullable Location loc, @NotNull Runnable task) {
        JavaPlugin plugin = MilkyPixelart.getInstance();
        if (isFolia()) {
            try {
                if (loc != null && loc.getWorld() != null && getRegionSchedulerMethod != null) {
                    RegionScheduler regionScheduler = (RegionScheduler) getRegionSchedulerMethod.invoke(plugin.getServer());
                    regionScheduler.execute(plugin, loc, () -> {
                        try {
                            task.run();
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    });
                } else if (getGlobalSchedulerMethod != null) {
                    GlobalRegionScheduler globalScheduler = (GlobalRegionScheduler) getGlobalSchedulerMethod.invoke(plugin.getServer());
                    globalScheduler.execute(plugin, () -> {
                        try {
                            task.run();
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    });
                } else {
                    plugin.getServer().getScheduler().runTask(plugin, task);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        plugin.getServer().getScheduler().runTask(plugin, task);
    }

    public static <T> CompletableFuture<T> callSyncMethod(@Nullable Location loc, @NotNull Callable<T> task) {
        JavaPlugin plugin = MilkyPixelart.getInstance();
        if (isFolia()) {
            CompletableFuture<T> future = new CompletableFuture<>();
            try {
                if (loc != null && getRegionSchedulerMethod != null) {
                    Method getRegionScheduler = getRegionSchedulerMethod;
                    RegionScheduler regionScheduler = (RegionScheduler) getRegionScheduler.invoke(plugin.getServer());
                    regionScheduler.execute(plugin, loc, () -> {
                        try {
                            future.complete(task.call());
                        } catch (Exception e) {
                            future.completeExceptionally(e);
                        }
                    });
                } else if(getGlobalSchedulerMethod != null) {
                    Method getGlobalScheduler = getGlobalSchedulerMethod;
                    GlobalRegionScheduler globalScheduler = (GlobalRegionScheduler) getGlobalScheduler.invoke(plugin.getServer());
                    globalScheduler.execute(plugin, () -> {
                        try {
                            future.complete(task.call());
                        } catch (Exception e) {
                            future.completeExceptionally(e);
                        }
                    });
                }
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
            return future;
        }
        CompletableFuture<T> cf = new CompletableFuture<>();
        try {
            Future<T> bukkitFuture = plugin.getServer().getScheduler().callSyncMethod(plugin, task);
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    T result = bukkitFuture.get();
                    cf.complete(result);
                } catch (Throwable ex) {
                    cf.completeExceptionally(ex);
                }
            });
        } catch (Throwable e) {
            cf.completeExceptionally(e);
        }
        return cf;
    }
}
