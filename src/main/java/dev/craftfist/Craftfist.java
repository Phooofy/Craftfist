package dev.craftfist;

import java.util.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.*;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.*;
import net.minecraft.registry.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.world.RaycastContext;

public class Craftfist implements ModInitializer {
 public static final Item GAUNTLET = new Item(new Item.Settings().maxCount(1));
 private static final Identifier SHIELD_ATTRIBUTE=Identifier.of("craftfist","shields");
 private static final Map<UUID, State> STATES = new HashMap<>();
 static class State {
  int tick, charge=-1, dash, slam, block, meteor, ammo=4, reload, ultimate, empoweredTicks, safeFall, shieldTicks;
  int shotIdle=20;
  double blocked;
  float grantedShield;
  float punchDamage;
  boolean empowered;
  boolean abilityMomentum,sentAbilityMomentum;
  int momentumGrace;
  int meteorForward,meteorSideways;
  long meteorInputTime=Long.MIN_VALUE;
  boolean meteorDescending;
  int meteorFallTicks,meteorWave;
  Vec3d meteorImpactPos=Vec3d.ZERO;
  ServerWorld meteorImpactWorld;
  Vec3d dashVelocity=Vec3d.ZERO;
  Vec3d lastDashPos=Vec3d.ZERO;
  final int[] cooldown=new int[5];
  final Set<Integer> hit=new HashSet<>();
  final Map<UUID,WallImpact> walls=new HashMap<>();
  int sentCharge=-2,sentDash=-1,sentBlock=-1,uppercutTicks,sentUppercut=-1;
  void launchPunch(Vec3d look,Vec3d incoming,Vec3d position){
   punchDamage=(4+Math.min(charge,30)*.2f)*(empowered?1.5f:1);
   Vec3d aimed=look.multiply(Motion.punchSpeed(charge,empowered));
   dashVelocity=new Vec3d(aimed.x,Motion.punchVertical(incoming.y,aimed.y),aimed.z);
   empowered=false;empoweredTicks=0;
   lastDashPos=position;
   dash=8+Math.min(charge,30)/5;charge=-1;block=0;cooldown[0]=80;hit.clear();safeFall=60;
   grantAbilityMomentum();
  }
  void grantAbilityMomentum(){abilityMomentum=true;momentumGrace=4;}
  void clearAbilityMomentum(){abilityMomentum=false;momentumGrace=0;}
  void beginMeteor(){meteor=Motion.METEOR_AIR_TICKS;meteorDescending=false;meteorFallTicks=0;}
  void tickMeteorAir(){if(meteor>0&&!meteorDescending&&--meteor==0)beginMeteorDescent();}
  boolean meteorRising(){return !meteorDescending&&meteor>Motion.METEOR_AIR_TICKS-Motion.METEOR_ASCENT_TICKS;}
  boolean canDiveEarly(){return meteor>0&&!meteorDescending&&meteor<=Motion.METEOR_AIR_TICKS-10;}
  void beginMeteorDescent(){meteor=1;meteorDescending=true;meteorFallTicks=0;}
  boolean tickMeteorDescent(boolean landed,boolean outOfWorld){
   if(!meteorDescending)return false;
   meteorFallTicks++;
   if(outOfWorld||meteorFallTicks>200){finishMeteor();return false;}
   if(!landed)return false;
   finishMeteor();return true;
  }
  void finishMeteor(){meteor=0;meteorDescending=false;meteorFallTicks=0;}
  void tickAbilityMomentum(boolean grounded,boolean cancel){
   if(cancel){clearAbilityMomentum();return;}
   if(momentumGrace>0)momentumGrace--;
   else if(grounded)clearAbilityMomentum();
  }
  void cancelCharge(){charge=-1;}
  boolean consumeShot(){
   if(ammo<=0||cooldown[4]>0)return false;
   ammo--;cooldown[4]=4;shotIdle=0;reload=0;return true;
  }
  void tickAmmo(){
   if(ammo==4){reload=0;shotIdle=20;return;}
   if(shotIdle<20){
    shotIdle++;reload=0;
    if(shotIdle==20)ammo++;
   }else if(++reload>=13){ammo++;reload=0;}
  }
  void tickCooldowns(){for(int i=0;i<cooldown.length;i++)cooldown[i]=Motion.cooldownTick(cooldown[i]);}
  boolean tickSlam(boolean grounded,boolean cancel){
   if(cancel){slam=0;return false;}
   if(slam<=0)return false;
   slam=Math.min(5,slam+1);
   if(!Motion.slamLanded(slam,grounded))return false;
   slam=0;return true;
  }
 }
 public static boolean equipped(LivingEntity p){return p.getMainHandStack().isOf(GAUNTLET)||p.getOffHandStack().isOf(GAUNTLET);}
 @Override public void onInitialize(){
  AbilitySounds.register();
  Registry.register(Registries.ITEM,Identifier.of("craftfist","gauntlet"),GAUNTLET);
  AttackEntityCallback.EVENT.register((player,world,hand,entity,result)->equipped(player)?ActionResult.FAIL:ActionResult.PASS);
  PayloadTypeRegistry.playC2S().register(AbilityPacket.ID,AbilityPacket.CODEC);
  PayloadTypeRegistry.playC2S().register(MeteorInputPacket.ID,MeteorInputPacket.CODEC);
  PayloadTypeRegistry.playS2C().register(PunchStatePacket.ID,PunchStatePacket.CODEC);
  PayloadTypeRegistry.playS2C().register(HudPacket.ID,HudPacket.CODEC);
  PayloadTypeRegistry.playS2C().register(FullVelocityPacket.ID,FullVelocityPacket.CODEC);
  ServerPlayNetworking.registerGlobalReceiver(AbilityPacket.ID,(packet,ctx)->ctx.server().execute(()->action(ctx.player(),packet.action())));
  ServerPlayNetworking.registerGlobalReceiver(MeteorInputPacket.ID,(packet,ctx)->ctx.server().execute(()->{
   var p=ctx.player();if(!equipped(p)||!p.isAlive()||p.isSpectator())return;
   var s=STATES.computeIfAbsent(p.getUuid(),k->new State());
   s.meteorForward=Math.clamp(packet.forward(),-1,1);s.meteorSideways=Math.clamp(packet.sideways(),-1,1);
   s.meteorInputTime=p.getServerWorld().getTime();
  }));
  ServerTickEvents.END_SERVER_TICK.register(server->{
   Set<UUID> online=new HashSet<>();
   for(ServerPlayerEntity p:server.getPlayerManager().getPlayerList()){online.add(p.getUuid());tick(p);}
   STATES.keySet().retainAll(online);
  });
  ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity,source,amount)->{
   if(entity instanceof ServerPlayerEntity p && equipped(p)){
    State s=STATES.computeIfAbsent(p.getUuid(),k->new State());
    if(s.meteor>0 || (source.isIn(net.minecraft.registry.tag.DamageTypeTags.IS_FALL)&&(s.safeFall>0||s.slam>0))) return false;
   } return true;
  });
 }
 /** Scale the original damage call, retaining its source, attacker, armor and enchantment rules. */
 public static float blockDamage(LivingEntity entity,DamageSource source,float amount){
  if(!(entity instanceof ServerPlayerEntity p)||!equipped(p)||amount<=0)return amount;
  State s=STATES.get(p.getUuid());
  Entity attacker=source.getSource();
  if(s==null||s.block<=0||s.meteor>0||attacker==null)return amount;
  Vec3d toward=attacker.getPos().subtract(p.getPos()).normalize();
  if(p.getRotationVec(1).dotProduct(toward)<=.15)return amount;
  s.blocked+=amount*.8;
  AbilitySounds.play(p,"block_hit");
  if(s.blocked>=8&&!s.empowered){s.empowered=true;s.empoweredTicks=200;s.cooldown[0]=0;AbilitySounds.play(p,"empowered");}
  particles(p,ParticleTypes.ELECTRIC_SPARK,10);
  return amount*.2f;
 }
 private static void action(ServerPlayerEntity p,int a){
  if(!equipped(p)||!p.isAlive()||p.isSpectator())return;
  State s=STATES.computeIfAbsent(p.getUuid(),k->new State());
  if(a<0||a>9)return;
  if(a==9){s.cancelCharge();return;}
  if(a==1){if(s.charge>=0)release(p,s);return;}
  if(a==5){s.block=0;return;}
  if(a==4 && s.block>0){s.block=0;return;}
  if(s.meteor>0){if(a==6 && s.canDiveEarly())s.beginMeteorDescent();return;}
  if(a==0 && s.cooldown[0]==0 && s.charge<0){s.charge=0;s.block=0;AbilitySounds.play(p,"punch_charge");}
  if(a==2 && s.cooldown[1]==0){
   AbilitySounds.play(p,"slam_launch");
   // Charge is converted into launch velocity before cancellation: punch -> slam carries momentum.
   Vec3d old=p.getVelocity();
   if(s.charge>=0){old=old.add(p.getRotationVec(1).multiply(punchSpeed(s.charge,s.empowered)));s.cooldown[0]=80;s.empowered=false;}
   s.charge=-1;s.dash=0;s.block=0;s.slam=1;s.safeFall=100;s.cooldown[1]=120;
   s.grantAbilityMomentum();
   Vec3d look=p.getRotationVec(1);Vec3d flat=new Vec3d(look.x,0,look.z).normalize();
   velocity(p,new Vec3d(Motion.slamHorizontal(old.x,flat.x),Motion.slamVertical(old.y,look.y),Motion.slamHorizontal(old.z,flat.z)));
  }
  if(a==3 && s.cooldown[2]==0){
   s.uppercutTicks=12;
   AbilitySounds.play(p,"uppercut");
   s.charge=-1;s.dash=0;s.block=0;s.safeFall=80;s.cooldown[2]=120;
   s.grantAbilityMomentum();
   Vec3d v=p.getVelocity();velocity(p,new Vec3d(v.x,1.25,v.z));
   for(LivingEntity e:targets(p,3)){
    hit(p,s,e,7);Vec3d ev=e.getVelocity();e.setVelocity(new Vec3d(ev.x,Math.max(1.35,ev.y),ev.z));e.velocityModified=true;
    if(e instanceof ServerPlayerEntity victim)victim.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket(victim));
   }
   particles(p,ParticleTypes.CLOUD,30);
  }
  if(a==4 && s.cooldown[3]==0){s.block=60;s.blocked=0;s.cooldown[3]=140;s.charge=-1;s.dash=0;s.clearAbilityMomentum();AbilitySounds.play(p,"block_start");}
  if(a==6 && s.ultimate>=100){s.ultimate=0;s.beginMeteor();s.charge=-1;s.dash=0;s.block=0;s.slam=0;s.safeFall=120;s.clearAbilityMomentum();AbilitySounds.play(p,"meteor_start");velocity(p,new Vec3d(0,2.3,0));}
  if(a==7 && s.ammo>0 && s.charge<0 && s.dash==0 && s.block==0){shoot(p,s);}
  if(a==8 && s.dash>0){s.dash=0;s.grantAbilityMomentum();Vec3d v=p.getVelocity();velocity(p,new Vec3d(v.x*1.15,Math.max(.45,v.y),v.z*1.15));}
 }
 private static double punchSpeed(int charge,boolean empowered){return Motion.punchSpeed(charge,empowered);}
 private static void release(ServerPlayerEntity p,State s){
  AbilitySounds.play(p,"punch_release");
  s.launchPunch(p.getRotationVec(1),p.getVelocity(),p.getPos());
  // The server resolves the first swept collision before sending launch velocity.
 }
 private static void tick(ServerPlayerEntity p){
  State s=STATES.computeIfAbsent(p.getUuid(),k->new State());
  // Cooldowns measure elapsed server time even while the gauntlet is put away.
  s.tickCooldowns();
  s.tickAmmo();
  if(s.meteorWave>0){meteorRing(s,9-s.meteorWave);s.meteorWave--;if(s.meteorWave==0)s.meteorImpactWorld=null;}
  if(!equipped(p)||!p.isAlive()){
   p.setAbsorptionAmount(Math.max(0,p.getAbsorptionAmount()-s.grantedShield));s.grantedShield=0;
   var maxShield=p.getAttributeInstance(EntityAttributes.GENERIC_MAX_ABSORPTION);if(maxShield!=null)maxShield.removeModifier(SHIELD_ATTRIBUTE);
   s.charge=-1;s.dash=s.slam=s.block=s.safeFall=s.uppercutTicks=0;s.finishMeteor();s.empowered=false;s.clearAbilityMomentum();s.walls.clear();syncPunch(p,s);return;
  }
  var maxShield=p.getAttributeInstance(EntityAttributes.GENERIC_MAX_ABSORPTION);
  if(maxShield!=null && !maxShield.hasModifier(SHIELD_ATTRIBUTE))maxShield.addTemporaryModifier(new EntityAttributeModifier(SHIELD_ATTRIBUTE,16,EntityAttributeModifier.Operation.ADD_VALUE));
  s.tick++;
  s.tickAbilityMomentum(p.isOnGround()&&s.dash==0,p.isTouchingWater()||p.isInLava()||p.hasVehicle()||p.getAbilities().flying||p.isSpectator());
  if(s.charge>=0||s.block>0||s.meteor>0)s.uppercutTicks=0;
  else if(s.uppercutTicks>0)s.uppercutTicks--;
  if(s.safeFall>0){s.safeFall--;p.fallDistance=0;}
  if(s.slam>0)p.fallDistance=0;
  if(s.empoweredTicks>0 && --s.empoweredTicks==0)s.empowered=false;
  s.grantedShield=Math.min(s.grantedShield,p.getAbsorptionAmount());
  if(s.shieldTicks>0)s.shieldTicks--;else if(s.grantedShield>0){float decay=Math.min(.1f,s.grantedShield);p.setAbsorptionAmount(Math.max(0,p.getAbsorptionAmount()-decay));s.grantedShield-=decay;}
  if(s.tick%20==0)s.ultimate=Math.min(100,s.ultimate+1);
  if(s.charge>=0){s.charge=Math.min(30,s.charge+1);particles(p,ParticleTypes.ELECTRIC_SPARK,2);}
  if(s.block>0){s.block--;velocity(p,p.getVelocity().multiply(.55,1,.55));}
  if(s.dash>0){
   s.dash--;
   // Grounded horizontal punches use vanilla step-up movement rather than accumulating downward dash velocity.
   if(p.isOnGround() && s.dashVelocity.y<0)s.dashVelocity=new Vec3d(s.dashVelocity.x,0,s.dashVelocity.z);
   // Cover movement since the previous server tick as well as the next dash step: fast punches cannot skip mobs.
   Vec3d movement=p.getPos().add(s.dashVelocity).subtract(s.lastDashPos);
   Box startBox=p.getBoundingBox().offset(s.lastDashPos.subtract(p.getPos()));
   Box sweep=startBox.stretch(movement).expand(1);
   Vec3d origin=startBox.getCenter();
   LivingEntity contact=null;double contactDistance=movement.length()+1;
   for(LivingEntity e:p.getServerWorld().getEntitiesByClass(LivingEntity.class,sweep,e->e!=p&&e.isAlive()&&p.canSee(e))){
    double distance=PunchCollision.contactDistance(e.getBoundingBox(),startBox,origin,movement);
    if(distance<contactDistance){contact=e;contactDistance=distance;}
   }
   if(contact!=null){
    AbilitySounds.play(p,"punch_hit");
    // Let Minecraft resolve terrain collisions; never teleport downward along the dash's gravity vector.
    Vec3d stop=s.lastDashPos.add(PunchCollision.stopOffset(movement,contactDistance));
    p.move(MovementType.SELF,new Vec3d(stop.x-p.getX(),0,stop.z-p.getZ()));
    p.requestTeleport(p.getX(),p.getY(),p.getZ());
    hit(p,s,contact,s.punchDamage);
    Vec3d knockback=new Vec3d(s.dashVelocity.x*.9,Math.max(.2,s.dashVelocity.y*.4),s.dashVelocity.z*.9);
    contact.setVelocity(knockback);contact.velocityModified=true;
    if(contact instanceof ServerPlayerEntity victim)victim.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket(victim));
    contact.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS,15,5));s.walls.put(contact.getUuid(),new WallImpact(knockback,p.getServerWorld().getTime()));
    s.dash=0;velocity(p,new Vec3d(0,p.isOnGround()?Math.max(0,p.getVelocity().y):p.getVelocity().y,0));
   }else{
    s.lastDashPos=p.getPos();
    velocity(p,s.dashVelocity);
    s.dashVelocity=new Vec3d(s.dashVelocity.x,(s.dashVelocity.y-.08)*.98,s.dashVelocity.z);
   }
   particles(p,ParticleTypes.CLOUD,6);
   if(s.dash>0 && p.horizontalCollision && !canStepForward(p,s.dashVelocity)){
    Vec3d probe=new Vec3d(s.dashVelocity.x,0,s.dashVelocity.z).normalize().multiply(.3);
    Vec3d clear=Entity.adjustMovementForCollisions(p,probe,p.getBoundingBox(),p.getServerWorld(),List.of());
    Vec3d slide=StepUp.slide(probe,clear);
    s.dashVelocity=new Vec3d(slide.x==0?0:s.dashVelocity.x,s.dashVelocity.y,slide.z==0?0:s.dashVelocity.z);
    if(s.dashVelocity.horizontalLengthSquared()<1e-8){s.dash=0;velocity(p,new Vec3d(0,p.getVelocity().y,0));}
    else velocity(p,s.dashVelocity);
   }
  }
  syncPunch(p,s);
  Iterator<Map.Entry<UUID,WallImpact>> it=s.walls.entrySet().iterator();
  while(it.hasNext()){
   var entry=it.next();Entity e=p.getServerWorld().getEntity(entry.getKey());
   WallImpact trace=entry.getValue();
   if(!(e instanceof LivingEntity l)||!l.isAlive()){it.remove();continue;}
   // Do not read a collision flag from before the knockback was applied.
   if(p.getServerWorld().getTime()<=trace.launchedAt)continue;
   Vec3d probe=trace.direction.multiply(.25);
   Vec3d clear=Entity.adjustMovementForCollisions(l,probe,l.getBoundingBox(),p.getServerWorld(),List.of());
   if(l.horizontalCollision&&Motion.opposingWall(probe.x,probe.z,clear.x,clear.z)){
    l.timeUntilRegen=0;hit(p,s,l,8);AbilitySounds.play(p,"wall_hit");l.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS,25,6));it.remove();
   }else if(--trace.remaining<=0)it.remove();
  }
  if(s.slam>0){
   // Keep the impact armed through long falls and ability chains. Liquids, flight,
   // riding, Meteor Strike, death, and unequipping explicitly cancel it.
   if(s.tickSlam(p.isOnGround(),p.isTouchingWater()||p.isInLava()||p.getAbilities().flying||p.hasVehicle()||p.isSpectator())){
    Vec3d facing=Vec3d.fromPolar(0,p.getYaw());
    for(LivingEntity e:p.getServerWorld().getEntitiesByClass(LivingEntity.class,p.getBoundingBox().expand(Motion.SLAM_RANGE,2,Motion.SLAM_RANGE),e->e!=p&&e.isAlive()&&p.canSee(e))){
     Vec3d offset=e.getPos().subtract(p.getPos());
     if(Motion.inSlamCone(facing.x,facing.z,offset.x,offset.z)){
      hit(p,s,e,8);e.setVelocity(e.getVelocity().add(0,.35,0));e.velocityModified=true;
      if(e instanceof ServerPlayerEntity victim)victim.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket(victim));
     }
    }
    slamParticles(p,facing);AbilitySounds.play(p,"slam_impact");s.slam=0;
   }
  }
  if(s.meteor>0){
   p.fallDistance=0;s.safeFall=60;
   if(!s.meteorDescending){
    s.tickMeteorAir();
    if(!s.meteorDescending){
     boolean fresh=s.meteorInputTime!=Long.MIN_VALUE&&p.getServerWorld().getTime()-s.meteorInputTime<=5;
     Vec3d horizontal=Motion.meteorHorizontal(p.getYaw(),fresh?s.meteorForward:0,fresh?s.meteorSideways:0);
     velocity(p,new Vec3d(horizontal.x,s.meteorRising()?2.3:0,horizontal.z));particles(p,ParticleTypes.END_ROD,4);
    }
   }
   if(s.meteorDescending){
    boolean landed=p.isOnGround()||p.isTouchingWater()||p.isInLava();
    if(s.tickMeteorDescent(landed,p.getY()<p.getServerWorld().getBottomY()-64)){
     velocity(p,Vec3d.ZERO);
     for(LivingEntity e:targets(p,8)){double d=e.distanceTo(p);hit(p,s,e,(float)Math.max(5,30-d*3));e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS,50,2));}
     s.meteorImpactPos=p.getPos();s.meteorImpactWorld=p.getServerWorld();s.meteorWave=8;
     particles(p,ParticleTypes.EXPLOSION_EMITTER,1);particles(p,ParticleTypes.CLOUD,30);AbilitySounds.play(p,"meteor_impact");
    }else if(s.meteorDescending){
     // Vanilla swept terrain collision stops this physical descent at the first surface.
     // FullVelocityPacket keeps the six-block/tick drop intact on the client.
     velocity(p,new Vec3d(0,-Motion.METEOR_DESCENT_SPEED,0));particles(p,ParticleTypes.END_ROD,8);
    }
   }
  }
  if(s.tick%2==0 && ServerPlayNetworking.canSend(p,HudPacket.ID))ServerPlayNetworking.send(p,new HudPacket(s.ammo,Math.max(0,20-s.shotIdle),s.reload,s.cooldown[0],s.cooldown[1],s.cooldown[2],s.cooldown[3],s.ultimate,s.charge,s.empowered));
 }
 private static boolean inPunchCone(ServerPlayerEntity p,State s,LivingEntity e){
  Vec3d offset=e.getBoundingBox().getCenter().subtract(p.getBoundingBox().getCenter());
  return Motion.inPunchCone(s.dashVelocity.x,s.dashVelocity.z,offset.x,offset.z);
 }
 private static boolean canStepForward(ServerPlayerEntity p,Vec3d direction){
  Vec3d horizontal=new Vec3d(direction.x,0,direction.z);
  if(horizontal.lengthSquared()<1e-8)return false;
  Box ahead=p.getBoundingBox().offset(horizontal.normalize().multiply(.3));
  // A collision flag can survive a completed step; open space should not terminate the punch.
  if(p.getServerWorld().isSpaceEmpty(p,ahead))return true;
  // Use the player's actual vanilla step height. Full blocks and low ceilings remain solid obstacles.
  return p.isOnGround() && p.getServerWorld().isSpaceEmpty(p,ahead.offset(0,p.getStepHeight(),0));
 }
 private static boolean stepOntoObstacle(ServerPlayerEntity p,Vec3d direction){
  Vec3d horizontal=new Vec3d(direction.x,0,direction.z);
  if(horizontal.lengthSquared()<1e-8||direction.y>.2)return false;
  Box body=p.getBoundingBox();
  // Also recognize support when the movement packet briefly clears the on-ground flag at a slab edge.
  if(!p.isOnGround() && p.getServerWorld().isSpaceEmpty(p,body.offset(0,-.05,0)))return false;
  Vec3d probe=horizontal.normalize().multiply(.35);
  double step=p.getStepHeight();
  List<Box> obstacles=new ArrayList<>();
  for(var shape:p.getServerWorld().getBlockCollisions(p,body.stretch(probe).stretch(0,step+.01,0)))obstacles.addAll(shape.getBoundingBoxes());
  double rise=StepUp.rise(body,probe,step,obstacles);
  if(rise<=0)return false;
  p.move(MovementType.SELF,new Vec3d(0,rise,0));
  p.requestTeleport(p.getX(),p.getY(),p.getZ());
  return true;
 }
 private static void slamParticles(ServerPlayerEntity p,Vec3d facing){
  double center=Math.atan2(facing.z,facing.x);
  // Filled fan and borders use the same range and angle as the damage footprint.
  for(double radius=.5;radius<=Motion.SLAM_RANGE;radius+=.5)for(int angle=-Motion.SLAM_HALF_ANGLE;angle<=Motion.SLAM_HALF_ANGLE;angle+=5){
   double a=center+Math.toRadians(angle);
   Vec3d sample=p.getPos().add(Math.cos(a)*radius,0,Math.sin(a)*radius);
   var ground=p.getServerWorld().raycast(new RaycastContext(sample.add(0,2,0),sample.add(0,-2,0),RaycastContext.ShapeType.COLLIDER,RaycastContext.FluidHandling.NONE,p));
   if(ground.getType()==net.minecraft.util.hit.HitResult.Type.MISS)continue;
   Vec3d at=ground.getPos().add(0,.12,0);
   if(!p.getServerWorld().raycast(new RaycastContext(p.getPos().add(0,.3,0),at,RaycastContext.ShapeType.COLLIDER,RaycastContext.FluidHandling.NONE,p)).getType().equals(net.minecraft.util.hit.HitResult.Type.MISS))continue;
   p.getServerWorld().spawnParticles(radius==Motion.SLAM_RANGE||Math.abs(angle)==Motion.SLAM_HALF_ANGLE?ParticleTypes.ELECTRIC_SPARK:ParticleTypes.CRIT,at.x,at.y,at.z,1,0,0,0,0);
  }
 }
 private static void meteorRing(State s,double radius){
  ServerWorld world=s.meteorImpactWorld;if(world==null)return;
  for(int angle=0;angle<360;angle+=5){
   double a=Math.toRadians(angle);
   Vec3d sample=s.meteorImpactPos.add(Math.cos(a)*radius,0,Math.sin(a)*radius);
   var ground=world.raycast(new RaycastContext(sample.add(0,2,0),sample.add(0,-2,0),RaycastContext.ShapeType.COLLIDER,RaycastContext.FluidHandling.NONE,net.minecraft.block.ShapeContext.absent()));
   if(ground.getType()==net.minecraft.util.hit.HitResult.Type.MISS)continue;
   Vec3d at=ground.getPos().add(0,.12,0);
   var obstruction=world.raycast(new RaycastContext(s.meteorImpactPos.add(0,.3,0),at,RaycastContext.ShapeType.COLLIDER,RaycastContext.FluidHandling.NONE,net.minecraft.block.ShapeContext.absent()));
   if(obstruction.getType()!=net.minecraft.util.hit.HitResult.Type.MISS)continue;
   world.spawnParticles(ParticleTypes.ELECTRIC_SPARK,at.x,at.y,at.z,1,0,0,0,0);
   world.spawnParticles(ParticleTypes.CRIT,at.x,at.y,at.z,2,.08,.04,.08,.03);
  }
 }
 private static void syncPunch(ServerPlayerEntity p,State s){
  if(s.sentCharge!=s.charge||s.sentDash!=s.dash||s.sentBlock!=s.block||s.sentUppercut!=s.uppercutTicks||s.sentAbilityMomentum!=s.abilityMomentum){
   if(ServerPlayNetworking.canSend(p,PunchStatePacket.ID))ServerPlayNetworking.send(p,new PunchStatePacket(s.charge,s.dash,s.block,s.uppercutTicks,s.abilityMomentum));
   s.sentCharge=s.charge;s.sentDash=s.dash;s.sentBlock=s.block;s.sentUppercut=s.uppercutTicks;s.sentAbilityMomentum=s.abilityMomentum;
  }
 }
 private static List<LivingEntity> targets(ServerPlayerEntity p,double radius){return p.getServerWorld().getEntitiesByClass(LivingEntity.class,p.getBoundingBox().expand(radius),e->e!=p&&e.isAlive()&&e.distanceTo(p)<=radius&&p.canSee(e));}
 private static void hit(ServerPlayerEntity p,State s,LivingEntity e,float damage){if(e.damage(p.getDamageSources().playerAttack(p),damage)){s.ultimate=Math.min(100,s.ultimate+(int)damage);float added=Math.max(0,Math.min(2,16-p.getAbsorptionAmount()));p.setAbsorptionAmount(p.getAbsorptionAmount()+added);s.grantedShield+=added;s.shieldTicks=60;p.getServerWorld().playSound(null,e.getBlockPos(),SoundEvents.ENTITY_PLAYER_ATTACK_STRONG,SoundCategory.PLAYERS,1,.7f);}}
 private static void velocity(ServerPlayerEntity p,Vec3d v){p.setVelocity(v);p.velocityModified=true;p.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket(p));}
 private static void particles(ServerPlayerEntity p,net.minecraft.particle.ParticleEffect particle,int n){p.getServerWorld().spawnParticles(particle,p.getX(),p.getY()+1,p.getZ(),n,.5,.5,.5,.08);}
 private static void shoot(ServerPlayerEntity p,State s){
  if(!s.consumeShot())return;
  p.swingHand(Hand.MAIN_HAND,true);
  AbilitySounds.play(p,"hand_cannon");
  Vec3d eye=p.getEyePos();
  Map<LivingEntity,Float> pellets=new HashMap<>();
  for(int i=0;i<7;i++){
   Vec3d dir=p.getRotationVec(1).add((p.getRandom().nextDouble()-.5)*.09,(p.getRandom().nextDouble()-.5)*.09,(p.getRandom().nextDouble()-.5)*.09).normalize();
   Vec3d end=eye.add(dir.multiply(18));var wall=p.getServerWorld().raycast(new RaycastContext(eye,end,RaycastContext.ShapeType.COLLIDER,RaycastContext.FluidHandling.NONE,p));
   double limit=eye.distanceTo(wall.getPos());LivingEntity nearest=null;double best=limit;
   for(LivingEntity e:targets(p,20)){var intersection=e.getBoundingBox().expand(.15).raycast(eye,end);if(intersection.isPresent()){double d=eye.distanceTo(intersection.get());if(d<best){best=d;nearest=e;}}}
   if(nearest!=null)pellets.merge(nearest,1.5f,Float::sum);
   for(double d=0;d<best;d+=.7){Vec3d at=eye.add(dir.multiply(d));p.getServerWorld().spawnParticles(ParticleTypes.CRIT,at.x,at.y,at.z,1,0,0,0,0);}
  }
  for(var pellet:pellets.entrySet())if(pellet.getKey().damage(p.getDamageSources().playerAttack(p),pellet.getValue()))s.ultimate=Math.min(100,s.ultimate+Math.round(pellet.getValue()));
 }
}
