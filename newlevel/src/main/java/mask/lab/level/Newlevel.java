package mask.lab.level;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Newlevel extends JavaPlugin implements Listener {

    private Map<UUID, Integer> playerKills;
    private Map<UUID, Integer> playerLevels;
    private Map<UUID, BossBar> playerBossBars;
    private Map<UUID, Integer> playerTimer;

    @Override
    public void onEnable() {
        playerKills = new HashMap<>();
        playerLevels = new HashMap<>();
        playerBossBars = new HashMap<>();
        playerTimer = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("NewLevelプラグインが有効化されました");
    }

    @Override
    public void onDisable() {
        playerKills.clear();
        playerLevels.clear();
        playerBossBars.clear();
        playerTimer.clear();
        getLogger().info("NewLevelプラグインが無効化されました");
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() instanceof Player) {
            Player player = event.getEntity().getKiller();
            UUID playerId = player.getUniqueId();

            if (event.getEntityType() == EntityType.ZOMBIE || event.getEntityType() == EntityType.SKELETON) {
                int kills = playerKills.getOrDefault(playerId, 0) + 1;
                playerKills.put(playerId, kills);

                int level = playerLevels.getOrDefault(playerId, 0);
                if (kills >= 10) {
                    level++;
                    kills = 0;
                    playerKills.put(playerId, kills);
                }
                playerLevels.put(playerId, level);

                updatePlayerLevel(player, level);
                updateBossBar(player, level);
            }

            resetBossBarTimer(player);
        }
    }

    private void updatePlayerLevel(Player player, int level) {
        ChatColor nameColor;

        if (level >= 100) {
            nameColor = ChatColor.RED;
        } else if (level >= 50) {
            nameColor = ChatColor.GREEN;
        } else if (level >= 20) {
            nameColor = ChatColor.AQUA;
        } else {
            nameColor = ChatColor.YELLOW;
        }

        String prefix = ChatColor.WHITE + "[" + nameColor + "Lv." + level + ChatColor.WHITE + "] " ;
        String playerName = player.getName();
        player.setPlayerListName(prefix + playerName);

        player.setDisplayName(prefix + playerName);
    }

    private void updateBossBar(Player player, int level) {
        BossBar bossBar = playerBossBars.get(player.getUniqueId());

        if (bossBar == null) {
            bossBar = Bukkit.createBossBar("残りキル数", BarColor.BLUE, BarStyle.SOLID);
            playerBossBars.put(player.getUniqueId(), bossBar);
        }

        int remainingKills = 10 - (playerKills.getOrDefault(player.getUniqueId(), 0) % 10);
        float progress = (float) remainingKills / 10;

        bossBar.setProgress(progress);
        bossBar.setTitle("残りキル数: " + remainingKills);
        bossBar.addPlayer(player);

        if (level < 0) {
            bossBar.setVisible(false);
        } else {
            bossBar.setVisible(true);
        }
    }

    private void resetBossBarTimer(Player player) {
        UUID playerId = player.getUniqueId();
        int taskId = playerTimer.getOrDefault(playerId, -1);

        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }

        taskId = Bukkit.getScheduler().runTaskLater(this, () -> {
            BossBar bossBar = playerBossBars.get(playerId);
            if (bossBar != null) {
                bossBar.setVisible(false);
            }
        }, 200).getTaskId();

        playerTimer.put(playerId, taskId);
    }
}
