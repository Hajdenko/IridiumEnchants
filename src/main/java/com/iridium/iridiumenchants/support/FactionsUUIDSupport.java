package com.iridium.iridiumenchants.support;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.Relation;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class FactionsUUIDSupport implements BuildSupport, FriendlySupport {
    @Override
    public boolean canBuild(Player player, Location location) {
        Faction faction = FPlayers.getInstance().getByPlayer(player).getFaction();
        FLocation loc = new FLocation(location);
        Faction B = Board.getInstance().getFactionAt(loc);
        return (ChatColor.stripColor(B.getTag()).equalsIgnoreCase("Wilderness")) || (faction == B);
    }

    @Override
    public boolean isFriendly(LivingEntity player, LivingEntity livingEntity) {
        if (player instanceof Player && livingEntity instanceof Player) {
            Relation r = FPlayers.getInstance().getByPlayer((Player) player).getRelationTo(FPlayers.getInstance().getByPlayer((Player) livingEntity));
            return r.isAlly() || r.isMember();
        }
        return false;
    }
}
