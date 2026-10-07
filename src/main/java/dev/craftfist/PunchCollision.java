package dev.craftfist;
import net.minecraft.util.math.*;
public final class PunchCollision {
 private PunchCollision(){}
 /** Contact correction never feeds falling velocity into the player's feet position. */
 public static Vec3d stopOffset(Vec3d movement,double contactDistance){
  if(movement.lengthSquared()<1e-12)return Vec3d.ZERO;
  Vec3d offset=movement.normalize().multiply(Math.max(0,contactDistance-.1));
  return new Vec3d(offset.x,0,offset.z);
 }
 public static double contactDistance(Box target,Box player,Vec3d origin,Vec3d movement){
  if(target.intersects(player))return 0;
  Box generous=target.expand(player.getLengthX()/2+.5,.7,player.getLengthZ()/2+.5);
  if(generous.contains(origin))return 0;
  return generous.raycast(origin,origin.add(movement)).map(origin::distanceTo).orElse(Double.POSITIVE_INFINITY);
 }
}
