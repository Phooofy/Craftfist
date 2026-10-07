package dev.craftfist;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class MotionTest {
 @Test void walkingAndUpwardMomentumAreDistinctFromStationaryGravity(){
  assertFalse(Motion.hasIncomingMomentum(0,-.078,0));
  assertTrue(Motion.hasIncomingMomentum(.2,0,0));
  assertTrue(Motion.hasIncomingMomentum(0,1.25,0));
 }
 @Test void slamFootprintIsSevenBlocksAndNinetyDegrees(){
  assertTrue(Motion.inSlamCone(0,1,0,7));
  assertFalse(Motion.inSlamCone(0,1,0,7.01));
  assertTrue(Motion.inSlamCone(0,1,7*Math.sin(Math.toRadians(45)),7*Math.cos(Math.toRadians(45))));
  assertTrue(Motion.inSlamCone(0,1,Math.sin(Math.toRadians(40)),Math.cos(Math.toRadians(40))));
  assertFalse(Motion.inSlamCone(0,1,Math.sin(Math.toRadians(46)),Math.cos(Math.toRadians(46))));
  assertFalse(Motion.inSlamCone(0,1,0,-1));
 }
 @Test void punchPreservesAnUppercutOrSlamAscent(){
  assertEquals(1.25,Motion.punchVertical(1.25,0),1e-9);
  assertEquals(.9,Motion.punchVertical(.9,-1),1e-9);
  assertEquals(2,Motion.punchVertical(.9,2),1e-9);
 }
 @Test void punchConeIncludesFrontAndFifteenDegreesButRejectsSidesAndBehind(){
  assertTrue(Motion.inPunchCone(0,2.4,0,2));
  assertTrue(Motion.inPunchCone(0,2.4,Math.sin(Math.toRadians(15)),Math.cos(Math.toRadians(15))));
  assertFalse(Motion.inPunchCone(0,2.4,Math.sin(Math.toRadians(16)),Math.cos(Math.toRadians(16))));
  assertFalse(Motion.inPunchCone(0,2.4,2,0));
  assertFalse(Motion.inPunchCone(0,2.4,0,-2));
 }
 @Test void chargeIsBoundedAndEmpowermentIncreasesTravel() {
  assertEquals(.65, Motion.punchSpeed(-20,false),1e-9);
  assertEquals(2.4, Motion.punchSpeed(30,false),1e-9);
  assertEquals(Motion.punchSpeed(30,false),Motion.punchSpeed(100,false),1e-9);
  assertTrue(Motion.punchSpeed(30,true)>Motion.punchSpeed(30,false));
 }
 @Test void chargedPunchIntoSlamCarriesMoreMomentumThanOrdinarySlam() {
  double ordinary=Motion.slamHorizontal(0,1);
  double chained=Motion.slamHorizontal(Motion.punchSpeed(30,false),1);
  assertTrue(chained>ordinary*2.1);
  assertEquals(4.23,chained,1e-9);
 }
 @Test void flatGroundSlamTravelsAboutThreeBlocksFartherUnderNormalAirPhysics(){
  double oldSpeed=1.65,newSpeed=Motion.slamHorizontal(0,1),oldDistance=0,newDistance=0;
  double height=0,vertical=Motion.slamVertical(0,0);
  for(int tick=0;tick<100;tick++){
   oldDistance+=oldSpeed;newDistance+=newSpeed;
   height+=vertical;vertical=(vertical-.08)*.98;
   oldSpeed*=.91;newSpeed*=.91;
   if(height<=0)break;
  }
  assertEquals(3,newDistance-oldDistance,.2);
 }
 @Test void SlamPreservesSidewaysMotionAndExistingUpwardSpeed() {
  assertEquals(1.9,Motion.slamHorizontal(2,0),1e-9);
  assertEquals(1.25,Motion.slamVertical(1.25,0),1e-9);
  assertEquals(.9,Motion.slamVertical(-2,-1),1e-9);
 }
}
