package dev.craftfist;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Vanilla velocity packets clamp components to 3.9 blocks/tick. */
public record FullVelocityPacket(double x,double y,double z) implements CustomPayload {
 public static final Id<FullVelocityPacket> ID=new Id<>(Identifier.of("craftfist","full_velocity"));
 public static final PacketCodec<PacketByteBuf,FullVelocityPacket> CODEC=PacketCodec.of(
  (p,b)->{b.writeDouble(p.x);b.writeDouble(p.y);b.writeDouble(p.z);},
  b->new FullVelocityPacket(b.readDouble(),b.readDouble(),b.readDouble()));
 public Id<? extends CustomPayload> getId(){return ID;}
}
