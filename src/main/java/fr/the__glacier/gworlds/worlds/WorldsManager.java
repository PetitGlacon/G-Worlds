package fr.the__glacier.gworlds.worlds;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import fr.the__glacier.gcore.GCore;
import fr.the__glacier.gworlds.Enums.Gamerules;
import fr.the__glacier.gworlds.GWorlds;
import fr.the__glacier.gworlds.configs.WorldConfig;
import io.papermc.paper.threadedregions.RegionizedServer;
import net.kyori.adventure.util.TriState;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.entity.npc.WanderingTraderSpawner;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.LevelDataAndDimensions;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.validation.ContentValidationException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.*;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.generator.CraftWorldInfo;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WorldsManager {
    private final Map<String, ChunkGenerator> worldGenerators = new HashMap<>();
    private final Map<String, Map<Gamerules, Object>> tempGamerules = new HashMap<>();
    private final Logger logger = LogManager.getLogger(this);

    public void addWorldGenerator(String name, ChunkGenerator generator){
        worldGenerators.put(name.toLowerCase(), generator);
    }
    public ChunkGenerator getWorldGenerator(String name){
        return worldGenerators.get(name.toLowerCase());
    }

    public void createWorld(WorldCreator worldCreator){
        try {
            World world = worldCreator.createWorld();
            loadGamerules(world);
        } catch (Exception e){
            if (GCore.isFolia){
                Bukkit.getGlobalRegionScheduler().execute(GWorlds.getInstance(), () -> createWorldFolia(worldCreator));
            }

        }
    }
    public void createWorld(String name, ChunkGenerator chunkGenerator, WorldType worldType){
        if (Bukkit.getWorld(name) != null) return;
        WorldCreator worldCreator = new WorldCreator(name);
        if (chunkGenerator != null) worldCreator.generator(chunkGenerator);
        if (worldType != null) worldCreator.type(worldType);
        worldCreator.keepSpawnLoaded(TriState.FALSE);
        createWorld(worldCreator);
    }
    
    public void loadWorld(String name){
        WorldCreator worldCreator = new WorldCreator(name);
        if (Bukkit.getWorld(name) == null) createWorld(worldCreator);
    }
    public void loadWorld(WorldConfig worldConfig){
        WorldCreator worldCreator = new WorldCreator(worldConfig.name);
        String generator = worldConfig.worldGenerator.toLowerCase();
        switch (generator){
            case "amplified" -> worldCreator.type(WorldType.AMPLIFIED);
            case "flat" -> worldCreator.type(WorldType.FLAT);
            case  "large_biomes" -> worldCreator.type(WorldType.LARGE_BIOMES);
            default -> worldCreator.generator(getWorldGenerator(generator));
        }

        if (Bukkit.getWorld(worldConfig.name) != null) return;

        tempGamerules.put(worldConfig.name.toLowerCase(), worldConfig.gamerules);
        createWorld(worldCreator);

        GWorlds.getInstance().getLogger().warning("Création du monde " + worldConfig.name);
        World world = Bukkit.getWorld(worldConfig.name);
        GWorlds.getInstance().getLogger().warning("Création en court.");
        if (world == null) return;
        GWorlds.getInstance().getLogger().warning("World pas null.");
        if (worldConfig.gamerules != null) {
            GWorlds.getInstance().getLogger().warning("Gamerules pas null.");
            for (Map.Entry<Gamerules, Object> entry : worldConfig.gamerules.entrySet()){
                GWorlds.getInstance().getLogger().warning("Gamerule " + entry.getKey().name() + " / " + entry.getValue());
                changeGamerule(world, entry.getKey(), entry.getValue());
            }
        }
        if (worldConfig.spawnLocation != null) {
            world.setSpawnLocation(worldConfig.spawnLocation.getLocation());
        }
    }

    private void createWorldFolia(WorldCreator creator) {
        CraftServer craftServer = (CraftServer) Bukkit.getServer();
        DedicatedServer console = craftServer.getServer();
        // Preconditions.checkState(console.getAllLevels().iterator().hasNext(), "Cannot create additional worlds on STARTUP");
        //Preconditions.checkState(!console.isIteratingOverLevels, "Cannot create a world while worlds are being ticked"); // Paper - Cat - Temp disable. We'll see how this goes.
        // Preconditions.checkArgument(creator != null, "WorldCreator cannot be null");

        String name = creator.name();

        String levelName = console.getProperties().levelName;
        ResourceKey<net.minecraft.world.level.Level> worldKey = null;
        if (name.equals(levelName)) {
            return;
        } else if (name.equals(levelName + "_nether")) {
            if (craftServer.getAllowNether()) {
                return;
            }
            worldKey = net.minecraft.world.level.Level.NETHER;
        } else if (name.equals(levelName + "_the_end")) {
            if (craftServer.getAllowEnd()) {
                return ;
            }
            worldKey = net.minecraft.world.level.Level.END;
        }

        ChunkGenerator generator = creator.generator();
        BiomeProvider biomeProvider = creator.biomeProvider();
        File folder = new File(craftServer.getWorldContainer(), "worlds/");
        File file = new File(folder, name);
        World world = craftServer.getWorld(name);

        // Paper start
        World worldByKey = craftServer.getWorld(creator.key());
        if (world != null || worldByKey != null) {
            logger.log(Level.WARN, "World already exist !");
            return;
        }

        if ((file.exists()) && (!file.isDirectory())) {
            logger.log(Level.WARN, "World folder invalid !");
            return;
        }

        if (generator == null) generator = craftServer.getGenerator(name);

        if (biomeProvider == null) biomeProvider = craftServer.getBiomeProvider(name);

        ResourceKey<LevelStem> actualDimension = switch (creator.environment()) {
            case NORMAL -> LevelStem.OVERWORLD;
            case NETHER -> LevelStem.NETHER;
            case THE_END -> LevelStem.END;
            default -> throw new IllegalArgumentException("Illegal dimension (" + creator.environment() + ")");
        };

        LevelStorageSource.LevelStorageAccess levelStorageAccess;
        try {
            levelStorageAccess = LevelStorageSource.createDefault(folder.toPath()).validateAndCreateAccess(name, actualDimension);
        } catch (IOException | ContentValidationException ex) {
            throw new RuntimeException(ex);
        }

        Dynamic<?> dynamic;
        if (levelStorageAccess.hasWorldData()) {
            net.minecraft.world.level.storage.LevelSummary worldinfo;

            try {
                dynamic = levelStorageAccess.getDataTag();
                worldinfo = levelStorageAccess.getSummary(dynamic);
            } catch (NbtException | ReportedNbtException | IOException ioexception) {
                LevelStorageSource.LevelDirectory convertable_b = levelStorageAccess.getLevelDirectory();

                MinecraftServer.LOGGER.warn("Failed to load world data from {}", convertable_b.dataFile(), ioexception);
                MinecraftServer.LOGGER.info("Attempting to use fallback");

                try {
                    dynamic = levelStorageAccess.getDataTagFallback();
                    worldinfo = levelStorageAccess.getSummary(dynamic);
                } catch (NbtException | ReportedNbtException | IOException ioexception1) {
                    MinecraftServer.LOGGER.error("Failed to load world data from {}", convertable_b.oldDataFile(), ioexception1);
                    MinecraftServer.LOGGER.error("Failed to load world data from {} and {}. World files may be corrupted. Shutting down.", convertable_b.dataFile(), convertable_b.oldDataFile());
                    return;
                }

                levelStorageAccess.restoreLevelDataFromOld();
            }

            if (worldinfo.requiresManualConversion()) {
                MinecraftServer.LOGGER.info("This world must be opened in an older version (like 1.6.4) to be safely converted");
                return;
            }

            if (!worldinfo.isCompatible()) {
                MinecraftServer.LOGGER.info("This world was created by an incompatible version.");
                return;
            }
        } else {
            dynamic = null;
        }

        boolean hardcore = creator.hardcore();

        PrimaryLevelData primaryLevelData;
        WorldLoader.DataLoadContext worldLoader = console.worldLoader;
        RegistryAccess.Frozen registryAccess = worldLoader.datapackDimensions();
        net.minecraft.core.Registry<LevelStem> contextLevelStemRegistry = registryAccess.lookupOrThrow(Registries.LEVEL_STEM);
        if (dynamic != null) {
            LevelDataAndDimensions leveldataanddimensions = LevelStorageSource.getLevelDataAndDimensions(dynamic, worldLoader.dataConfiguration(), contextLevelStemRegistry, worldLoader.datapackWorldgen());

            primaryLevelData = (PrimaryLevelData) leveldataanddimensions.worldData();
            registryAccess = leveldataanddimensions.dimensions().dimensionsRegistryAccess();
        } else {
            LevelSettings levelSettings;
            WorldOptions worldoptions = new WorldOptions(creator.seed(), creator.generateStructures(), false);
            WorldDimensions worldDimensions;

            DedicatedServerProperties.WorldDimensionData properties = new DedicatedServerProperties.WorldDimensionData(GsonHelper.parse((creator.generatorSettings().isEmpty()) ? "{}" : creator.generatorSettings()), creator.type().name().toLowerCase(Locale.ROOT));

            levelSettings = new LevelSettings(
                    name,
                    GameType.SURVIVAL,
                    hardcore,
                    Difficulty.NORMAL,
                    false,
                    new GameRules(worldLoader.dataConfiguration().enabledFeatures()),
                    worldLoader.dataConfiguration()
            );
            worldDimensions = properties.create(worldLoader.datapackWorldgen());

            WorldDimensions.Complete complete = worldDimensions.bake(contextLevelStemRegistry);
            Lifecycle lifecycle = complete.lifecycle().add(worldLoader.datapackWorldgen().allRegistriesLifecycle());

            primaryLevelData = new PrimaryLevelData(levelSettings, worldoptions, complete.specialWorldProperty(), lifecycle);
            registryAccess = complete.dimensionsRegistryAccess();
        }
        contextLevelStemRegistry = registryAccess.lookupOrThrow(Registries.LEVEL_STEM);
        primaryLevelData.customDimensions = contextLevelStemRegistry;
        primaryLevelData.checkName(name);
        primaryLevelData.setModdedInfo(console.getServerModName(), console.getModdedStatus().shouldReportAsModified());



        long i = BiomeManager.obfuscateSeed(primaryLevelData.worldGenOptions().seed()); // Paper - use world seed
        List<CustomSpawner> list = ImmutableList.of(
                new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(primaryLevelData)
        );
        LevelStem customStem = contextLevelStemRegistry.getValue(actualDimension);

        WorldInfo worldInfo = new CraftWorldInfo(primaryLevelData, levelStorageAccess, creator.environment(), customStem.type().value(), customStem.generator(), craftServer.getHandle().getServer().registryAccess()); // Paper - Expose vanilla BiomeProvider from WorldInfo
        if (biomeProvider == null && generator != null) {
            biomeProvider = generator.getDefaultBiomeProvider(worldInfo);
        }

        // Paper start - fix and optimise world upgrading
        if (console.options.has("forceUpgrade")) {
            net.minecraft.server.Main.forceUpgrade(
                    levelStorageAccess, primaryLevelData, DataFixers.getDataFixer(), console.options.has("eraseCache"), () -> false, registryAccess, console.options.has("recreateRegionFiles")
            );
        }

        if (worldKey == null) {
            worldKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(creator.key().namespace(), creator.key().value()));
        }

        if (creator.keepSpawnLoaded() == TriState.FALSE){
            primaryLevelData.getGameRules().getRule(GameRules.RULE_SPAWN_CHUNK_RADIUS).set(0, null);
        }
        ServerLevel internal = new ServerLevel(
                console,
                console.executor,
                levelStorageAccess,
                primaryLevelData,
                worldKey,
                customStem,
                console.progressListenerFactory.create(primaryLevelData.getGameRules().getInt(GameRules.RULE_SPAWN_CHUNK_RADIUS)),
                primaryLevelData.isDebugWorld(),
                i,
                creator.environment() == World.Environment.NORMAL ? list : ImmutableList.of(),
                true,
                console.overworld().getRandomSequences(),
                creator.environment(),
                generator,
                biomeProvider
        );

        console.addLevel(internal);

        int loadRegionRadius = 1024 >> 4;
        internal.randomSpawnSelection = new ChunkPos(internal.getChunkSource().randomState().sampler().findSpawnPosition());

        for (int currX = -loadRegionRadius; currX <= loadRegionRadius; ++currX) {
            for (int currZ = -loadRegionRadius; currZ <= loadRegionRadius; ++currZ) {
                ChunkPos pos = new ChunkPos(currX, currZ);
                internal.moonrise$getChunkTaskScheduler().chunkHolderManager.addTicketAtLevel(
                        net.minecraft.server.level.TicketType.UNKNOWN, pos, ca.spottedleaf.moonrise.patches.chunk_system.scheduling.ChunkHolderManager.MAX_TICKET_LEVEL, null
                );
            }
        }

        internal.setSpawnSettings(true);

        console.prepareLevels(internal.getChunkSource().chunkMap.progressListener, internal);

        RegionizedServer.getInstance().addWorld(internal);
        // internal.getWorld().loadChunk(internal.getWorld().getSpawnLocation().getChunk());

        Bukkit.getPluginManager().callEvent(new WorldLoadEvent(internal.getWorld()));
        loadGamerules(internal.getWorld());
        GWorlds.getInstance().getLogger().severe("Success");
    }

    public void loadGamerules(World w){
        Map<Gamerules, Object> gamerules = tempGamerules.get(w.getName().toLowerCase());
        if (gamerules == null) return;
        for (Map.Entry<Gamerules, Object> entry : gamerules.entrySet()){
            logger.log(Level.WARN, entry.getKey().name() + " / " + entry.getValue() + " / " + entry.getValue().getClass().getName());
            changeGamerule(w, entry.getKey().getGamerule(), entry.getValue());
        }
        tempGamerules.remove(w.getName());
    }
    public void changeGamerule(World w, Gamerules gamerule, Object o) {changeGamerule(w, gamerule.getGamerule(), o);}

    @SuppressWarnings("unchecked")
    public void changeGamerule(World w, GameRule<?> gameRule, Object value){
        try {
            if (gameRule.getType().isInstance(value)){
                w.setGameRule((GameRule<Object>) gameRule, value);
            } else {
                throw new IllegalArgumentException("Specified value doesn't match witch this gamerule.");
            }
        } catch (Exception ignored){
            Bukkit.getGlobalRegionScheduler().execute(GWorlds.getInstance(), () -> {
                if (gameRule.getType().isInstance(value)){
                    w.setGameRule((GameRule<Object>) gameRule, value);
                } else {
                    throw new IllegalArgumentException("Specified value doesn't match witch this gamerule.");
                }
            });
        }
    }
}