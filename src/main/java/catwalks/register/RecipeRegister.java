package catwalks.register;

import catwalks.Const;
import catwalks.item.crafting.RecipeDecorationRepair;
import catwalks.item.crafting.RecipeDecorationSplit;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapedOreRecipe;

import static net.minecraftforge.oredict.RecipeSorter.Category.SHAPELESS;

public class RecipeRegister {

	public static void register() {
		
		RecipeSorter.register(Const.MODID + ":decorationCombine",  RecipeDecorationRepair.class, SHAPELESS, "before:minecraft:repair");
		RecipeSorter.register(Const.MODID + ":decorationSplit",    RecipeDecorationSplit.class,  SHAPELESS, "after:minecraft:shapeless");
		
		int m_steel = 0, m_iesteel = 1, m_wood = 2;//, m_custom = 3;
		Item
			tape = ItemRegister.tape,
			lights = ItemRegister.lights,
			speed = ItemRegister.speed,
			grate = ItemRegister.grate,
			blowtorch = ItemRegister.tool,
			paper = Items.PAPER,
			sugar = Items.SUGAR,
			flintNsteel = Items.FLINT_AND_STEEL;
		Block
			catwalk = BlockRegister.catwalk,
			stair = BlockRegister.catwalkStair,
			ladder = BlockRegister.cagedLadder,
			scaffold = BlockRegister.scaffold,
			pPlateWood = Blocks.WOODEN_PRESSURE_PLATE,
			vladder = Blocks.LADDER;
		String
			iron = "ingotIron",
			slimeball = "slimeball",
			yellow = "dyeYellow",
			glowstone = "dustGlowstone",
			stick = "stickWood";
		
		
		CraftingManager.getInstance().addRecipe(new RecipeDecorationRepair(ItemRegister.lights));
		CraftingManager.getInstance().addRecipe(new RecipeDecorationRepair(ItemRegister.speed ));
		CraftingManager.getInstance().addRecipe(new RecipeDecorationRepair(ItemRegister.tape  ));
		CraftingManager.getInstance().addRecipe(new RecipeDecorationSplit (ItemRegister.lights));
		CraftingManager.getInstance().addRecipe(new RecipeDecorationSplit (ItemRegister.speed ));
		CraftingManager.getInstance().addRecipe(new RecipeDecorationSplit (ItemRegister.tape  ));
		
		// Items
		
		addShapedOreRecipe(true, new ItemStack(blowtorch),
			"  f",
			" i ",
			"i  ",
			'i', iron,
			'f', flintNsteel);
		
		addShapedOreRecipe(true, new ItemStack(grate, 16),
			"i i",
			" i ",
			"i i",
			'i', iron
		);
		
		addShapedOreRecipe(true, new ItemStack(speed, 1, 256-16),
			"ppp",
			"sbs",
			"ppp",
			'p', paper,
			's', sugar,
			'b', slimeball
		);
		
		addShapedOreRecipe(true, new ItemStack(tape, 1, 256-16),
			"ppp",
			"dbd",
			"ppp",
			'p', paper,
			'd', yellow,
			'b', slimeball
		);
		
		addShapedOreRecipe(true, new ItemStack(lights, 1, 256-16),
			"ppp",
			"gbg",
			"ppp",
			'p', paper,
			'g', glowstone,
			'b', slimeball
		);
		
		// Catwalks
		
		addShapedOreRecipe(true, new ItemStack(catwalk, 1, m_steel),
			"g g",
			" g ",
			'g', grate);
			// rusty/steel conversion
		addShapelessRecipe(true, new ItemStack(catwalk, 1, m_iesteel),
			new ItemStack(catwalk, 1, m_steel));
		addShapelessRecipe(true, new ItemStack(catwalk, 1, m_steel),
			new ItemStack(catwalk, 1, m_iesteel));
		
		addShapedOreRecipe(true, new ItemStack(catwalk, 1, m_wood),
			"s s",
			" p ",
			's', stick,
			'p', pPlateWood);
		
		// Stairs
		
		addShapedOreRecipe(true, new ItemStack(stair, 1, m_steel),
			"g  ",
			"gg ",
			" gg",
			'g', grate);
			// rusty/steel conversion
		addShapelessRecipe(true, new ItemStack(stair, 1, m_iesteel),
			new ItemStack(stair, 1, m_steel));
		addShapelessRecipe(true, new ItemStack(stair, 1, m_steel),
			new ItemStack(stair, 1, m_iesteel));
		
		addShapedOreRecipe(true, new ItemStack(stair, 1, m_wood),
			"s  ",
			"ps ",
			" ps",
			's', stick,
			'p', pPlateWood);
		
		// Ladders
		
		addShapedOreRecipe(true, new ItemStack(ladder, 1, m_steel),
			"glg",
			'g', grate,
			'l', vladder);
			// rusty/steel conversion
		addShapelessRecipe(true, new ItemStack(ladder, 1, m_iesteel),
			new ItemStack(ladder, 1, m_steel));
		addShapelessRecipe(true, new ItemStack(ladder, 1, m_steel),
			new ItemStack(ladder, 1, m_iesteel));
		
		addShapedOreRecipe(true, new ItemStack(ladder, 1, m_wood),
			"sls",
			's', stick,
			'l', vladder);
		
		// Ladders
		
		addShapedOreRecipe(true, new ItemStack(scaffold, 4, m_steel),
			"gg",
			"gg",
			'g', grate);
			// rusty/steel conversion
		addShapelessRecipe(true, new ItemStack(scaffold, 1, m_iesteel),
			new ItemStack(scaffold, 1, m_steel));
		addShapelessRecipe(true, new ItemStack(scaffold, 1, m_steel),
			new ItemStack(scaffold, 1, m_iesteel));
		
		addShapedOreRecipe(true, new ItemStack(scaffold, 8, m_wood),
			" p ",
			"s s",
			" p ",
			's', stick,
			'p', pPlateWood);
	}
	
	
	// below methods snagged from CoFHLib

	public static void addShapelessRecipe(boolean condition, ItemStack out, Object... recipe) {
		if (condition)
			GameRegistry.addShapelessRecipe(out, recipe);
	}

	public static void addShapelessRecipe(boolean condition, Item out, Object... recipe) {

		addShapelessRecipe(condition, new ItemStack(out), recipe);
	}

	public static void addShapelessRecipe(boolean condition, Block out, Object... recipe) {

		addShapelessRecipe(condition, new ItemStack(out), recipe);
	}

	public static void addShapedOreRecipe(boolean condition, ItemStack out, Object... recipe) {
		if (condition)
			GameRegistry.addRecipe(new ShapedOreRecipe(out, recipe));
	}

	public static void addShapedOreRecipe(boolean condition, Item out, Object... recipe) {
		if (condition)
			GameRegistry.addRecipe(new ShapedOreRecipe(out, recipe));
	}

	public static void addShapedOreRecipe(boolean condition, Block out, Object... recipe) {
		if (condition)
			GameRegistry.addRecipe(new ShapedOreRecipe(out, recipe));
	}
	
}
