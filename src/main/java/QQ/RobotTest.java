package QQ;


 
 import java.awt.AWTException;
 import java.awt.Robot;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 
 public class RobotTest {
     public static void main(String[] args){
         try {
             Robot robot = new Robot();
             //鼠标移动到坐标(635,454)
             RobotTest.clickMouse(robot, 635, 454, 500);
            
             int[] keys = {
                     KeyEvent.VK_H,KeyEvent.VK_E,
                     KeyEvent.VK_L,KeyEvent.VK_L,
                     KeyEvent.VK_O,KeyEvent.VK_S,
                     KeyEvent.VK_I,KeyEvent.VK_S,
                     KeyEvent.VK_T,KeyEvent.VK_E,
                     KeyEvent.VK_T,KeyEvent.VK_ENTER,
                     KeyEvent.VK_ENTER};
             robot.delay(500);
             RobotTest.pressKey(robot,keys,500);
         } catch (AWTException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
     
     public static void pressKey(Robot robot,int[] keys,int delay){
         for(int i=0;i<keys.length;i++){
             robot.keyPress(keys[i]);
             robot.keyRelease(keys[i]);
             robot.delay(500);
         }
         //处理完需要延迟
         robot.delay(delay);
     }
     
     public static void clickMouse(Robot robot,int x,int y,int delay){
         robot.mouseMove(x, y);
         robot.delay(500);
         robot.mousePress(InputEvent.BUTTON1_MASK);
         robot.mouseRelease(InputEvent.BUTTON1_MASK);
         robot.delay(delay);
     }
 }