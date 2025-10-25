package fr.the__glacier.gworlds.commands.GWorldsSubCommand;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import fr.the__glacier.gcore.commands.utils.SubCommand;
import fr.the__glacier.gcore.commands.utils.arguments.EnumArgument;
import fr.the__glacier.gcore.config.configObjects.SubCommandConfig;
import fr.the__glacier.gcore.util.PlayerUtil;
import fr.the__glacier.gcore.util.TimeUtil;
import fr.the__glacier.gworlds.Enums.Gamerules;
import fr.the__glacier.gworlds.GWorlds;
import fr.the__glacier.gworlds.worlds.WorldsManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GameruleSubCommand extends SubCommand {
    public GameruleSubCommand(SubCommandConfig config, TimeUtil.CooldownManager cooldownManager, String cooldownMessage){
        super(config, cooldownManager, cooldownMessage);
    }

    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length != 3) {
            commandSender.sendMessage("3 arguments : World + Gamerule + value");
            return true;
        }
        WorldsManager worldsManager = GWorlds.getInstance().worldsManager;
        World world = Bukkit.getWorld(args[0]);
        Gamerules gamerule = Gamerules.valueOf(args[1]);
        if (world == null) return false;
        try {
            Integer i = Integer.parseInt(args[2]);
            worldsManager.changeGamerule(world, gamerule, i);
            PlayerUtil.sendMiniMessage(commandSender, "<blue>Gamerule <white>" + gamerule.name() + "</white> mis à <white>" + i + "</white>.");
            return true;
        } catch (NumberFormatException ignored){}
        if (args[2].equalsIgnoreCase("true") || args[2].equalsIgnoreCase("false")){
            Boolean b = Boolean.parseBoolean(args[2]);
            worldsManager.changeGamerule(world, gamerule, b);
            PlayerUtil.sendMiniMessage(commandSender, "<blue>Gamerule <white>" + gamerule.name() + "</white> changée en <white>" + b + "</white>.");
            return true;
        }
        PlayerUtil.sendMiniMessage(commandSender, this.config.syntax);
        return true;
    }

    public @Nullable List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        return switch (args.length) {
            case 1 -> {
                for (World w : Bukkit.getWorlds()) {
                    list.add(w.getName());
                }
                yield list;
            }
            case 2 -> {
                for (Gamerules gamerule : Gamerules.values()) {
                    list.add(gamerule.name());
                }
                yield list;
            }
            case 3 -> {
                Gamerules gamerule = Gamerules.valueOf(args[1]);
                yield switch (gamerule) {
                    case DO_DAYLIGHT_CYCLE, DO_ENTITY_DROPS, DO_FIRE_TICK, DO_IMMEDIATE_RESPAWN, DO_INSOMNIA,
                         DO_LIMITED_CRAFTING, DO_MOB_LOOT, DO_MOB_SPAWNING, DO_PATROL_SPAWNING, DO_TILE_DROPS,
                         DO_TRADER_SPAWNING, DO_VINES_SPREAD, DO_WARDEN_SPAWNING, DO_WEATHER_CYCLE,
                         ANNOUNCE_ADVANCEMENTS, BLOCK_EXPLOSION_DROP_DECAY, COMMAND_BLOCK_OUTPUT,
                         DISABLE_ELYTRA_MOVEMENT_CHECK, DISABLE_RAIDS, DROWNING_DAMAGE,
                         ENDER_PEARLS_VANISH_ON_DEATH, FALL_DAMAGE, FIRE_DAMAGE, FREEZE_DAMAGE, FORGIVE_DEAD_PLAYERS,
                         GLOBAL_SOUND_EVENTS, KEEP_INVENTORY, LAVA_SOURCE_CONVERSION, LOG_ADMIN_COMMANDS, MOB_GRIEFING,
                         MOB_EXPLOSION_DROP_DECAY, NATURAL_REGENERATION, PROJECTILES_CAN_BREAK_BLOCKS,
                         REDUCED_DEBUG_INFO, SEND_COMMAND_FEEDBACK, SHOW_DEATH_MESSAGES, SPECTATORS_GENERATE_CHUNKS,
                         TNT_EXPLOSION_DROP_DECAY, UNIVERSAL_ANGER, WATER_SOURCE_CONVERSION -> List.of("true", "false");
                    case COMMAND_MODIFICATION_BLOCK_LIMIT, MAX_COMMAND_CHAIN_LENGTH, MAX_COMMAND_FORK_COUNT,
                         MAX_ENTITY_CRAMMING, PLAYERS_NETHER_PORTAL_CREATIVE_DELAY, PLAYERS_NETHER_PORTAL_DEFAULT_DELAY,
                         PLAYERS_SLEEPING_PERCENTAGE, RANDOM_TICK_SPEED, SNOW_ACCUMULATION_HEIGHT, SPAWN_CHUNK_RADIUS,
                         SPAWN_RADIUS -> List.of("<Integer>");
                    default -> null;
                };
            }
            default -> null;
        };
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getCommand(String s) {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(s);
        command.requires(sender -> sender.getSender().hasPermission(this.config.permission));

        command.then(Commands.argument("world", ArgumentTypes.world())
                        .suggests(((context, builder) -> {
                            Bukkit.getWorlds().stream()
                                    .filter(world -> world.getName().toLowerCase().startsWith(builder.getRemainingLowerCase()))
                                    .forEach(world -> builder.suggest(world.getName()));
                            return builder.buildFuture();
                        }))
                .then(Commands.argument("gamerule", new EnumArgument<>(Gamerules.class, this.config.messages.getOrDefault("notAGamerule", "Not a valid gamerule !")))
                        .then(Commands.argument("intValue", IntegerArgumentType.integer())
                                .executes(this::changeGameruleInt)
                        )
                        .then(Commands.argument("booleanValue", BoolArgumentType.bool())
                                .suggests((context, builder) -> {
                                    String input = context.getInput();
                                    String[] inputs = input.split("\\s");
                                    String str;
                                    if (input.endsWith(" ")){
                                        str = inputs[inputs.length -1];
                                    } else {
                                        str = inputs[inputs.length - 2];
                                    }
                                    try {
                                        Gamerules gamerule = Gamerules.valueOf(str.toUpperCase());
                                        switch (gamerule){
                                            case ANNOUNCE_ADVANCEMENTS,
                                                 DO_INSOMNIA,
                                                 DO_MOB_LOOT,
                                                 FALL_DAMAGE,
                                                 FIRE_DAMAGE,
                                                 DO_FIRE_TICK,
                                                 MOB_GRIEFING,
                                                 DISABLE_RAIDS,
                                                 DO_TILE_DROPS,
                                                 FREEZE_DAMAGE,
                                                 KEEP_INVENTORY,
                                                 DO_ENTITY_DROPS,
                                                 DO_MOB_SPAWNING,
                                                 DO_VINES_SPREAD,
                                                 DROWNING_DAMAGE,
                                                 UNIVERSAL_ANGER,
                                                 DO_WEATHER_CYCLE,
                                                 DO_DAYLIGHT_CYCLE,
                                                 DO_PATROL_SPAWNING,
                                                 DO_TRADER_SPAWNING,
                                                 DO_WARDEN_SPAWNING,
                                                 LOG_ADMIN_COMMANDS,
                                                 REDUCED_DEBUG_INFO,
                                                 DO_LIMITED_CRAFTING,
                                                 GLOBAL_SOUND_EVENTS,
                                                 SHOW_DEATH_MESSAGES,
                                                 COMMAND_BLOCK_OUTPUT,
                                                 DO_IMMEDIATE_RESPAWN,
                                                 FORGIVE_DEAD_PLAYERS,
                                                 NATURAL_REGENERATION,
                                                 SEND_COMMAND_FEEDBACK,
                                                 LAVA_SOURCE_CONVERSION,
                                                 WATER_SOURCE_CONVERSION,
                                                 MOB_EXPLOSION_DROP_DECAY,
                                                 TNT_EXPLOSION_DROP_DECAY,
                                                 BLOCK_EXPLOSION_DROP_DECAY,
                                                 SPECTATORS_GENERATE_CHUNKS,
                                                 ENDER_PEARLS_VANISH_ON_DEATH,
                                                 PROJECTILES_CAN_BREAK_BLOCKS,
                                                 DISABLE_PLAYER_MOVEMENT_CHECK,
                                                 DISABLE_ELYTRA_MOVEMENT_CHECK -> {
                                                builder.suggest("true");
                                                builder.suggest("false");
                                            }
                                            case SPAWN_RADIUS,
                                                 RANDOM_TICK_SPEED,
                                                 MINECART_MAX_SPEED,
                                                 SPAWN_CHUNK_RADIUS,
                                                 MAX_ENTITY_CRAMMING,
                                                 MAX_COMMAND_FORK_COUNT,
                                                 MAX_COMMAND_CHAIN_LENGTH,
                                                 SNOW_ACCUMULATION_HEIGHT,
                                                 PLAYERS_SLEEPING_PERCENTAGE,
                                                 COMMAND_MODIFICATION_BLOCK_LIMIT,
                                                 PLAYERS_NETHER_PORTAL_DEFAULT_DELAY,
                                                 PLAYERS_NETHER_PORTAL_CREATIVE_DELAY -> {
                                                builder.restart();
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(this::changeGameruleBool))
                )
        );

        return command;
    }

    public int changeGameruleInt(CommandContext<CommandSourceStack> context){
        World world = context.getArgument("world", World.class);
        Gamerules gamerule = context.getArgument("gamerule", Gamerules.class);
        int value = context.getArgument("intValue", int.class);
        WorldsManager worldsManager = GWorlds.getInstance().worldsManager;
        worldsManager.changeGamerule(world, gamerule.getGamerule(), value);
        return 0;
    }
    public int changeGameruleBool(CommandContext<CommandSourceStack> context){
        World world = context.getArgument("world", World.class);
        Gamerules gamerule = context.getArgument("gamerule", Gamerules.class);
        boolean value = context.getArgument("booleanValue", boolean.class);
        WorldsManager worldsManager = GWorlds.getInstance().worldsManager;
        worldsManager.changeGamerule(world, gamerule.getGamerule(), value);
        return 0;
    }
}
