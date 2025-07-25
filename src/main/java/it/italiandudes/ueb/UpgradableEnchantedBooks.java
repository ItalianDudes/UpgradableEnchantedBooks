package it.italiandudes.ueb;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Mod(modid = UpgradableEnchantedBooks.MODID, name = UpgradableEnchantedBooks.NAME, version = UpgradableEnchantedBooks.VERSION)
public final class UpgradableEnchantedBooks {

    // Mod Info
    public static final String MODID = "ueb";
    public static final String NAME = "UpgradableEnchantedBooks";
    public static final String VERSION = "1.1R";

    // Logger & Instance
    public static Logger LOGGER = LogManager.getLogger();

    // EventHandler
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {}
    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("Hello World from UpgradableEnchantedBooks!");
    }

    // Events
    @SubscribeEvent(receiveCanceled = true)
    public void onAnvilUpdate(AnvilUpdateEvent event) {
        if (event.isCanceled()) event.setCanceled(false);
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        boolean rightQuarkTome = Objects.requireNonNull(right.getItem().getRegistryName()).toString().equals("quark:ancient_tome");

        if (left.getItem() == Items.ENCHANTED_BOOK && (right.getItem() == Items.ENCHANTED_BOOK || rightQuarkTome)) {
            Map<Enchantment, Integer> leftEnchants = getEnchantmentsNBT(left);
            Map<Enchantment, Integer> rightEnchants = getEnchantmentsNBT(right);

            if (rightQuarkTome) { // Right == ANCIENT_TOME
                Optional<Enchantment> tomeEnchantment = rightEnchants.keySet().stream().findFirst();
                if (tomeEnchantment.isPresent()) {
                    Enchantment enchantment = tomeEnchantment.get();
                    int level = rightEnchants.get(enchantment);

                    if (leftEnchants.containsKey(enchantment) && leftEnchants.get(enchantment) == level) {
                        Map<Enchantment, Integer> upgraded = new HashMap<>();

                        for (Enchantment copy : leftEnchants.keySet()) {
                            upgraded.put(copy, leftEnchants.get(copy));
                        }

                        upgraded.replace(enchantment, level+1);
                        ItemStack output = new ItemStack(Items.ENCHANTED_BOOK);
                        EnchantmentHelper.setEnchantments(upgraded, output);
                        event.setOutput(output);
                        event.setCost(35);
                    }
                }
            } else { // Right == ENCHANTED_BOOK
                if (leftEnchants.size() == rightEnchants.size() && !leftEnchants.isEmpty()) {
                    boolean allMatch = true;

                    for (Map.Entry<Enchantment, Integer> entry : leftEnchants.entrySet()) {
                        Enchantment enchantment = entry.getKey();
                        int leftLevel = entry.getValue();

                        if (!rightEnchants.containsKey(enchantment) || rightEnchants.get(enchantment) != leftLevel) {
                            allMatch = false;
                            break;
                        }
                    }

                    if (allMatch) {
                        Map<Enchantment, Integer> upgraded = new HashMap<>();

                        for (Map.Entry<Enchantment, Integer> entry : leftEnchants.entrySet()) {
                            upgraded.put(entry.getKey(), entry.getValue() + 1);
                        }

                        int cost = 0;
                        if (ModConfig.costMultiplier > 0) {
                            for (int level : upgraded.values()) {
                                cost += level * ModConfig.costMultiplier;
                            }
                        }

                        ItemStack output = new ItemStack(Items.ENCHANTED_BOOK);
                        EnchantmentHelper.setEnchantments(upgraded, output);
                        event.setOutput(output);
                        event.setCost(cost);
                    }
                }
            }
        }
    }

    public static Map<Enchantment, Integer> getEnchantmentsNBT(@Nonnull ItemStack stack) { // JEID Compatible
        Map<Enchantment, Integer> enchantments = new HashMap<>();
        if (stack.hasTagCompound() && stack.getTagCompound() != null && stack.getTagCompound().hasKey("StoredEnchantments")) {
            NBTTagList list = stack.getTagCompound().getTagList("StoredEnchantments", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound enchantTag = list.getCompoundTagAt(i);
                int id = enchantTag.getShort("id");
                int lvl = enchantTag.getShort("lvl");

                Enchantment enchantment = Enchantment.getEnchantmentByID(id);
                if (enchantment != null) {
                    enchantments.put(enchantment, lvl);
                }
            }
        }
        return enchantments;
    }
}
