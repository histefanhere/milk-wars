package com.hiuni.geekwars.commands.subcommands;

import com.hiuni.geekwars.Clan;
import com.hiuni.geekwars.ClanMember;
import com.hiuni.geekwars.Flag;
import com.hiuni.geekwars.GeekWars;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.Collator;
import java.util.Collection;
import java.util.TreeSet;

public class ClanCommand {
    private final CommandAPICommand clanJoin = new CommandAPICommand("join")
            .withArguments(new PlayerArgument("player"))
            .withArguments(new MultiLiteralArgument("cows", "sheep"))
            .executes((sender, args) -> {
                Player player = (Player) args[0];

                int clanIndex = 0;
                switch ((String) args[1]) {
                    case "cows" -> clanIndex = 0;
                    case "sheep" -> clanIndex = 1;
                }
                Clan clan = GeekWars.clans[clanIndex];
                Clan oppositeClan = GeekWars.clans[1 - clanIndex];

                // First test if they're a part of the sheep clan...
                if (oppositeClan.hasMember(player)) {
                    player.sendMessage(
                            ChatColor.RED + "Cannot be a member of both clans!"
                    );
                    CommandAPI.fail("Cannot be a member of both clans!");
                    return;
                }

                // Try to add them to the clan...
                if (clan.addMember(player)) {
                    sender.sendMessage(
                            String.format(
                                    ChatColor.GREEN + "Successfully added %s to the %s!",
                                    player.getName(),
                                    clan.getName()
                            )
                    );
                    player.sendMessage(
                            String.format(ChatColor.GREEN + "Welcome to the %s!", clan.getName())
                    );
                    new SettingsCommand().updateNameTag(player);
                    return;
                }

                // They're already a part of the clan!
                player.sendMessage(
                        ChatColor.RED + "You are already a member of the clan!"
                );
                CommandAPI.fail("Player is already a member of the clan!");
            });

    private final CommandAPICommand clanLeave = new CommandAPICommand("leave")
            .withArguments(new PlayerArgument("player"))
            .executes((sender, args) -> {
                Player player = (Player) args[0];

                // Try to add the player to each clan
                for (Clan clan: GeekWars.clans) {
                    if (clan.removeMember(player)) {
                        sender.sendMessage(
                                String.format(
                                        ChatColor.GREEN + "Successfully removed %s from the %s!",
                                        player.getName(),
                                        clan.getName()
                                )
                        );
                        player.sendMessage(
                                String.format(ChatColor.GREEN + "Left the %s", clan.getName())
                        );
                        new SettingsCommand().updateNameTag(player);

                        // If the player is carrying a flag, it needs to be dropped.
                        for (Clan flagCheck: GeekWars.clans) {
                            if (player.getUniqueId().equals(flagCheck.getFlag().getWearer())) {
                                flagCheck.getFlag().dropFlag();
                            }
                        }

                        return;
                    }
                }

                // If we've got here, the player isn't in any clan
                CommandAPI.fail("Player isn't a part of a clan!");
            });

    private final CommandAPICommand leadersMembersKick = new CommandAPICommand("kick")
            .withArguments(new PlayerArgument("player"))
            .executesPlayer((player, args) -> {
                Player playerToKick = (Player) args[0];

                for (Clan clan: GeekWars.clans) {
                    if (clan.hasMember(player)) {
                        // We've found the clan of the leader.
                        // Now lets check whether the player he wants to kick is just a member

                        if (!clan.hasMember(playerToKick)) {
                            CommandAPI.fail("That player is not in your clan!");
                        }

                        if (clan.hasLeader(playerToKick)) {
                            CommandAPI.fail("You cannot kick leaders from your clan!");
                            return;
                        }

                        if (clan.removeMember(playerToKick)) {
                            new SettingsCommand().updateNameTag(playerToKick);
                            player.sendMessage(
                                    String.format(
                                            ChatColor.GREEN + "Successfully kicked %s from the %s!",
                                            playerToKick.getName(),
                                            clan.getName()
                                    )
                            );
                            playerToKick.sendMessage(
                                    String.format(
                                            ChatColor.YELLOW + "You have been kicked from the %s", clan.getName()
                                    )
                            );

                            // If the player is carrying a flag, it needs to be dropped.
                            for (Clan flagCheck: GeekWars.clans) {
                                if (player.getUniqueId().equals(flagCheck.getFlag().getWearer())) {
                                    flagCheck.getFlag().dropFlag();
                                }
                            }
                        }
                        else {
                            CommandAPI.fail("Unable to kick member from your clan!");
                        }
                        return;
                    }
                }
            });


    private final CommandAPICommand membersList = new CommandAPICommand("list")
            .withArguments(new MultiLiteralArgument("cows", "sheep"))
            .executes((sender, args) -> {
                int clanIndex = 0;
                switch ((String) args[0]) {
                    case "cows" -> clanIndex = 0;
                    case "sheep" -> clanIndex = 1;
                }
                Clan clan = GeekWars.clans[clanIndex];

                sendList(sender, clan);
            });

    private final CommandAPICommand leadersMembersList = new CommandAPICommand("list")
            .executesPlayer((player, args) -> {
                // For this command to be ran the player must be a leader
                // therefore we're certain he's in a clan
                for (Clan clan: GeekWars.clans) {
                    if (clan.hasLeader(player)) {
                        sendList(player, clan);
                        return;
                    }
                }

                CommandAPI.fail("Something went wrong!");
            });

    private void sendList(CommandSender sender, Clan clan) {
        int clanSize = clan.getAllMembers().size();
        if (clanSize == 0) {
            sender.sendMessage(String.format(ChatColor.YELLOW + "The %s is empty", clan.getName()));
            return;
        }

        // Tree sets are automatically sorted. Handy!
        Collection<String> names = new TreeSet<>(Collator.getInstance());
        String out = ChatColor.YELLOW + "Members of the " + clan.getName() + " (" +
                ChatColor.GOLD + "leaders" + ChatColor.YELLOW + "):\n";

        for (ClanMember clanMember: clan.getAllMembers()) {
            if (clanMember.isLeader()) {
                names.add(ChatColor.GOLD + clanMember.getName());
            }
            else {
                names.add(ChatColor.YELLOW + clanMember.getName());
            }
        }

        String delimiter = ChatColor.YELLOW + ", ";
        if (clanSize > 15) {
            delimiter = "\n";
        }
        out += String.join(delimiter, names);

        sender.sendMessage(out);
    }

    private final CommandAPICommand membersPromote = new CommandAPICommand("promote")
            .withArguments(new PlayerArgument("player"))
            .executes((sender, args) -> {
               Player player = (Player) args[0];

                // Try and promote the player in both clans
                for (Clan clan: GeekWars.clans) {
                    if (clan.promote(player)) {
                        sender.sendMessage(ChatColor.GREEN + "Promoted Successfully");
                        player.sendMessage(
                                String.format(ChatColor.GREEN + "You are now a leader of the %s!", clan.getName())
                        );
                        return;
                    }
                }

                // If we've got here, the player isn't in any clan
                CommandAPI.fail("Player must be a normal member of a clan to be promoted");
            });

    private final CommandAPICommand membersDemote = new CommandAPICommand("demote")
            .withArguments(new PlayerArgument("player"))
            .executes((sender, args) -> {
                Player player = (Player) args[0];

                // Try and demote the player in both clans
                for (Clan clan: GeekWars.clans) {
                    if (clan.demote(player)) {
                        sender.sendMessage(ChatColor.GREEN + "Demoted Successfully");
                        player.sendMessage(
                                String.format(ChatColor.GREEN + "You have been demoted from the %s!", clan.getName())
                        );
                        return;
                    }
                }

                // If we've got here, the player isn't a leader of a clan
                CommandAPI.fail("Player must be a leader of a clan to be demoted");
            });

    private final CommandAPICommand membersSignIn = new CommandAPICommand("signin")
            .withArguments(new PlayerArgument("player"))
            .withArguments(new MultiLiteralArgument("cows", "sheep"))
            .executes((sender, args) -> {
                Player player = (Player) args[0];

                int clanIndex = 0;
                switch ((String) args[1]) {
                    case "cows" -> clanIndex = 0;
                    case "sheep" -> clanIndex = 1;
                }
                Clan clan = GeekWars.clans[clanIndex];

                // If the player is in the clan, try signing them in
                if (clan.hasMember(player)) {
                    if (clan.getMember(player).signIn()) {
                        // Signed in successfully
                        sender.sendMessage(ChatColor.GREEN + "Signed in successfully");
                        player.sendMessage(ChatColor.GREEN + "You are now signed in. Good luck!");

                        // Update the player's name tag
                        new SettingsCommand().updateNameTag(player);
                    } else {
                        // Couldn't sign in
                        sender.sendMessage(ChatColor.RED + "Player is already signed in");
                        player.sendMessage(ChatColor.RED + "You are already signed in!");
                    }
                }
                else {
                    // Player isn't in the clan
                    sender.sendMessage(ChatColor.RED + "Player is not in the clan!");
                    player.sendMessage(ChatColor.RED + "You are not in this clan!");
                }
            });

    private final CommandAPICommand membersSignOut = new CommandAPICommand("signout")
            .withArguments(new PlayerArgument("player"))
            .withArguments(new MultiLiteralArgument("cows", "sheep"))
            .executes((sender, args) -> {
                Player player = (Player) args[0];

                int clanIndex = 0;
                switch ((String) args[1]) {
                    case "cows" -> clanIndex = 0;
                    case "sheep" -> clanIndex = 1;
                }
                Clan clan = GeekWars.clans[clanIndex];

                // If the player is in the clan, try signing them out
                if (clan.hasMember(player)) {
                    if (clan.getMember(player).signOut()) {
                        // Signed out successfully
                        sender.sendMessage(ChatColor.GREEN + "Signed out successfully");
                        player.sendMessage(ChatColor.GREEN + "You are now signed out.");

                        // Update the player's name tag
                        new SettingsCommand().updateNameTag(player);
                    } else {
                        // Couldn't sign out
                        sender.sendMessage(ChatColor.RED + "Player is already signed out");
                        player.sendMessage(ChatColor.RED + "You are already signed out!");
                    }
                }
                else {
                    // Player isn't in the clan
                    sender.sendMessage(ChatColor.RED + "Player is not in the clan!");
                    player.sendMessage(ChatColor.RED + "You are not in this clan!");
                }
            });

    private final CommandAPICommand clanGetHead = new CommandAPICommand("gethead")
            .withArguments(new MultiLiteralArgument("cows", "sheep"))
            .withArguments(new MultiLiteralArgument("active", "inactive"))
            .executesPlayer((player, args) -> {
                int clanIndex = 0;
                switch ((String) args[0]) {
                    case "cows" -> clanIndex = 0;
                    case "sheep" -> clanIndex = 1;
                }
                Clan clan = GeekWars.clans[clanIndex];

                boolean active = args[1].equals("active");

                ItemStack head = Flag.getHead(clanIndex, active);

                boolean accepted = player.getInventory().addItem(head).size() == 0;
                if (accepted) {
                    player.sendMessage(
                            String.format(ChatColor.GREEN + "Got an %s %s head!", args[1], clan.getName())
                    );
                }
                else {
                    CommandAPI.fail("Your inventory is full!");
                }
            });

    public CommandAPICommand getCommand() {
        return new CommandAPICommand("clan")
                .withSubcommand(clanJoin)
                .withSubcommand(clanLeave)
                .withSubcommand(clanGetHead)
                .withSubcommand(new CommandAPICommand("members")
                        .withSubcommand(membersList)
                        .withSubcommand(membersPromote)
                        .withSubcommand(membersDemote)
                        .withSubcommand(membersSignIn)
                        .withSubcommand(membersSignOut)
                )
                .withSubcommand(new TreasureCommand().getOpCommand());
    }

    public CommandAPICommand getLeadersMembersCommand() {
        return new CommandAPICommand("members")
                .withSubcommand(leadersMembersList)
                .withSubcommand(leadersMembersKick);
    }
}
