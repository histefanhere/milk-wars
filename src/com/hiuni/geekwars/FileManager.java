package com.hiuni.geekwars;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class FileManager {
    static FileConfiguration fileConfig;
    static File file;
    static String fileName;

    public static boolean setup(JavaPlugin plugin, String ymlName) {
        file = new File(plugin.getDataFolder(), ymlName);

        if (!file.exists()) {
            Bukkit.getServer().getLogger().severe(ChatColor.RED + "[Geek-Wars] Could not find " + ymlName + " Creating new file.");
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                Bukkit.getServer().getLogger().severe("[Geek-Wars] Failed to create new file." +
                        "\n" + e.getMessage());
                return false;
            }
        }
        fileName = ymlName;
        fileConfig = YamlConfiguration.loadConfiguration(file);
        return true;
    }

    public static FileConfiguration getConfig() {
        return fileConfig;
    }

    public static boolean saveConfig() {
        try {
            fileConfig.save(file);
        } catch (IOException e) {
            Bukkit.getServer().getLogger().severe("[Geek-Wars] Failed to save " + fileName);
            return false;
        }
        return true;
    }

    public static void reloadConfig() {
        fileConfig = YamlConfiguration.loadConfiguration(file);
    }
}
