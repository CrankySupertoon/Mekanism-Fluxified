package mekanism.common.block;

import java.util.List;
import java.util.Random;

import mekanism.api.Coord4D;
import mekanism.api.MekanismConfig.client;
import mekanism.api.MekanismConfig.general;
import mekanism.api.energy.IEnergizedItem;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.common.Mekanism;
import mekanism.common.Tier.BaseTier;
import mekanism.common.Tier.FluidTankTier;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.base.IFactory;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ISpecialBounds;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.ISustainedInventory;
import mekanism.common.base.ISustainedTank;
import mekanism.common.base.ITierItem;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.block.states.BlockStateBasic.BasicBlock;
import mekanism.common.block.states.BlockStateFacing;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.block.states.BlockStateMachine.MachineBlock;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.item.ItemBlockMachine;
import mekanism.common.network.PacketLogisticalSorterGui.LogisticalSorterGuiMessage;
import mekanism.common.network.PacketLogisticalSorterGui.SorterGuiPacket;
import mekanism.common.recipe.ShapedMekanismRecipe;
import mekanism.common.security.ISecurityItem;
import mekanism.common.security.ISecurityTile;
import mekanism.common.security.ISecurityTile.SecurityMode;
import mekanism.common.tile.TileEntityAdvancedFactory;
import mekanism.common.tile.TileEntityAmbientAccumulator;
import mekanism.common.tile.TileEntityBasicBlock;
import mekanism.common.tile.TileEntityChargepad;
import mekanism.common.tile.TileEntityChemicalCrystallizer;
import mekanism.common.tile.TileEntityChemicalDissolutionChamber;
import mekanism.common.tile.TileEntityChemicalInfuser;
import mekanism.common.tile.TileEntityChemicalInjectionChamber;
import mekanism.common.tile.TileEntityChemicalOxidizer;
import mekanism.common.tile.TileEntityChemicalWasher;
import mekanism.common.tile.TileEntityCombiner;
import mekanism.common.tile.TileEntityContainerBlock;
import mekanism.common.tile.TileEntityCrusher;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.tile.TileEntityElectricPump;
import mekanism.common.tile.TileEntityElectrolyticSeparator;
import mekanism.common.tile.TileEntityEliteFactory;
import mekanism.common.tile.TileEntityEnergizedSmelter;
import mekanism.common.tile.TileEntityEnrichmentChamber;
import mekanism.common.tile.TileEntityFactory;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.tile.TileEntityFluidicPlenisher;
import mekanism.common.tile.TileEntityFormulaicAssemblicator;
import mekanism.common.tile.TileEntityFuelwoodHeater;
import mekanism.common.tile.TileEntityLaser;
import mekanism.common.tile.TileEntityLaserAmplifier;
import mekanism.common.tile.TileEntityLaserTractorBeam;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.TileEntityMetallurgicInfuser;
import mekanism.common.tile.TileEntityOredictionificator;
import mekanism.common.tile.TileEntityOsmiumCompressor;
import mekanism.common.tile.TileEntityPRC;
import mekanism.common.tile.TileEntityPersonalChest;
import mekanism.common.tile.TileEntityPrecisionSawmill;
import mekanism.common.tile.TileEntityPurificationChamber;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.tile.TileEntityResistiveHeater;
import mekanism.common.tile.TileEntityRotaryCondensentrator;
import mekanism.common.tile.TileEntitySeismicVibrator;
import mekanism.common.tile.TileEntitySolarNeutronActivator;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import buildcraft.api.tools.IToolWrench;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Block class for handling multiple machine block IDs.
 * 0:0: Enrichment Chamber
 * 0:1: Osmium Compressor
 * 0:2: Combiner
 * 0:3: Crusher
 * 0:4: Digital Miner
 * 0:5: Basic Factory
 * 0:6: Advanced Factory
 * 0:7: Elite Factory
 * 0:8: Metallurgic Infuser
 * 0:9: Purification Chamber
 * 0:10: Energized Smelter
 * 0:11: Teleporter
 * 0:12: Electric Pump
 * 0:13: Electric Chest
 * 0:14: Chargepad
 * 0:15: Logistical Sorter
 * 1:0: Rotary Condensentrator
 * 1:1: Chemical Oxidizer
 * 1:2: Chemical Infuser
 * 1:3: Chemical Injection Chamber
 * 1:4: Electrolytic Separator
 * 1:5: Precision Sawmill
 * 1:6: Chemical Dissolution Chamber
 * 1:7: Chemical Washer
 * 1:8: Chemical Crystallizer
 * 1:9: Seismic Vibrator
 * 1:10: Pressurized Reaction Chamber
 * 1:11: Fluid Tank
 * 1:12: Fluidic Plenisher
 * 1:13: Laser
 * 1:14: Laser Amplifier
 * 1:15: Laser Tractor Beam
 * 2:0: Quantum Entangloporter
 * 2:1: Solar Neutron Activator
 * 2:2: Ambient Accumulator
 * 2:3: Oredictionificator
 * 2:4: Resistive Heater
 * 2:5: Formulaic Assemblicator
 * 2:6: Fuelwood Heater
 * 
 * @author AidanBrady
 *
 */
public abstract class BlockMachine extends BlockContainer implements ISpecialBounds//, IBlockCTM, ICustomBlockIcon
{
	//public CTMData[][] ctms = new CTMData[16][4];

	public BlockMachine()
	{
		super(Material.iron);
		setHardness(3.5F);
		setResistance(16F);
		setCreativeTab(Mekanism.tabMekanism);
	}

	public static BlockMachine getBlockMachine(MachineBlock block)
	{
		return new BlockMachine() {
			@Override
			public MachineBlock getMachineBlock()
			{
				return block;
			}
		};
	}

	public abstract MachineBlock getMachineBlock();

	public BlockState createBlockState()
	{
		return new BlockStateMachine(this, getProperty());
	}

	public PropertyEnum<MachineType> getProperty()
	{
		return getMachineBlock().getProperty();
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		MachineType type = MachineType.get(getMachineBlock(), meta&0xF);

		return getDefaultState().withProperty(getProperty(), type);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		MachineType type = state.getValue(getProperty());
		return type.meta;
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
	{
		TileEntity tile = worldIn.getTileEntity(pos);
		if(tile instanceof TileEntityBasicBlock && ((TileEntityBasicBlock)tile).facing != null)
		{
			state = state.withProperty(BlockStateFacing.facingProperty, ((TileEntityBasicBlock)tile).facing);
		}
		if(tile instanceof IActiveState)
		{
			state = state.withProperty(BlockStateMachine.activeProperty, ((IActiveState)tile).getActive());
		}
		return state;
	}



/*
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register)
	{
		BASE_ICON = register.registerIcon("mekanism:SteelCasing");
		DefIcon def = DefIcon.getAll(BASE_ICON);
		
		switch(blockType)
		{
			case MACHINE_BLOCK_1:
				ctms[11][0] = new CTMData("ctm/Teleporter", this, Arrays.asList(11)).addOtherBlockConnectivities(MekanismBlocks.BasicBlock, Arrays.asList(7)).registerIcons(register);
				
				MekanismRenderer.loadDynamicTextures(register, "enrichment_chamber/" + MachineType.ENRICHMENT_CHAMBER.name, icons[0], def);
				MekanismRenderer.loadDynamicTextures(register, "osmium_compressor/" + MachineType.OSMIUM_COMPRESSOR.name, icons[1], def);
				MekanismRenderer.loadDynamicTextures(register, "combiner/" + MachineType.COMBINER.name, icons[2], def);
				MekanismRenderer.loadDynamicTextures(register, "crusher/" + MachineType.CRUSHER.name, icons[3], def);
				
				for(RecipeType type : RecipeType.values())
				{
					MekanismRenderer.loadDynamicTextures(register, "factory/basic/" + type.getUnlocalizedName().toLowerCase() + "/" + BaseTier.BASIC.getName() + type.getUnlocalizedName() + MachineType.BASIC_FACTORY.name, factoryIcons[0][type.ordinal()],
							DefIcon.getActivePair(register.registerIcon("mekanism:factory/basic/BasicFactoryFront"), 2).setOverrides(false),
							DefIcon.getActivePair(register.registerIcon("mekanism:factory/basic/BasicFactoryTop"), 1).setOverrides(false), 
							DefIcon.getActivePair(register.registerIcon("mekanism:factory/basic/BasicFactoryBottom"), 0).setOverrides(false), 
							DefIcon.getActivePair(register.registerIcon("mekanism:factory/basic/BasicFactorySide"), 3, 4, 5).setOverrides(false));
					MekanismRenderer.loadDynamicTextures(register, "factory/advanced/" + type.getUnlocalizedName().toLowerCase() + "/" + BaseTier.ADVANCED.getName() + type.getUnlocalizedName() + MachineType.ADVANCED_FACTORY.name, factoryIcons[1][type.ordinal()], 
							DefIcon.getActivePair(register.registerIcon("mekanism:factory/advanced/AdvancedFactoryFront"), 2).setOverrides(false), 
							DefIcon.getActivePair(register.registerIcon("mekanism:factory/advanced/AdvancedFactoryTop"), 1).setOverrides(false), 
							DefIcon.getActivePair(register.registerIcon("mekanism:factory/advanced/AdvancedFactoryBottom"), 0).setOverrides(false), 
							DefIcon.getActivePair(register.registerIcon("mekanism:factory/advanced/AdvancedFactorySide"), 3, 4, 5).setOverrides(false));
					MekanismRenderer.loadDynamicTextures(register, "factory/elite/" + type.getUnlocalizedName().toLowerCase() + "/" + BaseTier.ELITE.getName() + type.getUnlocalizedName() + MachineType.ELITE_FACTORY.name, factoryIcons[2][type.ordinal()], 
							DefIcon.getActivePair(register.registerIcon("mekanism:factory/elite/EliteFactoryFront"), 2).setOverrides(false), 
							DefIcon.getActivePair(register.registerIcon("mekanism:factory/elite/EliteFactoryTop"), 1).setOverrides(false), 
							DefIcon.getActivePair(register.registerIcon("mekanism:factory/elite/EliteFactoryBottom"), 0).setOverrides(false), 
							DefIcon.getActivePair(register.registerIcon("mekanism:factory/elite/EliteFactorySide"), 3, 4, 5).setOverrides(false));
				}
				
				MekanismRenderer.loadDynamicTextures(register, "purification_chamber/" + MachineType.PURIFICATION_CHAMBER.name, icons[9], def);
				MekanismRenderer.loadDynamicTextures(register, "energized_smelter/" + MachineType.ENERGIZED_SMELTER.name, icons[10], def);
				icons[11][0] = ctms[11][0].mainTextureData.icon;
				
				break;
			case MACHINE_BLOCK_2:
				MekanismRenderer.loadDynamicTextures(register, "chemical_injection_chamber/" + MachineType.CHEMICAL_INJECTION_CHAMBER.name, icons[3], def);
				MekanismRenderer.loadDynamicTextures(register, "precision_sawmill/" + MachineType.PRECISION_SAWMILL.name, icons[5], def);
				
				break;
			case MACHINE_BLOCK_3:
				icons[0][0] = BASE_ICON;
				icons[2][0] = BASE_ICON;
				MekanismRenderer.loadDynamicTextures(register, "oredictionificator/" + MachineType.OREDICTIONIFICATOR.name, icons[3]);
				icons[4][0] = BASE_ICON;
				MekanismRenderer.loadDynamicTextures(register, "formulaic_assemblicator/" + MachineType.FORMULAIC_ASSEMBLICATOR.name, icons[5]);
				MekanismRenderer.loadDynamicTextures(register, "fuelwood_heater/" + MachineType.FUELWOOD_HEATER.name, icons[6]);
				
				break;
		}
	}
	
	@Override
	public IIcon getIcon(ItemStack stack, int side)
	{
		MachineType type = MachineType.get(stack);
		ItemBlockMachine item = (ItemBlockMachine)stack.getItem();
		
		if(type == MachineType.BASIC_FACTORY)
		{
			return factoryIcons[0][item.getRecipeType(stack)][side];
		}
		else if(type == MachineType.ADVANCED_FACTORY)
		{
			return factoryIcons[1][item.getRecipeType(stack)][side];
		}
		else if(type == MachineType.ELITE_FACTORY)
		{
			return factoryIcons[2][item.getRecipeType(stack)][side];
		}
		
		return getIcon(side, stack.getItemDamage());
	}
*/

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(pos);
		int side = MathHelper.floor_double((placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		int height = Math.round(placer.rotationPitch);
		int change = 3;

		if(tileEntity == null)
		{
			return;
		}

		if(tileEntity.canSetFacing(0) && tileEntity.canSetFacing(1))
		{
			if(height >= 65)
			{
				change = 1;
			}
			else if(height <= -65)
			{
				change = 0;
			}
		}

		if(change != 0 && change != 1)
		{
			switch(side)
			{
				case 0: change = 2; break;
				case 1: change = 5; break;
				case 2: change = 3; break;
				case 3: change = 4; break;
			}
		}

		tileEntity.setFacing((short)change);
		tileEntity.redstone = world.isBlockIndirectlyGettingPowered(pos) > 0;

		if(tileEntity instanceof TileEntityLogisticalSorter)
		{
			TileEntityLogisticalSorter transporter = (TileEntityLogisticalSorter)tileEntity;

			if(!transporter.hasInventory())
			{
				for(EnumFacing dir : EnumFacing.VALUES)
				{
					TileEntity tile = Coord4D.get(transporter).offset(dir).getTileEntity(world);

					if(tile instanceof IInventory)
					{
						tileEntity.setFacing((short)dir.getOpposite().ordinal());
						break;
					}
				}
			}
		}

		if(tileEntity instanceof IBoundingBlock)
		{
			((IBoundingBlock)tileEntity).onPlace();
		}
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(pos);

		if(tileEntity instanceof IBoundingBlock)
		{
			((IBoundingBlock)tileEntity).onBreak();
		}

		super.breakBlock(world, pos, state);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, BlockPos pos, IBlockState state, Random random)
	{
		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(pos);

		if(MekanismUtils.isActive(world, pos) && ((IActiveState)tileEntity).renderUpdate() && client.machineEffects)
		{
			float xRandom = (float)pos.getX() + 0.5F;
			float yRandom = (float)pos.getY() + 0.0F + random.nextFloat() * 6.0F / 16.0F;
			float zRandom = (float)pos.getZ() + 0.5F;
			float iRandom = 0.52F;
			float jRandom = random.nextFloat() * 0.6F - 0.3F;

			EnumFacing side = tileEntity.facing;

			if(tileEntity instanceof TileEntityMetallurgicInfuser)
			{
				side = side.getOpposite();
			}

			switch(side)
			{
				case WEST:
					world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, (xRandom - iRandom), yRandom, (zRandom + jRandom), 0.0D, 0.0D, 0.0D);
					world.spawnParticle(EnumParticleTypes.REDSTONE, (xRandom - iRandom), yRandom, (zRandom + jRandom), 0.0D, 0.0D, 0.0D);
					break;
				case EAST:
					world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, (xRandom + iRandom), yRandom, (zRandom + jRandom), 0.0D, 0.0D, 0.0D);
					world.spawnParticle(EnumParticleTypes.REDSTONE, (xRandom + iRandom), yRandom, (zRandom + jRandom), 0.0D, 0.0D, 0.0D);
					break;
				case NORTH:
					world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, (xRandom + jRandom), yRandom, (zRandom - iRandom), 0.0D, 0.0D, 0.0D);
					world.spawnParticle(EnumParticleTypes.REDSTONE, (xRandom + jRandom), yRandom, (zRandom - iRandom), 0.0D, 0.0D, 0.0D);
					break;
				case SOUTH:
					world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, (xRandom + jRandom), yRandom, (zRandom + iRandom), 0.0D, 0.0D, 0.0D);
					world.spawnParticle(EnumParticleTypes.REDSTONE, (xRandom + jRandom), yRandom, (zRandom + iRandom), 0.0D, 0.0D, 0.0D);
					break;
			}
		}
	}

	@Override
	public int getLightValue(IBlockAccess world, BlockPos pos)
	{
		if(client.enableAmbientLighting)
		{
			TileEntity tileEntity = world.getTileEntity(pos);
	
			if(tileEntity instanceof IActiveState)
			{
				if(((IActiveState)tileEntity).getActive() && ((IActiveState)tileEntity).lightUpdate())
				{
					return client.ambientLightingLevel;
				}
			}
		}

		return 0;
	}

/*
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta)
	{
		switch(blockType)
		{
			case MACHINE_BLOCK_1:
				switch(meta)
				{
					case 0:
					case 1:
					case 2:
					case 3:
					case 9:
					case 10:
						return icons[meta][side];
					default:
						return icons[meta][0] != null ? icons[meta][0] : BASE_ICON;
				}
			case MACHINE_BLOCK_2:
				switch(meta)
				{
					case 3:
					case 5:
						return icons[meta][side];
					default:
						return icons[meta][0] != null ? icons[meta][0] : BASE_ICON;
				}
			case MACHINE_BLOCK_3:
				switch(meta)
				{
					case 3:
					case 5:
					case 6:
						return icons[meta][side];
					default:
						return icons[meta][0] != null ? icons[meta][0] : BASE_ICON;
				}
			default:
				return BASE_ICON;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess world, BlockPos pos, int side)
	{
		int meta = world.getBlockMetadata(pos);
		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(pos);

		switch(blockType)
		{
			case MACHINE_BLOCK_1:
				switch(meta)
				{
					case 0:
					case 1:
					case 2:
					case 3:
					case 9:
					case 10:
						boolean active = MekanismUtils.isActive(world, pos);
						return icons[meta][MekanismUtils.getBaseOrientation(side, tileEntity.facing)+(active ? 6 : 0)];
					case 5:
					case 6:
					case 7:
						TileEntityFactory factory = (TileEntityFactory)tileEntity;
						active = MekanismUtils.isActive(world, pos);
						
						return factoryIcons[factory.tier.ordinal()][factory.recipeType.ordinal()][MekanismUtils.getBaseOrientation(side, tileEntity.facing)+(active ? 6 : 0)];
					default:
						return icons[meta][0];
				}
			case MACHINE_BLOCK_2:
				switch(meta)
				{
					case 3:
					case 5:
						boolean active = MekanismUtils.isActive(world, pos);
						return icons[meta][MekanismUtils.getBaseOrientation(side, tileEntity.facing)+(active ? 6 : 0)];
					default:
						return icons[meta][0];
				}
			case MACHINE_BLOCK_3:
				switch(meta)
				{
					case 3:
					case 5:
					case 6:
						boolean active = MekanismUtils.isActive(world, pos);
						return icons[meta][MekanismUtils.getBaseOrientation(side, tileEntity.facing)+(active ? 6 : 0)];
					default:
						return icons[meta][0];
				}
		}
		
		return null;
	}
*/

	@Override
	public int damageDropped(IBlockState state)
	{
		return getMetaFromState(state);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs creativetabs, List<ItemStack> list)
	{
		for(MachineType type : BlockStateMachine.MachineType.getValidMachines())
		{
			if(type.typeBlock == getMachineBlock() && type.isEnabled())
			{
				switch(type)
				{
					case BASIC_FACTORY:
					case ADVANCED_FACTORY:
					case ELITE_FACTORY:
						for(RecipeType recipe : RecipeType.values())
						{
							ItemStack stack = new ItemStack(item, 1, type.meta);
							((IFactory)stack.getItem()).setRecipeType(recipe.ordinal(), stack);
							list.add(stack);
						}
						
						break;
					case FLUID_TANK:
						ItemBlockMachine itemMachine = (ItemBlockMachine)item;
						
						for(FluidTankTier tier : FluidTankTier.values())
						{
							ItemStack stack = new ItemStack(item, 1, type.meta);
							itemMachine.setBaseTier(stack, tier.getBaseTier());
							list.add(stack);
						}

						if(general.prefilledPortableTanks)
						{
							for(Fluid f : FluidRegistry.getRegisteredFluids().values())
							{
								try { //Prevent bad IDs
									ItemStack filled = new ItemStack(item, 1, type.meta);
									itemMachine.setBaseTier(filled, BaseTier.ULTIMATE);
									itemMachine.setFluidStack(new FluidStack(f, itemMachine.getCapacity(filled)), filled);
									list.add(filled);
								} catch(Exception e) {}
							}
						}
						
						break;
					default:
						list.add(new ItemStack(item, 1, type.meta));
				}
			}
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if(world.isRemote)
		{
			return true;
		}

		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(pos);
		int metadata = state.getBlock().getMetaFromState(state);

		if(entityplayer.getCurrentEquippedItem() != null)
		{
			Item tool = entityplayer.getCurrentEquippedItem().getItem();

			if(MekanismUtils.hasUsableWrench(entityplayer, pos))
			{
				if(SecurityUtils.canAccess(entityplayer, tileEntity))
				{
					if(entityplayer.isSneaking())
					{
						dismantleBlock(world, x, y, z, false);
						
						return true;
					}
	
					if(MekanismUtils.isBCWrench(tool))
					{
						((IToolWrench)tool).wrenchUsed(entityplayer, pos);
					}
	
					int change = tileEntity.facing.rotateY().ordinal();
	
					if(tileEntity instanceof TileEntityLogisticalSorter)
					{
						if(!((TileEntityLogisticalSorter)tileEntity).hasInventory())
						{
							for(EnumFacing dir : EnumFacing.VALUES)
							{
								TileEntity tile = Coord4D.get(tileEntity).offset(dir).getTileEntity(world);
	
								if(tile instanceof IInventory)
								{
									change = dir.getOpposite().ordinal();
									break;
								}
							}
						}
					}
	
					tileEntity.setFacing((short)change);
					world.notifyBlocksOfNeighborChange(x, y, z, this);
				}
				else {
					SecurityUtils.displayNoAccess(entityplayer);
				}
				
				return true;
			}
		}

		if(tileEntity != null)
		{
			MachineType type = BlockStateMachine.MachineType.get(getMachineBlock(), metadata);

			switch(type)
			{
				case PERSONAL_CHEST:
					if(!entityplayer.isSneaking() && !world.isSideSolid(x, y + 1, z, ForgeDirection.DOWN))
					{
						TileEntityPersonalChest chest = (TileEntityPersonalChest)tileEntity;
						
						if(SecurityUtils.canAccess(entityplayer, tileEntity))
						{
							MekanismUtils.openPersonalChestGui((EntityPlayerMP)entityplayer, chest, null, true);
						}
						else {
							SecurityUtils.displayNoAccess(entityplayer);
						}
						
						return true;
					}
					
					break;
				case FLUID_TANK:
					if(!entityplayer.isSneaking())
					{
						if(SecurityUtils.canAccess(entityplayer, tileEntity))
						{
							if(entityplayer.getCurrentEquippedItem() != null && FluidContainerRegistry.isContainer(entityplayer.getCurrentEquippedItem()))
							{
								if(manageInventory(entityplayer, (TileEntityFluidTank)tileEntity))
								{
									entityplayer.inventory.markDirty();
									return true;
								}
							} 
							else {
								entityplayer.openGui(Mekanism.instance, type.guiId, world, x, y, z);
							}
						}
						else {
							SecurityUtils.displayNoAccess(entityplayer);
						}
						
						return true;
					}
					
					break;
				case LOGISTICAL_SORTER:
					if(!entityplayer.isSneaking())
					{
						if(SecurityUtils.canAccess(entityplayer, tileEntity))
						{
							LogisticalSorterGuiMessage.openServerGui(SorterGuiPacket.SERVER, 0, world, (EntityPlayerMP)entityplayer, Coord4D.get(tileEntity), -1);
						}
						else {
							SecurityUtils.displayNoAccess(entityplayer);
						}
						
						return true;
					}
					
					break;
				case TELEPORTER:
				case QUANTUM_ENTANGLOPORTER:
					if(!entityplayer.isSneaking())
					{
						String owner = ((ISecurityTile)tileEntity).getSecurity().getOwner();
						
						if(owner == null || entityplayer.getCommandSenderName().equals(owner))
						{
							entityplayer.openGui(Mekanism.instance, type.guiId, world, pos.getX(), pos.getY(), pos.getZ());
						}
						else {
							SecurityUtils.displayNoAccess(entityplayer);
						}
						
						return true;
					}
					
					break;
				default:
					if(!entityplayer.isSneaking() && type.guiId != -1)
					{
						if(SecurityUtils.canAccess(entityplayer, tileEntity))
						{
							entityplayer.openGui(Mekanism.instance, type.guiId, world, x, y, z);
						}
						else {
							SecurityUtils.displayNoAccess(entityplayer);
						}
						
						return true;
					}
					
					break;
			}
		}
		
		return false;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state)
	{
		int metadata = getMetaFromState(state);
		if(BlockStateMachine.MachineType.get(getMachineBlock(), metadata) == null)
		{
			return null;
		}

		return BlockStateMachine.MachineType.get(getMachineBlock(), metadata).create();
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata)
	{
		return null;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random random, int fortune)
	{
		return null;
	}

	@Override
	public int getRenderType()
	{
		return 3;
	}

	@Override
	public float getBlockHardness(World world, BlockPos pos)
	{
		TileEntity tile = world.getTileEntity(pos);
		
		if(tile instanceof ISecurityTile)
		{
			return SecurityUtils.getSecurity((ISecurityTile)tile) == SecurityMode.PUBLIC ? blockHardness : -1;
		}
		
		return blockHardness;
	}
	
	@Override
	public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion)
    {
		IBlockState state = world.getBlockState(pos);
		if(BlockStateMachine.MachineType.get(getMachineBlock(), state.getBlock().getMetaFromState(state)) != BlockStateMachine.MachineType.PERSONAL_CHEST)
		{
			return blockResistance;
		}
		else {
			return -1;
		}
    }

	@Override
	public boolean removedByPlayer(World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
	{
		if(!player.capabilities.isCreativeMode && !world.isRemote && willHarvest)
		{
			TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(pos);

			float motion = 0.7F;
			double motionX = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionY = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionZ = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;

			EntityItem entityItem = new EntityItem(world, pos.getX() + motionX, pos.getY() + motionY, pos.getZ() + motionZ, getPickBlock(null, world, pos, player));

			world.spawnEntityInWorld(entityItem);
		}

		return world.setBlockToAir(pos);
	}
	
	private boolean manageInventory(EntityPlayer player, TileEntityFluidTank tileEntity)
	{
		ItemStack itemStack = player.getCurrentEquippedItem();

		if(itemStack != null)
		{
			if(FluidContainerRegistry.isEmptyContainer(itemStack))
			{
				if(tileEntity.fluidTank.getFluid() != null && tileEntity.fluidTank.getFluid().amount >= FluidContainerRegistry.BUCKET_VOLUME)
				{
					ItemStack filled = FluidContainerRegistry.fillFluidContainer(tileEntity.fluidTank.getFluid(), itemStack);

					if(filled != null)
					{
						if(player.capabilities.isCreativeMode)
						{
							tileEntity.fluidTank.drain(FluidContainerRegistry.getFluidForFilledItem(filled).amount, true);

							return true;
						}

						if(itemStack.stackSize > 1)
						{
							if(player.inventory.addItemStackToInventory(filled))
							{
								itemStack.stackSize--;

								tileEntity.fluidTank.drain(FluidContainerRegistry.getFluidForFilledItem(filled).amount, true);
							}
						}
						else if(itemStack.stackSize == 1)
						{
							player.setCurrentItemOrArmor(0, filled);

							tileEntity.fluidTank.drain(FluidContainerRegistry.getFluidForFilledItem(filled).amount, true);

							return true;
						}
					}
				}
			}
			else if(FluidContainerRegistry.isFilledContainer(itemStack))
			{
				FluidStack itemFluid = FluidContainerRegistry.getFluidForFilledItem(itemStack);
				int needed = tileEntity.getCurrentNeeded();
				
				if((tileEntity.fluidTank.getFluid() == null && itemFluid.amount <= tileEntity.fluidTank.getCapacity()) || itemFluid.amount <= needed)
				{
					if(tileEntity.fluidTank.getFluid() != null && !tileEntity.fluidTank.getFluid().isFluidEqual(itemFluid))
					{
						return false;
					}
					
					boolean filled = false;
					
					if(player.capabilities.isCreativeMode)
					{
						filled = true;
					}
					else {
						ItemStack containerItem = itemStack.getItem().getContainerItem(itemStack);
	
						if(containerItem != null)
						{
							if(itemStack.stackSize == 1)
							{
								player.setCurrentItemOrArmor(0, containerItem);
								filled = true;
							}
							else {
								if(player.inventory.addItemStackToInventory(containerItem))
								{
									itemStack.stackSize--;
	
									filled = true;
								}
							}
						}
						else {
							itemStack.stackSize--;
	
							if(itemStack.stackSize == 0)
							{
								player.setCurrentItemOrArmor(0, null);
							}
	
							filled = true;
						}
					}

					if(filled)
					{
						int toFill = Math.min(tileEntity.fluidTank.getCapacity()-tileEntity.fluidTank.getFluidAmount(), itemFluid.amount);
						
						tileEntity.fluidTank.fill(itemFluid, true);
						
						if(itemFluid.amount-toFill > 0)
						{
							tileEntity.pushUp(new FluidStack(itemFluid.getFluid(), itemFluid.amount-toFill), true);
						}
						
						return true;
					}
				}
			}
		}

		return false;
	}

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock)
	{
		if(!world.isRemote)
		{
			TileEntity tileEntity = world.getTileEntity(pos);

			if(tileEntity instanceof TileEntityBasicBlock)
			{
				((TileEntityBasicBlock)tileEntity).onNeighborChange(neighborBlock);
			}
			
			if(tileEntity instanceof TileEntityLogisticalSorter)
			{
				TileEntityLogisticalSorter sorter = (TileEntityLogisticalSorter)tileEntity;

				if(!sorter.hasInventory())
				{
					for(EnumFacing dir : EnumFacing.VALUES)
					{
						TileEntity tile = Coord4D.get(tileEntity).offset(dir).getTileEntity(world);

						if(tile instanceof IInventory)
						{
							sorter.setFacing((short)dir.getOpposite().ordinal());
							return;
						}
					}
				}
			}
		}
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos, EntityPlayer player)
	{
		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(pos);
		IBlockState state = world.getBlockState(pos);
		ItemStack itemStack = new ItemStack(this, 1, state.getBlock().getMetaFromState(state));

		if(itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}
		
		if(tileEntity instanceof TileEntityFluidTank)
		{
			ITierItem tierItem = (ITierItem)itemStack.getItem();
			tierItem.setBaseTier(itemStack, ((TileEntityFluidTank)tileEntity).tier.getBaseTier());
		}
		
		if(tileEntity instanceof ISecurityTile)
		{
			ISecurityItem securityItem = (ISecurityItem)itemStack.getItem();
			
			if(securityItem.hasSecurity(itemStack))
			{
				securityItem.setOwner(itemStack, ((ISecurityTile)tileEntity).getSecurity().getOwner());
				securityItem.setSecurity(itemStack, ((ISecurityTile)tileEntity).getSecurity().getMode());
			}
		}

		if(tileEntity instanceof IUpgradeTile)
		{
			((IUpgradeTile)tileEntity).getComponent().write(itemStack.getTagCompound());
		}

		if(tileEntity instanceof ISideConfiguration)
		{
			ISideConfiguration config = (ISideConfiguration)tileEntity;

			config.getConfig().write(itemStack.getTagCompound());
		}
		
		if(tileEntity instanceof ISustainedData)
		{
			((ISustainedData)tileEntity).writeSustainedData(itemStack);
		}

		if(tileEntity instanceof IRedstoneControl)
		{
			IRedstoneControl control = (IRedstoneControl)tileEntity;
			itemStack.getTagCompound().setInteger("controlType", control.getControlType().ordinal());
		}

		if(tileEntity instanceof IStrictEnergyStorage)
		{
			IEnergizedItem energizedItem = (IEnergizedItem)itemStack.getItem();
			energizedItem.setEnergy(itemStack, ((IStrictEnergyStorage)tileEntity).getEnergy());
		}

		if(tileEntity instanceof TileEntityContainerBlock && ((TileEntityContainerBlock)tileEntity).inventory.length > 0)
		{
			ISustainedInventory inventory = (ISustainedInventory)itemStack.getItem();
			inventory.setInventory(((ISustainedInventory)tileEntity).getInventory(), itemStack);
		}

		if(((ISustainedTank)itemStack.getItem()).hasTank(itemStack))
		{
			if(tileEntity instanceof ISustainedTank)
			{
				if(((ISustainedTank)tileEntity).getFluidStack() != null)
				{
					((ISustainedTank)itemStack.getItem()).setFluidStack(((ISustainedTank)tileEntity).getFluidStack(), itemStack);
				}
			}
		}

		if(tileEntity instanceof TileEntityFactory)
		{
			IFactory factoryItem = (IFactory)itemStack.getItem();
			factoryItem.setRecipeType(((TileEntityFactory)tileEntity).recipeType.ordinal(), itemStack);
		}

		return itemStack;
	}
	
	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state)
	{
		TileEntity tileEntity = world.getTileEntity(pos);

		if(!world.isRemote)
		{
			if(tileEntity instanceof TileEntityBasicBlock)
			{
				((TileEntityBasicBlock)tileEntity).onAdded();
			}
		}
	}

	@Override
	public boolean canConnectRedstone(IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		IBlockState state = world.getBlockState(pos);
		MachineType type = BlockStateMachine.MachineType.get(getMachineBlock(), state.getBlock().getMetaFromState(state));

		switch(type)
		{
			case LASER_AMPLIFIER:
				return true;
			default:
				return false;
		}
	}

	public ItemStack dismantleBlock(World world, BlockPos pos, boolean returnBlock)
	{
		ItemStack itemStack = getPickBlock(null, world, pos, null);

		world.setBlockToAir(pos);

		if(!returnBlock)
		{
			float motion = 0.7F;
			double motionX = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionY = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionZ = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;

			EntityItem entityItem = new EntityItem(world, pos.getX() + motionX, pos.getY() + motionY, pos.getZ() + motionZ, itemStack);

			world.spawnEntityInWorld(entityItem);
		}

		return itemStack;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, BlockPos pos)
	{
		IBlockState state = world.getBlockState(pos);
		MachineType type = BlockStateMachine.MachineType.get(getMachineBlock(), state.getBlock().getMetaFromState(state));

		switch(type)
		{
			case CHARGEPAD:
				setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.06F, 1.0F);
				break;
			case FLUID_TANK:
				setBlockBounds(0.125F, 0.0F, 0.125F, 0.875F, 1.0F, 0.875F);
				break;
			default:
				setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
				break;
		}
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state)
	{
		setBlockBoundsBasedOnState(world, pos);
		
		if(world.getTileEntity(pos) instanceof TileEntityChargepad)
		{
			return null;
		}

		return super.getCollisionBoundingBox(world, pos, state);
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		IBlockState state = world.getBlockState(pos);
		MachineType type = BlockStateMachine.MachineType.get(getMachineBlock(), state.getBlock().getMetaFromState(state));

		switch(type)
		{
			case CHARGEPAD:
			case PERSONAL_CHEST:
				return false;
			case FLUID_TANK:
				return side == EnumFacing.UP || side == EnumFacing.DOWN;
			default:
				return true;
		}
	}

	@Override
	public void setRenderBounds(Block block, int metadata) {}

	@Override
	public boolean doDefaultBoundSetting(int metadata)
	{
		return false;
	}

	@Override
	public EnumFacing[] getValidRotations(World world, BlockPos pos)
	{
		TileEntity tile = world.getTileEntity(pos);
		EnumFacing[] valid = new EnumFacing[6];
		
		if(tile instanceof TileEntityBasicBlock)
		{
			TileEntityBasicBlock basicTile = (TileEntityBasicBlock)tile;
			
			for(EnumFacing dir : EnumFacing.VALUES)
			{
				if(basicTile.canSetFacing(dir.ordinal()))
				{
					valid[dir.ordinal()] = dir;
				}
			}
		}
		
		return valid;
	}

	@Override
	public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis)
	{
		TileEntity tile = world.getTileEntity(pos);
		
		if(tile instanceof TileEntityBasicBlock)
		{
			TileEntityBasicBlock basicTile = (TileEntityBasicBlock)tile;
			
			if(basicTile.canSetFacing(axis.ordinal()))
			{
				basicTile.setFacing((short)axis.ordinal());
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public int getWeakPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side)
    {
		TileEntity tile = world.getTileEntity(pos);
		
		if(tile instanceof TileEntityLaserAmplifier)
		{
			return ((TileEntityLaserAmplifier)tile).emittingRedstone ? 15 : 0;
		}
		
        return 0;
    }
/*
	@Override
	public CTMData getCTMData(IBlockAccess world, BlockPos pos, int meta)
	{
		if(ctms[meta][1] != null && MekanismUtils.isActive(world, pos))
		{
			return ctms[meta][1];
		}
		
		return ctms[meta][0];
	}

	@Override
	public boolean shouldRenderBlock(IBlockAccess world, BlockPos pos, int meta)
	{
		return !MachineType.get(this, meta).hasModel;
	}*/

}
