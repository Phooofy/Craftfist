package dev.craftfist;
import java.util.List;
import net.minecraft.util.math.*;

/** Finds a walking-height step with both forward and overhead clearance. */
public final class StepUp {
 private StepUp(){}
 /** Small movement increments allow each successive half-step to use ordinary walking clearance. */
 public static int substeps(Vec3d movement){return Math.max(1,(int)Math.ceil(Math.hypot(movement.x,movement.z)/.2));}
 /** Retain motion along a wall while discarding only the axis actually clipped by collision. */
 public static Vec3d slide(Vec3d requested,Vec3d actual){
  return new Vec3d(Math.abs(requested.x-actual.x)>1e-5?0:requested.x,requested.y,Math.abs(requested.z-actual.z)>1e-5?0:requested.z);
 }
 public static double rise(Box body,Vec3d forward,double maxStep,List<Box> obstacles){
  Box ahead=body.offset(forward);
  double rise=0;
  for(Box obstacle:obstacles)if(obstacle.intersects(ahead)){
   double height=obstacle.maxY-body.minY;
   if(height<=0||height>maxStep+1e-6)return 0;
   rise=Math.max(rise,height);
  }
  if(rise==0)return 0;
  double lift=rise+.001;
  Box ascent=body.stretch(0,lift,0),landing=ahead.offset(0,lift,0);
  for(Box obstacle:obstacles)if(obstacle.intersects(ascent)||obstacle.intersects(landing))return 0;
  return lift;
 }
}
