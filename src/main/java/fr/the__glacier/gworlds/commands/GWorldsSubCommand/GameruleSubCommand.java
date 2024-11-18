package fr.the__glacier.gworlds.commands.GWorldsSubCommand;

import fr.the__glacier.gcore.commands.utils.SubCommandInterface;
import fr.the__glacier.gcore.config.configObjects.SubCommandConfig;
import fr.the__glacier.gcore.util.PlayerUtil;
import fr.the__glacier.gworlds.Enums.Gamerules;
import fr.the__glacier.gworlds.GWorlds;
import fr.the__glacier.gworlds.worlds.WorldsManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GameruleSubCommand implements SubCommandInterface {
    SubCommandConfig subCommandConfig;
    public GameruleSubCommand(SubCommandConfig config){
        this.subCommandConfig = config;
    }
    @Override
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
        PlayerUtil.sendMiniMessage(commandSender, subCommandConfig.syntax);
        return true;
    }

    @Override
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
    public SubCommandConfig getSubCommandConfig() {
        return subCommandConfig;
    }
}
