package fr.the__glacier.gworlds;

import com.google.common.collect.ImmutableMap;
import fr.the__glacier.gcore.ConfigurationManager;
import fr.the__glacier.gcore.commands.utils.BrigadierCommands;
import fr.the__glacier.gcore.commands.utils.SubCommandsManager;
import fr.the__glacier.gworlds.Enums.Gamerules;
import fr.the__glacier.gworlds.commands.GWorldsCommand;
import fr.the__glacier.gworlds.configs.Commands;
import fr.the__glacier.gworlds.configs.WorldConfig;
import fr.the__glacier.gworlds.listeners.WorldLoadListener;
import fr.the__glacier.gworlds.worlds.WorldsManager;
import fr.the__glacier.gworlds.worlds.chunkGenerators.VoidGenerator;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GWorlds extends JavaPlugin {
    @Getter
    private static GWorlds instance;
    private static final org.apache.logging.log4j.Logger log = LogManager.getLogger(GWorlds.class);

    public ConfigurationManager configurationManager;
    public Commands commands;
    public final Map<String, WorldConfig> worldConfigMap = new HashMap<>();

    public WorldsManager worldsManager = new WorldsManager();

    @Override
    public void onEnable() {
        instance = this;
        this.configurationManager = new ConfigurationManager(ConfigurationManager.PersistType.YAML, this);
        temp();
        loadGenerators();
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
        WorldConfig worldConfig = configurationManager.load(WorldConfig.class, new File(getDataFolder().getPath() + File.separator + "worlds" + File.separator + "Test"));
        if (worldConfig.name == null){
            worldConfig = new WorldConfig("Test", true, "default", "normal", "normal", ImmutableMap.of(Gamerules.RANDOM_TICK_SPEED, 100));
        }
        worldConfigMap.put(worldConfig.name, worldConfig);
        saveWorldsConfig();
    }

    public void loadGenerators(){
        worldsManager.addWorldGenerator("void", new VoidGenerator());
    }
    public void loadWorldsConfig(){
        getLogger().info("Loading worlds ...");
        File file = new File(this.getDataFolder().getPath() + File.separator + "worlds");
        file.mkdirs();
        try {
            loadWorlds(file);
        } catch (Exception e) {
            getLogger().log(java.util.logging.Level.SEVERE, "Error loading worlds !", e);
            return;
        }
        getLogger().info("Worlds loaded.");
    }
    public void loadWorlds(File directory){
        if (!directory.exists()) return;
        File[] files = directory.listFiles();
        if (files == null) return;
        for (File file : files){
            if (file.isDirectory()){
                loadWorlds(file);
            } else if (file.getPath().endsWith(".yml")){
                try {
                    WorldConfig wc = configurationManager.load(WorldConfig.class, file);
                    configurationManager.saveFile(wc, file);
                    worldConfigMap.put(wc.name, wc);
                } catch (Exception e){
                    getLogger().severe(e.getMessage());
                }
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
        registerCommand(new GWorldsCommand(this, new SubCommandsManager(), commands.GWorldsCMD));
    }
    private void registerCommand(BrigadierCommands command){
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            List<String> alias = command.getCommandConfig().alias;
            if (alias == null || alias.isEmpty()){
                commands.registrar().register(command.getCommand().build());
            } else {
                commands.registrar().register(command.getCommand().build(), command.getCommandConfig().alias);
            }
        });
    }

    public void registerListeners(){
        Bukkit.getPluginManager().registerEvents(new WorldLoadListener(), this);
    }
}
