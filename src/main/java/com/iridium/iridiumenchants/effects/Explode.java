package com.iridium.iridiumenchants.effects;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

public class Explode implements Effect {
    @Override
    public void apply(LivingEntity player, LivingEntity target, String[] args, ItemStack itemStack, Event event) {
        int power;
        try {
            power = Integer.parseInt(args[1]);
        } catch (NumberFormatException exception) {
            power = 1;
        }
        boolean fire = args[2].equalsIgnoreCase("true");
        boolean breakBlocks = args[3].equalsIgnoreCase("true");
        if (args.length == 5 && args[4].equalsIgnoreCase("target")) {
            target.getWorld().createExplosion(target.getLocation().getX(), target.getLocation().getY(), target.getLocation().getZ(), power, fire, breakBlocks);
        } else {
            player.getWorld().createExplosion(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), power, fire, breakBlocks);
        }
    }
}
