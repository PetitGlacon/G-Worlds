package fr.the__glacier.gworlds.configs;

import fr.the__glacier.gworlds.Enums.Gamerules;

import java.util.Map;

public class WorldConfig {
    public String name;
    public boolean loadOnServerLoad;
    public String worldGenerator;
    public String environment;
    public String difficulty;
    public Map<Gamerules, Object> gamerules;

    public WorldConfig(){
        this.name = "null";
        this.loadOnServerLoad = false;
        this.worldGenerator = "Default";
        this.environment = "normal";
        this.difficulty = "normal";
        this.gamerules = null;
    }
    public WorldConfig(String name, boolean loadOnServerLoad, String worldGenerator, String environment, String difficulty, Map<Gamerules, Object> gamerules){
        this.name = name;
        this.loadOnServerLoad = loadOnServerLoad;
        this.worldGenerator = worldGenerator;
        this.environment = environment;
        this.difficulty = difficulty;
        this.gamerules = gamerules;
    }

}
