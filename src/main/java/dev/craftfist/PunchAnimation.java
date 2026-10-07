package dev.craftfist;
/** First-person animation driven by accepted server ability state. */
public final class PunchAnimation {
 private static int charge=-1,dash;
 private static boolean abilityMomentum;
 private static float pull,previousPull,thrust,previousThrust;
 private static int blockTicks;
 private static int uppercutTicks;
 private static float block,previousBlock;
 private PunchAnimation(){}
 public static void receive(int charging,int dashing){charge=charging;dash=dashing;}
 public static boolean shouldSlowCharge(){return charge>=0;}
 public static boolean carriesAbilityMomentum(){return abilityMomentum;}
 public static void receive(int charging,int dashing,int blocking){receive(charging,dashing);blockTicks=blocking;}
 public static void receive(int charging,int dashing,int blocking,int uppercut){receive(charging,dashing,blocking);uppercutTicks=uppercut;}
 public static void receive(int charging,int dashing,int blocking,int uppercut,boolean momentum){receive(charging,dashing,blocking,uppercut);abilityMomentum=momentum;}
 public static void tick(boolean equipped){
  previousPull=pull;previousThrust=thrust;
  previousBlock=block;
  if(!equipped){charge=-1;abilityMomentum=false;dash=blockTicks=uppercutTicks=0;block=previousBlock=pull=previousPull=thrust=previousThrust=0;return;}
  block+=((blockTicks>0?1:0)-block)*.55f;
  float target=charge>=0?.25f+.75f*Math.min(charge,30)/30f:0;
  pull+=(target-pull)*.5f;
  thrust+=((dash>0?1:0)-thrust)*(dash>0?.8f:.3f);
 }
 public static float pull(float delta){return previousPull+(pull-previousPull)*delta;}
 public static float thrust(float delta){return previousThrust+(thrust-previousThrust)*delta;}
 public static float block(float delta){return previousBlock+(block-previousBlock)*delta;}
 public static boolean isDashing(){return dash>0;}
 public static float uppercutLift(float delta){
  if(uppercutTicks<=0)return 0;
  float elapsed=Math.clamp(12-uppercutTicks+delta,0,12);
  if(elapsed<2)return -.18f*smooth(elapsed/2);
  if(elapsed<5)return -.18f+.73f*smooth((elapsed-2)/3);
  if(elapsed<7)return .55f;
  return .55f*(1-smooth((elapsed-7)/5));
 }
 private static float smooth(float t){return t*t*(3-2*t);}
}
