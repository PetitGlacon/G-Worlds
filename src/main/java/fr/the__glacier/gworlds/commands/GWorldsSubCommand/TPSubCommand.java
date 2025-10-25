package fr.the__glacier.gworlds.commands.GWorldsSubCommand;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.the__glacier.gcore.commands.utils.SubCommand;
import fr.the__glacier.gcore.config.configObjects.SubCommandConfig;
import fr.the__glacier.gcore.util.TimeUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver;
import io.papermc.paper.math.FinePosition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class TPSubCommand extends SubCommand {
    public TPSubCommand(SubCommandConfig config, TimeUtil.CooldownManager cooldownManager, String cooldownMessage){
        super(config, cooldownManager, cooldownMessage);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getCommand(String s) {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(s);
        command.requires(sender -> sender.getSender().hasPermission(this.config.permission));

        command.then(Commands.argument("world", ArgumentTypes.world())
                .suggests(((context, builder) -> {
                        Bukkit.getWorlds().stream()
                                .filter(world -> world.getName().toLowerCase().startsWith(builder.getRemainingLowerCase()))
                                .forEach(world -> builder.suggest(world.key().asString()));
                        return builder.buildFuture();
                    }))
                .then(Commands.argument("location", ArgumentTypes.finePosition(true))
                        .executes(this::tpPlayerLocation)
                )
                .executes(this::tpPlayerWorld)
        );

        return command;
    }

    private int tpPlayerLocation(CommandContext<CommandSourceStack> context) {
        World world = context.getArgument("world", World.class);
        FinePositionResolver resolver = context.getArgument("location", FinePositionResolver.class);
        try {
            FinePosition position = resolver.resolve(context.getSource());
            if (context.getSource().getSender() instanceof Player p){
                p.teleportAsync(position.toLocation(world));
                p.sendMessage("Téléporté !");
            } else {
                context.getSource().getSender().sendMessage("Tu n'es pas un joueur !");
            }
            return 1;
        } catch (CommandSyntaxException e) {
            context.getSource().getSender().sendMessage("Mauvaise position ! " + e);
            return 0;
        }
    }
    private int tpPlayerWorld(CommandContext<CommandSourceStack> context) {
        World world = context.getArgument("world", World.class);
        Location loc = world.getSpawnLocation();
        if (context.getSource().getSender() instanceof Player p){
            p.teleportAsync(loc);
            p.sendMessage("Téléporté !");
        } else {
            context.getSource().getSender().sendMessage("Tu n'es pas un joueur !");
        }
        return 1;
    }
}
