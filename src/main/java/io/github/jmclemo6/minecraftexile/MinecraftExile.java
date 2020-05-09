package io.github.jmclemo6.minecraftexile;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;

public final class MinecraftExile extends JavaPlugin {
    Exile exile = new Exile();
    @Override
    public void onEnable() {
        // Plugin startup logic
        try {
            BufferedReader in = new BufferedReader(new FileReader("./minecraft-exile-location-backup.txt"));
            String location;
            while ((location = in.readLine()) != null) {
                String[] parts = location.trim().split(";");
                String locationName = parts[0];
                ExileLocationInformation locationData = GetExileLocationFromString(parts[1]);
                if (locationData == null) return;

                this.exile.exileCoords.put(
                        locationName,
                        locationData
                );
            }

            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.getCommand("exile").setExecutor(this.exile);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("./minecraft-exile-location-backup.txt"));
            this.exile.exileCoords.forEach((key, value) -> {
                String backup = String.format(
                        "%s;%s",
                        key,
                        GetStringFromExileLocation(value)
                );

                try {
                    out.write(backup);
                    out.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String GetStringFromExileLocation(ExileLocationInformation info) {
        String worldName;
        try {
            worldName = info.world.getName();
        } catch (NullPointerException e) {
            return "";
        }

        try {
            return String.format(
                    "%s:%s:%s:%s",
                    worldName,
                    info.coords[0],
                    info.coords[1],
                    info.coords[2]
            );
        } catch (ArrayIndexOutOfBoundsException e) {
            return "";
        }
    }

    private ExileLocationInformation GetExileLocationFromString(String s) {
        if (s == null || s.equals("")) return null;

        String[] parts = s.split(":");
        if (parts.length != 4) return null;

        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;

        double x, y, z;
        try {
            x = Double.parseDouble(parts[1]);
            y = Double.parseDouble(parts[2]);
            z = Double.parseDouble(parts[3]);
        } catch (NumberFormatException e) {
            return null;
        }

        return new ExileLocationInformation(
                world,
                new Double[] {
                        x,
                        y,
                        z
                }
        );
    }
}
