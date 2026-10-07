package dev.craftfist;

import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;

/** Compact pixel icons and readable ability cards that stay in the left corner. */
public final class AbilityHud {
 private static final int WIDTH=192,HEIGHT=174,WHITE=0xFFE9F0F4,GOLD=0xFFFFCD70,TEAL=0xFF78E0CF,MUTED=0xFF94A5B3,BLUE=0xFF75BBFF;
 private static final String[][] ICONS={
  {"..........","..##..##..","..##..##..","..######..",".########.","#########.",".########.","..######..","...####...","...####..."},
  {"..........",".######...","########..","##########","##########",".#########","..########","...#######","....######",".........."},
  {"....##....","...####...","....##....","...####...","....##....","..######..",".########.","....##....","##########",".##.##.##."},
  {"....##....","...####...","..######..",".########.","....##....","....##....","....##....","...###....","..###.....",".###......"},
  {"..#.#.#...","..#.#.#.#.","..#######.","..#######.","#.#######.","#########.",".########.","..######..","...####...","...####..."},
  {".......###","......###.",".....###..","....###...",".######...","########..","########..",".######...","..####....",".........."}
 };
 private AbilityHud(){}
 public static void render(DrawContext draw,MinecraftClient client,HudPacket state,KeyBinding[] keys){
  if(client.player==null||client.options.hudHidden||!Craftfist.equipped(client.player)||state==null)return;
  float scale=Math.min(1,Math.min((draw.getScaledWindowHeight()-16f)/HEIGHT,(draw.getScaledWindowWidth()-16f)/WIDTH));
  if(scale<=0)return;
  draw.getMatrices().push();draw.getMatrices().translate(8,8,0);draw.getMatrices().scale(scale,scale,1);
  try {
   draw.fill(0,0,WIDTH,HEIGHT,0xC8101923);draw.fill(0,0,2,HEIGHT,GOLD);
   text(draw,client,"DOOMFIST",8,5,GOLD);
   if(state.empowered())right(draw,client,"EMPOWERED",184,5,GOLD);
   ammo(draw,client,state,keys[0]);
   ability(draw,client,1,"Rocket Punch",keys[1],state.punch(),80,state.charge()>=0?"CHARGE "+Math.min(100,state.charge()*100/30)+"%":state.empowered()?"EMPOWERED":null,state.charge()>=0?state.charge()/30.0:-1,state.empowered()||state.charge()>=0?GOLD:TEAL);
   ability(draw,client,2,"Seismic Slam",keys[2],state.slam(),120,null,-1,TEAL);
   ability(draw,client,3,"Rising Uppercut",keys[3],state.uppercut(),120,null,-1,TEAL);
   ability(draw,client,4,"Power Block",keys[4],state.block(),140,null,-1,TEAL);
   ability(draw,client,5,"Meteor Strike",keys[5],0,1,state.ultimate()>=100?"READY":state.ultimate()+"%",state.ultimate()/100.0,BLUE);
  }finally{draw.getMatrices().pop();}
 }
 private static void ammo(DrawContext d,MinecraftClient c,HudPacket s,KeyBinding key){
  int y=20;card(d,c,0,"Hand Cannon",key,y,s.ammo()>0?GOLD:MUTED);
  for(int i=0;i<4;i++){
   int x=32+i*21;d.fill(x,y+15,x+18,y+20,0xFF344350);
   if(i<s.ammo())d.fill(x,y+15,x+18,y+20,GOLD);
   else if(i==s.ammo()&&s.reloadDelay()==0)d.fill(x,y+15,x+(int)(18*Math.clamp(s.reloadProgress()/13.0,0,1)),y+20,TEAL);
  }
  String status=s.reloadDelay()>0?String.format(Locale.ROOT,"WAIT %.1f",s.reloadDelay()/20.0):s.ammo()<4?"RELOAD":s.ammo()+" / 4";
  right(d,c,status,184,y+12,s.reloadDelay()>0?MUTED:s.ammo()<4?TEAL:GOLD);
 }
 private static void ability(DrawContext d,MinecraftClient c,int icon,String name,KeyBinding key,int cooldown,int max,String override,double progress,int accent){
  int y=20+icon*25;
  boolean ready=cooldown==0;
  card(d,c,icon,name,key,y,ready?accent:MUTED);
  String status=override!=null?override:ready?"READY":String.format(Locale.ROOT,"%.1fs",cooldown/20.0);
  text(d,c,status,32,y+12,ready?accent:MUTED);
  double amount=progress>=0?progress:1-cooldown/(double)max;
  d.fill(126,y+16,184,y+19,0xFF344350);
  d.fill(126,y+16,126+(int)(58*Math.clamp(amount,0,1)),y+19,ready?accent:MUTED);
 }
 private static void card(DrawContext d,MinecraftClient c,int icon,String name,KeyBinding key,int y,int tint){
  d.fill(5,y,187,y+23,0xAA182532);d.fill(7,y+3,27,y+21,0xFF243643);
  d.getMatrices().push();d.getMatrices().translate(10,y+5,0);d.getMatrices().scale(1.5f,1.5f,1);
  try {
   String[] mask=ICONS[icon];
   for(int row=0;row<mask.length;row++)for(int col=0;col<mask[row].length();col++)if(mask[row].charAt(col)=='#')d.fill(col,row,col+1,row+1,tint);
  }finally{d.getMatrices().pop();}
  text(d,c,name,32,y+1,WHITE);
  String bind=bind(key);
  if(icon==1)bind=bind(c.options.useKey)+"/"+bind;
  bind=c.textRenderer.trimToWidth(bind,icon==1?48:32);
  int badgeWidth=Math.max(15,c.textRenderer.getWidth(bind)+6);
  d.fill(184-badgeWidth,y,184,y+10,0xFF344350);right(d,c,bind,181,y+1,WHITE);
 }
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
