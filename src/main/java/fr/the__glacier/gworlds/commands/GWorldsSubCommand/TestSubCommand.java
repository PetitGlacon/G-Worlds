package fr.the__glacier.gworlds.commands.GWorldsSubCommand;

import fr.the__glacier.gcore.commands.utils.deprecated.SubCommandInterface;
import fr.the__glacier.gcore.config.configObjects.SubCommandConfig;
import fr.the__glacier.gcore.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TestSubCommand implements SubCommandInterface {
    SubCommandConfig subCommandConfig;

    public TestSubCommand(SubCommandConfig config) {
        this.subCommandConfig = config;
    }

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        int i = 0;
        PlayerUtil.sendMiniMessage(commandSender, "<gray>----------<gold><bold>WORLDS</gold></bold>----------");
        for (World w : Bukkit.getWorlds()) {
            PlayerUtil.sendMiniMessage(commandSender, "<aqua>[" + i + "]" + " " + w.getName());
            i++;
        }
        PlayerUtil.sendMiniMessage(commandSender, "<gray>---------------------");
        if (args.length != 0){
            String arg = args[0];
            GameRule<Boolean> gameRule = GameRule.DO_DAYLIGHT_CYCLE;
            commandSender.sendMessage(String.valueOf(gameRule.getType().isInstance(Boolean.valueOf(arg))));
            commandSender.sendMessage(String.valueOf(gameRule.getType().isInstance(arg)));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }

    @Override
    public SubCommandConfig getSubCommandConfig() {
        return subCommandConfig;
    }
}