package me.chrizc.sitm;

import java.io.File;
import java.util.List;

import org.bukkit.util.config.Configuration;

public class SITMConfig {
    
    private final StuckInTheMud plugin;
    
    Configuration file;
    
    public boolean verbose = false;
    
    //Leaderboard variables
    public boolean leaderboard;
    
    //Various variables
    public int item;
    
    //Lobby variables
    public boolean lobby;
    public String lobby_world;
    public String lobby_area1;
    public String lobby_area2;
    
    //Arena variables
    public boolean arena;
    public String arena_world;
    public String arena_area1;
    public String arena_area2;
    public int numOfSpawns;
    public List<String> spawns;
    
    public SITMConfig(StuckInTheMud instance) {
        plugin = instance;
    }
    
    public void doConfig() {
        file = new Configuration(new File(plugin.getDataFolder(), "config.yml"));
        file.load();
        
        verbose = file.getBoolean("verbose", false);
        
        if (new File(plugin.getDataFolder(), "config.yml").exists()) {
            if (verbose == true) {
                System.out.println("[StuckInTheMud] Configuration file loaded!");
            }
        } else {
            file.setProperty("verbose", false);

            file.setProperty("useMySQL", false);
            file.setProperty("database.host", "localhost");
            file.setProperty("database.username", "root");
            file.setProperty("database.password", "password");
            file.setProperty("database.dbname", "stuckinthemud");
            file.setProperty("database.tablePrefix", "sitm");
            
            file.setProperty("listItem" , 359);

            file.setProperty("leaderboard", true);

            file.save();
            System.out.println("[StuckInTheMud] Configuration file created with default values!");
        }
        
        plugin.MySQL = file.getBoolean("useMySQL", false);
        plugin.dbHost = file.getString("database.host", "localhost");
        plugin.dbUser = file.getString("database.username", "root");
        plugin.dbPass = file.getString("database.password", "password");
        plugin.dbDatabase = file.getString("database.dbname", "stuckinthemud");
        plugin.dbTablePrefix = file.getString("database.tablePrefix", "sitm");
        
        item = file.getInt("listItem", 359);
        
        leaderboard = file.getBoolean("leaderboard", true);
    }
    
    public void doArena() {
        file = new Configuration(new File(plugin.getDataFolder(), "arena.yml"));
        file.load();
        
        if (new File(plugin.getDataFolder(), "arena.yml").exists()) {
            if (verbose == true) {
                System.out.println("[StuckInTheMud] Arena file loaded!");
            }
        } else {
            //Create empty lobby nodes.
            file.setProperty("arena.lobby.defined", false);
            file.setProperty("arena.lobby.world", null);
            file.setProperty("arena.lobby.p1", null);
            file.setProperty("arena.lobby.p2", null);
            
            //Create empty arena nodes
            file.setProperty("arena.area.defined", false);
            file.setProperty("arena.area.world", null);
            file.setProperty("arena.area.p1", null);
            file.setProperty("arena.area.p2", null);
            file.setProperty("arena.spawns.amount", 0);
            
            file.save();
            System.out.println("[StuckInTheMud] Arena file created with default values!");
        }
        
        arena = file.getBoolean("arena.area.defined", false);
        lobby = file.getBoolean("arena.lobby.defined", false);
        numOfSpawns = file.getInt("arena.spawns.amount", 0);
        
        //Check if arena is properly defined.
        if (arena) {
            arena_world = file.getString("arena.area.world", null);
            arena_area1 = file.getString("arena.area.p1", null);
            arena_area2 = file.getString("arena.area.p2", null);
            if (arena_world == null || arena_area1 == null || arena_area2 == null) arena = false;
        }
        
        //Check if lobby is properly defined.
        if (lobby) {
            lobby_world = file.getString("arena.lobby.world", null);
            lobby_area1 = file.getString("arena.lobby.p1", null);
            lobby_area2 = file.getString("arena.lobby.p2", null);
            if (lobby_world == null || lobby_area1 == null || lobby_area2 == null) lobby = false;
        }
        
        //Check that the spawnpoints are properly defined.
        if (numOfSpawns > 0) {
            spawns = file.getKeys("arena.spawns");
            spawns.remove("amount");
            numOfSpawns = spawns.size();
        }
        
    }
    
    public boolean saveLobby() {
        file = new Configuration(new File(plugin.getDataFolder(), "arena.yml"));
        file.load();
        if (new File(plugin.getDataFolder(), "arena.yml").exists()) {
            if (lobby_area1 == null || lobby_area2 == null || lobby_world == null) return false;
            file.setProperty("arena.lobby.defined", lobby);
            file.setProperty("arena.lobby.world", lobby_world);
            file.setProperty("arena.lobby.p1", lobby_area1);
            file.setProperty("arena.lobby.p2", lobby_area2);
            file.save();
            return true;
        }
        return false;
    }
    
    public boolean saveArena() {
        file = new Configuration(new File(plugin.getDataFolder(), "arena.yml"));
        file.load();
        if (new File(plugin.getDataFolder(), "arena.yml").exists()) {
            if (arena_area1 == null || arena_area2 == null || arena_world == null) return false;
            file.setProperty("arena.area.defined", arena);
            file.setProperty("arena.area.world", arena_world);
            file.setProperty("arena.area.p1", arena_area1);
            file.setProperty("arena.area.p2", arena_area2);
            file.save();
            return true;
        }
        return false;
    }
    
    public void clearArena() {
        file = new Configuration(new File(plugin.getDataFolder(), "arena.yml"));
        file.load();
        if (new File(plugin.getDataFolder(), "arena.yml").exists()) {
            file.setProperty("arena.area.defined", false);
            file.setProperty("arena.area.world", null);
            file.setProperty("arena.area.p1", null);
            file.setProperty("arena.area.p2", null);
            file.save();
            arena = false;
        }
    }
    
}
