package ru.nanolive.draconicplus.common.fusioncrafting.blocks;

import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import ru.nanolive.draconicplus.DraconicPlus;
import ru.nanolive.draconicplus.common.blocks.BlockDP;
import ru.nanolive.draconicplus.common.blocks.DraconicBlocks;
import ru.nanolive.draconicplus.common.fusioncrafting.BlockPos;
import ru.nanolive.draconicplus.common.fusioncrafting.client.gui.DPGuiHandler;
import ru.nanolive.draconicplus.common.fusioncrafting.tiles.TileCraftingInjector;
import ru.nanolive.draconicplus.common.fusioncrafting.tiles.TileFusionCraftingCore;

/**
 * Created by brandon3055 on 11/06/2016.
 */
public class FusionCraftingCore extends BlockDP implements ITileEntityProvider {

    public FusionCraftingCore(){
        super(Material.iron);
        this.setHardness(5F);
        this.setResistance(10F);
        this.setCreativeTab(DraconicPlus.draconicTab);
        this.setBlockName("fusionCraftingCore");
        this.setHarvestLevel("pickaxe", 3);
        this.setStepSound(soundTypeMetal);
        DraconicBlocks.register(this);
    }

	public int getRenderType() {
		return -1;
	}

	public boolean isOpaqueCube() {
		return false;
	}

	public boolean renderAsNormalBlock() {
		return false;
	}
    
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileFusionCraftingCore();
    }

    @Override
    public void breakBlock(World worldIn, int x, int y, int z, Block blockBroken, int meta) {
        TileEntity te = worldIn.getTileEntity(x,y,z);
        if (!(te instanceof TileFusionCraftingCore ci) || !(!worldIn.isRemote && worldIn.getGameRules().getGameRuleBooleanValue("doTileDrops") && !worldIn.restoringBlockSnapshots)) {
            super.breakBlock(worldIn, x, y, z, blockBroken, meta);
        } else {
            for (int i = 0; i < 2; i++) {
                if (ci.getStackInSlot(i) != null) {
                    float f = 0.7F;
                    double d0 = (double) (worldIn.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
                    double d1 = (double) (worldIn.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
                    double d2 = (double) (worldIn.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
                    EntityItem entityitem = new EntityItem(worldIn, (double) x + d0, (double) y + d1, (double) z + d2, ci.getStackInCore(0).copy());
                    if (ci.getStackInSlot(i).hasTagCompound())
                        entityitem.getEntityItem().setTagCompound(ci.getStackInSlot(i).getTagCompound());
                    entityitem.delayBeforeCanPickup = 10;
                    worldIn.spawnEntityInWorld(entityitem);
                    ci.setStackInCore(i,null);
                }
            }
            super.breakBlock(worldIn, x, y, z, blockBroken, meta);
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        TileEntity tile = world.getTileEntity(x, y, z);

        if (tile instanceof TileFusionCraftingCore){
            ((TileFusionCraftingCore) tile).updateInjectors(world, new BlockPos(x, y, z));
        }

        if (!world.isRemote) {
            FMLNetworkHandler.openGui(player, DraconicPlus.instance, DPGuiHandler.GUIID_FUSION_CRAFTING, world, x, y, z);
        }
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        return AxisAlignedBB.getBoundingBox(0.0625, 0.0625, 0.0625, 0.9375, 0.9375, 0.9375);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block blockIn) {
        if (!world.isRemote)
        {
            if (world.isBlockIndirectlyGettingPowered(x, y, z))
            {
                TileEntity tile = world.getTileEntity(x, y, z);
                if (tile instanceof TileFusionCraftingCore){
                    ((TileFusionCraftingCore) tile).attemptStartCrafting();
                }
            }
        }
    }
}