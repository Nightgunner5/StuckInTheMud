package me.chrizc.sitm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Random;
import me.chrizc.sitm.StuckInTheMud.areaMode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

import org.bukkit.entity.Player;

public class SITMCommandListener {
    
    private final StuckInTheMud plugin;
    private final SITMGameHandler gameHandler;
    private final SITMConfig config;
    private final SITMDatabaseHandler databaseHandler;
    
    public String PREFIX = ChatColor.DARK_GREEN + "[StuckInTheMud] ";
    public String ERR_PREFIX = ChatColor.RED + "[StuckInTheMud] ";
    
    public SITMCommandListener(StuckInTheMud instance, SITMGameHandler game, SITMConfig config, SITMDatabaseHandler database) {
        plugin = instance;
        gameHandler = game;
        databaseHandler = database;
        this.config = config;
    }
    
    public void setupCommands() {
        PluginCommand mud = plugin.getCommand("mud");
        CommandExecutor commandExecutor = new CommandExecutor() {
            public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {
                if (sender instanceof Player) {
                    if (args.length > 0) {
                        commandHandler((Player)sender, args);
                    }
                }
                
                  
                return true;
            }
        };
        if (mud != null) {
            mud.setExecutor(commandExecutor);
        }
    }
    
    public void commandHandler(Player player, String[] args) {
        if (args[0].equalsIgnoreCase("reg")) {
            gameHandler.startGame(player);
        } else if (args[0].equalsIgnoreCase("join")) {
            gameHandler.joinGame(player);
        } else if (args[0].equalsIgnoreCase("unreg")) {
            gameHandler.unreg(player);
        } else if (args[0].equalsIgnoreCase("begin")) {
            gameHandler.beginGame(player);
        } else if (args[0].equalsIgnoreCase("cancel")) {
            gameHandler.cancelCountdown(player);
        } else if (args[0].equalsIgnoreCase("list")) {
            gameHandler.listPlayers(player);
        } else if (args[0].equalsIgnoreCase("define")) {
            if (args.length < 1) {
                player.sendMessage(ERR_PREFIX + "You must specify an argument.");
            } else {
                if (plugin.checkPermissions("sitm.admin.define", player)) {
                    if (args[1].equalsIgnoreCase("lobby")) {
                        plugin.inAreaMode = true;
                        plugin.mode = areaMode.LOBBY_1;
                        plugin.areaPlayer = player;
                        player.sendMessage(PREFIX + "You're in lobby define mode! Punch the first block of your cuboid.");
                    } else if (args[1].equalsIgnoreCase("arena")) {
                        plugin.inAreaMode = true;
                        plugin.mode = areaMode.ARENA_1;
                        plugin.areaPlayer = player;
                        player.sendMessage(PREFIX + "You're in arena define mode! Punch the first block of your cuboid.");
                    }
                }
            }
        } else if (args[0].equalsIgnoreCase("generate")) {
            if (!plugin.checkPermissions("sitm.admin.generate", player)) return;
            
            if (args.length == 4) {
                Location loc = player.getLocation();
                plugin.arena = Arena.build(new Location(loc.getWorld(), loc.getX(), loc.getY()-1, loc.getZ()), Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), new Random());
                String[] bounds = plugin.arena.getBounds();
                config.arena = true;
                config.arena_area1 = bounds[0];
                config.arena_area2 = bounds[1];
                config.arena_world = player.getWorld().getName();
                config.saveArena();
                player.sendMessage(PREFIX + "Arena generated!");
            }
        } else if (args[0].equalsIgnoreCase("teardown")) {
            if (!plugin.checkPermissions("sitm.admin.teardown", player)) return;
            
            if (plugin.arena == null) return;
            
            plugin.arena.tearDown();
            config.clearArena();
            plugin.arena = null;
        } else if (args[0].equalsIgnoreCase("freeze")) {
            if (args.length == 1) { 
                player.sendMessage(ERR_PREFIX + "You must define a player to freeze."); 
                return; 
            }
            
            if (!plugin.checkPermissions("sitm.admin.freeze", player)) return;
            
            if (!plugin.inGame && !plugin.inRegistration) return;
            
            Player p = Bukkit.getServer().getPlayer(args[1]);
            
            if (!plugin.players.containsKey(p) || plugin.players.get(p).equalsIgnoreCase("FROZEN")) return;
            
            plugin.players.put(p, "FROZEN");
            p.sendMessage(PREFIX + "You've been frozen by " + ChatColor.YELLOW + player.getName() + ChatColor.DARK_GREEN + "!");
            player.sendMessage(PREFIX + "You've frozen " + ChatColor.YELLOW + args[1] + ChatColor.DARK_GREEN + "!");
        } else if (args[0].equalsIgnoreCase("unfreeze")) {
            if (args.length == 1) { 
                player.sendMessage(ERR_PREFIX + "You must define a player to unfreeze."); 
                return; 
            }
            
            if (!plugin.checkPermissions("sitm.admin.freeze", player)) return;
            
            if (!plugin.inGame && !plugin.inRegistration) return;
            
            Player p = Bukkit.getServer().getPlayer(args[1]);
            
            if (!plugin.players.containsKey(p) || plugin.players.get(p).equalsIgnoreCase("Regular")) return;
            
            plugin.players.put(p, "Regular");
            p.sendMessage(PREFIX + "You've been un-frozen by " + ChatColor.YELLOW + player.getName() + ChatColor.DARK_GREEN + "!");
            player.sendMessage(PREFIX + "You've un-frozen " + ChatColor.YELLOW + args[1] + ChatColor.DARK_GREEN + "!");
        } else if (args[0].equalsIgnoreCase("forcereg")) {
            if (args.length == 1) {
                player.sendMessage(ERR_PREFIX + "You must define a player to force register.");
                return;
            }
            
            if (!plugin.checkPermissions("sitm.admin.forcereg", player)) return;
            
            if (!plugin.inRegistration) return;
            
            Player p = Bukkit.getServer().getPlayer(args[1]);
            
            if (!p.isOnline()) return;
            
            if (plugin.players == null || plugin.players.isEmpty()) return;
            
            if (plugin.players.containsKey(p)) {
                player.sendMessage(ERR_PREFIX + "That player is already registered!");
                return;
            }
            
            gameHandler.joinGame(p);
            player.sendMessage(PREFIX + "You've successfully forced " + ChatColor.YELLOW + p.getName() + ChatColor.DARK_GREEN + " to register.");
        } else if (args[0].equalsIgnoreCase("endgame")) {
            System.out.println(plugin.checkPermissions("sitm.admin.endgame", player));
            if (!plugin.checkPermissions("sitm.admin.endgame", player)) return;
            
            if (plugin.inGame || plugin.inRegistration) {
                gameHandler.cleanUpGame();
                Bukkit.getServer().broadcastMessage(PREFIX + ChatColor.YELLOW + player.getName() + ChatColor.DARK_GREEN + " has ended the current game!");
            }
        } else if (args[0].equalsIgnoreCase("rank")) {
            String query = "SELECT * FROM " + plugin.dbTablePrefix + "_leaderboard WHERE name = '" + player.getName().toLowerCase() + "';";
            ResultSet result = databaseHandler.doQuery(query);
            
            try {
                if (result.next()) {
                    double wins = result.getInt("winsAsRegular") + result.getInt("winsAsChaser");
                    double plays = result.getInt("timesAsRegular") + result.getInt("timesAsChaser");
                    double ratio = 0;
                    if (plays > 0) {
                        ratio = roundTwoDecimals((wins / plays) * 100);
                    }
                    player.sendMessage(PREFIX + "[" + ChatColor.YELLOW + player.getName() + ChatColor.DARK_GREEN + 
                            "] Wins: " + ChatColor.YELLOW + wins + ChatColor.DARK_GREEN + " (Chaser/Regular: " + 
                            ChatColor.YELLOW + result.getInt("winsAsChaser") + "/" + result.getInt("winsAsRegular") +
                            ChatColor.DARK_GREEN + ") Times Played: " + ChatColor.YELLOW + plays + ChatColor.DARK_GREEN + 
                            " (Chaser/Regular: " + ChatColor.YELLOW + result.getInt("timesAsChaser") + "/" + 
                            result.getInt("timesAsRegular") + ChatColor.DARK_GREEN + ") Win Ratio: " + ChatColor.YELLOW +
                            ratio + "%");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (args[0].equalsIgnoreCase("top5")) {
            String checkQuery = "SELECT COUNT(*) AS 'Count' FROM " + plugin.dbTablePrefix + "_leaderboard LIMIT 5;";
            ResultSet checkResult = databaseHandler.doQuery(checkQuery);
            String query = "SELECT * FROM " + plugin.dbTablePrefix + "_leaderboard ORDER BY winsAsRegular DESC, winsAsChaser DESC LIMIT 5;";
            ResultSet result = databaseHandler.doQuery(query);
            
            try {
                if (checkResult.next() && checkResult.getInt("Count") != 0) {
                    int i = 1;
                    player.sendMessage(" ");
                    player.sendMessage(PREFIX + "Top 5 players:");
                    while (result.next()) {
                        String name = result.getString("name");
                        double wins = result.getInt("winsAsRegular") + result.getInt("winsAsChaser");
                        double plays = result.getInt("timesAsRegular") + result.getInt("timesAsChaser");
                        double ratio = 0;
                        if (plays > 0) {
                            ratio = roundTwoDecimals((wins / plays) * 100);
                        }
                        player.sendMessage(PREFIX + ChatColor.YELLOW + i + ChatColor.DARK_GREEN + ") " + ChatColor.YELLOW +
                                name + ChatColor.DARK_GREEN + " Wins: " + ChatColor.YELLOW + wins + ChatColor.DARK_GREEN +
                                " Losses: " + ChatColor.YELLOW + (plays - wins) + ChatColor.DARK_GREEN +
                                " Win Ratio: " + ChatColor.YELLOW + ratio + ChatColor.DARK_GREEN + "%");
                        i++;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (args[0].equalsIgnoreCase("takemetoarena")) {
            gameHandler.telePlayersToArena();
        }
    }
    
    double roundTwoDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(d));
    }
    
}
