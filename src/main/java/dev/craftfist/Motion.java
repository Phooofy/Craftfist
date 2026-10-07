package dev.craftfist;
import net.minecraft.util.math.Vec3d;

/** Minecraft-independent movement math, measured in blocks per tick. */
public final class Motion {
 public static final int SLAM_RANGE=7,SLAM_HALF_ANGLE=45;
 public static final double SLAM_HORIZONTAL=1.95,CHARGE_INPUT_SCALE=.35;
 public static final double METEOR_DESCENT_SPEED=6;
 public static final int METEOR_AIR_TICKS=100,METEOR_ASCENT_TICKS=12;
 private Motion() {}
 public static Vec3d meteorHorizontal(float yaw,int forward,int sideways){
  double f=Math.clamp(forward,-1,1),s=Math.clamp(sideways,-1,1),length=Math.hypot(f,s);
  if(length==0)return Vec3d.ZERO;
  double a=Math.toRadians(yaw),speed=1.2/length;
  return new Vec3d((s*Math.cos(a)-f*Math.sin(a))*speed,0,(f*Math.cos(a)+s*Math.sin(a))*speed);
 }
 public static boolean needsFullVelocity(double x,double y,double z){return Math.abs(x)>3.9||Math.abs(y)>3.9||Math.abs(z)>3.9;}
 /** Clipping across a wall matters only if it opposes the knockback direction. */
 public static boolean opposingWall(double requestedX,double requestedZ,double clearX,double clearZ){
  double lengthSquared=requestedX*requestedX+requestedZ*requestedZ;
  return lengthSquared>1e-8 && requestedX*clearX+requestedZ*clearZ<lengthSquared-1e-6;
 }
 public static int cooldownTick(int remaining){return Math.max(0,remaining-1);}
 public static boolean slamLanded(int age,boolean grounded){return age>=5&&grounded;}
 public static boolean hasIncomingMomentum(double x,double y,double z){return Math.hypot(x,z)>.04||Math.abs(y)>.12;}
 public static double punchVertical(double incoming,double aimed){return Math.max(incoming,aimed);}
 public static boolean inSlamCone(double forwardX,double forwardZ,double offsetX,double offsetZ){
  double distance=Math.hypot(offsetX,offsetZ),length=Math.hypot(forwardX,forwardZ);
  if(distance>SLAM_RANGE+1e-9)return false;
  if(distance<1e-6)return true;
  if(length<1e-6)return false;
  return (forwardX*offsetX+forwardZ*offsetZ)/(length*distance)>=Math.cos(Math.toRadians(SLAM_HALF_ANGLE))-1e-9;
 }
 /** Thirty-degree total horizontal cone, centered on the punch launch direction. */
 public static boolean inPunchCone(double forwardX,double forwardZ,double offsetX,double offsetZ){
  double forwardLength=Math.hypot(forwardX,forwardZ),offsetLength=Math.hypot(offsetX,offsetZ);
  if(offsetLength<1e-6)return true;
  if(forwardLength<1e-6)return false;
  return (forwardX*offsetX+forwardZ*offsetZ)/(forwardLength*offsetLength)>=Math.cos(Math.toRadians(15))-1e-9;
 }
 public static double punchSpeed(int charge, boolean empowered) {
  return (.65 + Math.clamp(charge, 0, 30) / 30.0 * 1.75) * (empowered ? 1.35 : 1);
 }
 public static double slamHorizontal(double incoming, double direction) {
  return incoming * .95 + direction * SLAM_HORIZONTAL;
 }
 public static double slamVertical(double incoming, double lookY) {
  return Math.max(incoming, .9) + Math.max(0, lookY) * .5;
 }
}
