package dev.craftfist;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PunchAnimationTest {
 @Test void everyAcceptedChargeSlowsWalkingInputUntilRelease(){
  PunchAnimation.tick(false);
  PunchAnimation.receive(0,0,0,0);
  assertTrue(PunchAnimation.shouldSlowCharge());
  PunchAnimation.receive(30,0,0,0);
  assertTrue(PunchAnimation.shouldSlowCharge());
  PunchAnimation.receive(-1,0,0,0);
  assertFalse(PunchAnimation.shouldSlowCharge());
  PunchAnimation.tick(false);
 }
 @Test void uppercutWindsDownDrivesUpAndRecovers(){
  PunchAnimation.tick(false);
  PunchAnimation.receive(-1,0,0,10);
  assertTrue(PunchAnimation.uppercutLift(0)<0);
  PunchAnimation.receive(-1,0,0,6);
  assertEquals(.55f,PunchAnimation.uppercutLift(0),1e-6);
  PunchAnimation.receive(-1,0,0,1);
  assertTrue(PunchAnimation.uppercutLift(0)<.1f);
  PunchAnimation.receive(-1,0,0,0);
  assertEquals(0,PunchAnimation.uppercutLift(1));
 }
 @Test void blockPoseRaisesAndRecoversOnServerCancellation(){
  PunchAnimation.tick(false);
  PunchAnimation.receive(-1,0,60);
  for(int i=0;i<5;i++)PunchAnimation.tick(true);
  assertTrue(PunchAnimation.block(1)>.95f);
  PunchAnimation.receive(-1,0,0);
  for(int i=0;i<15;i++)PunchAnimation.tick(true);
  assertTrue(PunchAnimation.block(1)<.001f);
  PunchAnimation.tick(false);
 }
 @Test void poseChargesThrustsAndResetsOnUnequip(){
  PunchAnimation.tick(false);
  PunchAnimation.receive(30,0);PunchAnimation.tick(true);
  assertTrue(PunchAnimation.pull(1)>0);
  assertEquals(0,PunchAnimation.thrust(1));
  PunchAnimation.receive(-1,12);PunchAnimation.tick(true);
  assertTrue(PunchAnimation.thrust(1)>.7f);
  PunchAnimation.receive(-1,0);
  for(int i=0;i<30;i++)PunchAnimation.tick(true);
  assertTrue(PunchAnimation.thrust(1)<.001f);
  PunchAnimation.tick(false);
  assertEquals(0,PunchAnimation.pull(1));assertEquals(0,PunchAnimation.thrust(1));
 }
}
