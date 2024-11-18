package fr.the__glacier.gworlds.commands;

import fr.the__glacier.gcore.commands.utils.Commands;
import fr.the__glacier.gcore.commands.utils.SubCommandInterface;
import fr.the__glacier.gcore.commands.utils.SubCommandsManager;
import fr.the__glacier.gcore.config.configObjects.CommandConfig;
import fr.the__glacier.gcore.config.configObjects.SubCommandConfig;
import fr.the__glacier.gcore.util.PlayerUtil;
import fr.the__glacier.gworlds.GWorlds;
import fr.the__glacier.gworlds.commands.GWorldsSubCommand.CreateSubCMD;
import fr.the__glacier.gworlds.commands.GWorldsSubCommand.GameruleSubCommand;
import fr.the__glacier.gworlds.commands.GWorldsSubCommand.TPSubCommand;
import fr.the__glacier.gworlds.commands.GWorldsSubCommand.TestSubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class GWorldsCommand extends Commands {
    public GWorldsCommand(GWorlds plugin, GWorldsCommandManager commandManager, CommandConfig command){
        super(plugin, commandManager, command);
        registerSubCommands();
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 0){
            String subCommand = args[0].trim().toLowerCase();
            String[] listArgs = Arrays.copyOfRange(args, 1, args.length);
            SubCommandInterface subCommandInterface = this.commandsManager.getSubCommand(subCommand);
            if (subCommandInterface != null){
                boolean b = subCommandInterface.onCommand(plugin, sender, command, label, listArgs);
                if (!b){
                    PlayerUtil.sendMiniMessage(sender, subCommandInterface.getSubCommandConfig().syntax);
                }
            } else {
                PlayerUtil.sendMiniMessage(sender, GWorlds.getInstance().commands.syntax);
            }
            return true;
        };
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> tab = new ArrayList<>();
        if (args.length == 1){
            tab.addAll(commandsManager.getCommandMap().keySet().stream().filter(str -> str.startsWith(args[args.length -1])).toList());
        } else {
            String arg = args[0];
            SubCommandInterface subCommandInterface = commandsManager.getSubCommand(arg);
            if (subCommandInterface != null){
                tab = subCommandInterface.onTabComplete(plugin, sender, command, label, Arrays.copyOfRange(args, 1, args.length));
                if (tab != null){
                    tab = tab.stream().filter(str -> str.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).toList();
                }
                return tab;
            }
        }
        return tab;
    }

    private void registerSubCommands(){
        assert command.subCommands != null;
        commandsManager.registerSubCommand(new CreateSubCMD(command.subCommands.get(fr.the__glacier.gworlds.configs.Commands.subCommandsEnum.create.name())));
        commandsManager.registerSubCommand(new TPSubCommand(command.subCommands.get(fr.the__glacier.gworlds.configs.Commands.subCommandsEnum.teleport.name())));
        commandsManager.registerSubCommand(new GameruleSubCommand(command.subCommands.get(fr.the__glacier.gworlds.configs.Commands.subCommandsEnum.gamerules.name())));
        commandsManager.registerSubCommand(new TestSubCommand(new SubCommandConfig(List.of("test"), "", "", "", "", 0L, true)));
    }



    public static class GWorldsCommandManager extends SubCommandsManager {
        public Map<String, SubCommandInterface> subCommands = new HashMap<>();

        @Override
        public void registerSubCommand(@NotNull SubCommandInterface subCommandInterface) {
            if (!subCommandInterface.getSubCommandConfig().enabled) return;
            for (String str : subCommandInterface.getSubCommandConfig().aliases){
                subCommands.put(str, subCommandInterface);
            }
        }

        @Override
        public SubCommandInterface getSubCommand(@NotNull String s) {
            return subCommands.getOrDefault(s, null);
        }

        @Override
        public Map<String, SubCommandInterface> getCommandMap() {
            return subCommands;
        }

    }
}
