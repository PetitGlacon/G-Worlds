package fr.the__glacier.gworlds.commands.GWorldsSubCommand;

import fr.the__glacier.gcore.commands.utils.SubCommandInterface;
import fr.the__glacier.gcore.config.configObjects.SubCommandConfig;
import fr.the__glacier.gworlds.worlds.WorldsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CreateSubCMD implements SubCommandInterface {
    SubCommandConfig subCommandConfig;
    public CreateSubCMD(SubCommandConfig config){
        this.subCommandConfig = config;
    }
    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        commandSender.sendMessage("étape 1");
        WorldsManager wManager = new WorldsManager();
        if (args.length == 0){
            wManager.createWorld("Test", null);
        } else if (args.length == 1){
            wManager.createWorld(args[0], null);
        } else {
            return false;
        }
        commandSender.sendMessage("validé");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return List.of("Validé !");
    }

    @Override
    public SubCommandConfig getSubCommandConfig() {
        return subCommandConfig;
    }
}
