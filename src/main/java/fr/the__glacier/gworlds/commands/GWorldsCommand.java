package fr.the__glacier.gworlds.commands;

import fr.the__glacier.gcore.commands.utils.BrigadierCommands;
import fr.the__glacier.gcore.commands.utils.SubCommandsManager;
import fr.the__glacier.gcore.config.configObjects.CommandConfig;
import fr.the__glacier.gworlds.GWorlds;
import fr.the__glacier.gworlds.commands.GWorldsSubCommand.CreateSubCMD;
import fr.the__glacier.gworlds.commands.GWorldsSubCommand.GameruleSubCommand;
import fr.the__glacier.gworlds.commands.GWorldsSubCommand.TPSubCommand;
import fr.the__glacier.gworlds.configs.Commands;

public class GWorldsCommand extends BrigadierCommands {
    public GWorldsCommand(GWorlds plugin, SubCommandsManager commandManager, CommandConfig command){
        super(plugin, commandManager, command);
        if (command.subCommands != null){
            this.commandsManager.registerSubCommand(new CreateSubCMD(command.subCommands.get(Commands.subCommandsEnum.create.name()), this.cooldownManager, this.commandConfig.isOnCooldown));
            this.commandsManager.registerSubCommand(new GameruleSubCommand(command.subCommands.get(Commands.subCommandsEnum.gamerules.name()), this.cooldownManager, this.commandConfig.isOnCooldown));
            this.commandsManager.registerSubCommand(new TPSubCommand(command.subCommands.get(Commands.subCommandsEnum.teleport.name()), this.cooldownManager, this.commandConfig.isOnCooldown));
        }
        registerCommand();
    }

//    private void registerSubCommands(){
//        assert command.subCommands != null;
//        commandsManager.registerSubCommand(new CreateSubCMD(command.subCommands.get(fr.the__glacier.gworlds.configs.Commands.subCommandsEnum.create.name()), this.cooldownManager, this.command.isOnCooldown));
//        commandsManager.registerSubCommand(new TPSubCommand(command.subCommands.get(fr.the__glacier.gworlds.configs.Commands.subCommandsEnum.teleport.name())));
//        commandsManager.registerSubCommand(new GameruleSubCommand(command.subCommands.get(fr.the__glacier.gworlds.configs.Commands.subCommandsEnum.gamerules.name())));
//        // commandsManager.registerSubCommand(new TestSubCommand(new SubCommandConfig(List.of("test"), "", "", "", "", 0L, true)));
//
//    }
}
