package catwalks.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import catwalks.CatwalksMod;
import catwalks.block.extended.BlockExtended;
import catwalks.block.extended.TileExtended;
import catwalks.block.property.UPropertyBool;
import catwalks.block.property.UPropertyEnum;
import catwalks.register.ItemRegister;
import catwalks.shade.ccl.vec.BlockCoord;
import catwalks.shade.ccl.vec.Cuboid6;
import catwalks.shade.ccl.vec.Matrix4;
import catwalks.shade.ccl.vec.Vector3;
import catwalks.util.ExtendedFlatHighlightMOP;
import catwalks.util.GeneralUtil;
import catwalks.util.Trimap;
import catwalks.util.WrenchChecker;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class BlockCatwalkBase extends BlockExtended implements ICatwalkConnect {

	public BlockCatwalkBase(Material material, String name) {
		super(material, name);
		init();
	}
	
	public BlockCatwalkBase(Material materialIn, String name, Class<?> clazz) {
		super(materialIn, name, clazz);
		init();
	}
	
	public void init() {
		initSides();
		initColllisionBoxes();
	}
	
	public static UPropertyBool BOTTOM = new UPropertyBool("bottom");
	public static UPropertyBool TOP    = new UPropertyBool("top");
	public static UPropertyBool NORTH  = new UPropertyBool("north");
	public static UPropertyBool SOUTH  = new UPropertyBool("south");
	public static UPropertyBool EAST   = new UPropertyBool("east");
	public static UPropertyBool WEST   = new UPropertyBool("west");
	
	public static UPropertyBool TAPE   = new UPropertyBool("tape");
	public static UPropertyBool LIGHTS = new UPropertyBool("lights");
	public static UPropertyBool VINES  = new UPropertyBool("vines");
	
	public static UPropertyEnum<EnumFacing> FACING = UPropertyEnum.create("facing", EnumFacing.class);
	
	public static Trimap<UPropertyBool, EnumFacing, Integer> sides = new Trimap<>(UPropertyBool.class, EnumFacing.class, Integer.class);
	static int I_BOTTOM=0, I_TOP=1, I_NORTH=2, I_SOUTH=3, I_EAST=4, I_WEST=5,  I_FACING_ID=6, I_FACING_LEN=3, I_TAPE=10, I_LIGHTS=11, I_VINES=12;

	static {
		sides.put(BOTTOM, EnumFacing.DOWN,  I_BOTTOM);
		sides.put(TOP,    EnumFacing.UP,    I_TOP   );
		sides.put(NORTH,  EnumFacing.NORTH, I_NORTH );
		sides.put(SOUTH,  EnumFacing.SOUTH, I_SOUTH );
		sides.put(EAST,   EnumFacing.EAST,  I_EAST  );
		sides.put(WEST,   EnumFacing.WEST,  I_WEST  );
	}
	
	protected static int I_BASE_LEN=24;
	
	public static PropertyEnum<EnumCatwalkMaterial> MATERIAL = PropertyEnum.create("material", EnumCatwalkMaterial.class);
	
	public static enum EnumCatwalkMaterial implements IStringSerializable {
		STEEL, STONE, WOOD, CUSTOM;

		@Override
		public String getName() {
			return this.name();
		}
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumFacing side, float hitX, float hitY, float hitZ) {
		
		if( playerIn.inventory.getCurrentItem() != null) {
			if(!WrenchChecker.isAWrench( playerIn.inventory.getCurrentItem().getItem() ))
				return false;
			if(playerIn.inventory.getCurrentItem().getItem() instanceof ItemBlock)
				return false;
		} else {
			return false;
		}
		
		TileExtended tile = (TileExtended) worldIn.getTileEntity(pos);
		int id = sides.getC(side);
		
		if(side != EnumFacing.UP ) {
			tile.setBoolean(id, !tile.getBoolean(id));
			tile.markDirty();
			worldIn.markBlockForUpdate(pos);
			return true;
		}
		
		return super.onBlockActivated(worldIn, pos, state, playerIn, side, hitX, hitY, hitZ);
	}
	
	public boolean putDecoration(World world, BlockPos pos, String name, boolean value) {
		TileExtended tile = (TileExtended) world.getTileEntity(pos);
		
		if("lights".equals(name)) {
			if(tile.getBoolean(I_LIGHTS) == value) {
				return false;
			}
			tile.setBoolean(I_LIGHTS, value);
			return true;
		}
		if("tape".equals(name)) {
			if(tile.getBoolean(I_TAPE) == value) {
				return false;
			}
			tile.setBoolean(I_TAPE, value);
			return true;
		}
		if("vines".equals(name)) {
			if(tile.getBoolean(I_VINES) == value) {
				return false;
			}
			tile.setBoolean(I_VINES, value);
			return true;
		}
		return false;
	}
	
	public static String makeTextureGenName(String type, String part, EnumCatwalkMaterial material, boolean tape, boolean lights, boolean vines) {
		String str = CatwalksMod.MODID + ":/gen/" + type + "_" + part + "_";
		str += material.getName().toLowerCase() + "_";
		if(tape)   str += 't';
		if(lights) str += 'l';
		if(vines)  str += 'v';
		return str;
	}

	{/* ICatwalkConnectable */}
	
	@Override
	public boolean canConnectToSide(World world, BlockPos pos, EnumFacing side) {
		return true;
	}
	
	@Override
	public boolean isSideOpen(World world, BlockPos pos, EnumFacing side) {
		TileExtended tile = (TileExtended) world.getTileEntity(pos);
		return tile.getBoolean(sides.getC(side));
	}

	@Override
	public boolean isWide(World world, BlockPos pos, EnumFacing side) {
		return true;
	}
	
	{ /* state */ }
	
	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		TileExtended tile = (TileExtended) worldIn.getTileEntity(pos);
		
		boolean pass = tile != null;
		
		return ((IExtendedBlockState)state)
				.withProperty(BOTTOM, pass && tile.getBoolean(I_BOTTOM))
				.withProperty(NORTH,  pass && tile.getBoolean(I_NORTH) )
				.withProperty(SOUTH,  pass && tile.getBoolean(I_SOUTH) )
				.withProperty(EAST,   pass && tile.getBoolean(I_EAST)  )
				.withProperty(WEST,   pass && tile.getBoolean(I_WEST)  )
				.withProperty(TAPE,   pass && tile.getBoolean(I_TAPE)  )
				.withProperty(LIGHTS, pass && tile.getBoolean(I_LIGHTS))
				.withProperty(VINES,  pass && tile.getBoolean(I_VINES) )
				.withProperty(FACING, pass ? EnumFacing.VALUES[tile.getNumber(I_FACING_ID, I_FACING_LEN)] : EnumFacing.UP)
		;
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	protected BlockState createBlockState() {
		IProperty[] listedProperties = new IProperty[] { MATERIAL };
	    List<IUnlistedProperty> unlistedProperties = new ArrayList<IUnlistedProperty>();
	    unlistedProperties.addAll(Lists.asList(BOTTOM, new IUnlistedProperty[] { NORTH, SOUTH, WEST, EAST, FACING, TAPE, LIGHTS, VINES }));
	    
	    return new ExtendedBlockState(this, listedProperties, unlistedProperties.toArray(new IUnlistedProperty[0]));
	}

	{ /* rendering */ }
	
	@Override
	public int getLightValue(IBlockAccess world, BlockPos pos) {
		IExtendedBlockState state = (IExtendedBlockState) getExtendedState(world.getBlockState(pos), world, pos);
		return state.getValue(LIGHTS) ? 15 : 0;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	@Override
	public boolean isFullCube() {
		return false;
	}
	
	@Override
	public EnumWorldBlockLayer getBlockLayer() {
		return EnumWorldBlockLayer.CUTOUT_MIPPED;
	}
	
	{ /* placing/breaking */ }

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		
		IExtendedBlockState estate = (IExtendedBlockState) getExtendedState(state, worldIn, pos);
		List<ItemStack> drops = new ArrayList<>();
		if(estate.getValue(TAPE)) {
			drops.add(new ItemStack(ItemRegister.tape, 1, ItemRegister.tape.getMaxDamage() - 1));
		}
		if(estate.getValue(LIGHTS)) {
			drops.add(new ItemStack(ItemRegister.lights, 1, ItemRegister.lights.getMaxDamage() - 1));
		}
		if(estate.getValue(VINES)) {
			drops.add(new ItemStack(ItemRegister.vines, 1, ItemRegister.vines.getMaxDamage() - 1));
		}
		
		for (ItemStack stack : drops) {
			GeneralUtil.spawnItemStack(worldIn, pos.getX()+0.5, pos.getY()+0.5, pos.getZ	()+0.5, stack);
		}
		
		for (EnumFacing direction : EnumFacing.HORIZONTALS) {
			if(worldIn.getBlockState(pos.offset(direction)).getBlock() instanceof BlockCatwalkBase) {
				TileExtended tile = (TileExtended) worldIn.getTileEntity(pos.offset(direction));
				if(tile.getBoolean(sides.getC(direction.getOpposite())) == false) {
					tile.setBoolean(sides.getC(direction.getOpposite()), true);
				}
			}
			
		}
		
		super.breakBlock(worldIn, pos, state);
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		TileExtended ourTile = (TileExtended) worldIn.getTileEntity(pos);
		
		ourTile.setNumber(I_FACING_ID, I_FACING_LEN, placer.getHorizontalFacing().ordinal());
		
		for (EnumFacing direction : EnumFacing.VALUES) {
			ourTile.setBoolean(sides.getC(direction), true);
		}
		
		for (EnumFacing direction : EnumFacing.HORIZONTALS) {
			if(worldIn.getBlockState(pos.offset(direction)).getBlock() instanceof BlockCatwalkBase) {
				TileExtended tile = (TileExtended) worldIn.getTileEntity(pos.offset(direction));
				ourTile.setBoolean(sides.getC(direction), false);
				   tile.setBoolean(sides.getC(direction.getOpposite()), false);
			}
			
		}
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
	}


	{ /* meta */ }
	
	@Override
	public int damageDropped(IBlockState state) {
	    return state.getValue(MATERIAL).ordinal();
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock(this);
	}

	@Override
	public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
		return true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list)
	{
	    for (EnumCatwalkMaterial enumdyecolor : EnumCatwalkMaterial.values())
	    {
	        list.add(new ItemStack(itemIn, 1, enumdyecolor.ordinal()));
	    }
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
	    return this.getDefaultState().withProperty(MATERIAL, EnumCatwalkMaterial.values()[ meta ]);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
	    return state.getValue(MATERIAL).ordinal();
	}
	
	{ /* collision */ }
	
	public abstract void initColllisionBoxes();
	public abstract List<CollisionBox> getCollisionBoxes(IExtendedBlockState state);
	
	@Override
	public void addCollisionBoxesToList(World world, BlockPos pos, IBlockState rawState, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity) {
		
		AxisAlignedBB originMask = mask.offset(-pos.getX(), -pos.getY(), -pos.getZ());
		
		boolean eNull = collidingEntity == null;
		
        IExtendedBlockState state = (IExtendedBlockState) getExtendedState(world.getBlockState(pos), world, pos);
		
		List<CollisionBox> boxes = getCollisionBoxes(state);
		
    	for (CollisionBox box : boxes) {
    		if(!state.getValue(box.enableProperty))
    			continue;
			Cuboid6 cuboid = (eNull || !collidingEntity.isSneaking()) ? box.normal : box.sneak;
			
			if(cuboid.aabb().intersectsWith(originMask)) {
				list.add(cuboid.aabb().offset(pos.getX(), pos.getY(), pos.getZ()));
			}
		}
	}
	
	{ /* raytracing */}
	
	public abstract void initSides();
	
	public abstract List<LookSide> lookSides(IExtendedBlockState state);
	
	@Override
	public MovingObjectPosition collisionRayTrace(World world, BlockPos pos, EntityPlayer player, Vec3 startRaw, Vec3 endRaw) {
		IExtendedBlockState state = (IExtendedBlockState) getExtendedState(world.getBlockState(pos), world, pos);
		boolean hasWrench = player.inventory.getCurrentItem() != null && WrenchChecker.isAWrench(player.inventory.getCurrentItem().getItem());
		
		Vector3
			blockPosVec = new Vector3(pos),
			start = new Vector3(startRaw).sub(blockPosVec),
			end   = new Vector3(  endRaw).sub(blockPosVec);
		
		List<LookSide> sides = lookSides(state);
		if(sides == null) {
			return null;
		}
		
		LookSide hitSide = null;
		Vector3 hitVector = null;
		double smallestDistanceSq = Double.POSITIVE_INFINITY;
		
		for (LookSide side : sides) {
			Quad quad = null;
			if(side.showProperty == null || state.getValue(side.showProperty)) {
				quad = side.mainSide;
			} else if(side.showWithoutWrench || hasWrench) {
				quad = side.wrenchSide;
			}
			if(quad == null)
				continue;
			
			Tri[] tris = quad.tris();
			for (Tri tri : tris) {
				Vector3 vec = GeneralUtil.intersectRayTri(start, end, tri);
				if(vec != null) {
					double distanceSq = vec.copy().sub(start).magSquared();
					if(distanceSq < smallestDistanceSq) {
						hitSide = side;
						hitVector = vec.copy().add(blockPosVec);
						smallestDistanceSq = distanceSq;
					}
				}
			}
		}
		
		if(hitSide == null)
			return null;
		
		Quad quad = null;
		
		if(hitSide.showProperty == null || state.getValue(hitSide.showProperty)) {
			quad = hitSide.mainSide;
		} else if(hitSide.showWithoutWrench || hasWrench) {
			quad = hitSide.wrenchSide;
		}
		
		ExtendedFlatHighlightMOP mop = new ExtendedFlatHighlightMOP(quad, hitVector,
						( hitSide.side == null ? EnumFacing.UP : hitSide.side ).ordinal(),
						new BlockCoord(pos), null,
						Math.sqrt(smallestDistanceSq)
				) ;

		
		
		return mop;
	}
	
	{ /* internal classes */ }
	
	public static class CollisionBox {
		
		public Cuboid6 normal, sneak;
		public UPropertyBool enableProperty;
		
		public void apply(Matrix4 matrix) {
			normal.apply(matrix);
			sneak.apply(matrix);
		}
		
		@Override
		public String toString() {
			return enableProperty.getName() + " => " + normal.toString() + " | " + sneak.toString();
		}
		
	}
	
	public static class LookSide {
		public Quad mainSide, wrenchSide;
		public UPropertyBool showProperty;
		public boolean showWithoutWrench;
		public EnumFacing side;
		
		public LookSide() {}
		
		public LookSide(Quad mainSide, Quad wrenchSide, EnumFacing side, UPropertyBool showProperty, boolean showWithoutWrench) {
			this.mainSide = mainSide;
			this.wrenchSide = wrenchSide;
			this.showProperty = showProperty;
			this.showWithoutWrench = showWithoutWrench;
			this.side = side;
		}
		
		public void apply(Matrix4 matrix) {
			mainSide.apply(matrix);
			wrenchSide.apply(matrix);
		}
		
		public LookSide copy() {
			LookSide side = new LookSide();
			
			side.mainSide   = mainSide.copy();
			side.wrenchSide = wrenchSide.copy();
			side.showProperty = showProperty;
			side.showWithoutWrench = showWithoutWrench;
			
			return side;
		}
		
		@Override
		public String toString() {
			return showProperty.getName() + " => " + mainSide.toString() + " | " + wrenchSide.toString() + " @ " + side.getName();
		}
		
	}
	
	public static class Quad {
		public Vector3 v1, v2, v3, v4;
		
		public Quad(Vector3 v1, Vector3 v2, Vector3 v3, Vector3 v4) {
			this.v1 = v1;
			this.v2 = v2;
			this.v3 = v3;
			this.v4 = v4;
		}
		
		public void apply(Matrix4 matix) {
			v1.apply(matix);
			v2.apply(matix);
			v3.apply(matix);
			v4.apply(matix);
		}
		
		public Quad copy() {
			return new Quad(v1.copy(), v2.copy(), v3.copy(), v4.copy());
		}
		
		public Tri[] tris() {
			Tri[] tris = new Tri[2];
			tris[0] = new Tri(v1, v2, v3);
			tris[1] = new Tri(v1, v3, v4);
			return tris;
		}
		
		@Override
		public String toString() {
			return "(" + v1.toString() + ", " + v2.toString() + ", " + v3.toString() + ", " + v4.toString() + ")";
		}
	}
	
	public static class Tri {
		public Vector3 v1, v2, v3;
		public Tri(Vector3 v1, Vector3 v2, Vector3 v3) {
			this.v1 = v1;
			this.v2 = v2;
			this.v3 = v3;
		}
		
		public void apply(Matrix4 matix) {
			v1.apply(matix);
			v2.apply(matix);
			v3.apply(matix);
		}
		
		public Tri copy() {
			return new Tri(v1.copy(), v2.copy(), v3.copy());
		}
		
		@Override
		public String toString() {
			return "(" + v1.toString() + ", " + v2.toString() + ", " + v3.toString() + ")";
		}
	}
}
