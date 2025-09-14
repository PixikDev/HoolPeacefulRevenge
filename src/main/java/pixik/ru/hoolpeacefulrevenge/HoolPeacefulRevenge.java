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

import java.util.*;

public class HoolPeacefulRevenge extends JavaPlugin implements Listener {

    private final Set<UUID> revengeMobs = new HashSet<>();
    private final double DAMAGE = 2.0; // 2 единицы здоровья
    private final double LOOT_MULTIPLIER = 2.5;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("PeacefulRevenge plugin enabled!");
    }

    @Override
    public void onDisable() {
        revengeMobs.clear();
        getLogger().info("PeacefulRevenge plugin disabled!");
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        Entity entity = event.getEntity();

        // Проверяем, что это мирный моб
        if (isPeacefulMob(entity)) {
            LivingEntity mob = (LivingEntity) entity;

            // Помечаем моба для увеличения лута
            revengeMobs.add(mob.getUniqueId());

            // Заставляем моба атаковать игрока
            if (mob instanceof Creature creature) {
                creature.setTarget(player);

                // Даем мобу эффекты для лучшей атаки
                mob.addPotionEffect(new PotionEffect(
                        PotionEffectType.SPEED, 200, 1, false, false
                ));

                // Добавляем свечение для видимости
                mob.addPotionEffect(new PotionEffect(
                        PotionEffectType.GLOWING, 100, 1, false, false
                ));
            }

            // Наносим урон игроку
            player.damage(DAMAGE, mob);

            player.sendMessage("§cМирный моб защищается!");
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        if (revengeMobs.contains(entity.getUniqueId())) {
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

            revengeMobs.remove(entity.getUniqueId());
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