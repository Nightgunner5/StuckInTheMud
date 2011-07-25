package me.chrizc.sitm;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

import org.bukkit.entity.Player;

public class SITMCommandListener {
    
    private final StuckInTheMud plugin;
    
    public SITMCommandListener(StuckInTheMud instance) {
        plugin = instance;
    }
    
    public void setupCommands() {
        PluginCommand mud = plugin.getCommand("mud");
        CommandExecutor commandExecutor = new CommandExecutor() {
            public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {
                if (sender instanceof Player) {
                    if (args.length > 0) {
                        //stocks(sender, args);
                    }
                } else {
                    //consolestocks(sender, args);
                }
                
                  
                return true;
            }
        };
        if (mud != null) {
            mud.setExecutor(commandExecutor);
        }
    }
    
}
