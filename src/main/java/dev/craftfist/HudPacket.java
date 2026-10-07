package dev.craftfist;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
public record HudPacket(int ammo,int reloadDelay,int reloadProgress,int punch,int slam,int uppercut,int block,int ultimate,int charge,boolean empowered) implements CustomPayload {
 public static final Id<HudPacket> ID=new Id<>(Identifier.of("craftfist","hud_v2"));
 public static final PacketCodec<PacketByteBuf,HudPacket> CODEC=PacketCodec.of((p,b)->{
  b.writeVarInt(p.ammo);b.writeVarInt(p.reloadDelay);b.writeVarInt(p.reloadProgress);
  b.writeVarInt(p.punch);b.writeVarInt(p.slam);b.writeVarInt(p.uppercut);b.writeVarInt(p.block);
  b.writeVarInt(p.ultimate);b.writeVarInt(p.charge);b.writeBoolean(p.empowered);
 },b->new HudPacket(b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readBoolean()));
 public Id<? extends CustomPayload> getId(){return ID;}
}
