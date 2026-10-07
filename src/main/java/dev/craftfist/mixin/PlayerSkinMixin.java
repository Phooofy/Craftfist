package dev.craftfist.mixin;
import dev.craftfist.Craftfist;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(AbstractClientPlayerEntity.class)
public class PlayerSkinMixin {
 @Inject(method="getSkinTextures",at=@At("RETURN"),cancellable=true)
 private void craftfist$skin(CallbackInfoReturnable<SkinTextures> cir){
  if(Craftfist.equipped((AbstractClientPlayerEntity)(Object)this)){
   SkinTextures old=cir.getReturnValue();
   cir.setReturnValue(new SkinTextures(Identifier.of("craftfist","textures/entity/doomfist.png"),old.textureUrl(),old.capeTexture(),old.elytraTexture(),SkinTextures.Model.WIDE,false));
  }
 }
}
