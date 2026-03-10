package com.kipti.bnb.foundation.command;

import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.kipti.bnb.content.pipes_screensaver.PipesScreensaver;
import com.kipti.bnb.content.kinetics.cogwheel_chain.behaviour.CogwheelChainBehaviour;
import com.kipti.bnb.network.packets.to_client.PeekCogwheelChainControllerHighlightPacket;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class BnbCommands {

    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("bitsnbobs")
                .requires(source -> source.hasPermission(0))
                .then(Commands.literal("peek")
                        .then(registerCogwheelChainControllerPeek())));
        dispatcher.register(registerPipesCommand());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> registerPipesCommand() {
        return Commands.literal("pipes")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("start")
                        .executes(context -> PipesScreensaver.startDemo(context.getSource())))
                .then(Commands.literal("stop")
                        .executes(context -> PipesScreensaver.stopDemo(context.getSource())));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> registerCogwheelChainControllerPeek() {
        return Commands.literal("cogwheel_chain_controller")
                .executes(context -> highlightCogwheelChainController(context.getSource()));
    }

    private static int highlightCogwheelChainController(final CommandSourceStack source) throws CommandSyntaxException {
        final ServerPlayer player = source.getPlayerOrException();
        final BlockHitResult hitResult = raycastLookedAtBlock(player);
        if (hitResult.getType() == HitResult.Type.MISS) {
            source.sendFailure(Component.literal("Look at a block that is part of a cogwheel chain and try again."));
            return 0;
        }

        final Level level = player.level();
        final BlockPos targetPos = hitResult.getBlockPos();
        if (!level.isLoaded(targetPos)) {
            source.sendFailure(Component.literal("The block you are looking at is not loaded."));
            return 0;
        }

        final CogwheelChainBehaviour behaviour = SuperBlockEntityBehaviour
                .getOptional(level, targetPos, CogwheelChainBehaviour.TYPE)
                .orElse(null);
        if (behaviour == null || !behaviour.isPartOfChain()) {
            source.sendFailure(Component.literal("The block you are looking at does not have a cogwheel chain drive."));
            return 0;
        }

        final BlockPos controllerPos = resolveControllerPos(targetPos, behaviour);
        if (controllerPos == null) {
            source.sendFailure(Component.literal("That cogwheel chain is missing controller data."));
            return 0;
        }

        if (!level.isLoaded(controllerPos)) {
            source.sendFailure(Component.literal("The cogwheel chain controller is outside the loaded area."));
            return 0;
        }

        CatnipServices.NETWORK.sendToClient(player, new PeekCogwheelChainControllerHighlightPacket(controllerPos));
        source.sendSuccess(() -> Component.literal("Highlighted cogwheel chain controller at " + controllerPos.toShortString() + "."), false);
        return Command.SINGLE_SUCCESS;
    }

    private static BlockHitResult raycastLookedAtBlock(final ServerPlayer player) {
        final double distance = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
        final Vec3 start = player.getEyePosition(1);
        final Vec3 look = player.getViewVector(1);
        final Vec3 end = start.add(look.x * distance, look.y * distance, look.z * distance);
        return player.level().clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
    }

    private static @Nullable BlockPos resolveControllerPos(final BlockPos targetPos, final CogwheelChainBehaviour behaviour) {
        if (behaviour.isController()) {
            return targetPos;
        }

        final Vec3i controllerOffset = behaviour.getControllerOffset();
        return controllerOffset == null ? null : targetPos.offset(controllerOffset);
    }
}
