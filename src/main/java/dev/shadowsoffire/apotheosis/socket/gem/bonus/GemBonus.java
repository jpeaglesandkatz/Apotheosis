package dev.shadowsoffire.apotheosis.socket.gem.bonus;

import javax.annotation.Nullable;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.special.AllStatsBonus;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.special.BloodyArrowBonus;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.special.DropTransformBonus;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.special.LeechBlockBonus;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.special.MageSlayerBonus;
import dev.shadowsoffire.placebo.codec.CodecMap;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;

public abstract class GemBonus implements CodecProvider<GemBonus> {

    // TODO: Convert to Registry<Codec<?>> instead of using a raw codec map.
    public static final CodecMap<GemBonus> CODEC = new CodecMap<>("Gem Bonus");

    protected final ResourceLocation id;
    protected final GemClass gemClass;

    public GemBonus(ResourceLocation id, GemClass gemClass) {
        this.id = id;
        this.gemClass = gemClass;
    }

    /**
     * Validates that this gem bonus has been deserialized into a valid state.
     * If not, throws an error.
     *
     * @return this
     * @apiNote Overriders should strongly-type to their class.
     */
    public abstract GemBonus validate();

    /**
     * Checks if this bonus supports the quality.
     *
     * @param quality The quality being checked.
     * @return True, if this bonus contains values for the specified quality.
     * @apiNote Other methods in this class will throw an exception if the bonus does not support the quality.
     */
    public abstract boolean supports(Purity quality);

    /**
     * Gets the one-line socket bonus tooltip.
     *
     * @param gem    The gem stack.
     * @param rarity The rarity of the gem.
     */
    public abstract Component getSocketBonusTooltip(GemInstance inst, AttributeTooltipContext ctx);

    /**
     * Retrieve the modifiers from this bonus to be applied to the socketed stack.
     * All modifiers for all slots should be supplied unconditionally.
     * <p>
     * To generate modifier ids, use {@link #makeModifierId(GemInstance, EquipmentSlotGroup, String)}
     *
     * @param inst  The current gem instance.
     * @param event The attribute modifier event, which will accept any created modifiers.
     */
    public void addModifiers(GemInstance inst, ItemAttributeModifierEvent event) {}

    /**
     * Calculates the protection value of this bonus, with respect to the given damage source.
     *
     * @param inst   The current gem instance.
     * @param source The damage source to compare against.
     * @return How many protection points this affix is worth against this source.
     */
    public float getDamageProtection(GemInstance inst, DamageSource source) {
        return 0;
    }

    /**
     * Calculates the additional damage this bonus provides.
     * This damage is dealt as player physical damage.
     *
     * @param inst The current gem instance.
     * @param type The type of the mob.
     */
    public float getDamageBonus(GemInstance inst, Entity target) {
        return 0.0F;
    }

    /**
     * Called when someone attacks an entity with an item that has this bonus.<br>
     * Specifically, this is invoked whenever the user attacks a target, while having an item with this bonus in either hand or any armor slot.
     *
     * @param inst   The current gem instance.
     * @param user   The wielder of the weapon. The weapon stack will be in their main hand.
     * @param target The target entity being attacked.
     */
    public void doPostAttack(GemInstance inst, LivingEntity user, @Nullable Entity target) {}

    /**
     * Called when an entity that has this bonus on one of its armor items is damaged.
     *
     * @param inst   The current gem instance.
     * @param user   The entity wearing an item with this bonus.
     * @param source The source of the attack.
     */
    public void doPostHurt(GemInstance inst, LivingEntity user, DamageSource source) {}

    /**
     * Called when a user fires an arrow from a bow or crossbow with this affix on it.
     */
    public void onArrowFired(GemInstance inst, LivingEntity user, AbstractArrow arrow) {}

    /**
     * Called when {@link Item#useOn(ItemUseContext)} would be called for an item with this affix.
     * Return null to not impact the original result type.
     */
    @Nullable
    public InteractionResult onItemUse(GemInstance inst, UseOnContext ctx) {
        return null;
    }

    /**
     * Called when an arrow that was marked with this affix hits a target.
     */
    public void onArrowImpact(GemInstance inst, AbstractArrow arrow, HitResult res) {}

    /**
     * Called when a shield with this affix blocks some amount of damage.
     *
     * @param inst   The current gem instance.
     * @param entity The blocking entity.
     * @param source The damage source being blocked.
     * @param amount The amount of damage blocked.
     * @return The amount of damage that is *actually* blocked by the shield, after this affix applies.
     */
    public float onShieldBlock(GemInstance inst, LivingEntity entity, DamageSource source, float amount) {
        return amount;
    }

    /**
     * Called when a player with this affix breaks a block.
     *
     * @param inst   The current gem instance.
     * @param player The breaking player.
     * @param level  The level the block was broken in.
     * @param pos    The position of the block.
     * @param state  The state that was broken.
     */
    public void onBlockBreak(GemInstance inst, Player player, LevelAccessor level, BlockPos pos, BlockState state) {

    }

    /**
     * Allows an affix to reduce durability damage to an item.
     *
     * @param inst The current gem instance.
     * @param user The user of the item, if applicable.
     * @return The percentage [0, 1] of durability damage to ignore. This value will be summed with all other affixes that increase it.
     */
    public float getDurabilityBonusPercentage(GemInstance inst, @Nullable ServerPlayer user) {
        return 0;
    }

    /**
     * Fires during the {@link LivingHurtEvent}, and allows for modification of the damage value.<br>
     * If the value is set to zero or below, the event will be cancelled.
     *
     * @param inst   The current gem instance.
     * @param src    The Damage Source of the attack.
     * @param user   The entity being attacked.
     * @param amount The amount of damage that is to be taken.
     * @return The amount of damage that will be taken, after modification. This value will propagate to other bonuses.
     */
    public float onHurt(GemInstance inst, DamageSource src, LivingEntity user, float amount) {
        return amount;
    }

    /**
     * Fires during {@link GetEnchantmentLevelEvent} and allows for increasing enchantment levels.
     *
     * @param inst The current gem instance.
     * @param ench The enchantment being queried for.
     * @return The bonus level to be added to the current enchantment.
     */
    public void getEnchantmentLevels(GemInstance inst, ItemEnchantments.Mutable enchantments) {}

    /**
     * Fires from {@link LootModifier#apply(ObjectArrayList, LootContext)} when this bonus is active on the tool given by the context.
     *
     * @param inst The current gem instance.
     * @param loot The generated loot.
     * @param ctx  The loot context.
     */
    public void modifyLoot(GemInstance inst, ObjectArrayList<ItemStack> loot, LootContext ctx) {}

    public ResourceLocation getId() {
        return this.id;
    }

    public GemClass getGemClass() {
        return this.gemClass;
    }

    /**
     * Generates a deterministic {@link ResourceLocation} that is unique for a given socketed gem instance.
     * <p>
     * Can be used to generate attribute modifiers, track cooldowns, and other things that need to be unique per-gem-in-slot.
     * 
     * @param inst The owning gem instance for the bonus
     * @param salt A salt value, which can be used if the bonus needs multiple modifiers.
     */
    protected static ResourceLocation makeUniqueId(GemInstance inst, String salt) {
        return ResourceLocation.fromNamespaceAndPath(inst.gem().getId().getNamespace(), inst.gem().getId().getPath() + "_modifier_" + inst.category().getSlots().getSerializedName() + "_" + inst.slot() + salt);
    }

    /**
     * Calls {@link #makeUniqueId(GemInstance, String)} with an empty salt value.
     */
    protected static ResourceLocation makeUniqueId(GemInstance inst) {
        return makeUniqueId(inst, "");
    }

    public static void initCodecs() {
        register("attribute", AttributeBonus.CODEC);
        register("multi_attribute", MultiAttrBonus.CODEC);
        register("durability", DurabilityBonus.CODEC);
        register("damage_reduction", DamageReductionBonus.CODEC);
        register("enchantment", EnchantmentBonus.CODEC);
        register("bloody_arrow", BloodyArrowBonus.CODEC);
        register("leech_block", LeechBlockBonus.CODEC);
        register("all_stats", AllStatsBonus.CODEC);
        register("drop_transform", DropTransformBonus.CODEC);
        register("mageslayer", MageSlayerBonus.CODEC);
        register("mob_effect", MobEffectBonus.CODEC);
    }

    protected static <T extends GemBonus> App<RecordCodecBuilder.Mu<T>, GemClass> gemClass() {
        return GemClass.CODEC.fieldOf("gem_class").forGetter(GemBonus::getGemClass);
    }

    private static void register(String id, Codec<? extends GemBonus> codec) {
        CODEC.register(Apotheosis.loc(id), codec);
    }

}
