package dev.craftfist;

import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;

/** Text HUD anchored above the hotbar in the bottom-left corner. */
public final class AbilityHud {
 private static final int WIDTH=228,HEIGHT=126;
 private static final int WHITE=0xFFE7EBEF,MUTED=0xFFA4AFB9,GOLD=0xFFFFCD70,READY=0xFFBBD6CA;
 private AbilityHud(){}
 public static void render(DrawContext draw,MinecraftClient client,HudPacket s,KeyBinding[] keys){
  if(client.player==null||client.options.hudHidden||!Craftfist.equipped(client.player)||s==null)return;
  float scale=Math.min(1,Math.min((draw.getScaledWindowHeight()-60f)/HEIGHT,(draw.getScaledWindowWidth()-16f)/WIDTH));
  if(scale<=0)return;
  float top=draw.getScaledWindowHeight()-52-HEIGHT*scale;
  draw.getMatrices().push();draw.getMatrices().translate(8,top,0);draw.getMatrices().scale(scale,scale,1);
  try {
   draw.fill(0,0,WIDTH,HEIGHT,0x92101923);
   draw.fill(8,17,WIDTH-8,18,0x553F4F5C);
   text(draw,client,"DOOMFIST",8,5,GOLD);
   if(s.empowered())right(draw,client,"EMPOWERED",WIDTH-8,5,GOLD);
   String ammo=s.ammo()+"/4";
   if(s.ammo()<4)ammo+=s.reloadDelay()>0?String.format(Locale.ROOT,"  %.1fs",s.reloadDelay()/20.0):"  RELOAD";
   row(draw,client,0,"Hand Cannon",bind(keys[0]),ammo,s.ammo()>0?WHITE:MUTED,-1);
   String punch=s.charge()>=0?"CHARGE "+Math.min(100,s.charge()*100/30)+"%":cooldown(s.punch());
   row(draw,client,1,"Rocket Punch",bind(client.options.useKey)+"/"+bind(keys[1]),punch,s.charge()>=0?GOLD:s.punch()==0?READY:MUTED,s.charge()>=0?s.charge()/30.0:s.punch()>0?1-s.punch()/80.0:-1);
   row(draw,client,2,"Seismic Slam",bind(keys[2]),cooldown(s.slam()),s.slam()==0?READY:MUTED,s.slam()>0?1-s.slam()/120.0:-1);
   row(draw,client,3,"Uppercut",bind(keys[3]),cooldown(s.uppercut()),s.uppercut()==0?READY:MUTED,s.uppercut()>0?1-s.uppercut()/120.0:-1);
   row(draw,client,4,"Power Block",bind(keys[4]),cooldown(s.block()),s.block()==0?READY:MUTED,s.block()>0?1-s.block()/140.0:-1);
   row(draw,client,5,"Meteor Strike",bind(keys[5]),s.ultimate()>=100?"READY":s.ultimate()+"%",s.ultimate()>=100?GOLD:WHITE,s.ultimate()/100.0);
  }finally{draw.getMatrices().pop();}
 }
 private static void row(DrawContext d,MinecraftClient c,int index,String name,String key,String value,int color,double progress){
  int y=23+index*17;
  text(d,c,name,8,y,WHITE);
  text(d,c,c.textRenderer.trimToWidth(key,45),101,y,MUTED);
  right(d,c,value,WIDTH-8,y,color);
  if(progress>=0){
   d.fill(8,y+12,WIDTH-8,y+13,0x403F4F5C);
   d.fill(8,y+12,8+(int)((WIDTH-16)*Math.clamp(progress,0,1)),y+13,0x80000000|(color&0xFFFFFF));
  }
 }
 private static String cooldown(int ticks){return ticks==0?"READY":String.format(Locale.ROOT,"%.1fs",ticks/20.0);}
 private static String bind(KeyBinding key){
  String translation=key.getBoundKeyTranslationKey();
  if(translation.contains("shift"))return "SHIFT";
  if(translation.equals("key.mouse.left"))return "LMB";
  if(translation.equals("key.mouse.right"))return "RMB";
  return key.getBoundKeyLocalizedText().getString().toUpperCase(Locale.ROOT);
 }
 private static void text(DrawContext d,MinecraftClient c,String text,int x,int y,int color){d.drawTextWithShadow(c.textRenderer,text,x,y,color);}
 private static void right(DrawContext d,MinecraftClient c,String text,int x,int y,int color){text(d,c,text,x-c.textRenderer.getWidth(text),y,color);}
}
