package coda.croodaceous.common;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.registry.CEBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CroodaceousMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonEvents {

    @SubscribeEvent
    public static void interactBlock(PlayerInteractEvent.RightClickBlock e) {
        BlockPos pos = e.getPos();
        Level level = e.getLevel();
        Player player = e.getEntity();
        InteractionHand hand = e.getHand();

        if (player.getItemInHand(hand).is(Items.RED_DYE) && level.getBlockState(pos).is(Blocks.STONE))  {
            level.setBlock(pos, CEBlocks.PAINTED_STONE.get().defaultBlockState(), 3);
            level.playSound(player, pos, SoundEvents.MAGMA_CUBE_SQUISH, SoundSource.BLOCKS, 1.0F, 1.0F);

            player.swing(hand);

            for (int i = 0; i < 20; i++) {
                double x = pos.getX() + level.random.nextFloat() + e.getFace().getStepX();
                double y = pos.getY() + level.random.nextFloat() + e.getFace().getStepY();
                double z = pos.getZ() + level.random.nextFloat() + e.getFace().getStepZ();
                level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.RED_DYE)), x, y, z, 0.0D, 0.0D, 0.0D);
            }

            if (!player.getAbilities().instabuild) {
                player.getItemInHand(hand).shrink(1);
            }
        }
    }
}
