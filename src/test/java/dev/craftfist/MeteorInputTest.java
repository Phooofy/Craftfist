package dev.craftfist;

import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MeteorInputTest {
 @Test void releasingMovementKeysHoversAtEveryCameraAngle(){
  for(int yaw=-180;yaw<=180;yaw+=15)assertEquals(Vec3d.ZERO,Motion.meteorHorizontal(yaw,0,0));
 }
 @Test void wasdMovesForwardBackwardAndSidewaysRelativeToTheCamera(){
  assertEquals(1.2,Motion.meteorHorizontal(0,1,0).z,1e-9);
  assertEquals(-1.2,Motion.meteorHorizontal(0,-1,0).z,1e-9);
  assertEquals(1.2,Motion.meteorHorizontal(0,0,1).x,1e-9);
  assertEquals(-1.2,Motion.meteorHorizontal(0,0,-1).x,1e-9);
  assertEquals(-1.2,Motion.meteorHorizontal(90,1,0).x,1e-9);
 }
 @Test void diagonalsAndInvalidInputCannotIncreaseTheExistingSpeed(){
  for(int f=-1;f<=1;f++)for(int s=-1;s<=1;s++){
   if(f!=0||s!=0)assertEquals(1.2,Motion.meteorHorizontal(37,f,s).length(),1e-9);
  }
  assertEquals(1.2,Motion.meteorHorizontal(0,100,-100).length(),1e-9);
 }
}
