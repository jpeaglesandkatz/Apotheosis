package dev.shadowsoffire.apotheosis.potion;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.Apotheosis.ApotheosisConstruction;
import dev.shadowsoffire.apotheosis.Apotheosis.ApotheosisReloadEvent;
import dev.shadowsoffire.apotheosis.ench.objects.GlowyBlockItem.GlowyItem;
import dev.shadowsoffire.apotheosis.potion.compat.CuriosCompat;
import dev.shadowsoffire.attributeslib.api.ALObjects;
import dev.shadowsoffire.placebo.config.Configuration;
import dev.shadowsoffire.placebo.registry.RegistryEvent.Register;
import dev.shadowsoffire.placebo.tabs.TabFillingRegistry;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.SlotTypeMessage;

public class PotionModule {

    public static final Logger LOG = LogManager.getLogger("Apotheosis : Potion");
    public static final ResourceLocation POTION_TEX = new ResourceLocation(Apotheosis.MODID, "textures/potions.png");

    static boolean charmsInCuriosOnly = false;

    @SubscribeEvent
    public void preInit(ApotheosisConstruction e) {
        this.reload(null);
        if (FMLEnvironment.dist.isClient()) {
            FMLJavaModLoadingContext.get().getModEventBus().register(PotionModuleClient.class);
        }
        InterModComms.sendTo("curios", "REGISTER_TYPE", () -> new SlotTypeMessage.Builder("charm").size(1).build());

    }

    @SubscribeEvent
    public void init(FMLCommonSetupEvent e) {
        e.enqueueWork(() -> {
            PotionBrewing.addMix(Potions.AWKWARD, Items.SHULKER_SHELL, Apoth.Potions.RESISTANCE.get());
            PotionBrewing.addMix(Apoth.Potions.RESISTANCE.get(), Items.REDSTONE, Apoth.Potions.LONG_RESISTANCE.get());
            PotionBrewing.addMix(Apoth.Potions.RESISTANCE.get(), Items.GLOWSTONE_DUST, Apoth.Potions.STRONG_RESISTANCE.get());

            PotionBrewing.addMix(Apoth.Potions.RESISTANCE.get(), Items.FERMENTED_SPIDER_EYE, Apoth.Potions.SUNDERING.get());
            PotionBrewing.addMix(Apoth.Potions.LONG_RESISTANCE.get(), Items.FERMENTED_SPIDER_EYE, Apoth.Potions.LONG_SUNDERING.get());
            PotionBrewing.addMix(Apoth.Potions.STRONG_RESISTANCE.get(), Items.FERMENTED_SPIDER_EYE, Apoth.Potions.STRONG_SUNDERING.get());
            PotionBrewing.addMix(Apoth.Potions.SUNDERING.get(), Items.REDSTONE, Apoth.Potions.LONG_SUNDERING.get());
            PotionBrewing.addMix(Apoth.Potions.SUNDERING.get(), Items.GLOWSTONE_DUST, Apoth.Potions.STRONG_SUNDERING.get());

            PotionBrewing.addMix(Potions.AWKWARD, Items.GOLDEN_APPLE, Apoth.Potions.ABSORPTION.get());
            PotionBrewing.addMix(Apoth.Potions.ABSORPTION.get(), Items.REDSTONE, Apoth.Potions.LONG_ABSORPTION.get());
            PotionBrewing.addMix(Apoth.Potions.ABSORPTION.get(), Items.GLOWSTONE_DUST, Apoth.Potions.STRONG_ABSORPTION.get());

            PotionBrewing.addMix(Potions.AWKWARD, Items.MUSHROOM_STEW, Apoth.Potions.HASTE.get());
            PotionBrewing.addMix(Apoth.Potions.HASTE.get(), Items.REDSTONE, Apoth.Potions.LONG_HASTE.get());
            PotionBrewing.addMix(Apoth.Potions.HASTE.get(), Items.GLOWSTONE_DUST, Apoth.Potions.STRONG_HASTE.get());

            PotionBrewing.addMix(Apoth.Potions.HASTE.get(), Items.FERMENTED_SPIDER_EYE, Apoth.Potions.FATIGUE.get());
            PotionBrewing.addMix(Apoth.Potions.LONG_HASTE.get(), Items.FERMENTED_SPIDER_EYE, Apoth.Potions.LONG_FATIGUE.get());
            PotionBrewing.addMix(Apoth.Potions.STRONG_HASTE.get(), Items.FERMENTED_SPIDER_EYE, Apoth.Potions.STRONG_FATIGUE.get());
            PotionBrewing.addMix(Apoth.Potions.FATIGUE.get(), Items.REDSTONE, Apoth.Potions.LONG_FATIGUE.get());
            PotionBrewing.addMix(Apoth.Potions.FATIGUE.get(), Items.GLOWSTONE_DUST, Apoth.Potions.STRONG_FATIGUE.get());

            if (Apoth.Items.SKULL_FRAGMENT.isPresent()) PotionBrewing.addMix(Potions.AWKWARD, Apoth.Items.SKULL_FRAGMENT.get(), Apoth.Potions.WITHER.get());
            else PotionBrewing.addMix(Potions.AWKWARD, Items.WITHER_SKELETON_SKULL, Apoth.Potions.WITHER.get());
            PotionBrewing.addMix(Apoth.Potions.WITHER.get(), Items.REDSTONE, Apoth.Potions.LONG_WITHER.get());
            PotionBrewing.addMix(Apoth.Potions.WITHER.get(), Items.GLOWSTONE_DUST, Apoth.Potions.STRONG_WITHER.get());

            PotionBrewing.addMix(Potions.AWKWARD, Items.EXPERIENCE_BOTTLE, Apoth.Potions.KNOWLEDGE.get());
            PotionBrewing.addMix(Apoth.Potions.KNOWLEDGE.get(), Items.REDSTONE, Apoth.Potions.LONG_KNOWLEDGE.get());
            PotionBrewing.addMix(Apoth.Potions.KNOWLEDGE.get(), Items.EXPERIENCE_BOTTLE, Apoth.Potions.STRONG_KNOWLEDGE.get());

            PotionBrewing.addMix(Potions.AWKWARD, Apoth.Items.LUCKY_FOOT.get(), Potions.LUCK);

            PotionBrewing.addMix(Potions.AWKWARD, Items.SWEET_BERRIES, Apoth.Potions.VITALITY.get());
            PotionBrewing.addMix(Apoth.Potions.VITALITY.get(), Items.REDSTONE, Apoth.Potions.LONG_VITALITY.get());
            PotionBrewing.addMix(Apoth.Potions.VITALITY.get(), Items.GLOWSTONE_DUST, Apoth.Potions.STRONG_VITALITY.get());

            PotionBrewing.addMix(Apoth.Potions.VITALITY.get(), Items.FERMENTED_SPIDER_EYE, Apoth.Potions.GRIEVOUS.get());
            PotionBrewing.addMix(Apoth.Potions.LONG_VITALITY.get(), Items.FERMENTED_SPIDER_EYE, Apoth.Potions.LONG_GRIEVOUS.get());
            PotionBrewing.addMix(Apoth.Potions.STRONG_VITALITY.get(), Items.FERMENTED_SPIDER_EYE, Apoth.Potions.STRONG_GRIEVOUS.get());
            PotionBrewing.addMix(Apoth.Potions.GRIEVOUS.get(), Items.REDSTONE, Apoth.Potions.LONG_GRIEVOUS.get());
            PotionBrewing.addMix(Apoth.Potions.GRIEVOUS.get(), Items.GLOWSTONE_DUST, Apoth.Potions.STRONG_GRIEVOUS.get());

            PotionBrewing.addMix(Potions.SLOW_FALLING, Items.FERMENTED_SPIDER_EYE, Apoth.Potions.LEVITATION.get());
            PotionBrewing.addMix(Apoth.Potions.LEVITATION.get(), Items.POPPED_CHORUS_FRUIT, Apoth.Potions.FLYING.get());
            PotionBrewing.addMix(Apoth.Potions.FLYING.get(), Items.REDSTONE, Apoth.Potions.LONG_FLYING.get());
            PotionBrewing.addMix(Apoth.Potions.LONG_FLYING.get(), Items.REDSTONE, Apoth.Potions.EXTRA_LONG_FLYING.get());
        });
        Apotheosis.HELPER.registerProvider(factory -> {
            Ingredient fireRes = Apotheosis.potionIngredient(Potions.FIRE_RESISTANCE);
            Ingredient abs = Apotheosis.potionIngredient(Apoth.Potions.STRONG_ABSORPTION.get());
            Ingredient res = Apotheosis.potionIngredient(Apoth.Potions.RESISTANCE.get());
            Ingredient regen = Apotheosis.potionIngredient(Potions.STRONG_REGENERATION);
            factory.addShaped(Items.ENCHANTED_GOLDEN_APPLE, 3, 3, fireRes, regen, fireRes, abs, Items.GOLDEN_APPLE, abs, res, abs, res);
        });

        MinecraftForge.EVENT_BUS.addListener(this::drops);
        MinecraftForge.EVENT_BUS.addListener(this::reload);
    }

    @SubscribeEvent
    public void items(Register<Item> e) {
        e.getRegistry().registerAll(new GlowyItem(new Item.Properties()), "lucky_foot", new PotionCharmItem(), "potion_charm");
        TabFillingRegistry.register(CreativeModeTabs.INGREDIENTS, Apoth.Items.LUCKY_FOOT);
        TabFillingRegistry.register(CreativeModeTabs.TOOLS_AND_UTILITIES, Apoth.Items.POTION_CHARM);
    }

    @SubscribeEvent
    public void types(Register<Potion> e) {

        e.getRegistry().registerAll(
            new Potion("resistance", new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 3600)), "resistance",
            new Potion("resistance", new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 9600)), "long_resistance",
            new Potion("resistance", new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1800, 1)), "strong_resistance",
            new Potion("absorption", new MobEffectInstance(MobEffects.ABSORPTION, 1200, 1)), "absorption",
            new Potion("absorption", new MobEffectInstance(MobEffects.ABSORPTION, 3600, 1)), "long_absorption",
            new Potion("absorption", new MobEffectInstance(MobEffects.ABSORPTION, 600, 3)), "strong_absorption",
            new Potion("haste", new MobEffectInstance(MobEffects.DIG_SPEED, 3600)), "haste",
            new Potion("haste", new MobEffectInstance(MobEffects.DIG_SPEED, 9600)), "long_haste",
            new Potion("haste", new MobEffectInstance(MobEffects.DIG_SPEED, 1800, 1)), "strong_haste",
            new Potion("fatigue", new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 3600)), "fatigue",
            new Potion("fatigue", new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 9600)), "long_fatigue",
            new Potion("fatigue", new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 1800, 1)), "strong_fatigue",
            new Potion("wither", new MobEffectInstance(MobEffects.WITHER, 3600)), "wither",
            new Potion("wither", new MobEffectInstance(MobEffects.WITHER, 9600)), "long_wither",
            new Potion("wither", new MobEffectInstance(MobEffects.WITHER, 1800, 1)), "strong_wither",
            new Potion("sundering", new MobEffectInstance(ALObjects.MobEffects.SUNDERING.get(), 3600)), "sundering",
            new Potion("sundering", new MobEffectInstance(ALObjects.MobEffects.SUNDERING.get(), 9600)), "long_sundering",
            new Potion("sundering", new MobEffectInstance(ALObjects.MobEffects.SUNDERING.get(), 1800, 1)), "strong_sundering",
            new Potion("knowledge", new MobEffectInstance(ALObjects.MobEffects.KNOWLEDGE.get(), 2400)), "knowledge",
            new Potion("knowledge", new MobEffectInstance(ALObjects.MobEffects.KNOWLEDGE.get(), 4800)), "long_knowledge",
            new Potion("knowledge", new MobEffectInstance(ALObjects.MobEffects.KNOWLEDGE.get(), 1200, 1)), "strong_knowledge",
            new Potion("vitality", new MobEffectInstance(ALObjects.MobEffects.VITALITY.get(), 4800)), "vitality",
            new Potion("vitality", new MobEffectInstance(ALObjects.MobEffects.VITALITY.get(), 14400)), "long_vitality",
            new Potion("vitality", new MobEffectInstance(ALObjects.MobEffects.VITALITY.get(), 3600, 1)), "strong_vitality",
            new Potion("grievous", new MobEffectInstance(ALObjects.MobEffects.GRIEVOUS.get(), 4800)), "grievous",
            new Potion("grievous", new MobEffectInstance(ALObjects.MobEffects.GRIEVOUS.get(), 14400)), "long_grievous",
            new Potion("grievous", new MobEffectInstance(ALObjects.MobEffects.GRIEVOUS.get(), 3600, 1)), "strong_grievous",
            new Potion("levitation", new MobEffectInstance(MobEffects.LEVITATION, 2400)), "levitation",
            new Potion("flying", new MobEffectInstance(ALObjects.MobEffects.FLYING.get(), 9600)), "flying",
            new Potion("flying", new MobEffectInstance(ALObjects.MobEffects.FLYING.get(), 18000)), "long_flying",
            new Potion("flying", new MobEffectInstance(ALObjects.MobEffects.FLYING.get(), 36000)), "extra_long_flying");

    }

    @SubscribeEvent
    public void serializers(Register<RecipeSerializer<?>> e) {
        e.getRegistry().register(PotionCharmRecipe.Serializer.INSTANCE, Apoth.Items.POTION_CHARM.getId());
        e.getRegistry().register(PotionEnchantingRecipe.SERIALIZER, "potion_charm_enchanting");
    }

    @SubscribeEvent
    public void imcEvent(InterModEnqueueEvent e) {
        if (ModList.get().isLoaded("curios")) CuriosCompat.sendIMC();
    }

    public void drops(LivingDropsEvent e) {
        if (e.getEntity() instanceof Rabbit rabbit) {
            if (rabbit.level().random.nextFloat() < 0.045F + 0.045F * e.getLootingLevel()) {
                e.getDrops().clear();
                e.getDrops().add(new ItemEntity(rabbit.level(), rabbit.getX(), rabbit.getY(), rabbit.getZ(), new ItemStack(Apoth.Items.LUCKY_FOOT.get())));
            }
        }
    }

    public void reload(ApotheosisReloadEvent e) {
        Configuration config = new Configuration(new File(Apotheosis.configDir, "potion.cfg"));
        config.setTitle("Apotheosis Potion Module Configuration");
        charmsInCuriosOnly = config.getBoolean("Restrict Charms to Curios", "general", charmsInCuriosOnly, "If Potion Charms will only work when in a curios slot, instead of in the inventory.");

        String[] defExt = { ForgeRegistries.MOB_EFFECTS.getKey(MobEffects.NIGHT_VISION).toString(), ForgeRegistries.MOB_EFFECTS.getKey(MobEffects.HEALTH_BOOST).toString() };
        String[] names = config.getStringList("Extended Potion Charms", "general", defExt,
            "A list of effects that, when as charms, will be applied and reapplied at a longer threshold to avoid issues at low durations, like night vision.\nServer-authoritative.");
        PotionCharmItem.EXTENDED_POTIONS.clear();
        for (String s : names) {
            try {
                PotionCharmItem.EXTENDED_POTIONS.add(new ResourceLocation(s));
            }
            catch (ResourceLocationException ex) {
                LOG.error("Invalid extended potion charm entry {} will be ignored.", s);
            }
        }

        String[] blacklist = config.getStringList("Blacklisted Potion Charm Effects", "general", new String[0],
            "A list of effects that, cannot be crafted into Potion Charms.\nServer-authoritative.");
        PotionCharmItem.BLACKLIST.clear();
        for (String s : blacklist) {
            try {
                PotionCharmItem.BLACKLIST.add(new ResourceLocation(s));
            }
            catch (ResourceLocationException ex) {
                LOG.error("Invalid blacklisted potion charm entry {} will be ignored.", s);
            }
        }

        if (e == null && config.hasChanged()) config.save();
    }

}
