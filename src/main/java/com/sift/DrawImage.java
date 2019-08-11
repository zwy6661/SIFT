package com.sift;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import static java.lang.System.*;
import java.lang.*;
import java.util.ArrayList;

import javax.imageio.ImageIO;
public class DrawImage {
	
	
	private static BufferedImage bi=null;
	private static ImagePixelArray[] smoothedImgs;  //高斯塔
	private static ImagePixelArray[] diffImags;  //差分金字塔
	
	public static void drawImage(ArrayList<OctaveSpace> octlist) throws IOException {
		
		
		for(OctaveSpace oct:octlist) {
			System.out.println("开始输出图像");
			smoothedImgs=oct.smoothedImgs;
			diffImags=oct.diffImags;
			int width=oct.baseImg.width;
			int height=oct.baseImg.height;
			int layer=(int) oct.baseScale;
			bi=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
			for(int i=0;i<height;i++) {
				for(int j=0;j<width;j++) {
					out.println(diffImags[0].data[i*j+j]);
				}
			}
			for(int i=0;i<smoothedImgs.length;i++) {
				
				bi.setRGB(0, 0, width, height, (smoothedImgs[i].datas), 0, width);
				ImageIO.write(bi, "jpg", new File("C:\\Users\\22682\\Desktop\\高斯\\高斯图像_"+layer+"_"+i+".jpg"));
				bi.flush();
			}
			for(int i=0;i<diffImags.length;i++) {
				bi.setRGB(0, 0, width, height, (diffImags[i].datas), 0, width);
				ImageIO.write(bi, "jpg", new File("C:\\Users\\22682\\Desktop\\高斯\\高斯差分图像_"+layer+"_"+i+".jpg"));
			}
			
			
			
			
		}
		
		
		
		
		
		
		
		
	}
	
}
