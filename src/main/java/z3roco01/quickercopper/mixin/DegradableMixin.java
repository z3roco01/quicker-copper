package z3roco01.quickercopper.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Degradable;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import z3roco01.quickercopper.QuickerCopper;

import javax.swing.*;
import java.util.Optional;

@Mixin(Degradable.class)
public interface DegradableMixin<T extends Enum<T>> {
    @Shadow T getDegradationLevel();
    @Shadow Optional<BlockState> getDegradationResult(BlockState var1);

    @Inject(method = "tickDegradation", at = @At("HEAD"), cancellable = true)
    private void tickDegradation(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        double chanceOxidize = 0.03;

        if(world.hasRain(pos))
            chanceOxidize += 0.02; // add 2% to the chance to oxidize

        int degradedLvlOrdi = this.getDegradationLevel().ordinal();
        Iterable<BlockPos> iterator = BlockPos.iterateOutwards(pos, 1, 1, 1);
        for(BlockPos blockPos : iterator) {
            if(blockPos == pos) continue;
            chanceOxidize += getIncrease(degradedLvlOrdi, blockPos, world);
        }


        QuickerCopper.LOGGER.info(pos + " " + chanceOxidize);

        if(random.nextDouble() < chanceOxidize)
            this.getDegradationResult(state).ifPresent(degraded -> world.setBlockState(pos, (BlockState)degraded));

        ci.cancel();
    }

    // gets the amount we should increase the chance by for a blockpos
    private double getIncrease(int degradedLvl, BlockPos blockPos, ServerWorld world) {
        double increase = 0;
        Block block = world.getBlockState(blockPos).getBlock();
        if(block instanceof Degradable) {
            Degradable degradable = (Degradable) block;

            int blockDegradeOrdi = degradable.getDegradationLevel().ordinal();
            if (blockDegradeOrdi > degradedLvl)
                increase += 0.008; // add 0.003 to the chance to oxidize per oxidizable block with higher level, can become an increase of 6.4% or 0.064
        }
        if(block == Blocks.WATER)
            increase += 0.004; // add 0.0015 to the chance to oxidize per water block around it, can become an increase of 3.2% or 0.032

        return increase;
    }

}
