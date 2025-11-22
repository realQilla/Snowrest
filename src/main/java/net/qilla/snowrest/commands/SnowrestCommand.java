package net.qilla.snowrest.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.snowrest.Snowrest;
import net.qilla.snowrest.config.SnowrestConfig;
import net.qilla.snowrest.data.TxtDecorations;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class SnowrestCommand {
    private final String command = "snowrest";
    private final String enableArg = "enable";
    private final String disableArg = "disable";
    private final String settingsArg = "settings";
    private final String surfaceArg = "surface";
    private final String waterArg = "water";
    private final String snowingArg = "snowing";
    private final String blockArg = "block";
    private final String addArg = "add";
    private final String removeArg = "remove";
    private final String resetArg = "reset";

    private final Commands commands;
    private final SnowrestConfig config;

    public SnowrestCommand(@NotNull final SnowrestConfig config, @NotNull final Commands commands) {
        this.commands = commands;
        this.config = config;
    }

    public void register() {
        final LiteralArgumentBuilder<CommandSourceStack> commandNode = Commands.literal(command)
                .requires(src -> src.getSender().isOp())
                .then(Commands.literal(enableArg)
                        .executes(cmd -> globalModule(cmd, true))
                ).then(Commands.literal(disableArg)
                        .executes(cmd -> globalModule(cmd, false))
                );

        final LiteralArgumentBuilder<CommandSourceStack> settingsNode = Commands.literal(settingsArg)
                .then(Commands.literal(surfaceArg)
                        .then(Commands.literal(enableArg)
                                .executes(cmd -> surfaceReplacement(cmd, true)))
                        .then(Commands.literal(disableArg)
                                .executes(cmd -> surfaceReplacement(cmd, false)))
                        .then(Commands.literal(addArg)
                                .then(Commands.argument(blockArg, ArgumentTypes.blockState())
                                        .executes(cmd -> surfaceBlocks(cmd, true)))
                        ).then(Commands.literal(removeArg)
                                .then(Commands.argument(blockArg, ArgumentTypes.blockState())
                                        .suggests((ctx, builder) -> {
                                            String argument = builder.getRemaining();

                                            for(BlockState blockState : config.surfaceReplacement()) {
                                                String name = blockState.getBlockData().getAsString();

                                                if(name.regionMatches(true, 0, argument, 0, argument.length()))
                                                    builder.suggest(name);
                                            }

                                            return builder.buildFuture();
                                        }).executes(cmd -> surfaceBlocks(cmd, false)))
                        )
                ).then(Commands.literal(waterArg)
                        .then(Commands.literal(enableArg)
                                .executes(cmd -> waterReplacement(cmd, true)))
                        .then(Commands.literal(disableArg)
                                .executes(cmd -> waterReplacement(cmd, false)))
                        .then(Commands.literal(addArg)
                                .then(Commands.argument(blockArg, ArgumentTypes.blockState())
                                        .executes(cmd -> waterBlocks(cmd, true)))
                        ).then(Commands.literal(removeArg)
                                .then(Commands.argument(blockArg, ArgumentTypes.blockState())
                                        .suggests((ctx, builder) -> {
                                            String argument = builder.getRemaining();

                                            for(BlockState blockState : config.waterReplacement()) {
                                                String name = blockState.getBlockData().getAsString();

                                                if(name.regionMatches(true, 0, argument, 0, argument.length()))
                                                    builder.suggest(name);
                                            }

                                            return builder.buildFuture();
                                        }).executes(cmd -> waterBlocks(cmd, false)))
                        )
                ).then(Commands.literal(snowingArg)
                        .then(Commands.literal(enableArg)
                                .executes(cmd -> snowyWeather(cmd, true)))
                        .then(Commands.literal(disableArg)
                                .executes(cmd -> snowyWeather(cmd, false)))
                ).then(Commands.literal(resetArg)
                        .executes(this::reset)
                );

        commands.register(commandNode
                .then(settingsNode)
                .build()
        );
    }

    private int reset(CommandContext<CommandSourceStack> ctx) {
        final CommandSender sender = ctx.getSource().getSender();

        config.resetDefaults();
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Snowrest blocks have been reset to their defaults.</green>"));

        return Command.SINGLE_SUCCESS;
    }

    private int surfaceBlocks(CommandContext<CommandSourceStack> ctx, boolean add) {
        final CommandSender sender = ctx.getSource().getSender();
        BlockState blockState = ctx.getArgument(blockArg, BlockState.class);
        String blockName = blockState.getBlockData().getAsString();

        if(add) {
            if(config.surfaceReplacement().contains(blockState)) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Block \"" + blockName + "\" is already added to the surface replacement list.</red>"));
            } else {
                config.addSurfaceCover(blockState);
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Block \"" + blockName + "\" has been added to the surface replacement list.</green>"));
            }
        } else {
            if(!config.surfaceReplacement().contains(blockState)) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Block \"" + blockName + "\" is not added to the surface replacement list.</red>"));
            } else {
                config.removeSurfaceCover(blockState);
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Block \"" + blockName + "\" has been removed from the surface replacement list.</green>"));
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    private int waterBlocks(CommandContext<CommandSourceStack> ctx, boolean add) {
        final CommandSender sender = ctx.getSource().getSender();
        BlockState blockState = ctx.getArgument(blockArg, BlockState.class);
        String blockName = blockState.getBlockData().getAsString();

        if(add) {
            if(config.waterReplacement().contains(blockState)) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Block \"" + blockName + "\" is already added to the water replacement list.</red>"));
            } else {
                config.addWaterReplacement(blockState);
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Block \"" + blockName + "\" has been added to the water replacement list.</green>"));
            }
        } else {
            if(!config.waterReplacement().contains(blockState)) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Block \"" + blockName + "\" is not added to the water replacement list.</red>"));
            } else {
                config.removeWaterReplacement(blockState);
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Block \"" + blockName + "\" has been removed from the water replacement list.</green>"));
            }
        }

        return Command.SINGLE_SUCCESS;
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
