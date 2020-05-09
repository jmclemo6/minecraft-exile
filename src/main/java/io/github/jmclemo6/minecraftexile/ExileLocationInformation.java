package io.github.jmclemo6.minecraftexile;

import org.bukkit.World;

public class ExileLocationInformation {
    World world;
    Double[] coords;
    public ExileLocationInformation(World world, Double[] coords) {
        this.world = world;
        this.coords = coords;
    }
}
