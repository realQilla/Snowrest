package net.qilla.snowrest.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.snowrest.Snowrest;
import net.qilla.snowrest.config.SnowrestConfig;
import net.qilla.snowrest.data.TxtDecorations;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class SnowrestCommand {

    private final String command = "snowrest";
    private final String enable = "enable";
    private final String disable = "disable";
    private final String settings = "settings";
    private final String surface_replacement = "surface_replacement";
    private final String water_replacement = "water_replacement";
    private final String snowy_weather = "snowy_weather";

    private final Commands commands;
    private final SnowrestConfig config;

    public SnowrestCommand(@NotNull final Snowrest plugin, @NotNull final Commands commands) {
        this.commands = commands;
        this.config = plugin.config();
    }

    public void register() {
        final LiteralArgumentBuilder<CommandSourceStack> commandNode = Commands.literal(command)
                .requires(src -> src.getSender().isOp())
                .then(Commands.literal(enable)
                        .executes(cmd -> globalModule(cmd, true))
                ).then(Commands.literal(disable)
                        .executes(cmd -> globalModule(cmd, false))
                );

        final LiteralArgumentBuilder<CommandSourceStack> settingsNode = Commands.literal(settings)
                .then(Commands.literal(surface_replacement)
                        .then(Commands.literal(enable)
                                .executes(cmd -> surfaceReplacement(cmd, true)))
                        .then(Commands.literal(disable)
                                .executes(cmd -> surfaceReplacement(cmd, false)))
                ).then(Commands.literal(water_replacement)
                        .then(Commands.literal(enable)
                                .executes(cmd -> waterReplacement(cmd, true)))
                        .then(Commands.literal(disable)
                                .executes(cmd -> waterReplacement(cmd, false)))
                )
                .then(Commands.literal(snowy_weather)
                        .then(Commands.literal(enable)
                                .executes(cmd -> snowyWeather(cmd, true)))
                        .then(Commands.literal(disable)
                                .executes(cmd -> snowyWeather(cmd, false)))
                );

        commands.register(commandNode
                .then(settingsNode)
                .build()
        );
    }

    private int surfaceReplacement(CommandContext<CommandSourceStack> ctx, boolean enable) {
        final CommandSender sender = ctx.getSource().getSender();

        if(enable) {
            if(config.isSurfaceCovered()) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Surface snow is already " + TxtDecorations.ENABLED + "</red>"));
                return Command.SINGLE_SUCCESS;
            }

            config.setIsSurfaceCovered(true);
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<aqua>Surface snow has been " + TxtDecorations.ENABLED + ".</aqua>"));
        } else {
            if(!config.isSurfaceCovered()) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Surface snow is already " + TxtDecorations.DISABLED + ".</red>"));
                return Command.SINGLE_SUCCESS;
            }

            config.setIsSurfaceCovered(false);
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<aqua>Surface snow has been " + TxtDecorations.DISABLED + ".</aqua>"));
            return Command.SINGLE_SUCCESS;
        }

        return Command.SINGLE_SUCCESS;
    }

    private int waterReplacement(CommandContext<CommandSourceStack> ctx, boolean enable) {
        final CommandSender sender = ctx.getSource().getSender();

        if(enable) {
            if(config.isWaterFrozen()) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Frozen water is already " + TxtDecorations.ENABLED + "</red>"));
                return Command.SINGLE_SUCCESS;
            }

            config.setIsWaterReplaced(true);
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<aqua>Frozen water has been " + TxtDecorations.ENABLED + ".</aqua>"));
        } else {
            if(!config.isWaterFrozen()) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Frozen water is already " + TxtDecorations.DISABLED + ".</red>"));
                return Command.SINGLE_SUCCESS;
            }

            config.setIsWaterReplaced(false);
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<aqua>Frozen water has been " + TxtDecorations.DISABLED + ".</aqua>"));
            return Command.SINGLE_SUCCESS;
        }

        return Command.SINGLE_SUCCESS;
    }

    private int snowyWeather(CommandContext<CommandSourceStack> ctx, boolean enable) {
        final CommandSender sender = ctx.getSource().getSender();

        if(enable) {
            if(config.isSnowing()) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Snowy weather is already " + TxtDecorations.ENABLED + "</red>"));
                return Command.SINGLE_SUCCESS;
            }

            config.setIsSnowing(true);
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<aqua>Snowy weather has been " + TxtDecorations.ENABLED + ".</aqua>"));
        } else {
            if(!config.isSnowing()) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Snowy weather is already " + TxtDecorations.DISABLED + ".</red>"));
                return Command.SINGLE_SUCCESS;
            }

            config.setIsSnowing(false);
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<aqua>Snowy weather has been " + TxtDecorations.DISABLED + ".</aqua>"));
            return Command.SINGLE_SUCCESS;
        }

        return Command.SINGLE_SUCCESS;
    }

    private int globalModule(CommandContext<CommandSourceStack> ctx, boolean enable) {
        final CommandSender sender = ctx.getSource().getSender();

        if(enable) {
            if(config.isEnabled()) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Snowrest is already " + TxtDecorations.ENABLED + "</red>"));
                return Command.SINGLE_SUCCESS;
            }

            config.setEnabled(true);
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<aqua>Global snowy decoration has been " + TxtDecorations.ENABLED + ".</aqua>"));
        } else {
            if(!config.isEnabled()) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Snowrest is already " + TxtDecorations.DISABLED + ".</red>"));
                return Command.SINGLE_SUCCESS;
            }

            config.setEnabled(false);
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<aqua>Global snowy decoration has been " + TxtDecorations.DISABLED + ".</aqua>"));
            return Command.SINGLE_SUCCESS;
        }

        return Command.SINGLE_SUCCESS;
    }
}
