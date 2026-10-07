package dev.craftfist.mixin;

import dev.craftfist.FullVelocityPacket;
import dev.craftfist.Motion;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonNetworkHandler.class)
public class FullVelocityMixin {
 @Inject(method="sendPacket",at=@At("HEAD"),cancellable=true)
 private void craftfist$fullVelocity(Packet<?> packet,CallbackInfo ci){
  if(!((Object)this instanceof ServerPlayNetworkHandler handler)
    || !(packet instanceof EntityVelocityUpdateS2CPacket update)
    || update.getEntityId()!=handler.player.getId())return;
  var v=handler.player.getVelocity();
  if(!Motion.needsFullVelocity(v.x,v.y,v.z) || !ServerPlayNetworking.canSend(handler.player,FullVelocityPacket.ID))return;
  // Match the snapshot before substituting, so deliberately different vanilla updates remain intact.
  var snapshot=new EntityVelocityUpdateS2CPacket(handler.player);
  if(update.getVelocityX()!=snapshot.getVelocityX() || update.getVelocityY()!=snapshot.getVelocityY() || update.getVelocityZ()!=snapshot.getVelocityZ())return;
  ServerPlayNetworking.send(handler.player,new FullVelocityPacket(v.x,v.y,v.z));
  ci.cancel();
 }
}
