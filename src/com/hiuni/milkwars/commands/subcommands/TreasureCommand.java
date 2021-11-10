package com.hiuni.milkwars.commands.subcommands;

import com.hiuni.milkwars.Clan;
import com.hiuni.milkwars.MilkWars;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.*;
import org.bukkit.entity.Player;

public class TreasureCommand {
    private final CommandAPICommand treasureSetLocation = new CommandAPICommand("setlocation")
            .withRequirement((sender -> {
                Player player = (Player) sender;

                // Check if player is in either clan, and if they're a leader
                for (Clan clan: MilkWars.clans) {
                    if (clan.hasLeader(player)) {
                        return true;
                    }
                }
                return false;
            }))
            .executesPlayer((player, args) -> {
                // Because of the requirement, we know the player
                // is a clan leader.

                // Here we check the various requirements for the flag pole location, which are:
                // - has to be in the overworld
                // - has to be within 5k of spawn
                // TODO: Check requirements

                Location location = player.getLocation();
                for (Clan clan: MilkWars.clans) {
                    if (clan.hasLeader(player)) {
                        clan.getFlag().setFlagPoleLocation(location);
                        player.sendMessage(
                                ChatColor.GREEN + "Successfully set the treasure's home location!"
                        );
                        return;
                    }
                }

                CommandAPI.fail("Something has gone wrong!");
            });

    public CommandAPICommand getCommand() {
        return new CommandAPICommand("treasure")
                .withSubcommand(treasureSetLocation);
    }
}