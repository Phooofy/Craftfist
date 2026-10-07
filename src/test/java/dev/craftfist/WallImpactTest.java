package dev.craftfist;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WallImpactTest {
 @Test void unrelatedWallContactDoesNotCountAsAnImpact(){
  // A wall beside or behind the entity leaves forward knockback unobstructed.
  assertFalse(Motion.opposingWall(.25,0,.25,0));
  assertFalse(Motion.opposingWall(0,.25,0,.25));
  assertFalse(Motion.opposingWall(0,0,0,0));
 }
 @Test void straightAndDiagonalKnockbackCountWhenBlockedInTheirTravelDirection(){
  assertTrue(Motion.opposingWall(.25,0,0,0));
  assertTrue(Motion.opposingWall(-.25,0,0,0));
  assertTrue(Motion.opposingWall(.2,.15,.2,0));
  assertFalse(Motion.opposingWall(.2,.15,.2,.15));
 }
}
