package dev.craftfist;
import java.util.List;
import net.minecraft.util.math.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class StepUpTest {
 @Test void brushingAWallRetainsTheUnblockedAxis(){
  Vec3d slide=StepUp.slide(new Vec3d(.14,-.01,.14),new Vec3d(.03,-.01,.14));
  assertEquals(0,slide.x);assertEquals(.14,slide.z);assertEquals(-.01,slide.y);
 }
 @Test void fullWallStopsBothHorizontalAxes(){
  assertEquals(0,StepUp.slide(new Vec3d(.14,0,.14),Vec3d.ZERO).horizontalLengthSquared());
 }
 @Test void collisionRoundoffDoesNotFreezeAnAxis(){
  assertEquals(.2,StepUp.slide(new Vec3d(.2,0,0),new Vec3d(.199999,0,0)).x);
 }
 @Test void fullSpeedPunchIsSplitIntoWalkingSizedMoves(){
  Vec3d movement=new Vec3d(0,-.08,2.4);
  int count=StepUp.substeps(movement);
  assertTrue(count>=12);assertTrue(movement.multiply(1.0/count).horizontalLength()<=.2+1e-9);
 }
 @Test void consecutiveSlabHeightsCanBeClimbedOneSubstepAtATime(){
  Box initial=new Box(-.3,0,-.3,.3,1.8,.3);
  double first=StepUp.rise(initial,new Vec3d(0,0,.2),.6,List.of(slab));
  assertEquals(.501,first,1e-9);
  Box elevated=initial.offset(0,first,.9);
  assertTrue(StepUp.rise(elevated,new Vec3d(0,0,.2),.6,List.of(new Box(-1,0,1.21,1,1,2.21)))>0);
 }
 private final Box player=new Box(-.3,0,-.3,.3,1.8,.3);
 private final Vec3d forward=new Vec3d(0,0,.35);
 private final Box slab=new Box(-1,0,.31,1,.5,1.31);
 @Test void bottomSlabCanBeSteppedOnto(){assertEquals(.501,StepUp.rise(player,forward,.6,List.of(slab)),1e-9);}
 @Test void fullBlockStopsTheDash(){assertEquals(0,StepUp.rise(player,forward,.6,List.of(new Box(-1,0,.31,1,1,1.31))));}
 @Test void lowCeilingPreventsStepping(){assertEquals(0,StepUp.rise(player,forward,.6,List.of(slab,new Box(-1,2,-1,1,3,2))));}
 @Test void flatGroundDoesNotRaiseThePlayer(){assertEquals(0,StepUp.rise(player,forward,.6,List.of(new Box(-1,-1,-1,1,0,2))));}
}
