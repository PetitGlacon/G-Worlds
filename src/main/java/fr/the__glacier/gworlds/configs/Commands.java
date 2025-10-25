package fr.the__glacier.gworlds.configs;

import com.google.common.collect.ImmutableMap;
import fr.the__glacier.gcore.config.configObjects.CommandConfig;
import fr.the__glacier.gcore.config.configObjects.SubCommandConfig;

import java.util.List;
import java.util.Map;

public class Commands {
    public String noPermission;
    public String syntax;
    public CommandConfig GWorldsCMD;

    public Commands(){
        noPermission = "You don't have permission to do that !";
        syntax = "/gworlds <subCommand>";
        Map<String, SubCommandConfig> map = ImmutableMap.of(
                subCommandsEnum.create.toString(), new SubCommandConfig(
                        List.of("create"),
                        "Créer un nouveau monde.",
                        "/gworlds create <world>",
                        "perm",
                        noPermission,
                        0L,
                        true),
                subCommandsEnum.teleport.toString(), new SubCommandConfig(
                        List.of("teleport", "tp"),
                        "Se téléporter à un endroit",
                        "/gworlds tp <x> <y> <z> <world>",
                        "perm",
                        noPermission,
                        0L,
                        true),
                subCommandsEnum.gamerules.toString(), new SubCommandConfig(
                        List.of("gamerule", "gamerules", "gm", "gr"),
                        "Change les gamerules d'un monde",
                        "/gworlds gm <world> <gamerule> <value>",
                        "perm",
                        noPermission,
                        0L,
                        true)
        );
        GWorldsCMD = new CommandConfig("gworld", "Main plugin command.", "/gworlds", "permission", noPermission, 0L, "You are on cooldown",true, map, List.of("gw", "ceci est un test", "GWORLD", "GWorld"));
    }

    public static enum subCommandsEnum{
        create,
        teleport,
        gamerules
    }
}
