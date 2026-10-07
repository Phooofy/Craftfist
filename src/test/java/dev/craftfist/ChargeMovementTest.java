package dev.craftfist;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;

class ChargeMovementTest {
 @Test void ordinaryMotionIsBoundedEvenIfItArrivesAfterChargeStarted(){
  var movement=new ChargeMovement();
  assertEquals(.07,.2*movement.horizontalScale(true,false,.2),1e-9);
  // A later sprint-jump/knockback impulse is slowed despite the charge already being active.
  assertEquals(.1,1.2*movement.horizontalScale(true,false,1.2),1e-9);
  for(int tick=0;tick<20;tick++)assertEquals(1,movement.horizontalScale(true,false,.07));
  assertEquals(1,movement.horizontalScale(true,true,5));
 }
 @Test void momentumProvenanceTravelsWithTheAcceptedChargeState(){
  var buffer=new PacketByteBuf(Unpooled.buffer());
  try {
   var chained=new PunchStatePacket(12,0,0,3,true);
   var ordinary=new PunchStatePacket(13,0,0,0,false);
   PunchStatePacket.CODEC.encode(buffer,chained);PunchStatePacket.CODEC.encode(buffer,ordinary);
   assertEquals(chained,PunchStatePacket.CODEC.decode(buffer));
   assertEquals(ordinary,PunchStatePacket.CODEC.decode(buffer));assertEquals(0,buffer.readableBytes());
  }finally{buffer.release();}
 }
 @Test void ordinaryWalkingOrSprintJumpingIsBrakedOnChargeWithoutRepeatedFreezing(){
  var movement=new ChargeMovement();
  assertEquals(.35,movement.horizontalScale(true,false));
  for(int tick=0;tick<30;tick++)assertEquals(1,movement.horizontalScale(true,false));
  movement.horizontalScale(false,false);
  assertEquals(.35,movement.horizontalScale(true,false));
 }
 @Test void anAbilityChainKeepsVelocityButLosesItsExemptionAfterLanding(){
  var movement=new ChargeMovement();
  for(int tick=0;tick<30;tick++)assertEquals(1,movement.horizontalScale(true,true));
  assertEquals(.35,movement.horizontalScale(true,false));
  assertEquals(1,movement.horizontalScale(true,false));
 }
 @Test void cancellingAnOrdinaryChargeAndStartingAnAbilityChainDoesNotBrakeTheAbility(){
  var movement=new ChargeMovement();
  assertEquals(.35,movement.horizontalScale(true,false));
  movement.horizontalScale(false,false);
  assertEquals(1,movement.horizontalScale(true,true));
 }
 @Test void onlyAnExplicitAbilityLaunchGrantsMomentumAndLandingOrCancellationClearsIt(){
  var state=new Craftfist.State();
  assertFalse(state.abilityMomentum);
  for(int tick=0;tick<30;tick++)state.tickAbilityMomentum(false,false);
  assertFalse(state.abilityMomentum); // Ordinary airtime/jumping does not grant it.
  state.grantAbilityMomentum();
  for(int tick=0;tick<4;tick++){state.tickAbilityMomentum(true,false);assertTrue(state.abilityMomentum);}
  for(int tick=0;tick<60;tick++){state.tickAbilityMomentum(false,false);assertTrue(state.abilityMomentum);}
  state.tickAbilityMomentum(true,false);assertFalse(state.abilityMomentum);
  state.grantAbilityMomentum();state.tickAbilityMomentum(false,true);assertFalse(state.abilityMomentum);
 }
 @Test void chargeStillSlowsSteeringWhileAbilityMomentumIsProtected(){
  PunchAnimation.tick(false);
  PunchAnimation.receive(10,0,0,0,true);
  assertTrue(PunchAnimation.shouldSlowCharge());assertTrue(PunchAnimation.carriesAbilityMomentum());
  PunchAnimation.receive(11,0,0,0,false);
  assertTrue(PunchAnimation.shouldSlowCharge());assertFalse(PunchAnimation.carriesAbilityMomentum());
  PunchAnimation.tick(false);assertFalse(PunchAnimation.carriesAbilityMomentum());
 }
}
