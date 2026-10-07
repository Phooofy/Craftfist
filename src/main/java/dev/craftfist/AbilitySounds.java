package dev.craftfist;
import java.util.*;
import net.minecraft.registry.*;
import net.minecraft.sound.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
/** Named resource-pack-overridable events. Bundled aliases use Minecraft sound effects. */
public final class AbilitySounds {
 private static final Map<String,SoundEvent> EVENTS=new HashMap<>();
 public static void register(){for(String name:List.of("punch_charge","punch_release","punch_hit","wall_hit","slam_launch","slam_impact","uppercut","block_start","block_hit","empowered","meteor_start","meteor_impact","hand_cannon")){
  Identifier id=Identifier.of("craftfist",name);EVENTS.put(name,Registry.register(Registries.SOUND_EVENT,id,SoundEvent.of(id)));
 }}
 public static void play(ServerPlayerEntity player,String name){player.getServerWorld().playSound(null,player.getBlockPos(),EVENTS.get(name),SoundCategory.PLAYERS,.8f,1);}
}
