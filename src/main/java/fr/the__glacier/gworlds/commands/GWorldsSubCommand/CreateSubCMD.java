package fr.the__glacier.gworlds.commands.GWorldsSubCommand;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import fr.the__glacier.gcore.commands.utils.SubCommand;
import fr.the__glacier.gcore.config.configObjects.SubCommandConfig;
import fr.the__glacier.gcore.util.TimeUtil;
import fr.the__glacier.gworlds.GWorlds;
import fr.the__glacier.gworlds.configs.WorldConfig;
import fr.the__glacier.gworlds.worlds.WorldsManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.WorldType;

public class CreateSubCMD extends SubCommand {
    SubCommandConfig subCommandConfig;
    public CreateSubCMD(SubCommandConfig config, TimeUtil.CooldownManager cooldownManager, String cooldownMessage){
        super(config, cooldownManager, cooldownMessage);
        this.subCommandConfig = config;
    }


    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getCommand(String s) {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(s);
        command.requires(sender -> sender.getSender().hasPermission(this.config.permission));

        command.then(Commands.argument("worldName", StringArgumentType.word())
                .then(Commands.argument("worldGenerator", StringArgumentType.word())
                        .executes(this::createWorldWorldGen))
                .executes(this::createWorld));

        return command;
    }

    public int createWorld(CommandContext<CommandSourceStack> context){
        String worldName = context.getArgument("worldName", String.class);
        WorldsManager worldsManager = new WorldsManager();
        worldsManager.createWorld(worldName, null, WorldType.NORMAL);
        context.getSource().getSender().sendMessage("Création validée");
        createFile(worldName, null);
        return 0;
    }

    public int createWorldWorldGen(CommandContext<CommandSourceStack> context){
        String worldName = context.getArgument("worldName", String.class);
        String worldGenerator = context.getArgument("worldGenerator", String.class);
        WorldsManager worldsManager = GWorlds.getInstance().worldsManager;
        switch (worldGenerator){
            case "amplified" -> worldsManager.createWorld(worldName, null, WorldType.AMPLIFIED);
            case "flat" -> worldsManager.createWorld(worldName, null, WorldType.FLAT);
            case  "large_biomes" -> worldsManager.createWorld(worldName, null, WorldType.LARGE_BIOMES);
            default -> worldsManager.createWorld(worldName, worldsManager.getWorldGenerator(worldGenerator), WorldType.NORMAL);
        }
        createFile(worldName, worldGenerator);
        return 0;
    }


    public void createFile(String name, String generator){
        WorldConfig worldConfig = new WorldConfig(name, false, generator, "normal", "normal", null);
        GWorlds.getInstance().worldConfigMap.put(name, worldConfig);
        GWorlds.getInstance().saveWorldsConfig();
    }
    @Override
    public SubCommandConfig getSubCommandConfig() {
        return subCommandConfig;
    }
}
