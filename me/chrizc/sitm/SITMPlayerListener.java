package me.chrizc.sitm;

import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;

public class SITMPlayerListener extends PlayerListener {
    
    private final StuckInTheMud plugin;
    private final SITMGameHandler gameHandler;
    
    public String PREFIX = ChatColor.DARK_PURPLE + "[StuckInTheMud] ";
    public String ERR_PREFIX = ChatColor.RED + "[StuckInTheMud] ";
    
    public SITMPlayerListener (StuckInTheMud instance, SITMGameHandler game) {
        plugin = instance;
        gameHandler = game;
    }
    
    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (plugin.players.containsKey(player)) {
            if (plugin.inRegistration == true) {
                plugin.players.remove(player);
                Bukkit.getServer().broadcastMessage(ERR_PREFIX + ChatColor.YELLOW + player.getName() + ChatColor.DARK_PURPLE + " has left the server! They've been un-registered from the current game.");
                if (plugin.numOfPlayers == 1) {
                    Bukkit.getServer().broadcastMessage(ERR_PREFIX + "There are no more players left in the game! The game has ended.");
                    plugin.inRegistration = false;
                }
                plugin.numOfPlayers--;
                
            } else if (plugin.inGame == true) {
                plugin.players.remove(player);
                Bukkit.getServer().broadcastMessage(ERR_PREFIX + ChatColor.YELLOW + player.getName() + ChatColor.DARK_PURPLE + " has left the server! They've been removed from the current game.");
                if (plugin.numOfPlayers == 2) {
                    Bukkit.getServer().broadcastMessage(ERR_PREFIX + "There are not enough players left to continue. The game has ended.");
                    gameHandler.cleanUpGame();
                } else {
                    plugin.numOfPlayers--;
                }
            }
        }
    }
    
    @Override
    public void onPlayerKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        if (plugin.players.containsKey(player)) {
            if (plugin.inRegistration == true) {
                plugin.players.remove(player);
                Bukkit.getServer().broadcastMessage(ERR_PREFIX + ChatColor.YELLOW + player.getName() + ChatColor.DARK_PURPLE + " has been kicked from the server! They've been un-registered from the current game.");
                if (plugin.numOfPlayers == 1) {
                    Bukkit.getServer().broadcastMessage(ERR_PREFIX + "There are no more players left in the game! The game has ended.");
                    plugin.inRegistration = false;
                }
                plugin.numOfPlayers--;
                
            } else if (plugin.inGame == true) {
                plugin.players.remove(player);
                Bukkit.getServer().broadcastMessage(ERR_PREFIX + ChatColor.YELLOW + player.getName() + ChatColor.DARK_PURPLE + " has been kicked from the server! They've been removed from the current game.");
                if (plugin.numOfPlayers == 2) {
                    Bukkit.getServer().broadcastMessage(ERR_PREFIX + "There are not enough players left to continue. The game has ended.");
                    gameHandler.cleanUpGame();
                } else {
                    plugin.numOfPlayers--;
                }
            }
        }
    }
    
}
