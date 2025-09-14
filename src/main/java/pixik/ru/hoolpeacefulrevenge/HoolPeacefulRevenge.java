package pixik.ru.hoolpeacefulrevenge;

import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class HoolPeacefulRevenge extends JavaPlugin implements Listener {

    private final Set<UUID> revengeMobs = new HashSet<>();
    private final double DAMAGE = 4.5;
    private final double LOOT_MULTIPLIER = 2.5;
    private final Map<UUID, Integer> attackTasks = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        revengeMobs.clear();
        for (Integer taskId : attackTasks.values()) {
            getServer().getScheduler().cancelTask(taskId);
        }
        attackTasks.clear();
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        Entity entity = event.getEntity();

        if (isPeacefulMob(entity)) {
            LivingEntity mob = (LivingEntity) entity;
            revengeMobs.add(mob.getUniqueId());

            if (mob instanceof Creature creature) {
                startChasing(creature, player);
            }

            player.damage(DAMAGE, mob);
        }
    }

    private void startChasing(Creature mob, Player target) {
        mob.setTarget(target);

        mob.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED, 600, 1, false, false
        ));

        mob.addPotionEffect(new PotionEffect(
                PotionEffectType.GLOWING, 600, 1, false, false
        ));

        UUID mobId = mob.getUniqueId();
        if (attackTasks.containsKey(mobId)) {
            getServer().getScheduler().cancelTask(attackTasks.get(mobId));
        }

        int taskId = new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 600;

            @Override
            public void run() {
                if (!mob.isValid() || !target.isOnline()) {
                    cancel();
                    attackTasks.remove(mobId);
                    return;
                }

                if (ticks >= MAX_TICKS) {
                    mob.setTarget(null);
                    cancel();
                    attackTasks.remove(mobId);
                    return;
                }

                mob.setTarget(target);

                if (mob.getLocation().distance(target.getLocation()) < 2.0) {
                    target.damage(DAMAGE, mob);
                }

                ticks += 20;
            }
        }.runTaskTimer(this, 0, 20).getTaskId();

        attackTasks.put(mobId, taskId);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        UUID entityId = entity.getUniqueId();

        if (revengeMobs.contains(entityId)) {
            List<ItemStack> drops = event.getDrops();
            List<ItemStack> newDrops = new ArrayList<>();

            for (ItemStack drop : drops) {
                ItemStack newDrop = drop.clone();
                int originalAmount = drop.getAmount();
                int newAmount = (int) Math.max(1, Math.round(originalAmount * LOOT_MULTIPLIER));
                newDrop.setAmount(newAmount);
                newDrops.add(newDrop);
            }

            event.getDrops().clear();
            event.getDrops().addAll(newDrops);

            int originalExp = event.getDroppedExp();
            int newExp = (int) Math.round(originalExp * LOOT_MULTIPLIER);
            event.setDroppedExp(newExp);

            revengeMobs.remove(entityId);

            if (attackTasks.containsKey(entityId)) {
                getServer().getScheduler().cancelTask(attackTasks.get(entityId));
                attackTasks.remove(entityId);
            }
        }
    }

    private boolean isPeacefulMob(Entity entity) {
        if (!(entity instanceof LivingEntity)) return false;

        return entity instanceof Cow ||
                entity instanceof Sheep ||
                entity instanceof Pig ||
                entity instanceof Chicken ||
                entity instanceof Rabbit ||
                entity instanceof Horse ||
                entity instanceof Donkey ||
                entity instanceof Mule ||
                entity instanceof Llama ||
                entity instanceof Panda ||
                entity instanceof Fox ||
                entity instanceof Bee ||
                entity instanceof Dolphin ||
                entity instanceof Axolotl ||
                entity instanceof Goat ||
                entity instanceof Frog ||
                entity instanceof Camel ||
                entity instanceof MushroomCow ||
                entity instanceof Cat ||
                entity instanceof Ocelot ||
                (entity instanceof Wolf && !((Wolf) entity).isAngry());
    }
}