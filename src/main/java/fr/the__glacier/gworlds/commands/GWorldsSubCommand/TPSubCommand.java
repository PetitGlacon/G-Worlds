package fr.the__glacier.gworlds.commands.GWorldsSubCommand;

import fr.the__glacier.gcore.commands.utils.SubCommandInterface;
import fr.the__glacier.gcore.config.configObjects.SubCommandConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TPSubCommand implements SubCommandInterface {
    SubCommandConfig subCommandConfig;
    public TPSubCommand(SubCommandConfig config){
        this.subCommandConfig = config;
    }
    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length != 4) {
            commandSender.sendMessage("4 arguments");
            return true;
        }
        if (commandSender instanceof Player p){
            p.teleportAsync(new Location(Bukkit.getWorld(args[3]), Long.parseLong(args[0]), Long.parseLong(args[1]), Long.parseLong(args[2])));
            commandSender.sendMessage("Téléporté !");
        }
        commandSender.sendMessage("Terminé !");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        switch (args.length){
            case 1 : return List.of("<x>");
            case 2 : return List.of("<y>");
            case 3 : return List.of("<z>");
            case 4 : {
                List<String> list = new ArrayList<>();
                for (World w : Bukkit.getWorlds()){
                    list.add(w.getName());
                }
                return list;
            }
            default: return null;
        }
    }

    @Override
    public SubCommandConfig getSubCommandConfig() {
        return subCommandConfig;
    }
}
