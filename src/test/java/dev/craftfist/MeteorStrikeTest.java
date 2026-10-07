package dev.craftfist;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MeteorStrikeTest {
 @Test void aimingLastsFiveSecondsButTheAscentStillEndsAfterTwelveTicks(){
  var s=new Craftfist.State();s.beginMeteor();
  for(int tick=1;tick<=99;tick++){
   s.tickMeteorAir();assertFalse(s.meteorDescending);
   assertEquals(tick<12,s.meteorRising());
   assertEquals(tick>=10,s.canDiveEarly());
  }
  s.tickMeteorAir();assertTrue(s.meteorDescending);assertFalse(s.canDiveEarly());
 }
 @Test void descentStaysActiveWhileAirborneAndImpactsOnlyOnceOnLanding(){
  var s=new Craftfist.State();s.beginMeteor();s.beginMeteorDescent();
  for(int tick=0;tick<40;tick++){
   assertFalse(s.tickMeteorDescent(false,false));
   assertTrue(s.meteorDescending);assertEquals(1,s.meteor);
  }
  assertTrue(s.tickMeteorDescent(true,false));assertEquals(0,s.meteor);
  assertFalse(s.meteorDescending);assertFalse(s.tickMeteorDescent(true,false));
 }
 @Test void voidDescentCancelsWithoutProducingAnAirborneImpact(){
  var s=new Craftfist.State();s.beginMeteor();s.beginMeteorDescent();
  assertFalse(s.tickMeteorDescent(false,true));assertEquals(0,s.meteor);
  assertFalse(s.meteorDescending);assertFalse(s.tickMeteorDescent(true,false));
 }
 @Test void missingTerrainCannotKeepMeteorInvulnerabilityForever(){
  var s=new Craftfist.State();s.beginMeteor();s.beginMeteorDescent();
  for(int tick=0;tick<200;tick++)assertFalse(s.tickMeteorDescent(false,false));
  assertEquals(1,s.meteor);
  assertFalse(s.tickMeteorDescent(false,false));assertEquals(0,s.meteor);
  assertFalse(s.tickMeteorDescent(true,false));
 }
 @Test void aNewCastResetsThePreviousDescentPhase(){
  var s=new Craftfist.State();s.beginMeteor();s.beginMeteorDescent();
  s.tickMeteorDescent(false,false);s.finishMeteor();s.beginMeteor();
  assertEquals(100,s.meteor);assertEquals(0,s.meteorFallTicks);assertFalse(s.meteorDescending);
  assertFalse(s.tickMeteorDescent(true,false));
 }
 @Test void theFastDownwardVelocitySurvivesNetworkSerialization(){
  var packet=new FullVelocityPacket(0,-Motion.METEOR_DESCENT_SPEED,0);
  assertTrue(Motion.needsFullVelocity(packet.x(),packet.y(),packet.z()));
  var buffer=new PacketByteBuf(Unpooled.buffer());
  try {
   FullVelocityPacket.CODEC.encode(buffer,packet);
   assertEquals(packet,FullVelocityPacket.CODEC.decode(buffer));
  }finally{buffer.release();}
 }
}
