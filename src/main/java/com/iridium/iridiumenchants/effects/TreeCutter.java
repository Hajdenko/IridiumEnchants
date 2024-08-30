package com.iridium.iridiumenchants.effects;

import com.iridium.iridiumcore.dependencies.xseries.XMaterial;
import com.iridium.iridiumenchants.IridiumEnchants;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.logging.Level;

public class TreeCutter implements Effect {
    private final Set<Location> pBlocks = new HashSet<>();

    @Override
    public void apply(LivingEntity entity, LivingEntity target, String[] args, ItemStack itemStack, Event event) {
        try {
            if (!(entity instanceof Player)) {
                throw new IllegalArgumentException("TreeCutter effect can only be applied to players.");
            }
            if (!(event instanceof BlockBreakEvent)) {
                throw new IllegalArgumentException("TreeCutter effect can only be triggered by BlockBreakEvent.");
            }

            Player player = (Player) entity;
            BlockBreakEvent blockBreakEvent = (BlockBreakEvent) event;
            Block initialBlock = blockBreakEvent.getBlock();

            if (!isWood(initialBlock)) {
                return;
            }

            int level = enchantLevel(args);
            int breakLimit = get_breakLimit(args, level);
            boolean instantMine = args.length > 2 && args[2].equalsIgnoreCase("true");
            int breakDelay = get_breakDelay(level);

            pBlocks.clear();
            startTree(player, initialBlock, level, breakLimit, instantMine, breakDelay);
        } catch (Exception e) {
            logError("Error in TreeCutter.apply()", e);
        }
    }

    private void startTree(Player player, Block startBlock, int level, int breakLimit, boolean instantMine, int breakDelay) {
        Queue<Block> blocksToProcess = new LinkedList<>();
        blocksToProcess.add(startBlock);

        new BukkitRunnable() {
            int blocksBroken = 0;

            @Override
            public void run() {
                try {
                    if (blocksToProcess.isEmpty() || blocksBroken >= breakLimit) {
                        this.cancel();
                        return;
                    }

                    Block currentBlock = blocksToProcess.poll();
                    if (currentBlock == null || !isWood(currentBlock) || !IridiumEnchants.getInstance().canBuild(player, currentBlock.getLocation())) {
                        return;
                    }

                    if (pBlocks.contains(currentBlock.getLocation())) {
                        return;
                    }

                    pBlocks.add(currentBlock.getLocation());

                    if (instantMine) {
                        blockbreak(player, currentBlock);
                    } else {
                        currentBlock.breakNaturally(player.getItemInHand());
                    }

                    player.playSound(currentBlock.getLocation(), Sound.BLOCK_WOOD_BREAK, 1.0f, 1.0f);
                    blocksBroken++;

                    for (Block nearbyBlock : getAreaAround(currentBlock)) {
                        if (isWood(nearbyBlock) && !pBlocks.contains(nearbyBlock.getLocation())) {
                            blocksToProcess.add(nearbyBlock);
                        }
                    }
                } catch (Exception e) {
                    logError("Error in TreeCutter runnable", e);
                    this.cancel();
                }
            }
        }.runTaskTimer(IridiumEnchants.getInstance(), 0L, breakDelay);
    }

    private boolean isWood(Block block) {
        return block != null && (block.getType().name().endsWith("_LOG") || block.getType().name().endsWith("_WOOD"));
    }

    private List<Block> getAreaAround(Block block) {
        List<Block> aBlocks = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    Block relativeBlock = block.getRelative(x, y, z);
                    if (relativeBlock != null) {
                        aBlocks.add(relativeBlock);
                    }
                }
            }
        }
        return aBlocks;
    }

    private int enchantLevel(String[] args) {
        try {
            return args != null && args.length > 1 ? Integer.parseInt(args[1]) : 1;
        } catch (NumberFormatException e) {
            logError("Invalid enchant level provided. Using default level 1.", e);
            return 1;
        }
    }

    private int get_breakLimit(String[] args, int level) {
        try {
            return args != null && args.length > 3 ? Integer.parseInt(args[3]) : 10;
        } catch (NumberFormatException e) {
            logError("Invalid break limit provided. Using default limit 10.", e);
            return 10;
        }
    }

    private int get_breakDelay(int level) {
        switch (level) {
            case 1:
                return 10;
            case 2:
                return 5;
            case 3:
                return new Random().nextInt(2) + 2;
            default:
                logError("Invalid enchant level: " + level + ". Using default delay 10.", null);
                return 10;
        }
    }

    private void blockbreak(Player player, Block block) {
        try {
            BlockBreakEvent breakEvent = new BlockBreakEvent(block, player);
            IridiumEnchants.getInstance().getServer().getPluginManager().callEvent(breakEvent);
            if (!breakEvent.isCancelled()) {
                block.breakNaturally(player.getItemInHand());
            }
        } catch (Exception e) {
            logError("Error breaking block at " + block.getLocation(), e);
        }
    }

    private void logError(String message, Exception e) {
        IridiumEnchants.getInstance().getLogger().log(Level.SEVERE, "TreeCutter Error: " + message, e);
    }
}
