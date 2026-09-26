package com.liaoliao.flighthelmet;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.liaoliao.flighthelmet.network.ModNetwork;
import com.liaoliao.flighthelmet.network.PacketBreakBlock;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class NicePickaxeItem extends ItemPickaxe {
    public static final float FIXED_MINING_SPEED = 65035.0F;

    private static final String PRECISE_MODE_KEY = "NicePickaxePreciseMode";
    private static final UUID ATTACK_DAMAGE_MODIFIER = UUID.fromString("9b1f0e5a-6f4a-4b1e-9e5c-1f0a3d7c2b41");
    private static final UUID ATTACK_SPEED_MODIFIER = UUID.fromString("2c7d4e19-8a3b-4f60-b2d5-6e9c1a4f7d38");
    private static boolean clientRightClickHeld;

    public NicePickaxeItem() {
        super(ToolMaterial.DIAMOND);
        this.setCreativeTab(CreativeTabs.TOOLS);
        this.setRegistryName(FlightHelmetMod.MOD_ID, "nice_pickaxe");
        this.setTranslationKey(FlightHelmetMod.MOD_ID + ".nice_pickaxe");
    }

    /**
     * 无耐久 + 隐藏原版附魔行。
     * 1.12.2 的耐久判定是 ItemStack.isItemStackDamageable()：getMaxDamage(stack) > 0 且没有 Unbreakable 标签才算可损坏，
     * 所以不能把 getMaxDamage 改成 0——那样 ItemModelMesher 会改用真实损伤值查找模型，旧存档里已受损的工具会丢模型。
     * 而精准采集是真附魔，不隐藏的话会和本模组的「精准采集：开启」提示重复。
     */
    private static void ensureToolTags(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        if (!tag.getBoolean("Unbreakable")) {
            tag.setBoolean("Unbreakable", true);
        }
        if (tag.getInteger("HideFlags") != 1) {
            tag.setInteger("HideFlags", 1);
        }
        if (tag.hasKey("Damage")) {
            tag.removeTag("Damage");
        }
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return false;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            ItemStack stack = new ItemStack(this);
            ensureToolTags(stack);
            items.add(stack);
        }
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        ensureToolTags(stack);
    }

    /**
     * Precise mode adds a real Silk Touch enchantment, which would make the item render through the
     * enchantment glint pass and stops the animated texture from playing. This tool never glints.
     */
    @Override
    public boolean hasEffect(ItemStack stack) {
        return false;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, net.minecraft.block.state.IBlockState state) {
        return FIXED_MINING_SPEED;
    }

    @Override
    public boolean canHarvestBlock(net.minecraft.block.state.IBlockState state, ItemStack stack) {
        return true;
    }

    /**
     * 数值沿用 1.20.1/1.21 线的 8 点攻击伤害与 20 点攻击速度，1.12.2 的基准值同样是 1.0 / 4.0。
     */
    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot slot) {
        Multimap<String, AttributeModifier> modifiers = HashMultimap.create();
        if (slot == EntityEquipmentSlot.MAINHAND) {
            modifiers.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
                    new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Tool modifier", 7.0D, 0));
            modifiers.put(SharedMonsterAttributes.ATTACK_SPEED.getName(),
                    new AttributeModifier(ATTACK_SPEED_MODIFIER, "Tool modifier", 16.0D, 0));
        }
        return modifiers;
    }

    /**
     * 1.12.2 的客户端在 onItemUseFirst 返回非 PASS 时一定会把右键包发给服务端（SUCCESS 与 FAIL 都会发），
     * 所以「按住右键只破一格」不能靠返回值实现：破坏由客户端首次按下时发出的 PacketBreakBlock 触发，
     * 本方法只负责吞掉这次交互（非 PASS 即不会触发方块交互），并用返回值控制挥臂。
     */
    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side,
                                           float hitX, float hitY, float hitZ, EnumHand hand) {
        if (hand != EnumHand.MAIN_HAND) {
            return EnumActionResult.PASS;
        }
        if (world.isRemote) {
            if (clientRightClickHeld) {
                // 按住不放：不挥臂，也不再发包
                return EnumActionResult.FAIL;
            }
            clientRightClickHeld = true;
            ModNetwork.CHANNEL.sendToServer(new PacketBreakBlock(pos));
            // 首次按下：返回 SUCCESS 让客户端挥手（按住期间的重发返回 FAIL，所以只有第一下会挥）
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.FAIL;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, net.minecraft.client.util.ITooltipFlag flag) {
        tooltip.add(net.minecraft.util.text.translation.I18n.translateToLocal(isPreciseMode(stack)
                ? "tooltip.pigthings.nice_pickaxe.precise.on"
                : "tooltip.pigthings.nice_pickaxe.precise.off"));
    }

    public static boolean isPreciseMode(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.getBoolean(PRECISE_MODE_KEY);
    }

    public static void setPreciseMode(ItemStack stack, boolean preciseMode) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        tag.setBoolean(PRECISE_MODE_KEY, preciseMode);
    }

    /**
     * Precise mode is implemented by adding or removing Silk Touch.
     */
    public static void applySilkTouch(ItemStack stack, boolean preciseMode) {
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        if (preciseMode) {
            enchantments.put(Enchantments.SILK_TOUCH, 1);
        } else {
            enchantments.remove(Enchantments.SILK_TOUCH);
        }
        EnchantmentHelper.setEnchantments(enchantments, stack);
    }

    public static void resetClientRightClick() {
        clientRightClickHeld = false;
    }
}
