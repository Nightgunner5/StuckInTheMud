package me.chrizc.sitm;

import com.alta189.sqllibrary.mysql.mysqlCore;
import com.alta189.sqllibrary.sqlite.sqlCore;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

import java.util.HashMap;
import java.util.logging.Logger;
import org.bukkit.Location;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;

public class StuckInTheMud extends JavaPlugin {
    
    //MySQL handlers
    public mysqlCore manageMySQL;
    public sqlCore manageSQLite;
    
    //MySQL settings variables
    public boolean MySQL = false;
    public String dbHost = null;
    public String dbUser = null;
    public String dbPass = null;
    public String dbDatabase = null;
    public String dbTablePrefix = null;
    
    private final SITMConfig config = new SITMConfig(this);
    private final SITMDatabaseHandler databaseHandler = new SITMDatabaseHandler(this, config);
    private final SITMGameHandler gameHandler = new SITMGameHandler(this, databaseHandler, config);
    private final SITMEntityListener entityListener = new SITMEntityListener(this, gameHandler);
    private final SITMPlayerListener playerListener = new SITMPlayerListener(this, gameHandler, databaseHandler, config);
    private final SITMBlockListener blockListener = new SITMBlockListener(this, config);
    private final SITMCommandListener cmdHandler = new SITMCommandListener(this, gameHandler, config, databaseHandler);
    
    public String logPrefix = "[StuckInTheMud] "; // Prefix to go in front of all log entries
    public static final Logger log = Logger.getLogger("Minecraft"); // Minecraft log and console
    
    //Permissions
    public static PermissionHandler permissionHandler;
    
    //Global variables.
    public boolean inGame = false;
    public boolean inCountdown = false;
    public boolean inRegistration = false;
    public boolean inAreaMode = false;
    public Player areaPlayer;
    public Arena arena;
    public enum areaMode { LOBBY_1, LOBBY_2, ARENA_1, ARENA_2, NONE }
    public areaMode mode = areaMode.NONE;
    public int numOfPlayers = 0;
    public int numOfFrozen = 0;
    public int numOfChasers = 0;
    public HashMap<Player, Location> oldLocations = new HashMap<Player, Location>();
    
    //Global player list.
    public HashMap<Player, String> players = new HashMap<Player, String>();
    
    @Override
    public void onDisable() {
        if (arena != null) {
            arena.tearDown();
            config.clearArena();
        }
        gameHandler.cleanUpGame();
        log.info(logPrefix + "disabled.");
    }
    
    @Override
    public void onEnable() {
        //Get plugin manager, to register events.
        PluginManager pm = getServer().getPluginManager();
        
        //Core game-related events.
        pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.Highest, this);
        pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Highest, this);
        
        //User-related events.
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_KICK, playerListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Highest, this);
        pm.registerEvent(Event.Type.PLAYER_RESPAWN, playerListener, Priority.Highest, this);
        
        //Block related events.
        pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Highest, this);
        pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Priority.Highest, this);
        pm.registerEvent(Event.Type.BLOCK_DAMAGE, blockListener, Priority.Monitor, this);
        
        config.doConfig();
        config.doArena();
        databaseHandler.setupDatabases();
        cmdHandler.setupCommands();
        setupPermissions();
        PluginDescriptionFile pdfFile = this.getDescription();
        log.info(logPrefix + "version v" + pdfFile.getVersion() + " is enabled.");
    }
    
    private void setupPermissions() {
      Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");

      if (this.permissionHandler == null) {
            if (permissionsPlugin != null) {
                this.permissionHandler = ((Permissions) permissionsPlugin).getHandler();
                log.info(logPrefix + "hooked into Permissions.");
            } else {
                log.info(logPrefix + "Permissions not found, defaulting to SuperPerms.");
            }
        }
    }
    
    public boolean checkPermissions(String node, Player player) {
        if (this.permissionHandler != null) {
            return this.permissionHandler.has(player, node);
        } else {
            return player.hasPermission(node);
        }
    }
}
