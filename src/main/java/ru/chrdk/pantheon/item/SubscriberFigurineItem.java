package ru.chrdk.pantheon.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import ru.chrdk.pantheon.registry.PantheonContent;

/** Персональная фигурка. Имя задаётся наковальней; последующие рецепты сохранят его. */
public final class SubscriberFigurineItem extends Item {
    public SubscriberFigurineItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static ItemStack named(String nick, int tier) {
        ItemStack stack = new ItemStack(PantheonContent.SUBSCRIBER_FIGURINE);
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Фигурка " + nick).withStyle(ChatFormatting.GOLD));
        return stack;
    }

    public static String nameOf(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() != PantheonContent.SUBSCRIBER_FIGURINE
                || !stack.has(DataComponents.CUSTOM_NAME)) return null;
        String name = stack.getHoverName().getString().trim();
        return name.startsWith("Фигурка ") ? name.substring(8).trim() : name;
    }
}
