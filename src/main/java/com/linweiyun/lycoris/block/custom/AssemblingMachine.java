package com.linweiyun.lycoris.block.custom;

import com.linweiyun.lycoris.GenAnimations.AssemblingMachineBlockEntityRender;
import com.linweiyun.lycoris.LycPerseusMod;
import com.linweiyun.lycoris.block.blockentity.AssemblingMachineBlockEntity;
import com.linweiyun.lycoris.block.blockentity.LPBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
@Mod.EventBusSubscriber(modid = LycPerseusMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class AssemblingMachine extends BaseEntityBlock {
    public AssemblingMachine(Properties pProperties) {
        super(pProperties);
    }
    public static final BooleanProperty IS_ON = BooleanProperty.create("is_on");


    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(new Property[]{HorizontalDirectionalBlock.FACING}).add(new Property[]{IS_ON});
    }


    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite()).setValue(IS_ON, false);
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof AssemblingMachineBlockEntity) {
                ((AssemblingMachineBlockEntity) blockEntity).drops();
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide()) {
            BlockEntity entity = pLevel.getBlockEntity(pPos);
            if (entity instanceof AssemblingMachineBlockEntity) {
                NetworkHooks.openScreen(((ServerPlayer) pPlayer), (AssemblingMachineBlockEntity) entity, pPos);
            } else {
                throw new IllegalStateException("Our Container provider is missing!");
            }
        }

        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new AssemblingMachineBlockEntity(blockPos, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, LPBlockEntities.ASSEMBLING_MACHINE.get(), AssemblingMachineBlockEntity::tick);
    }

    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(LPBlockEntities.ASSEMBLING_MACHINE.get(), AssemblingMachineBlockEntityRender::new);
    }

    public void setIsOn(BlockState pState, Level pLevel, BlockPos pPos, boolean isOn) {
        pLevel.setBlock(pPos, pState.setValue(IS_ON, isOn), 3);
    }

    /**
     * 获取 IS_ON 属性
     * @param pState 当前的 BlockState
     * @return 是否开启
     */
    public boolean getIsOn(BlockState pState) {
        return pState.getValue(IS_ON);
    }

}
