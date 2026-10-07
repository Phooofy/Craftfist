package dev.craftfist;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record MeteorInputPacket(int forward,int sideways) implements CustomPayload {
 public static final Id<MeteorInputPacket> ID=new Id<>(Identifier.of("craftfist","meteor_input"));
 public static final PacketCodec<PacketByteBuf,MeteorInputPacket> CODEC=PacketCodec.of(
  (p,b)->{b.writeVarInt(p.forward);b.writeVarInt(p.sideways);},b->new MeteorInputPacket(b.readVarInt(),b.readVarInt()));
 public Id<? extends CustomPayload> getId(){return ID;}
}
