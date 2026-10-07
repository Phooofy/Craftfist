package dev.craftfist.mixin;
import dev.craftfist.Craftfist;
import dev.craftfist.PunchAnimation;
import dev.craftfist.ChargeMovement;
import dev.craftfist.Motion;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.input.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(KeyboardInput.class)
public class SlamSneakMixin {
 @Unique private final ChargeMovement craftfist$chargeMovement=new ChargeMovement();
 @Inject(method="tick",at=@At("TAIL"))
 private void craftfist$abilityMovement(boolean slowDown,float slowFactor,CallbackInfo ci){
  MinecraftClient c=MinecraftClient.getInstance();
  if(c.player!=null && Craftfist.equipped(c.player)){
   Input input=(Input)(Object)this;
   boolean wasSneaking=input.sneaking;
   input.sneaking=false;
   if(wasSneaking && slowDown && slowFactor>0){input.movementForward/=slowFactor;input.movementSideways/=slowFactor;}
   boolean charging=PunchAnimation.shouldSlowCharge(),carryingAbility=PunchAnimation.carriesAbilityMomentum();
   double scale=craftfist$chargeMovement.horizontalScale(charging,carryingAbility,c.player.getVelocity().horizontalLength());
   if(scale!=1)c.player.setVelocity(c.player.getVelocity().multiply(scale,1,scale));
   // Both ground and air steering are slowed. Only tagged ability momentum bypasses the initial brake.
   if(charging){
    input.movementForward*= (float)Motion.CHARGE_INPUT_SCALE;
    input.movementSideways*= (float)Motion.CHARGE_INPUT_SCALE;
    if(!carryingAbility)c.player.setSprinting(false);
   }
  }else craftfist$chargeMovement.horizontalScale(false,false);
 }
}
