package io.github.jmclemo6.minecraftexile;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Exile implements CommandExecutor {
    Map<String, ExileLocationInformation> exileCoords = new HashMap<>();
    List<String> playersToExile = new ArrayList<>();
    String placeToExileTo = "";
    String reasonForExile = "";
    String[] subcommands = new String[] {
            "confirm",
            "register",
            "delete",
            "locations",
            "cancel"
    };

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
       if (!(sender instanceof Player) || args.length == 0) return false;
       Player exiler = (Player) sender;
       if (!exiler.hasPermission("exile.canexile")) {
           exiler.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
           return true;
       }

       String subcommand = args[0];

       if (subcommand.equalsIgnoreCase("confirm")) {
           ExileLocationInformation exileInformation = exileCoords.get(placeToExileTo);

           if (exileInformation == null) {
               return false;
           }

           Location exileLocation = new Location(
                   exileInformation.world,
                   exileInformation.coords[0],
                   exileInformation.coords[1],
                   exileInformation.coords[2]
           );

           for (String nameOfPlayerToExile : playersToExile) {
               Player playerEntity = Bukkit.getPlayer(nameOfPlayerToExile);
               if (playerEntity != null) {
                   playerEntity.teleport(exileLocation);
                   String message = String.format(
                           "%s was exiled to %s (%s @ %s,%s,%s) for %s",
                           nameOfPlayerToExile,
                           placeToExileTo,
                           exileInformation.world.getName(),
                           exileLocation.getX(),
                           exileLocation.getY(),
                           exileLocation.getZ(),
                           reasonForExile
                   );
                   Bukkit.broadcastMessage(message);
               } else {
                   String message = String.format(
                           "Player %s could not be found",
                           nameOfPlayerToExile
                   );
                   exiler.sendMessage(ChatColor.RED + message);
               }
           }

           reasonForExile = "";
           placeToExileTo = "";
           playersToExile.clear();
       } else if (subcommand.equalsIgnoreCase("register")) {
           String exileLocationName;
           try {
               exileLocationName = args[1];
           } catch (ArrayIndexOutOfBoundsException e) {
               return false;
           }

           if (Arrays.asList(subcommands).contains(exileLocationName)) {
               exiler.sendMessage(ChatColor.RED + "Your location name matches a subcommand's name. Please choose a different one.");
               return false;
           }

           World worldToExileTo = exiler.getWorld();
           Double[] coordsToExileTo;
           String x, y, z;
           try {
               x = args[2];
               y = args[3];
               z = args[4];
           } catch (ArrayIndexOutOfBoundsException e) {
               return false;
           }

           try {
               coordsToExileTo = new Double[]{
                       Double.parseDouble(x),
                       Double.parseDouble(y),
                       Double.parseDouble(z)
               };
           } catch (NumberFormatException e) {
               return false;
           }

           exileCoords.put(
                   exileLocationName,
                   new ExileLocationInformation(worldToExileTo, coordsToExileTo)
           );

           String successMessage = String.format(
               "Exile location %s registered for world %s with coordinates %s %s %s",
               exileLocationName,
               worldToExileTo.getName(),
               x,
               y,
               z
           );
           exiler.sendMessage(ChatColor.GREEN + successMessage);
       } else if (subcommand.equalsIgnoreCase("delete")) {
           String exileLocationName;
           try {
               exileLocationName = args[1];
           } catch (ArrayIndexOutOfBoundsException e) {
               return false;
           }

           if (!exileCoords.containsKey(exileLocationName)) {
               String message = String.format(
                       "The exile location %s is not currently registered? Did you misspell it?",
                       exileLocationName
               );
               exiler.sendMessage(ChatColor.RED + message);
               return true;
           }

           exileCoords.remove(exileLocationName);
       } else if (subcommand.equalsIgnoreCase("locations")) {
           if (exileCoords.isEmpty()) {
               exiler.sendMessage("There are no locations registered.");
           } else {
               exiler.sendMessage(ChatColor.UNDERLINE + "Locations currently registered:");
               exileCoords.forEach((key, value) -> {
                   String message = String.format(
                           "%s: %s @ %s %s %s",
                           key,
                           value.world.getName(),
                           value.coords[0],
                           value.coords[1],
                           value.coords[2]
                   );
                   exiler.sendMessage(message);
               });
           }
       } else if (subcommand.equalsIgnoreCase("cancel")) {
           playersToExile.clear();
           placeToExileTo = "";
           reasonForExile = "";

           Bukkit.broadcast(ChatColor.RED + "Exile cancelled.", "exile.canexile");
       } else {
           int locationOfFirstTo = Arrays.asList(args).indexOf("to");
           playersToExile = new ArrayList<>(Arrays.asList(Arrays.copyOfRange(args, 0, locationOfFirstTo)));
           placeToExileTo = args[locationOfFirstTo + 1];

           int locationOfFirstFor = Arrays.asList(args).indexOf("for");
           reasonForExile = String.join(
                   " ",
                   Arrays.asList(
                           Arrays.copyOfRange(
                                   args,
                                   locationOfFirstFor + 1,
                                   args.length
                           )
                   )
           );


           String allPlayersToExile = String.join(",", playersToExile);
           String confirmationMessage = String.format(
                   "Use [/exile confirm] to confirm the exile of %s to %s for %s or [/exile cancel] to cancel this exile.",
                   allPlayersToExile,
                   placeToExileTo,
                   reasonForExile
           );
           Bukkit.broadcast(ChatColor.GREEN + confirmationMessage, "exile.canexile");
       }

       return true;
    }
}
