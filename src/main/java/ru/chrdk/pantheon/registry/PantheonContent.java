package ru.chrdk.pantheon.registry;

import java.util.function.Consumer;
import java.util.function.Function;

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
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import ru.chrdk.pantheon.PantheonMod;
import ru.chrdk.pantheon.block.AtticAltarBlock;
import ru.chrdk.pantheon.block.AtticGlassBlock;
import ru.chrdk.pantheon.block.CandelabraBlock;
import ru.chrdk.pantheon.block.ObeliskBlock;
import ru.chrdk.pantheon.block.PedestalBlock;
import ru.chrdk.pantheon.block.PedestalBlockEntity;
import ru.chrdk.pantheon.block.PortraitBlock;
import ru.chrdk.pantheon.block.PortraitBlockEntity;
import ru.chrdk.pantheon.block.SubscriberChestBlock;
import ru.chrdk.pantheon.block.SubscriberChestBlockEntity;
import ru.chrdk.pantheon.item.NameScrollItem;

/** Всё, что мод добавляет в игру: блоки, предметы, сущности блоков и вкладка креатива. */
public final class PantheonContent {
	public static final ResourceKey<Block> ATTIC_ALTAR_BLOCK_KEY = blockKey("attic_altar");
	public static final ResourceKey<Item> ATTIC_ALTAR_ITEM_KEY = itemKey("attic_altar");
	public static final ResourceKey<Block> PEDESTAL_BLOCK_KEY = blockKey("pedestal");
	public static final ResourceKey<Item> PEDESTAL_ITEM_KEY = itemKey("pedestal");
	public static final ResourceKey<Block> PORTRAIT_BLOCK_KEY = blockKey("portrait");
	public static final ResourceKey<Item> PORTRAIT_ITEM_KEY = itemKey("portrait");
	public static final ResourceKey<Block> OBELISK_BLOCK_KEY = blockKey("obelisk");
	public static final ResourceKey<Item> OBELISK_ITEM_KEY = itemKey("obelisk");
	public static final ResourceKey<Block> CANDELABRA_BLOCK_KEY = blockKey("candelabra");
	public static final ResourceKey<Item> CANDELABRA_ITEM_KEY = itemKey("candelabra");
	public static final ResourceKey<Block> SUBSCRIBER_CHEST_BLOCK_KEY = blockKey("subscriber_chest");
	public static final ResourceKey<Item> SUBSCRIBER_CHEST_ITEM_KEY = itemKey("subscriber_chest");
	public static final ResourceKey<Block> ATTIC_DUST_BLOCK_KEY = blockKey("attic_dust");
	public static final ResourceKey<Item> ATTIC_DUST_ITEM_KEY = itemKey("attic_dust");
	public static final ResourceKey<Block> ATTIC_WEB_BLOCK_KEY = blockKey("attic_web");
	public static final ResourceKey<Item> ATTIC_WEB_ITEM_KEY = itemKey("attic_web");
	public static final ResourceKey<Block> ATTIC_GLASS_BLOCK_KEY = blockKey("attic_glass");
	public static final ResourceKey<Item> ATTIC_GLASS_ITEM_KEY = itemKey("attic_glass");
	public static final ResourceKey<Item> BLANK_SEAL_KEY = itemKey("blank_seal");
	public static final ResourceKey<Item> NAME_SCROLL_KEY = itemKey("name_scroll");
	public static final ResourceKey<CreativeModeTab> ATTIC_TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, PantheonMod.id("attic"));

	public static Block ATTIC_ALTAR;
	public static Item ATTIC_ALTAR_ITEM;
	public static Block PEDESTAL;
	public static Item PEDESTAL_ITEM;
	public static Block PORTRAIT;
	public static Item PORTRAIT_ITEM;
	public static Block OBELISK;
	public static Item OBELISK_ITEM;
	public static Block CANDELABRA;
	public static Item CANDELABRA_ITEM;
	public static Block SUBSCRIBER_CHEST;
	public static Item SUBSCRIBER_CHEST_ITEM;
	public static Block ATTIC_DUST;
	public static Item ATTIC_DUST_ITEM;
	public static Block ATTIC_WEB;
	public static Item ATTIC_WEB_ITEM;
	public static Block ATTIC_GLASS;
	public static Item ATTIC_GLASS_ITEM;
	public static Item BLANK_SEAL;
	public static Item NAME_SCROLL;
	public static BlockEntityType<PedestalBlockEntity> PEDESTAL_ENTITY;
	public static BlockEntityType<PortraitBlockEntity> PORTRAIT_ENTITY;
	public static BlockEntityType<SubscriberChestBlockEntity> SUBSCRIBER_CHEST_ENTITY;
	public static CreativeModeTab ATTIC_TAB;

	private PantheonContent() {
	}

	public static void register() {
		ATTIC_ALTAR = registerBlock(ATTIC_ALTAR_BLOCK_KEY, AtticAltarBlock::new, Blocks.OAK_PLANKS);
		ATTIC_ALTAR_ITEM = registerBlockItem(ATTIC_ALTAR, ATTIC_ALTAR_ITEM_KEY);

		PEDESTAL = registerBlock(PEDESTAL_BLOCK_KEY, PedestalBlock::new, Blocks.STONE_BRICKS);
		PEDESTAL_ITEM = registerBlockItem(PEDESTAL, PEDESTAL_ITEM_KEY);

		PORTRAIT = registerBlock(PORTRAIT_BLOCK_KEY, PortraitBlock::new, Blocks.OAK_PLANKS);
		PORTRAIT_ITEM = registerBlockItem(PORTRAIT, PORTRAIT_ITEM_KEY);

		OBELISK = registerBlock(OBELISK_BLOCK_KEY, ObeliskBlock::new, Blocks.STONE_BRICKS);
		OBELISK_ITEM = registerBlockItem(OBELISK, OBELISK_ITEM_KEY);

		// Канделябр светит как хороший фонарь — им и освещают чердак.
		CANDELABRA = registerBlock(CANDELABRA_BLOCK_KEY, CandelabraBlock::new, Blocks.GOLD_BLOCK,
				properties -> properties.lightLevel(state -> 14));
		CANDELABRA_ITEM = registerBlockItem(CANDELABRA, CANDELABRA_ITEM_KEY);

		// Ларь подписчика: пока его не открывали, подсвечивается изнутри.
		SUBSCRIBER_CHEST = registerBlock(SUBSCRIBER_CHEST_BLOCK_KEY, SubscriberChestBlock::new, Blocks.CHEST,
				properties -> properties.lightLevel(state -> state.getValue(SubscriberChestBlock.OPENED) ? 0 : 8));
		SUBSCRIBER_CHEST_ITEM = registerBlockItem(SUBSCRIBER_CHEST, SUBSCRIBER_CHEST_ITEM_KEY);

		// Декор чердака: пыль, паутина и витраж с гербом.
		ATTIC_DUST = registerBlock(ATTIC_DUST_BLOCK_KEY, CarpetBlock::new, Blocks.MOSS_CARPET);
		ATTIC_DUST_ITEM = registerBlockItem(ATTIC_DUST, ATTIC_DUST_ITEM_KEY);

		ATTIC_WEB = registerBlock(ATTIC_WEB_BLOCK_KEY, WebBlock::new, Blocks.COBWEB);
		ATTIC_WEB_ITEM = registerBlockItem(ATTIC_WEB, ATTIC_WEB_ITEM_KEY);

		ATTIC_GLASS = registerBlock(ATTIC_GLASS_BLOCK_KEY, AtticGlassBlock::new, Blocks.GLASS);
		ATTIC_GLASS_ITEM = registerBlockItem(ATTIC_GLASS, ATTIC_GLASS_ITEM_KEY);

		BLANK_SEAL = Registry.register(BuiltInRegistries.ITEM, BLANK_SEAL_KEY,
				new Item(new Item.Properties().stacksTo(16).setId(BLANK_SEAL_KEY)));

		NAME_SCROLL = Registry.register(BuiltInRegistries.ITEM, NAME_SCROLL_KEY,
				new NameScrollItem(new Item.Properties().stacksTo(1).setId(NAME_SCROLL_KEY)));

		PEDESTAL_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, PantheonMod.id("pedestal"),
				FabricBlockEntityTypeBuilder.create(PedestalBlockEntity::new, PEDESTAL).build());

		PORTRAIT_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, PantheonMod.id("portrait"),
				FabricBlockEntityTypeBuilder.create(PortraitBlockEntity::new, PORTRAIT).build());

		SUBSCRIBER_CHEST_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, PantheonMod.id("subscriber_chest"),
				FabricBlockEntityTypeBuilder.create(SubscriberChestBlockEntity::new, SUBSCRIBER_CHEST).build());

		ATTIC_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ATTIC_TAB_KEY,
				FabricCreativeModeTab.builder()
						.title(Component.translatable("itemGroup.chrdk_pantheon.attic"))
						.icon(() -> new ItemStack(BLANK_SEAL))
						.displayItems((context, entries) -> {
							entries.accept(ATTIC_ALTAR_ITEM);
							entries.accept(PEDESTAL_ITEM);
							entries.accept(PORTRAIT_ITEM);
							entries.accept(OBELISK_ITEM);
							entries.accept(CANDELABRA_ITEM);
							entries.accept(SUBSCRIBER_CHEST_ITEM);
							entries.accept(ATTIC_DUST_ITEM);
							entries.accept(ATTIC_WEB_ITEM);
							entries.accept(ATTIC_GLASS_ITEM);
							entries.accept(BLANK_SEAL);
							entries.accept(NAME_SCROLL);
						})
						.build());
	}

	private static <T extends Block> T registerBlock(ResourceKey<Block> key, Function<BlockBehaviour.Properties, T> factory, Block copyFrom) {
		return registerBlock(key, factory, copyFrom, properties -> {
		});
	}

	private static <T extends Block> T registerBlock(ResourceKey<Block> key, Function<BlockBehaviour.Properties, T> factory, Block copyFrom, Consumer<BlockBehaviour.Properties> tweaks) {
		// noOcclusion: модели мода выходят за границы блока (свеча, статуэтка, голова в раме),
		// поэтому блоку не стоит отсекать грани соседей.
		BlockBehaviour.Properties properties = BlockBehaviour.Properties.ofFullCopy(copyFrom).noOcclusion().setId(key);
		tweaks.accept(properties);
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
