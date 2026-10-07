package dev.craftfist.mixin;

import dev.craftfist.PunchAnimation;
import dev.craftfist.StepUp;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class PunchMovementMixin {
 @Unique private boolean craftfist$moving;

 @Inject(method="move",at=@At("HEAD"),cancellable=true)
 private void craftfist$walkDash(MovementType type,Vec3d movement,CallbackInfo ci){
  Entity entity=(Entity)(Object)this;
  if(craftfist$moving || !(entity instanceof ClientPlayerEntity) || type!=MovementType.SELF || !PunchAnimation.isDashing())return;
  int count=StepUp.substeps(movement);
  if(count<=1)return;
  ci.cancel();craftfist$moving=true;
  try {
   Vec3d part=movement.multiply(1.0/count);
   boolean brushedWall=false;
   for(int i=0;i<count;i++){
    Box body=entity.getBoundingBox();
    boolean supported=entity.isOnGround() || !entity.getWorld().isSpaceEmpty(entity,body.offset(0,-.05,0));
    if(supported && part.y<=.02){
     Vec3d forward=new Vec3d(part.x,0,part.z);
     double height=entity.getStepHeight();
     List<Box> obstacles=new ArrayList<>();
     for(var shape:entity.getWorld().getBlockCollisions(entity,body.stretch(forward).stretch(0,height+.01,0)))obstacles.addAll(shape.getBoundingBoxes());
     double rise=StepUp.rise(body,forward,height,obstacles);
     if(rise>0)entity.move(type,new Vec3d(0,rise,0));
    }
    Vec3d before=entity.getPos();
    entity.move(type,part);
    if(entity.horizontalCollision){
     brushedWall=true;
     part=StepUp.slide(part,entity.getPos().subtract(before));
     if(part.horizontalLengthSquared()<1e-10){
      // Preserve the rest of this tick's gravity even when forward travel is completely blocked.
      entity.move(type,new Vec3d(0,part.y*(count-i-1),0));
      break;
     }
    }
   }
   if(brushedWall)entity.horizontalCollision=true;
  }finally{craftfist$moving=false;}
 }
}
