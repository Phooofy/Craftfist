package dev.craftfist;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
public record AbilityPacket(int action) implements CustomPayload {
 public static final Id<AbilityPacket> ID = new Id<>(Identifier.of("craftfist", "ability"));
 public static final PacketCodec<RegistryByteBuf, AbilityPacket> CODEC = PacketCodec.of((p,b)->b.writeVarInt(p.action), b->new AbilityPacket(b.readVarInt()));
 public Id<? extends CustomPayload> getId(){return ID;}
}
