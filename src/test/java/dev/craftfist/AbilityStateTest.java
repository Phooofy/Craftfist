package dev.craftfist;

import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AbilityStateTest {
 @Test void launchConsumesEmpowermentButKeepsItsDamageSpeedAndIncomingAscent(){
  var s=new Craftfist.State();s.charge=30;s.empowered=true;s.empoweredTicks=200;
  s.launchPunch(new Vec3d(0,0,1),new Vec3d(0,1.25,0),new Vec3d(3,6,9));
  assertFalse(s.empowered);assertEquals(0,s.empoweredTicks);
  assertEquals(15,s.punchDamage);assertEquals(3.24,s.dashVelocity.z,1e-9);
  assertEquals(1.25,s.dashVelocity.y,1e-9);assertEquals(14,s.dash);
  // Jump/ability cancellation cannot return the empowerment already spent at launch.
  s.dash=0;s.charge=30;s.launchPunch(new Vec3d(0,0,1),Vec3d.ZERO,Vec3d.ZERO);
  assertEquals(10,s.punchDamage);assertEquals(2.4,s.dashVelocity.z,1e-9);
 }
 @Test void menuCancellationDoesNotLaunchOrSpendACharge(){
  var s=new Craftfist.State();s.charge=30;s.empowered=true;s.empoweredTicks=100;
  s.cancelCharge();
  assertEquals(-1,s.charge);assertEquals(0,s.dash);assertEquals(0,s.cooldown[0]);
  assertTrue(s.empowered);assertEquals(Vec3d.ZERO,s.dashVelocity);
 }
 @Test void cooldownsExpireWithoutAnyEquippedAbilityActivity(){
  var s=new Craftfist.State();s.cooldown[0]=80;s.cooldown[1]=120;s.cooldown[3]=140;
  for(int i=0;i<120;i++)s.tickCooldowns();
  assertEquals(0,s.cooldown[0]);assertEquals(0,s.cooldown[1]);assertEquals(20,s.cooldown[3]);
  for(int i=0;i<100;i++)s.tickCooldowns();
  assertArrayEquals(new int[5],s.cooldown);
 }
 @Test void longAirborneSlamSurvivesUntilOneLandingImpact(){
  var s=new Craftfist.State();s.slam=1;
  for(int i=0;i<300;i++)assertFalse(s.tickSlam(false,false));
  assertTrue(s.tickSlam(true,false));assertEquals(0,s.slam);
  assertFalse(s.tickSlam(true,false));
 }
 @Test void slamHasALaunchGracePeriodAndExplicitCancellation(){
  var s=new Craftfist.State();s.slam=1;
  for(int i=0;i<3;i++)assertFalse(s.tickSlam(true,false));
  assertTrue(s.tickSlam(true,false));
  s.slam=1;assertFalse(s.tickSlam(false,true));
  for(int i=0;i<300;i++)assertFalse(s.tickSlam(false,false));
  assertFalse(s.tickSlam(true,false));
 }
}
