package me.nazarxexe.job.admintool.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.Getter;
import me.nazarxexe.job.admintool.ReportTool;
import me.nazarxexe.job.admintool.database.data.ReportData;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerListener implements Listener {

    private static @Getter Set<String> lock = new HashSet<>();

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (lock.contains(e.getPlayer().getName())) {
            e.getPlayer().sendTitle(ReportTool.getInstance().getSuspend_title(), ReportTool.getInstance().getSuspend_subtitle(), 0, 10, 10);
            e.getPlayer().sendMessage(ReportTool.getInstance().getSuspend_message());
            e.getPlayer().sendActionBar(Component.text(ReportTool.getInstance().getSuspend_actionbar()));
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
        if (lock.contains(((Player) e.getDamager()).getName())) e.setCancelled(true);
    }

    public void onGetDamage(EntityDamageEvent e) {
        if (!(e instanceof Player)) return;
        if (lock.contains(((Player) e.getEntity()).getName())) e.setCancelled(true);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {

        if (lock.contains(e.getPlayer().getName())) return;

        ReportTool.getInstance().getServer().getScheduler().runTaskAsynchronously(ReportTool.getInstance(), new Runnable() {
            @Override
            public void run() {

                try {
                    Record record = DSL.using(ReportTool.getInstance().getDatabase().getConnection(), SQLDialect.MYSQL)
                            .select()
                            .from(DSL.table("reports"))
                            .where(DSL.field("player").eq(e.getPlayer().getName()))
                            .fetchOne();

                    if (record == null) {
                        ReportTool.getCache().put(
                                e.getPlayer().getName(),
                                new ReportData(
                                        e.getPlayer().getName(),
                                        "",
                                        0,
                                        0,
                                        new ArrayList<>()
                                )
                        );
                        return;
                    }

                    ReportTool.getCache().put(
                            e.getPlayer().getName(),
                            new ReportData(
                                    e.getPlayer().getName(),
                                    record.get(DSL.field("message")).toString(),
                                    (int) record.get(DSL.field("reports")),
                                    ((Timestamp) record.get(DSL.field("date"))).getTime(),
                                    new ArrayList<>()
                            )
                    );

                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }

            }
        });

    }

}
