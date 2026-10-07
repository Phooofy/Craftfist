package dev.craftfist;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VelocitySyncTest {
 @Test void empoweredComboSurvivesPacketRoundTripWithoutClipping(){
  double speed=Motion.slamHorizontal(Motion.punchSpeed(30,true),1);
  assertEquals(5.028,speed,1e-9);
  assertTrue(Motion.needsFullVelocity(speed,0,0));
  var packet=new FullVelocityPacket(speed,1.25,-speed);
  var buffer=new PacketByteBuf(Unpooled.buffer());
  try {
   FullVelocityPacket.CODEC.encode(buffer,packet);
   assertEquals(packet,FullVelocityPacket.CODEC.decode(buffer));
   assertEquals(0,buffer.readableBytes());
  }finally{buffer.release();}
 }
 @Test void vanillaIsOnlyBypassedWhenAComponentWouldBeClipped(){
  assertFalse(Motion.needsFullVelocity(3.9,-3.9,3.9));
  assertTrue(Motion.needsFullVelocity(3.90001,0,0));
  assertTrue(Motion.needsFullVelocity(0,-4,0));
  assertTrue(Motion.needsFullVelocity(0,0,4));
  assertFalse(Motion.needsFullVelocity(3,3,3));
 }
}
