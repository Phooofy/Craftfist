package dev.craftfist.mixin;
import dev.craftfist.Craftfist;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(HeldItemFeatureRenderer.class)
public class HiddenGauntletMixin {
 @Inject(method="renderItem",at=@At("HEAD"),cancellable=true)
 private void craftfist$hide(LivingEntity entity,ItemStack stack,ModelTransformationMode mode,Arm arm,MatrixStack matrices,VertexConsumerProvider vertices,int light,CallbackInfo ci){if(stack.isOf(Craftfist.GAUNTLET))ci.cancel();}
}
