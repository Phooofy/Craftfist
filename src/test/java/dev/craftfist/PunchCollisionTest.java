package dev.craftfist;
import net.minecraft.util.math.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PunchCollisionTest {
 @Test void impactIncludesSideBySideAndClusteredEnemies(){
  Vec3d impact=new Vec3d(0,.9,2),player=new Vec3d(0,.9,1),forward=new Vec3d(0,0,2.4);
  assertTrue(PunchCollision.inImpactGroup(new Box(-.3,0,1.7,.3,1.8,2.3),impact,player,forward));
  assertTrue(PunchCollision.inImpactGroup(new Box(.8,0,1.7,1.4,1.8,2.3),impact,player,forward));
  assertTrue(PunchCollision.inImpactGroup(new Box(-.3,0,2.7,.3,1.8,3.3),impact,player,forward));
 }
 @Test void impactRejectsDistantHighAndBehindThePlayerTargets(){
  Vec3d impact=new Vec3d(0,.9,2),player=new Vec3d(0,.9,1),forward=new Vec3d(0,0,2.4);
  assertFalse(PunchCollision.inImpactGroup(new Box(2,0,1.7,2.6,1.8,2.3),impact,player,forward));
  assertFalse(PunchCollision.inImpactGroup(new Box(-.3,3,1.7,.3,4.8,2.3),impact,player,forward));
  assertFalse(PunchCollision.inImpactGroup(new Box(-.3,0,.2,.3,1.8,.8),impact,player,forward));
 }
 @Test void fallingPunchCannotCorrectThePlayersFeetIntoTheGround(){
  Vec3d offset=PunchCollision.stopOffset(new Vec3d(0,-1.5,2),2);
  assertEquals(0,offset.y);assertTrue(offset.z>0);
 }
 private final Box player=new Box(-.3,0,-.3,.3,1.8,.3);
 private final Vec3d origin=player.getCenter(),forward=new Vec3d(0,0,2.4);
 @Test void collisionOccursBeforeThePlayerCanPassThroughATarget(){
  double distance=PunchCollision.contactDistance(new Box(-.3,0,1,.3,1.8,1.6),player,origin,forward);
  assertEquals(.2,distance,1e-9);
 }
 @Test void nearbyButOffPathEntitiesDoNotStopThePunch(){
  assertEquals(Double.POSITIVE_INFINITY,PunchCollision.contactDistance(new Box(1.5,0,1,2.1,1.8,1.6),player,origin,forward));
 }
 @Test void crosshairHalfABlockOutsideTheMobStillHits(){
  assertTrue(Double.isFinite(PunchCollision.contactDistance(new Box(.5,0,1,1.1,1.8,1.6),player,origin,forward)));
 }
 @Test void longMovementSweepCatchesAMobPassedBetweenTicks(){
  assertTrue(Double.isFinite(PunchCollision.contactDistance(new Box(-.3,0,2,.3,1.8,2.6),player,origin,new Vec3d(0,0,6))));
 }
 @Test void overlappingTargetStopsTravelImmediately(){assertEquals(0,PunchCollision.contactDistance(player,player,origin,forward));}
}
