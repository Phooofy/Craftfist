package dev.craftfist;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
public record PunchStatePacket(int charge,int dash,int block,int uppercut,boolean abilityMomentum) implements CustomPayload {
 public static final Id<PunchStatePacket> ID=new Id<>(Identifier.of("craftfist","punch_state"));
 public static final PacketCodec<PacketByteBuf,PunchStatePacket> CODEC=PacketCodec.of((p,b)->{b.writeVarInt(p.charge);b.writeVarInt(p.dash);b.writeVarInt(p.block);b.writeVarInt(p.uppercut);b.writeBoolean(p.abilityMomentum);},b->new PunchStatePacket(b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readBoolean()));
 public Id<? extends CustomPayload> getId(){return ID;}
}
