package dev.craftfist;

/** Brake ordinary incoming motion once, without repeatedly damping an ability chain. */
public final class ChargeMovement {
 private static final double ORDINARY_CHARGE_MAX_SPEED=.1;
 private boolean wasCharging,wasCarryingAbility;
 public double horizontalScale(boolean charging,boolean carryingAbility){
  boolean brake=charging&&!carryingAbility&&(!wasCharging||wasCarryingAbility);
  wasCharging=charging;
  wasCarryingAbility=charging&&carryingAbility;
  return brake?Motion.CHARGE_INPUT_SCALE:1;
 }
 public double horizontalScale(boolean charging,boolean carryingAbility,double horizontalSpeed){
  double scale=horizontalScale(charging,carryingAbility);
  // Cap newly gained ordinary motion too, rather than multiplying it every tick.
  // This prevents sprint-jump boosts or knockback from creating a second exemption.
  if(charging&&!carryingAbility&&horizontalSpeed*scale>ORDINARY_CHARGE_MAX_SPEED)
   scale=ORDINARY_CHARGE_MAX_SPEED/horizontalSpeed;
  return scale;
 }
}
