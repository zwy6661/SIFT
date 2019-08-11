package com.brief;

import static java.lang.System.*;
import java.util.*;

import javax.imageio.ImageIO;

import java.io.*;
import java.awt.*;
import java.awt.image.*;

public class gray {
	public static int size=32;
	public static int smallSize=8;
	public static int getPixel(int alpha,int weight) {
		int result=alpha;
		
		result=result<<8;
		result+=weight;
		result=result<<8;
		result+=weight;
		result=result<<8;
		result+=weight;
		return result;	
	}
	public static BufferedImage getScale(BufferedImage bi) {
		BufferedImage bis=new BufferedImage(size,size,bi.getType());
		
		Graphics graphics=bis.createGraphics();
		
		graphics.drawImage(bi, 0, 0,size,size, null);
		
		graphics.dispose();
		
		return bis;
	}
	
	public static double[][] gray(BufferedImage bi) {
		
		int width=bi.getWidth();
		int height=bi.getHeight();
		double[][] pixels=new double[width][height];
		//out.println("Width : "+width+"  Height : "+height);
		int[][] array=new int[width][height];
		for(int i=0;i<array.length;i++) {
			for(int j=0;j<array[i].length;j++) {
				int pixel=bi.getRGB(i, j);
				int r=(pixel>>16)&0xff;
				int g=(pixel>>8)&0xff;
				int b=(pixel)&0xff;
				int weight=(int) (0.3*r+0.59*g+0.11*b);
				pixels[i][j]=weight;
				//if(weight>130)weight=255;
				//else weight=0;
				pixel=getPixel(255,weight);
				
				bi.setRGB(i, j, pixel);
				
			}
		}
		return pixels;
	}
	
	public static double[][] getDCT(double[][] array){
		
		double[] c=new double[size];
		
		for(int i=1;i<c.length;i++) {
			c[i]=1;
		}
		c[0]=1/(Math.sqrt(2.0));
		int N=array.length;
		
		double[][] DCT=new double[N][N];
		
		for(int u=0;u<N;u++) {
			for(int v=0;v<N;v++) {
				double sum=0.0d;
				for(int i=0;i<N;i++) {
					for(int j=0;j<N;j++) {
						sum+=Math.cos(((2*i+1)/(2.0*N))*u*Math.PI)*Math.cos(((2*j+1)/(2.0*N))*v*Math.PI)*array[i][j];
					}
				}
				sum*=(c[u]*c[v]/4.0);
				DCT[u][v]=sum;
			}
		}
		
		
		
		return DCT;
	}
	
	
	public static String getHash(double[][] dct){
		
		double[][] array=new double[8][8];
		double totals=0;
		for(int i=0;i<smallSize;i++) {
			for(int j=0;j<smallSize;j++) {
				totals+=dct[i][j];
			}
		}
		
		totals=totals-dct[0][0];
		
		double avg=totals/((double)(smallSize*smallSize-1));
		
		String pHash="";
		
		for(int i=0;i<smallSize;i++) {
			for(int j=0;j<smallSize;j++) {
				if(i!=0&&i!=0) {
					pHash+=(dct[i][j]>avg?"1":"0");
					
				}
			}
			
		}
		
		
		return pHash;
		
	}
	
	public static int diff(String str1,String str2) {
		int result=0;
		int len=str1.length();
		for(int i=0;i<len;i++) {
			if(str1.charAt(i)!=str2.charAt(i)) {
				result++;
			}
			
		}
		return result;
	}
	
	public static int Compared(String f1,String f2) throws InterruptedException, IOException {
		
		long current=currentTimeMillis();
		File file1=new File(f1);
		File file=new File(f2);
		
		BufferedImage bi1=ImageIO.read(file1);
		BufferedImage bi2=ImageIO.read(file);
		bi1=gray.getScale(bi1);
		bi2=gray.getScale(bi2);
		double[][] ar1=gray.gray(bi1);
		
		ar1=gray.getDCT(ar1);
		
		String str1=gray.getHash(ar1);
		
		double[][] ar2=gray.gray(bi2);
		
		ar2=gray.getDCT(ar2);
		
		String str2=gray.getHash(ar2);
		
		int n=gray.diff(str1, str2);
		System.out.println("汉明距离值："+n);
		return n;
		
	}
	
	
	public static void main(String[]a) throws InterruptedException, IOException {
		
		
		 
		File file=new File("C:\\Users\\22682\\Desktop\\图片测试");
		File[] files=file.listFiles();
		
		
		for(File f:files) {
			int q=0;
			File file2=new File("C:\\Users\\22682\\Desktop\\Piceture");
			File[] files2=file2.listFiles();
			for(File fs:files2) {
				
				int n=gray.Compared(f.getAbsolutePath(), fs.getAbsolutePath());
				if(n<=3) {
					q=1;
					break;
				}
			}
			if(q==0) {
				BufferedImage bi=ImageIO.read(f);
				ImageIO.write(bi, "jpg",new File("C:\\Users\\22682\\Desktop\\Piceture\\"+f.getName()+".jpg"));
			}
			
		
		}
		
		
		
		
	}
	
	
}
