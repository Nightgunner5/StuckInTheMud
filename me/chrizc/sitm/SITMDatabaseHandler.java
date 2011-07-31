package me.chrizc.sitm;

import com.alta189.sqllibrary.mysql.mysqlCore;
import com.alta189.sqllibrary.sqlite.sqlCore;

import java.util.logging.Logger;

import java.net.MalformedURLException;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.bukkit.entity.Player;

public class SITMDatabaseHandler {
    
    private final StuckInTheMud plugin;
    SITMConfig config;
    
    public String logPrefix = "[StuckInTheMud] "; // Prefix to go in front of all log entries
    public Logger log = Logger.getLogger("Minecraft"); // Minecraft log and console
    
    
    public SITMDatabaseHandler(StuckInTheMud instance, SITMConfig config) {
        plugin = instance;
        this.config = config;
    }
    
    public void setupDatabases() {
        if (plugin.MySQL) {
            plugin.manageMySQL = new mysqlCore(log, logPrefix, plugin.dbHost, plugin.dbDatabase, plugin.dbUser, plugin.dbPass);
            if (config.verbose) {
                log.info(logPrefix + "MySQL initializing.");
            }
            plugin.manageMySQL.initialize();

            try {
                if (plugin.manageMySQL.checkConnection()) {
                    if (config.verbose == true) {
                        log.info(logPrefix + "MySQL connection successful");
                    }

                    if (!plugin.manageMySQL.checkTable(plugin.dbTablePrefix + "_leaderboard")) {
                        if (config.verbose == true) {
                            log.info(logPrefix + "Creating leaderboard table.");
                        }
                        String query = "CREATE TABLE " + plugin.dbTablePrefix + "_leaderboard (id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, name VARCHAR(50) NOT NULL, timesAsChaser INT NOT NULL, timesAsRegular INT NOT NULL, winsAsChaser INT NOT NULL, winsAsRegular INT NOT NULL);";
                        plugin.manageMySQL.createTable(query);
                    }
                } else {
                    log.severe(logPrefix + "MySQL connection failed");
                    plugin.MySQL = false;
                }
            } catch (MalformedURLException e) {
                    e.printStackTrace();
            } catch (InstantiationException e) {
                    e.printStackTrace();
            } catch (IllegalAccessException e) {
                    e.printStackTrace();
            }

        } else {

            if (config.verbose == true) {
                log.info(logPrefix + "SQLite initializing.");
            }

            plugin.manageSQLite = new sqlCore(log, logPrefix, "StuckInTheMud", plugin.getDataFolder().toString());

            plugin.manageSQLite.initialize();

            if (!plugin.manageSQLite.checkTable(plugin.dbTablePrefix + "_leaderboard")) {
                if (config.verbose == true) {
                    log.info(logPrefix + "Creating leaderboard table.");
                }
                String query = "CREATE TABLE " + plugin.dbTablePrefix + "_leaderboard (id INT PRIMARY_KEY AUTO_INCREMENT, name VARCHAR(50), timesAsChaser INT, timesAsRegular INT, winsAsChaser INT, winsAsRegular INT););";
                plugin.manageSQLite.createTable(query);
            }
        }
    }
    
    public boolean hasRow(Player player) {
        String query = "SELECT COUNT(*) AS `Count` FROM " + plugin.dbTablePrefix + "_leaderboard WHERE name = '" + player.getName().toLowerCase() + "' LIMIT 1;";
        ResultSet result = this.doQuery(query);
        
        try {
            if (result.next() && result.getInt("Count") == 1) return true;
            else return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean createRow(Player player) {
        if (this.hasRow(player)) return false;
        
        String query = "INSERT INTO " + plugin.dbTablePrefix + "_leaderboard (name, timesAsChaser, timesAsRegular, winsAsRegular, winsAsChaser) VALUES ('" + player.getName().toLowerCase() + "', 0, 0, 0, 0);";
        if (this.insertQuery(query)) return true;
        else return false;
        
    }
    
    public boolean updateQuery(String query) {
        if (plugin.MySQL) {
            try {
                plugin.manageMySQL.updateQuery(query);
                return true;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return false;
        } else {
            plugin.manageSQLite.updateQuery(query);
            return true;
        }
    }
    
    public ResultSet doQuery(String query) {
        ResultSet result = null;
        if (plugin.MySQL) {
            try {
                result = plugin.manageMySQL.sqlQuery(query);
                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        } else {
            return plugin.manageSQLite.sqlQuery(query);
        }
    }
    
    public boolean insertQuery(String query) {
        if (plugin.MySQL) {
            try {
                plugin.manageMySQL.insertQuery(query);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return false;
            } catch (InstantiationException e) {
                e.printStackTrace();
                return false;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        } else {
            plugin.manageSQLite.insertQuery(query);
            return true;
        }
    }
    
    public boolean deleteQuery(String query) {
        if (plugin.MySQL) {
            try {
                plugin.manageMySQL.insertQuery(query);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return false;
            } catch (InstantiationException e) {
                e.printStackTrace();
                return false;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        } else {
            plugin.manageSQLite.insertQuery(query);
            return true;
        }
    }
    
}
