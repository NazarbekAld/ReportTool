package me.nazarxexe.job.admintool.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.nazarxexe.job.admintool.ReportTool;
import me.nazarxexe.job.admintool.database.ReportsTable;
import me.nazarxexe.job.admintool.impl.PlayerLockable;
import me.nazarxexe.job.admintool.impl.TableManageable;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;

import java.util.HashSet;
import java.util.Set;

public class PlayerListener implements Listener, PlayerLockable {

    private final Set<String> lock = new HashSet<>();

    private final TableManageable table;

    public PlayerListener(TableManageable table) {
        this.table = table;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (lock.contains(e.getPlayer().getName())) {
            e.getPlayer().sendTitle(ReportTool.getSUSPEND_TITLE(), ReportTool.getSUSPEND_SUBTITLE(), 0, 10, 10);
            e.getPlayer().sendMessage(ReportTool.getSUSPEND_MESSAGE());
            e.getPlayer().sendActionBar(Component.text(ReportTool.getSUSPEND_ACTIONBAR()));
            e.setCancelled(true);
        }

    }
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (lock.contains(e.getPlayer().getName())) e.setCancelled(true);
    }
    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (lock.contains(e.getPlayer().getName())) e.setCancelled(true);
    }

    @EventHandler
    public void onPickup(PlayerMoveEvent e) {
        if (lock.contains(e.getPlayer().getName())) e.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerMoveEvent e) {
        if (lock.contains(e.getPlayer().getName())) e.setCancelled(true);
    }

    @EventHandler
    public void onInventory(InventoryOpenEvent e) {
        if (lock.contains(e.getPlayer().getName())) e.setCancelled(true);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (lock.contains(e.getPlayer().getName())) e.setCancelled(true);
    }

    @EventHandler
    public void onChat(AsyncChatEvent e) {
        if (lock.contains(e.getPlayer().getName())) e.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        if (lock.contains(e.getDamager().getName())) e.setCancelled(true);
    }

    @EventHandler
    public void onGetDamage(EntityDamageEvent e) {
        if (!(e instanceof Player)) return;
        if (lock.contains(e.getEntity().getName())) e.setCancelled(true);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {

        if (lock.contains(e.getPlayer().getName())) return;

        table.exist(e.getPlayer().getName())
                .thenAcceptAsync((exist) -> {
                    if (exist)
                        table.getReportDataByCondition(ReportsTable.PLAYER.eq(e.getPlayer().getName()))
                                .thenAccept((reportData -> {
                                    if (reportData.get(0)
                                            .getReports() >= 3) {
                                        lock(e.getPlayer().getName());
                                    }
                                })).join();
                }).join();
    }

    @Override
    public void lock(String name) {
        lock.add(name);
    }

    @Override
    public void unlock(String name) {
        lock.remove(name);
    }

    @Override
    public boolean isLocked(String name) {
        return lock.contains(name);
    }
}
