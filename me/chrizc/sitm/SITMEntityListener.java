package me.chrizc.sitm;

import org.bukkit.ChatColor;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class SITMEntityListener extends EntityListener {
    
    private final StuckInTheMud plugin;
    private final SITMGameHandler gameHandler;
    
    public String PREFIX = ChatColor.DARK_GREEN + "[StuckInTheMud] ";
    public String ERR_PREFIX = ChatColor.RED + "[StuckInTheMud] ";
    
    public SITMEntityListener(StuckInTheMud instance, SITMGameHandler game) {
        plugin = instance;
        gameHandler = game;
    }
    
    @Override
    public void onEntityDamage(EntityDamageEvent event) {
        //If we're not in game, don't do anything else. - performance measure.
        if (plugin.inGame == false) return;
        
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent edbee = (EntityDamageByEntityEvent) event;
            
            //If we're not dealing with player-to-player damage, don't do anything else.
            if (!(edbee.getDamager() instanceof Player) || !(edbee.getEntity() instanceof Player)) return;
        
            Player damager = (Player) edbee.getDamager();
            Player damagee = (Player) edbee.getEntity();
            
            //If our damager isn't in the game, we don't care what they're doing.
            if (!plugin.players.containsKey(damager) || !plugin.players.containsKey(damagee)) return;
        
            if (plugin.players.get(damager).equalsIgnoreCase("Chaser") && plugin.players.get(damagee).equalsIgnoreCase("Regular")) {
                edbee.setCancelled(true);
                plugin.players.put(damagee, "FROZEN");
                damagee.sendMessage(PREFIX + "You've been frozen by " + ChatColor.YELLOW + damager.getName() + ChatColor.DARK_GREEN + "! Wait for somebody else to tag you, to be unfrozen.");
                damager.sendMessage(PREFIX + "You've frozen " + ChatColor.YELLOW + damagee.getName() + ChatColor.DARK_GREEN + "!");
                plugin.numOfFrozen++;
                if (gameHandler.checkVictory() == true) gameHandler.victory();
                return;
            }
            
            if (plugin.players.get(damagee).equalsIgnoreCase("Chaser")) edbee.setCancelled(true);
            
            if (plugin.players.get(damager).equalsIgnoreCase("Regular") && plugin.players.get(damagee).equalsIgnoreCase("FROZEN")) {
                edbee.setCancelled(true);
                plugin.players.put(damagee, "Regular");
                damagee.sendMessage(PREFIX + "You've been un-frozen by " + ChatColor.YELLOW + damager.getName() + ChatColor.DARK_GREEN + "!");
                damager.sendMessage(PREFIX + "You've un-frozen " + ChatColor.YELLOW + damagee.getName() + ChatColor.DARK_GREEN + "!");
                plugin.numOfFrozen--;
            }
            
        }
    }
    
}
