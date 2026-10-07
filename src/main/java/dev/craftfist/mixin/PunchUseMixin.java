package dev.craftfist.mixin;
import dev.craftfist.Craftfist;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(MinecraftClient.class)
public class PunchUseMixin {
 @Inject(method="doAttack",at=@At("HEAD"),cancellable=true)
 private void craftfist$handCannonOwnsSwing(CallbackInfoReturnable<Boolean> cir){
  MinecraftClient client=(MinecraftClient)(Object)this;
  if(client.player!=null && Craftfist.equipped(client.player))cir.setReturnValue(false);
 }
 @Inject(method="doItemUse",at=@At("HEAD"),cancellable=true)
 private void craftfist$reserveRightClick(CallbackInfo ci){
  MinecraftClient client=(MinecraftClient)(Object)this;
  if(client.player!=null && Craftfist.equipped(client.player))ci.cancel();
 }
}
