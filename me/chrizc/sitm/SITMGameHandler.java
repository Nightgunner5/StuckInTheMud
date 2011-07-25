package me.chrizc.sitm;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

public class SITMGameHandler {
    
    private final StuckInTheMud plugin;
    
    public String PREFIX = ChatColor.DARK_PURPLE + "[StuckInTheMud] ";
    public String ERR_PREFIX = ChatColor.RED + "[StuckInTheMud] ";
    
    public SITMGameHandler(StuckInTheMud instance) {
        plugin = instance;
    }
    
    public void startGame(Player player) {
        if (plugin.inRegistration == false && plugin.inGame == false) {
            if (plugin.checkPermissions("sitm.admin.start", player) == true) {
                plugin.inRegistration = true;
                Bukkit.getServer().broadcastMessage(PREFIX + "A new game of Stuck In The Mud has begun! To join, type /mud join");
                plugin.players.put(player, "Regular");
                player.sendMessage(PREFIX + "You've started a new game, and successfully registered to play. To start the game, type /mud begin with at least 5 players registered.");
            } else {
                player.sendMessage(ERR_PREFIX + "You do not have permission to do that.");
            }
        } else {
            if (plugin.inRegistration == true) {
                player.sendMessage(ERR_PREFIX + "There is already a game in the registration stage! To join, type /mud join");
            } else {
                player.sendMessage(ERR_PREFIX + "There is already a game in progress! Wait for this game to finish before starting a new one.");
            }
        }
    }
    
    public void joinGame(Player player) {
        if (plugin.inRegistration == true && plugin.inGame == false) {
            if (plugin.players.containsKey(player) == false) {
                plugin.players.put(player, "Regular");
                player.sendMessage(PREFIX + "You've successfully registered to play. There are currently " + ChatColor.YELLOW + plugin.players.size() + ChatColor.DARK_PURPLE + " players registered.");
            } else {
                player.sendMessage(ERR_PREFIX + "You're already registered! To un-register, type /mud leave");
            }
        } else {
            if (plugin.inRegistration == false && plugin.inGame == false) {
                player.sendMessage(ERR_PREFIX + "There isn't a game registration in progress. Get an admin to start the game for you.");
            } else if (plugin.inRegistration == false && plugin.inGame == true) {
                player.sendMessage(ERR_PREFIX + "There's already a game in progress. Wait until the next game starts.");
            }
        }
    }
    
    public void cleanUpGame() {
        plugin.players.clear();
        plugin.numOfPlayers = 0;
        plugin.inGame = false;
        plugin.inRegistration = false;
        plugin.inCountdown = false;
    }
    
    public void beginGame(Player player) {
        if (plugin.numOfPlayers < 5) {
            player.sendMessage(ERR_PREFIX + "There are not enough players to begin the game. You need at least " + ChatColor.YELLOW + "5" + ChatColor.DARK_PURPLE + " players.");
        } else {
            plugin.inRegistration = false;
            plugin.inGame = true;
            plugin.inCountdown = true;
        }
    }
    
}
