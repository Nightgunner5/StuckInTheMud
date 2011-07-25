package me.chrizc.sitm;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;

public class StuckInTheMud extends JavaPlugin {
    
    private final SITMConfig config = new SITMConfig(this);
    private final SITMGameHandler gameHandler = new SITMGameHandler(this);
    private final SITMPlayerListener playerListener = new SITMPlayerListener(this, gameHandler);
    private final SITMCommandListener cmdHandler = new SITMCommandListener(this);
    
    public String logPrefix = "[StuckInTheMud] "; // Prefix to go in front of all log entries
    public static final Logger log = Logger.getLogger("Minecraft"); // Minecraft log and console
    
    //Permissions
    public static PermissionHandler permissionHandler;
    
    //Global variables.
    public boolean inGame = false;
    public boolean inCountdown = false;
    public boolean inRegistration = false;
    public int numOfPlayers = 0;
    
    //Global player list.
    public HashMap<Player, String> players = new HashMap<Player, String>();
    
    @Override
    public void onDisable() {
        log.info(logPrefix + "disabled.");
    }
    
    @Override
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.ENTITY_DAMAGE, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_KICK, playerListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
        //config.doConfig();
        //updater.update(true);
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
