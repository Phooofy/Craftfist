package dev.craftfist.mixin;
import dev.craftfist.Craftfist;
import dev.craftfist.PunchAnimation;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(HeldItemRenderer.class)
public abstract class PunchHandMixin {
 @Shadow private void renderArmHoldingItem(MatrixStack matrices,VertexConsumerProvider vertices,int light,float equip,float swing,Arm arm){throw new AssertionError();}
 // Replace the held item with the textured player fist in its own matrix scope.
 @Inject(method="renderFirstPersonItem",at=@At("HEAD"),cancellable=true)
 private void craftfist$punchPose(AbstractClientPlayerEntity player,float tickDelta,float pitch,Hand hand,float swing,ItemStack stack,float equip,MatrixStack matrices,VertexConsumerProvider vertices,int light,CallbackInfo ci){
  if(!stack.isOf(Craftfist.GAUNTLET))return;
  ci.cancel();matrices.push();
  Arm arm=hand==Hand.MAIN_HAND?player.getMainArm():player.getMainArm().getOpposite();
  int side=arm==Arm.RIGHT?1:-1;
  float pull=PunchAnimation.pull(tickDelta),thrust=PunchAnimation.thrust(tickDelta);
  float block=PunchAnimation.block(tickDelta);
  float uppercut=PunchAnimation.uppercutLift(tickDelta);
  float rising=Math.max(0,uppercut)/.55f;
  // Positive screen-side travel winds the fist outward; positive roll points it upward-left.
  matrices.translate(side*(-.04+.12*pull-.18*thrust),.02+.15*pull+.16*thrust,-.12+.12*pull-.65*thrust);
  matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(side*(24*pull*(1-block)+10*thrust)));
  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-12*pull-8*thrust));
  // Rotate around the visible fist rather than the camera, preventing the guard from orbiting off screen.
  matrices.translate(-side*.18*block,.12*block,-.06*block);
  // A compact upward drive keeps the knuckles in view instead of exposing the back of a twisted wrist.
  matrices.translate(-side*.08*rising,uppercut*.7f,-.08*rising);
  matrices.translate(side*.55,-.3,-.65);
  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(side*(40*block-12*rising)));
  matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(side*(25*block+8*rising)));
  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-10*block-15*rising));
  matrices.translate(-side*.55,.3,.65);
  if(!player.isInvisible()){matrices.scale(1.2f,1.2f,1.2f);renderArmHoldingItem(matrices,vertices,light,0,0,arm);}
  matrices.pop();
 }
}
