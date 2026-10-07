package dev.craftfist;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
public class CraftfistClient implements ClientModInitializer {
 private final KeyBinding punch=key("punch",GLFW.GLFW_KEY_R),slam=key("slam",GLFW.GLFW_KEY_LEFT_SHIFT),upper=key("uppercut",GLFW.GLFW_KEY_X),block=key("block",GLFW.GLFW_KEY_V),meteor=key("meteor",GLFW.GLFW_KEY_Z);
 private static HudPacket hud;
 private boolean punching,jumping,slamming;
 private static KeyBinding key(String name,int code){return KeyBindingHelper.registerKeyBinding(new KeyBinding("key.craftfist."+name,InputUtil.Type.KEYSYM,code,"category.craftfist"));}
 private static void send(int a){if(ClientPlayNetworking.canSend(AbilityPacket.ID))ClientPlayNetworking.send(new AbilityPacket(a));}
 public void onInitializeClient(){
  ClientPlayNetworking.registerGlobalReceiver(HudPacket.ID,(packet,context)->context.client().execute(()->hud=packet));
  HudRenderCallback.EVENT.register((draw,ticks)->AbilityHud.render(draw,MinecraftClient.getInstance(),hud,new KeyBinding[]{MinecraftClient.getInstance().options.attackKey,punch,slam,upper,block,meteor}));
  ClientPlayNetworking.registerGlobalReceiver(PunchStatePacket.ID,(packet,context)->context.client().execute(()->PunchAnimation.receive(packet.charge(),packet.dash(),packet.block(),packet.uppercut(),packet.abilityMomentum())));
  ClientPlayNetworking.registerGlobalReceiver(FullVelocityPacket.ID,(packet,context)->context.client().execute(()->{
   if(context.client().player!=null)context.client().player.setVelocityClient(packet.x(),packet.y(),packet.z());
  }));
  ClientTickEvents.END_CLIENT_TICK.register(c->{
  PunchAnimation.tick(c.player!=null && Craftfist.equipped(c.player));
  if(c.player==null)hud=null;
  if(c.player==null||c.currentScreen!=null||!Craftfist.equipped(c.player)){if(punching&&c.player!=null)send(9);punching=jumping=slamming=false;return;}
  boolean p=c.options.useKey.isPressed()||punch.isPressed(),j=c.options.jumpKey.isPressed();
  boolean s=slam.isPressed()||(slam.matchesKey(GLFW.GLFW_KEY_LEFT_SHIFT,0)&&InputUtil.isKeyPressed(c.getWindow().getHandle(),GLFW.GLFW_KEY_LEFT_SHIFT));
  if(p&&!punching)send(0);if(!p&&punching)send(1);
  while(block.wasPressed())send(4);
  if(s&&!slamming)send(2);while(slam.wasPressed()){}while(upper.wasPressed())send(3);while(meteor.wasPressed())send(6);
  if(c.options.attackKey.isPressed())send(7);if(j&&!jumping)send(8);
  punching=p;jumping=j;slamming=s;
 });}
}
