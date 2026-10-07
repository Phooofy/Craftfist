package dev.craftfist.mixin;

import dev.craftfist.Craftfist;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class PowerBlockDamageMixin {
 @ModifyVariable(method="damage",at=@At("HEAD"),argsOnly=true,ordinal=0)
 private float craftfist$blockDamage(float amount,DamageSource source,float originalAmount){
  return Craftfist.blockDamage((LivingEntity)(Object)this,source,amount);
 }
}
