package com.hiuni.milkwars.commands.subcommands;

import com.hiuni.milkwars.Clan;
import com.hiuni.milkwars.ClanMember;
import com.hiuni.milkwars.MilkWars;
import com.hiuni.milkwars.commands.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

public class MembersCommand extends SubCommand {

    @Override
    public String getName() {
        return "members";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getSyntax() {
        return null;
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length >= 2) {

            // Handles the "/clan members list ..." subcommand option
            if (args[1].equalsIgnoreCase("list")) {
                if (args.length == 3) {
                    if (args[2].equalsIgnoreCase("cows")) {
                        // List all members of cow clan
                        listClan(player, MilkWars.clans[0]);
                    }
                    else if (args[2].equalsIgnoreCase("sheep")) {
                        // List all members of sheep clan
                        listClan(player, MilkWars.clans[1]);
                    }
                    else {
                        player.sendMessage("Usage: /clan members list [cows | sheep]");
                    }
                }
                else {
                    for (Clan clan: MilkWars.clans) {
                        if (clan.hasMember(player)) {
                            listClan(player, clan);
                            return;
                        }
                    }
                    // Player is not a part of any clan
                    player.sendMessage(ChatColor.RED + "You are not a part of any clan");
                }
            }

            // Handles the "/clan members join ..." subcommand option
            else if (args[1].equalsIgnoreCase("join")) {
                if (args.length == 3) {
                    if (args[2].equalsIgnoreCase("cows")) {
                        joinClan(player, 0);
                    }
                    else if (args[2].equalsIgnoreCase("sheep")) {
                        joinClan(player, 1);
                    }
                    else {
                        player.sendMessage("Usage: /clan members join <cows | sheep>");
                    }
                }
                else {
                    player.sendMessage("Usage: /clan members join <cows | sheep>");
                }
            }

            // Handles the "/clan members leave ..." subcommand option
            else if (args[1].equalsIgnoreCase("leave")) {
                if (MilkWars.clans[0].removeMember(player)) {
                    player.sendMessage(
                            ChatColor.YELLOW + "Left the " + MilkWars.clans[0].getName()
                    );
                }
                else if (MilkWars.clans[1].removeMember(player)) {
                    player.sendMessage(
                            ChatColor.YELLOW + "Left the " + MilkWars.clans[1].getName()
                    );
                }
                // Player is in no clan
                else {
                    player.sendMessage(
                            ChatColor.RED + "You are not in any clan!"
                    );
                }
            }

            // Handles the "/clan members promote ..." subcommand option
            else if (args[1].equalsIgnoreCase("promote")) {
                if (args.length == 3) {
                    // Find the player from args
                    Player targetPlayer = null;
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.getName().equals(args[2])) {
                            targetPlayer = p;
                            break;
                        }
                    }

                    if (targetPlayer != null) {
                        // We've found the player!

                        // Try and promote the player in both clans
                        for (Clan clan: MilkWars.clans) {
                            if (clan.promote(player)) {
                                player.sendMessage(ChatColor.GREEN + "Promoted Successfully");
                                player.sendMessage(
                                        ChatColor.GREEN + "You are now a leader of the " + clan.getName() + "!"
                                );
                                return;
                            }
                        }

                        // If we've got here, the player isn't in any clan
                        player.sendMessage(ChatColor.RED + "Player must be a normal member of a clan to be promoted");

                    }
                    else {
                        // We couldn't find the player
                        player.sendMessage(ChatColor.RED + "Invalid player");
                    }
                }
                else {
                    player.sendMessage("Usage: /clan members promote <player>");
                }
            }

            // Handles the "/clan members demote ..." subcommand option
            else if (args[1].equalsIgnoreCase("demote")) {
                if (args.length == 3) {
                    // Find the player from args
                    Player targetPlayer = null;
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.getName().equals(args[2])) {
                            targetPlayer = p;
                            break;
                        }
                    }

                    if (targetPlayer != null) {
                        // We've found the player!

                        // Try and demote the player in both clans
                        for (Clan clan: MilkWars.clans) {
                            if (clan.demote(player)) {
                                player.sendMessage(ChatColor.GREEN + "Demoted Successfully");
                                player.sendMessage(
                                        ChatColor.GREEN + "You have been demoted from the " + clan.getName()
                                );
                                return;
                            }
                        }

                        // If we've got here, the player isn't in any clan
                        player.sendMessage(ChatColor.RED + "Player must be a leader of a clan to be demoted");

                    }
                    else {
                        // We couldn't find the player
                        player.sendMessage(ChatColor.RED + "Invalid player");
                    }
                }
                else {
                    player.sendMessage("Usage: /clan members promote <player>");
                }
            }

            else {
                player.sendMessage("Usage: /clan members <list | join | leave | promote | demote>");
            }
        }
        else {
            player.sendMessage("Usage: /clan members <list | join | leave | promote | demote>");
        }
    }

    private void joinClan(Player player, int clan) {
        int oppositeClan = 1 - clan;
        // First test if they're a part of the sheep clan...
        if (MilkWars.clans[oppositeClan].hasMember(player)) {
            player.sendMessage(ChatColor.RED + "You cannot be a member of both clans!");
            return;
        }

        // Try to add them to the clan...
        if (MilkWars.clans[clan].addMember(player)) {
            player.sendMessage(
                    ChatColor.GREEN + "Welcome to the " + MilkWars.clans[clan].getName() + "!"
            );
        }
        // They're already a part of the clan!
        else {
            player.sendMessage(
                    ChatColor.RED + "You are already in the " + MilkWars.clans[clan].getName() + "!"
            );
        }
    }

    private void listClan(Player player, Clan clan) {
        // List all the members of clan `clan` and send it to `player`

        int clanSize = clan.getAllMembers().size();
        if (clanSize == 0) {
            player.sendMessage(ChatColor.YELLOW + "The " + clan.getName() + " is empty");
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

        player.sendMessage(out);
    }

    // This method gets called when tab completion options need to be presented
    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {

        List<String> arguments = new ArrayList<>();

        // The player has typed in "/clan members ..." and needs to be suggested
        // A subcommand option
        if (args.length == 2) {
            arguments.add("list");
            arguments.add("join");
            arguments.add("leave");
            arguments.add("promote");
            arguments.add("demote");
        }

        // The player has typed in "/clan members option" and some options need
        // to be passed a value, e.g. "/clan members list sheep"
        else if (args.length == 3) {
            switch (args[1].toLowerCase()) {
                case "list":
                case "join":
                    arguments.add("cows");
                    arguments.add("sheep");
                    break;
                case "promote":
                case "demote":
                    for (Player p: Bukkit.getOnlinePlayers()) {
                        arguments.add(p.getName());
                    }
                    break;
                case "leave":
                default:
                    break;
            }
        }

        return arguments;
    }
}
