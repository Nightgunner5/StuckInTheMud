package me.chrizc.sitm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

public class SITMGameHandler {
    
    private final StuckInTheMud plugin;
    private final SITMDatabaseHandler databaseHandler;
    private final SITMConfig config;
    
    public String PREFIX = ChatColor.DARK_GREEN + "[StuckInTheMud] ";
    public String ERR_PREFIX = ChatColor.RED + "[StuckInTheMud] ";
    
    public HashMap<Player, ItemStack[]> inventories = new HashMap<Player, ItemStack[]>();
    
    public SITMGameHandler(StuckInTheMud instance, SITMDatabaseHandler database, SITMConfig config) {
        plugin = instance;
        databaseHandler = database;
        this.config = config;
    }
    
    public void startGame(Player player) {
        if (plugin.inRegistration == false && plugin.inGame == false) {
            if (plugin.checkPermissions("sitm.admin.start", player) == true) {
                plugin.inRegistration = true;
                Bukkit.getServer().broadcastMessage(PREFIX + "A new game of Stuck In The Mud has begun! To join, type /mud join");
                plugin.players.put(player, "Regular");
                plugin.numOfPlayers++;
                if (config.lobby) {
                    plugin.oldLocations.put(player, player.getLocation());
                    telePlayerToLobby(player);
                }
                player.sendMessage(ChatColor.RED + "[StuckInTheMud] You've started a new game, and successfully registered to play. To start the game, type /mud begin with at least 5 players registered.");
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
    
    public void listPlayers(Player player) {
        if (plugin.inGame == false && plugin.inRegistration == false) return;
        
        Iterator<Player> i = plugin.players.keySet().iterator();
        player.sendMessage(" ");
        player.sendMessage(PREFIX + "Current player list:");
        
        while (i.hasNext()) {
            Player current = i.next();
            String status;
            if (plugin.players.get(current).equalsIgnoreCase("FROZEN")) {
                status = ChatColor.BLUE + "FROZEN";
            } else if (plugin.players.get(current).equalsIgnoreCase("Chaser")) {
                status = ChatColor.RED + "Chaser";
            } else {
                status = ChatColor.GREEN + "Un-frozen";
            }
            player.sendMessage(PREFIX + ChatColor.YELLOW + current.getName() + ChatColor.DARK_GREEN + " (Status: " + status + ChatColor.DARK_GREEN + ")");
            
        }
    }
    
    public void joinGame(Player player) {
        if (plugin.inRegistration == true && plugin.inGame == false) {
            if (plugin.checkPermissions("sitm.users.join", player) == true) {
                if (plugin.players.containsKey(player) == false) {
                    plugin.players.put(player, "Regular");
                    if (config.lobby) {
                        plugin.oldLocations.put(player, player.getLocation());
                        telePlayerToLobby(player);
                    }
                    plugin.numOfPlayers++;
                    Bukkit.getServer().broadcastMessage(PREFIX + ChatColor.YELLOW + player.getName() + ChatColor.DARK_GREEN + " has registered! There are currently " + ChatColor.YELLOW + plugin.players.size() + ChatColor.DARK_GREEN + " players registered.");
                    player.sendMessage(ERR_PREFIX + "You've successfully registered to play. There are currently " + ChatColor.YELLOW + plugin.players.size() + ChatColor.RED + " players registered.");
                } else {
                    player.sendMessage(ERR_PREFIX + "You're already registered! To un-register, type /mud leave");
                }
            } else {
                player.sendMessage(ERR_PREFIX + "You do not have permission to do that.");
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
        this.unTelePlayers();
        plugin.numOfPlayers = 0;
        plugin.numOfChasers = 0;
        plugin.numOfFrozen = 0;
        plugin.inGame = false;
        plugin.inRegistration = false;
        plugin.inCountdown = false;
    }
    
    public void unreg(Player player) {
        if (plugin.inRegistration == false) return;
        
        if (plugin.inRegistration == true && plugin.inCountdown == false) {
            plugin.players.remove(player);
            Bukkit.getServer().broadcastMessage(ERR_PREFIX + ChatColor.YELLOW + player.getName() + ChatColor.RED + " has un-registered from the current game!");
            if (plugin.numOfPlayers == 1) {
                Bukkit.getServer().broadcastMessage(ERR_PREFIX + "There are no more players left in the game! The game has ended.");
                plugin.inRegistration = false;
            }
            plugin.numOfPlayers--;
        } else {
            player.sendMessage(ERR_PREFIX + "You can't leave a game in the countdown.");
        }
    }
    
    public void beginGame(Player player) {
        if (!plugin.inRegistration || plugin.inGame) return;
        
        if (plugin.checkPermissions("sitm.admin.begin", player) == true) {
            if (plugin.numOfPlayers < 5) {
                player.sendMessage(ERR_PREFIX + "There are not enough players to begin the game. You need at least " + ChatColor.YELLOW + "5" + ChatColor.RED + " players.");
            } else {
                plugin.inRegistration = false;
                plugin.inCountdown = true;

                //How many chasers will we have?
                plugin.numOfChasers = (int)Math.floor(plugin.numOfPlayers / 5);
                Object[] key = plugin.players.keySet().toArray();
                
                HashSet<Player> chasers = new HashSet<Player>();

                //Assign the chasers
                for (int x = 1; x <= plugin.numOfChasers; x++) {
                    Random random = new Random();
                    int chaser = random.nextInt(plugin.numOfPlayers);

                    //Have we selected somebody who is already a chaser?
                    while (plugin.players.get((Player) key[chaser]).equalsIgnoreCase("Chaser")) {
                        chaser = random.nextInt(plugin.numOfPlayers);
                    }

                    Player chaserP = (Player) key[chaser];

                    plugin.players.put(chaserP, "Chaser");
                    chasers.add(chaserP);
                    chaserP.sendMessage(PREFIX + "You are a " + ChatColor.YELLOW + "CHASER" + ChatColor.DARK_GREEN + "! Freeze other players by punching them!");
                }
                
                String message = PREFIX + "The chasers are: ";
                Object[] arr = chasers.toArray();
                
                for (int i = 0; i < chasers.size(); i++) {
                    Player p = (Player) arr[i];
                    message += ChatColor.YELLOW + p.getName();
                    if (i == (chasers.size() - 1)) {
                        message += ChatColor.DARK_GREEN + ". ";
                    } else {
                        message += ChatColor.DARK_GREEN + ", ";
                    }
                }
                
                Bukkit.getServer().broadcastMessage(message);
                
                if (!config.lobby) {
                    plugin.oldLocations.put(player, player.getLocation());
                }
                
                telePlayersToArena();

                this.startCountdown();
            }
        }
    }
    
    public void startCountdown() {
        if (plugin.inCountdown == true) {
            Bukkit.getServer().broadcastMessage(PREFIX + "The game is about to begin!");
            Bukkit.getServer().broadcastMessage(PREFIX + "3..");
            Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    Bukkit.getServer().broadcastMessage(PREFIX + "2..");
                }
            }, 30L);
            Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    Bukkit.getServer().broadcastMessage(PREFIX + "1..");
                }
            }, 60L);
            Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    plugin.inGame = true;
                    Bukkit.getServer().broadcastMessage(PREFIX + "GO!");
                    plugin.inCountdown = false;
                }
            }, 90L);
        }
    }
    
    public void cancelCountdown(Player player) {
        if (plugin.checkPermissions("sitm.admin.cancel", player) == true) {
            if (plugin.inCountdown == true) {
                Bukkit.getServer().getScheduler().cancelTasks(plugin);
                plugin.inGame = false;
                plugin.inCountdown = false;
                plugin.inRegistration = true;
                plugin.numOfChasers = 0;
                plugin.numOfFrozen = 0;
                Bukkit.getServer().broadcastMessage(PREFIX + ChatColor.YELLOW + player.getName() + " cancelled the countdown!");
            }
        }
    }
    
    public boolean checkVictory() {
        if (plugin.inGame == false) return false;
        
        if (plugin.numOfFrozen == (plugin.numOfPlayers - plugin.numOfChasers)) return true;
        
        return false;
    }
    
    public void victory() {
        if (this.checkVictory()) {
            Bukkit.getServer().broadcastMessage(PREFIX + "The game is over! The chasers have won!");
            
            if (config.leaderboard) {
                Iterator<Player> i = plugin.players.keySet().iterator();

                while (i.hasNext()) {
                    Player p = i.next();
                    String query;
                    if (plugin.players.get(p).equalsIgnoreCase("FROZEN") || plugin.players.get(p).equalsIgnoreCase("Regular")) {                    
                        if (databaseHandler.hasRow(p)) {
                            query = "UPDATE " + plugin.dbTablePrefix + "_leaderboard SET timesAsRegular = timesAsRegular + 1 WHERE name = '" + p.getName().toLowerCase() + "' LIMIT 1;";
                        } else {
                            databaseHandler.createRow(p);
                            query = "UPDATE " + plugin.dbTablePrefix + "_leaderboard SET timesAsRegular = timesAsRegular + 1 WHERE name = '" + p.getName().toLowerCase() + "' LIMIT 1;";
                        }
                        databaseHandler.updateQuery(query);
                    } else {
                        if (databaseHandler.hasRow(p)) {
                            query = "UPDATE " + plugin.dbTablePrefix + "_leaderboard SET timesAsChaser = timesAsChaser + 1, winsAsChaser = winsAsChaser + 1 WHERE name = '" + p.getName().toLowerCase() + "' LIMIT 1;";
                        } else {
                            databaseHandler.createRow(p);
                            query = "UPDATE " + plugin.dbTablePrefix + "_leaderboard SET timesAsChaser = timesAsChaser + 1, winsAsChaser = winsAsChaser + 1 WHERE name = '" + p.getName().toLowerCase() + "' LIMIT 1;";
                        }
                        databaseHandler.updateQuery(query);
                    }
                }
            }
            
            this.cleanUpGame();
        }
    }
    
    public void regularVictory() {
        if (!this.checkVictory()) {
            Bukkit.getServer().broadcastMessage(PREFIX + "The game is over! The regulars have won!");
            
            if (config.leaderboard) {
                Iterator<Player> i = plugin.players.keySet().iterator();

                while (i.hasNext()) {
                    Player p = i.next();
                    String query;
                    if (plugin.players.get(p).equalsIgnoreCase("FROZEN") || plugin.players.get(p).equalsIgnoreCase("Regular")) {                    
                        if (databaseHandler.hasRow(p)) {
                            query = "UPDATE " + plugin.dbTablePrefix + "_leaderboard SET timesAsRegular = timesAsRegular + 1, winsAsRegular = winsAsRegular + 1 WHERE name = '" + p.getName().toLowerCase() + "' LIMIT 1;";
                        } else {
                            databaseHandler.createRow(p);
                            query = "UPDATE " + plugin.dbTablePrefix + "_leaderboard SET timesAsRegular = timesAsRegular + 1, winsAsRegular = winsAsRegular + 1 WHERE name = '" + p.getName().toLowerCase() + "' LIMIT 1;";
                        }
                        databaseHandler.updateQuery(query);
                    } else {
                        if (databaseHandler.hasRow(p)) {
                            query = "UPDATE " + plugin.dbTablePrefix + "_leaderboard SET timesAsChaser = timesAsChaser + 1 WHERE name = '" + p.getName().toLowerCase() + "' LIMIT 1;";
                        } else {
                            databaseHandler.createRow(p);
                            query = "UPDATE " + plugin.dbTablePrefix + "_leaderboard SET timesAsChaser = timesAsChaser + 1 WHERE name = '" + p.getName().toLowerCase() + "' LIMIT 1;";
                        }
                        databaseHandler.updateQuery(query);
                    }
                }
            }
            
            this.cleanUpGame();
        }
    }
    
    public boolean storeInventories() {
        if(plugin.players == null || plugin.players.isEmpty()) return false;
        
        Iterator i = plugin.players.keySet().iterator();
        
        while (i.hasNext()) {
            Player p = (Player) i.next();
            ItemStack[] pI = p.getInventory().getContents();
            
            inventories.put(p, pI);
            p.getInventory().clear();
            p.getInventory().addItem(new ItemStack(config.item, 1));
        }
        return true;
    }
    
    public boolean restoreInventories() {
        if (inventories == null || inventories.isEmpty()) return false;
        
        Iterator i = inventories.keySet().iterator();
        
        while (i.hasNext()) {
            Player p = (Player) i.next();
            p.getInventory().setContents(inventories.get(p));
            inventories.remove(p);
        }
        return true;
    }
    
    public boolean restoreInventory(Player player) {
        if (inventories == null || inventories.isEmpty() || !inventories.containsKey(player)) return false;
        
        player.getInventory().setContents(inventories.get(player));
        inventories.remove(player);
        return true;
    }
    
    public void telePlayersToArena() {
        if (!config.arena) return;
        Iterator<Player> i = plugin.players.keySet().iterator();
        
        while (i.hasNext()) {
            Player p = i.next();
            
            //if (config.numOfSpawns == 0 || config.spawns == null) {
                int[][] arr = new int[2][3];
                try {
                    String[] p1 = config.arena_area1.split(",");
                    String[] p2 = config.arena_area2.split(",");
                    arr = Arena.parseMinMax(p1, p2);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                Random random = new Random();
                int xGap = (arr[1][0] - arr[0][0]) - 2;
                int toX = (arr[0][0] + 1) + random.nextInt(xGap);
                int zGap = (arr[1][2] - arr[0][2]) - 2;
                int toZ = (arr[0][2] + 1) + random.nextInt(zGap);
                int toY = arr[0][1] + 1;
                World w = Bukkit.getServer().getWorld(config.arena_world);
                while (w.getBlockAt(toX, toY -1, toZ).getType() == Material.WATER || w.getBlockAt(toX, toY -1, toZ).getType() == Material.STATIONARY_WATER || w.getBlockAt(toX, toY -1, toZ).getType() == Material.STATIONARY_LAVA || w.getBlockAt(toX, toY -1, toZ).getType() == Material.LAVA) {
                    toX = arr[0][0] + random.nextInt(arr[1][0] - arr[0][0]);
                    toZ = arr[0][2] + random.nextInt(arr[1][2] - arr[0][2]);
                }
                
                while (w.getBlockAt(toX, toY, toZ).getType() != Material.AIR) {
                    toY = toY + 1;
                }
                p.teleport(new Location(w, toX, toY, toZ));
            //}
        }
    }
    
    public void telePlayerToLobby(Player p) {
        if (!config.lobby) return;
            
        int[][] arr = new int[2][3];
        try {
            String[] p1 = config.lobby_area1.split(",");
            String[] p2 = config.lobby_area2.split(",");
            arr = Arena.parseMinMax(p1, p2);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        Random random = new Random();
        int xGap = (arr[1][0] - arr[0][0]) - 2;
        int toX = (arr[0][0] + 1) + random.nextInt(xGap);
        int zGap = (arr[1][2] - arr[0][2]) - 2;
        int toZ = (arr[0][2] + 1) + random.nextInt(zGap);
        int toY = arr[0][1] + 1;
        World w = Bukkit.getServer().getWorld(config.lobby_world);
        while (w.getBlockAt(toX, toY, toZ).getType() == Material.AIR) {
            toY = toY - 1;
        }
        while (w.getBlockAt(toX, toY -1, toZ).getType() == Material.WATER || w.getBlockAt(toX, toY -1, toZ).getType() == Material.STATIONARY_WATER || w.getBlockAt(toX, toY -1, toZ).getType() == Material.STATIONARY_LAVA || w.getBlockAt(toX, toY -1, toZ).getType() == Material.LAVA) {
            toX = arr[0][0] + random.nextInt(arr[1][0] - arr[0][0]);
            toZ = arr[0][2] + random.nextInt(arr[1][2] - arr[0][2]);
        }

        while (w.getBlockAt(toX, toY, toZ).getType() != Material.AIR) {
            toY = toY + 1;
        }
        p.teleport(new Location(w, toX, toY, toZ));
    }
    
    public void unTelePlayers() {
        if (plugin.oldLocations == null) return;
        
        Iterator<Player> i = plugin.oldLocations.keySet().iterator();
        
        while (i.hasNext()) {
            Player player = i.next();
            player.teleport(plugin.oldLocations.get(player));
        }
        plugin.oldLocations.clear();
    }
    
    public void unTelePlayer(Player p) {
        if (plugin.oldLocations == null || !plugin.oldLocations.containsKey(p)) return;
        
        p.teleport(plugin.oldLocations.get(p));
        plugin.oldLocations.remove(p);
    }
    
}
