package QQ;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
/**
 * Java实现在QQ上输入文字并发送
 * @author Wayss
 *
 */
public class TestDeskTop {
    static Desktop deskapp = Desktop.getDesktop();

    public static void main(String [] args) throws AWTException{
        openQQ();
    	inputQQ();
    }

    public static void openQQ(){
        //判断当前系统释放支持Desktop提供的接口
        if(Desktop.isDesktopSupported()){
            try {
            	System.out.println("jinru");
                deskapp.open(new File("C:\\Program Files (x86)\\Tencent\\QQ\\Bin\\QQScLauncher.exe"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
    }

    public static void inputQQ() throws AWTException{
    	
        Robot robot = new Robot();
        //3等待3秒后开始执行下面的自动键盘事件
        robot.delay(3000);
        //点击鼠标左键(目的是让光标放到QQ上)
        TestInput.mouseLeftHit(robot);

        for(int i = 0 ; i < 10; i++){
            //输入笑脸
            TestInput.keyPressString(robot, "/wx");
            //按下回车
            TestInput.keyPress(robot, KeyEvent.VK_ENTER);
        }
    }
}
