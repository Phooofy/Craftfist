package dev.craftfist;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HandCannonTest {
 private void ticks(Craftfist.State s,int count){for(int i=0;i<count;i++){s.tickCooldowns();s.tickAmmo();}}
 @Test void rapidBurstCannotRegenerateExtraShotsBetweenItsFourRounds(){
  var s=new Craftfist.State();
  for(int i=0;i<4;i++){
   assertTrue(s.consumeShot());assertEquals(3-i,s.ammo);
   ticks(s,4);
  }
  assertEquals(0,s.ammo);assertFalse(s.consumeShot());
  ticks(s,15);assertEquals(0,s.ammo);
  ticks(s,1);assertEquals(1,s.ammo);
 }
 @Test void anotherAcceptedShotRestartsTheFullIdleDelay(){
  var s=new Craftfist.State();assertTrue(s.consumeShot());ticks(s,19);
  assertEquals(3,s.ammo);assertTrue(s.consumeShot());
  ticks(s,19);assertEquals(2,s.ammo);
  ticks(s,1);assertEquals(3,s.ammo);
 }
 @Test void firingCooldownRejectsDuplicateShotRequestsWithoutSpendingAmmo(){
  var s=new Craftfist.State();assertTrue(s.consumeShot());
  for(int i=0;i<20;i++)assertFalse(s.consumeShot());
  assertEquals(3,s.ammo);assertEquals(0,s.shotIdle);
  ticks(s,4);assertTrue(s.consumeShot());assertEquals(2,s.ammo);
 }
 @Test void quietReloadRestoresFirstRoundAtOneSecondAndThenEveryThirteenTicks(){
  var s=new Craftfist.State();
  for(int i=0;i<4;i++){s.consumeShot();if(i<3)ticks(s,4);}
  ticks(s,19);assertEquals(0,s.ammo);
  ticks(s,1);assertEquals(1,s.ammo);
  ticks(s,12);assertEquals(1,s.ammo);
  ticks(s,1);assertEquals(2,s.ammo);
  ticks(s,100);assertEquals(4,s.ammo);
 }
 @Test void hudPacketRetainsAmmoCooldownChargeAndEmpowermentValues(){
  var packet=new HudPacket(1,18,0,75,100,40,120,87,23,true);
  var buffer=new PacketByteBuf(Unpooled.buffer());
  try {HudPacket.CODEC.encode(buffer,packet);assertEquals(packet,HudPacket.CODEC.decode(buffer));assertEquals(0,buffer.readableBytes());}
  finally{buffer.release();}
 }
}
