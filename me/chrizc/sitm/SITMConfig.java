package me.chrizc.sitm;

import java.io.File;

import org.bukkit.util.config.Configuration;

public class SITMConfig {
    
    private final StuckInTheMud plugin;
    
    Configuration file;
    
    public boolean verbose;
    
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
            
            file.save();
            System.out.println("[StuckInTheMud] Configuration file created with default values!");
        }
    }
    
}
