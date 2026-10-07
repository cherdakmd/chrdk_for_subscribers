package ru.chrdk.pantheon.registry;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import ru.chrdk.pantheon.PantheonMod;
import ru.chrdk.pantheon.block.AtticAltarBlock;
import ru.chrdk.pantheon.block.PedestalBlock;
import ru.chrdk.pantheon.block.PedestalBlockEntity;
import ru.chrdk.pantheon.item.NameScrollItem;

/** Всё, что мод добавляет в игру: блоки, предметы, постамент-сущность и вкладка креатива. */
public final class PantheonContent {
	public static final ResourceKey<Block> ATTIC_ALTAR_BLOCK_KEY = blockKey("attic_altar");
	public static final ResourceKey<Item> ATTIC_ALTAR_ITEM_KEY = itemKey("attic_altar");
	public static final ResourceKey<Block> PEDESTAL_BLOCK_KEY = blockKey("pedestal");
	public static final ResourceKey<Item> PEDESTAL_ITEM_KEY = itemKey("pedestal");
	public static final ResourceKey<Item> BLANK_SEAL_KEY = itemKey("blank_seal");
	public static final ResourceKey<Item> NAME_SCROLL_KEY = itemKey("name_scroll");
	public static final ResourceKey<CreativeModeTab> ATTIC_TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, PantheonMod.id("attic"));

	public static Block ATTIC_ALTAR;
	public static Item ATTIC_ALTAR_ITEM;
	public static Block PEDESTAL;
	public static Item PEDESTAL_ITEM;
	public static Item BLANK_SEAL;
	public static Item NAME_SCROLL;
	public static BlockEntityType<PedestalBlockEntity> PEDESTAL_ENTITY;
	public static CreativeModeTab ATTIC_TAB;

	private PantheonContent() {
	}

	public static void register() {
		ATTIC_ALTAR = registerBlock(ATTIC_ALTAR_BLOCK_KEY,
				properties -> new AtticAltarBlock(properties), Blocks.OAK_PLANKS);
		ATTIC_ALTAR_ITEM = registerBlockItem(ATTIC_ALTAR, ATTIC_ALTAR_ITEM_KEY);

		PEDESTAL = registerBlock(PEDESTAL_BLOCK_KEY,
				properties -> new PedestalBlock(properties), Blocks.STONE_BRICKS);
		PEDESTAL_ITEM = registerBlockItem(PEDESTAL, PEDESTAL_ITEM_KEY);

		BLANK_SEAL = Registry.register(BuiltInRegistries.ITEM, BLANK_SEAL_KEY,
				new Item(new Item.Properties().stacksTo(16).setId(BLANK_SEAL_KEY)));

		NAME_SCROLL = Registry.register(BuiltInRegistries.ITEM, NAME_SCROLL_KEY,
				new NameScrollItem(new Item.Properties().stacksTo(1).setId(NAME_SCROLL_KEY)));

		PEDESTAL_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, PantheonMod.id("pedestal"),
				FabricBlockEntityTypeBuilder.create(PedestalBlockEntity::new, PEDESTAL).build());

		ATTIC_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ATTIC_TAB_KEY,
				FabricCreativeModeTab.builder()
						.title(Component.translatable("itemGroup.chrdk_pantheon.attic"))
						.icon(() -> new ItemStack(BLANK_SEAL))
						.displayItems((context, entries) -> {
							entries.accept(ATTIC_ALTAR_ITEM);
							entries.accept(PEDESTAL_ITEM);
							entries.accept(BLANK_SEAL);
							entries.accept(NAME_SCROLL);
						})
						.build());
	}

	private static <T extends Block> T registerBlock(ResourceKey<Block> key, java.util.function.Function<BlockBehaviour.Properties, T> factory, Block copyFrom) {
		// noOcclusion: модели алтаря и постамента чуть выходят за границы блока (свеча, статуэтка),
		// поэтому блоку не стоит отсекать грани соседей.
		BlockBehaviour.Properties properties = BlockBehaviour.Properties.ofFullCopy(copyFrom).noOcclusion().setId(key);
		return Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(properties));
	}

	private static Item registerBlockItem(Block block, ResourceKey<Item> key) {
		return Registry.register(BuiltInRegistries.ITEM, key, new BlockItem(block, new Item.Properties().setId(key)));
	}

	private static ResourceKey<Block> blockKey(String path) {
		return ResourceKey.create(Registries.BLOCK, PantheonMod.id(path));
	}

	private static ResourceKey<Item> itemKey(String path) {
		return ResourceKey.create(Registries.ITEM, PantheonMod.id(path));
	}
}
