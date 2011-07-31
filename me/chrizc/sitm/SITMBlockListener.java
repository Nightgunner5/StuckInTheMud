package me.chrizc.sitm;

import me.chrizc.sitm.StuckInTheMud.areaMode;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

public class SITMBlockListener extends BlockListener {
    
    private final StuckInTheMud plugin;
    private final SITMConfig config;
    
    public String PREFIX = ChatColor.DARK_GREEN + "[StuckInTheMud] ";
    public String ERR_PREFIX = ChatColor.RED + "[StuckInTheMud] ";
    
    public SITMBlockListener(StuckInTheMud instance, SITMConfig config) {
        plugin = instance;
        this.config = config;
    }
    
    @Override
    public void onBlockDamage(BlockDamageEvent event) {
        if (!plugin.inAreaMode) return;
        
        if (plugin.mode == areaMode.NONE) return;
        
        if (plugin.areaPlayer == null) return;
        
        if (plugin.areaPlayer != event.getPlayer()) return;
        
        Block block = event.getBlock();
            
        if (plugin.mode == areaMode.LOBBY_1) {
            if (config.lobby_world == null) {
                config.lobby_world = block.getWorld().getName();
            }
            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();

            config.lobby_area1 = x + "," + y + "," + z;
            event.getPlayer().sendMessage(PREFIX + "Lobby position #1 set to " + ChatColor.YELLOW + config.lobby_area1 + ChatColor.DARK_GREEN + " in world " + ChatColor.YELLOW + config.lobby_world);
            plugin.mode = areaMode.LOBBY_2;
        } else if (plugin.mode == areaMode.LOBBY_2) {
            if (!config.lobby_world.equals(block.getWorld().getName())) return;
            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();

            config.lobby_area2 = x + "," + y + "," + z;
            config.lobby = true;
            event.getPlayer().sendMessage(PREFIX + "Lobby position #2 set to " + ChatColor.YELLOW + config.lobby_area2 + ChatColor.DARK_GREEN + " in world " + ChatColor.YELLOW + config.arena_world);
            if(config.saveLobby()) {
                event.getPlayer().sendMessage(PREFIX + "Lobby region defined!");
            } else {
                event.getPlayer().sendMessage(ERR_PREFIX + "Error in saving lobby region definition!");
                return;
            } 
            plugin.inAreaMode = false;
            plugin.areaPlayer = null;
            plugin.mode = areaMode.NONE;
        } else if (plugin.mode == areaMode.ARENA_1) {
            if (config.arena_world == null) {
                config.arena_world = block.getWorld().getName();
            }
            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();

            config.arena_area1 = x + "," + y + "," + z;
            event.getPlayer().sendMessage(PREFIX + "Arena position #1 set to " + ChatColor.YELLOW + config.arena_area1 + ChatColor.DARK_GREEN + " in world " + ChatColor.YELLOW + config.arena_world);
            plugin.mode = areaMode.ARENA_2;
        } else {
            if (!config.arena_world.equals(block.getWorld().getName())) return;
            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();

            config.arena_area2 = x + "," + y + "," + z;
            config.arena = true;
            event.getPlayer().sendMessage(PREFIX + "Arena position #2 set to " + ChatColor.YELLOW + config.arena_area2 + ChatColor.DARK_GREEN + " in world " + ChatColor.YELLOW + config.arena_world);
            if(config.saveArena()) {
                event.getPlayer().sendMessage(PREFIX + "Arena region defined!");
            } else {
                event.getPlayer().sendMessage(ERR_PREFIX + "Error in saving arena region definition!");
                return;
            }   
            plugin.inAreaMode = false;
            plugin.areaPlayer = null;
                     
            plugin.mode = areaMode.NONE;
        }
    }
    
    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!config.arena && !config.lobby) return;
        
        if (config.arena) {
            int[][] arr = new int[2][3];
            try {
                String[] p1 = config.arena_area1.split(",");
                String[] p2 = config.arena_area2.split(",");
                arr = Arena.parseMinMax(p1, p2);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            if (Arena.isWithin(arr[0], arr[1], event.getBlock())) event.setCancelled(true);
        }
        
        if (config.lobby) {
            int[][] arr1 = new int[2][3];
            try {
                String[] p1 = config.lobby_area1.split(",");
                String[] p2 = config.lobby_area2.split(",");
                arr1 = Arena.parseMinMax(p1, p2);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            if (Arena.isWithin(arr1[0], arr1[1], event.getBlock())) event.setCancelled(true);
        }
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (!config.arena && !config.lobby) return;
        
        if (config.arena) {
            int[][] arr = new int[2][3];
            try {
                String[] p1 = config.arena_area1.split(",");
                String[] p2 = config.arena_area2.split(",");
                arr = Arena.parseMinMax(p1, p2);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            if (Arena.isWithin(arr[0], arr[1], event.getBlock())) event.setCancelled(true);
        }
        
        if (config.lobby) {
            int[][] arr1 = new int[2][3];
            try {
                String[] p1 = config.lobby_area1.split(",");
                String[] p2 = config.lobby_area2.split(",");
                arr1 = Arena.parseMinMax(p1, p2);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            if (Arena.isWithin(arr1[0], arr1[1], event.getBlock())) event.setCancelled(true);
        }
    }
}
