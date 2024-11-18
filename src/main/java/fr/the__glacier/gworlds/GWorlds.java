package fr.the__glacier.gworlds;

import fr.the__glacier.gcore.ConfigurationManager;
import fr.the__glacier.gworlds.commands.GWorldsCommand;
import fr.the__glacier.gworlds.configs.Commands;
import fr.the__glacier.gworlds.configs.WorldConfig;
import fr.the__glacier.gworlds.listeners.WorldLoadListener;
import fr.the__glacier.gworlds.worlds.WorldsManager;
import lombok.Getter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class GWorlds extends JavaPlugin {
    @Getter
    private static GWorlds instance;
    private static final org.apache.logging.log4j.Logger log = LogManager.getLogger(GWorlds.class);

    public ConfigurationManager configurationManager;
    public Commands commands;
    public final Map<String, WorldConfig> worldConfigMap = new HashMap<>();

    public GWorldsCommand.GWorldsCommandManager gWorldsCmdManager = new GWorldsCommand.GWorldsCommandManager();
    public WorldsManager worldsManager = new WorldsManager();

    @Override
    public void onEnable() {
        instance = this;
        this.configurationManager = new ConfigurationManager(ConfigurationManager.PersistType.YAML, this);
        loadConfig();
        saveConfig();
        loadWorldsConfig();
        loadWorldAtServerStart();
        registerCommands();
        registerListeners();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    public void temp(){
        worldConfigMap.put("Test", new WorldConfig("Test", false, null, "normal", "normal", null));
        saveWorldsConfig();
    }

    public void loadWorldsConfig(){
        File file = new File(this.getDataFolder().getPath() + File.separator + "worlds");
        file.mkdirs();
        file.listFiles();
        for (File f : file.listFiles()){
            if (!f.getPath().endsWith(".yml")) continue;
            try{
                WorldConfig wc = configurationManager.load(WorldConfig.class, f);
                configurationManager.saveWithFolders(wc, wc.name, "worlds");
                worldConfigMap.put(wc.name, wc);
            } catch (Exception e){
                log.log(Level.FATAL, "La fichier n'est pas correctement configuré. Nom du fichier : " + f.getPath(), e);
            }
        }
    }
    public void loadWorldAtServerStart(){
        for (String str : worldConfigMap.keySet()){
            WorldConfig wc = worldConfigMap.get(str);
            if (wc.loadOnServerLoad) worldsManager.loadWorld(wc);
        }
    }
    public void saveWorldsConfig(){
        for (Map.Entry<String, WorldConfig> entry : worldConfigMap.entrySet()){
            WorldConfig wc = entry .getValue();
            configurationManager.saveWithFolders(wc, wc.name,"worlds");
        }
    }

    public void loadConfig(){
        commands = configurationManager.load(Commands.class);
    }
    public void saveConfig(){
        configurationManager.save(commands);
    }

    public void registerCommands(){
        registerCommand("gworlds", new GWorldsCommand(this, new GWorldsCommand.GWorldsCommandManager(), commands.GWorldsCMD));
    }
    private void registerCommand(String command, GWorldsCommand gWorldsCommand){
        PluginCommand cmd = getServer().getPluginCommand(command);
        if (cmd == null){
            getLogger().severe(command + " is not a valid command !");
        } else {
            cmd.setExecutor(gWorldsCommand);
        }
    }

    public void registerListeners(){
        Bukkit.getPluginManager().registerEvents(new WorldLoadListener(), this);
    }
}
