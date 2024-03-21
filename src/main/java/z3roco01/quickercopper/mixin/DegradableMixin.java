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

import java.util.Optional;

@Mixin(Degradable.class)
public interface DegradableMixin<T extends Enum<T>> {
    @Shadow T getDegradationLevel();
    @Shadow float getDegradationChanceMultiplier();
    @Shadow Optional<BlockState> getDegradationResult(BlockState var1);

    @Inject(method = "tickDegradation", at = @At("HEAD"), cancellable = true)
    private void tickDegradation(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        double chanceOxidize = 0.03;

        if(world.hasRain(pos))
            chanceOxidize += 0.02; // add 2% to the chance to oxidize

        int degradedLvlOrdi = this.getDegradationLevel().ordinal();
        Iterable<BlockPos.Mutable> iterator = BlockPos.iterateInSquare(pos, 1, Direction.EAST, Direction.SOUTH);
        for(BlockPos.Mutable blockPos : iterator) {
            Block block = world.getBlockState(blockPos).getBlock();
            // Check that the block is not this one and is degradable
            if(blockPos.equals(pos)) continue;

            if(block instanceof Degradable) {
                Degradable degradable = (Degradable) block;

                int blockDegradeOrdi = degradable.getDegradationLevel().ordinal();
                if (blockDegradeOrdi > degradedLvlOrdi)
                    chanceOxidize += 0.008; // add 0.003 to the chance to oxidize per oxidizable block with higher level, can become an increase of 6.4% or 0.064
            }
            if(block == Blocks.WATER)
                chanceOxidize += 0.004; // add 0.0015 to the chance to oxidize per water block around it, can become an increase of 3.2% or 0.032
        }


        QuickerCopper.LOGGER.info(pos + " " + chanceOxidize);
        if(random.nextDouble() < chanceOxidize)
            this.getDegradationResult(state).ifPresent(degraded -> world.setBlockState(pos, (BlockState)degraded));

        ci.cancel();
    }

    /*@Inject(method = "tryDegrade", cancellable = true, at = @At("HEAD"))
    private void tryDegrade(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfoReturnable<Optional<BlockState>> cir) {
        BlockPos blockPos;
        int i = ((Enum)this.getDegradationLevel()).ordinal();
        int blocksEqLowLvl = 0;
        int blocksHigherLvl = 0;
        Iterator<BlockPos> iterator = BlockPos.iterateOutwards(pos, 4, 4, 4).iterator();
        while (iterator.hasNext() && ((blockPos = iterator.next()).getManhattanDistance(pos)) <= 4) {
            Block block;
            if (blockPos.equals(pos) || !((block = world.getBlockState(blockPos).getBlock()) instanceof Degradable)) continue;
            Degradable degradable = (Degradable)((Object)block);
            T blockDegradeLvl = (T)degradable.getDegradationLevel();
            if (this.getDegradationLevel().getClass() != blockDegradeLvl.getClass()) continue;
            int blockDegradeOrdi = blockDegradeLvl.ordinal();
            if (blockDegradeOrdi > i) {
                ++blocksHigherLvl;
                continue;
            }
            ++blocksEqLowLvl;
        }
        float f = (float)(blocksHigherLvl + 1) / (float)(blocksHigherLvl + blocksEqLowLvl + 1);
        float g = f * f * this.getDegradationChanceMultiplier();
        if (random.nextFloat() < g) {
            cir.setReturnValue(this.getDegradationResult(state));
            cir.cancel();
            return;
        }
        cir.setReturnValue(Optional.empty());
        cir.cancel();
        return;
    }*/
}
