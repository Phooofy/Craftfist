package dev.craftfist;

import net.minecraft.util.math.Vec3d;

/** A short-lived knockback trace; only terrain opposing the punch counts as an impact. */
final class WallImpact {
 final Vec3d direction;
 final long launchedAt;
 int remaining=16;
 WallImpact(Vec3d knockback,long launchedAt){
  direction=new Vec3d(knockback.x,0,knockback.z).normalize();
  this.launchedAt=launchedAt;
 }
}
