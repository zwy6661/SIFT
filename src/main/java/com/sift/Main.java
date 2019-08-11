
package com.sift;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.sift.ModifiableConst;
import com.sift.KDFeaturePointInfoReader;
import com.sift.KDFeaturePointListInfo;
import com.sift.Match;
import com.sift.MatchKeys;
import com.sift.RenderImage;
import com.sift.ResultList.Info;
import com.sift.KDFeaturePoint;

public class Main {

	static {
		System.setProperty(ModifiableConst._TOWPNTSCALAMINUS, "8.0");
		System.setProperty(ModifiableConst._SLOPEARCSTEP, "5");
		System.setProperty(ModifiableConst._TOWPNTORIENTATIONMINUS, "0.05");

	}
	
	private static int q=13;
	
	public static void drawCre(BufferedImage logo,SIFT sift) throws Exception{
		
		List<FeaturePoint> globalFeaturePoints=sift.getGlobalFeaturePoints();
		
		int lw = logo.getWidth();
		int lh = logo.getHeight();
		
		BufferedImage outputImage = new BufferedImage(lw, lh,
				BufferedImage.TYPE_INT_RGB);

		Graphics2D g = outputImage.createGraphics();
		g.drawImage(logo, 0, 0, lw, lh, null);
		g.setColor(Color.GREEN);
		for (FeaturePoint feature: globalFeaturePoints) {
			g.setColor(Color.RED);
			int x=(int) feature.x;
			int y=(int) feature.y;
			int ox=(int)(2*1/Math.cos(feature.orientation));
			int oy=(int) Math.sqrt(4-ox*ox);
			//System.out.println("方向："+feature.orientation*10);
			//g.drawLine(x, y, (int) ((x+2)-(int)(2*1/Math.cos(feature.orientation*10))), y+2-oy);
			g.drawOval(x, y, 4, 4);
		}
		g.dispose();
		FileOutputStream fos = new FileOutputStream("C:\\Users\\22682\\Desktop\\图片"+q+".jpg");
		q++;
		ImageIO.write(outputImage, "JPEG", fos);
		fos.close();
		
	}
	
	
	
	
	
	
	
	public static void drawImage(BufferedImage logo, BufferedImage model,
			String file, List<Match> ms) throws Exception {
		int lw = logo.getWidth();
		int lh = logo.getHeight();

		int mw = model.getWidth();
		int mh = model.getHeight();

		BufferedImage outputImage = new BufferedImage(lw + mw, lh + mh,
				BufferedImage.TYPE_INT_RGB);

		Graphics2D g = outputImage.createGraphics();
		g.drawImage(logo, 0, 0, lw, lh, null);
		g.drawImage(model, lw, lh, mw, mh, null);
		Color[] color=new Color[10];
		color[0]=Color.red;
		color[1]=Color.cyan;
		color[2]=Color.PINK;
		color[3]=Color.WHITE;
		color[4]=Color.GREEN;
		color[5]=Color.MAGENTA;
		color[6]=Color.BLUE;
		color[7]=Color.black;
		g.setColor(Color.GREEN);
		for (Match m : ms) {
			g.setColor(color[(int)(Math.random()*8)]);
			KDFeaturePoint fromPoint = m.fp1;
			KDFeaturePoint toPoint = m.fp2;
			g.fillOval((int) fromPoint.x, (int) fromPoint.y, 6, 6);
			g.fillOval((int) toPoint.x+ lw, (int) toPoint.y+ lh, 6, 6);
			g.drawLine((int) fromPoint.x, (int) fromPoint.y, (int) toPoint.x
					+ lw, (int) toPoint.y + lh);
		}
		g.dispose();
		FileOutputStream fos = new FileOutputStream(file);
		ImageIO.write(outputImage, "JPEG", fos);
		fos.close();
		System.out.println("匹配特征点数："+ms.size());
	}
	
	
	//批量生成特征点
	
	public static void FilesFeatureRequest(String path,String savepath){
		
		File file=new File(path);
		
		File[] files=file.listFiles();
		
		for(File f:files) {
			
			String name = f.getName();
			String absname=f.getAbsolutePath();
			
			try {
				BufferedImage bi=ImageIO.read(f);
				if(bi.getHeight()<25&&bi.getWidth()<25) {
					continue;
				}
				RenderImage ri=new RenderImage(bi);
				SIFT sift=new SIFT();
				sift.detectFeatures(ri.toPixelFloatArray(null));
				List<KDFeaturePoint> point=sift.getGlobalKDFeaturePoints();
				System.out.println("特征点的数目："+point.size());
				KDFeaturePointListInfo kdli=new KDFeaturePointListInfo();
				kdli.setList(point);
				kdli.setImageFile(f.getName());
				kdli.setWidth(ri.getWidth());
				kdli.setHeight(ri.getHeight());
				String save=savepath+"\\"+f.getName().substring(0, f.getName().lastIndexOf("."))+"KD数据.txt";
				KDFeaturePointWriter.writeComplete(save, kdli);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
		}
		
		
	}
	
	
	//单个文件生成特征点
	
    public static void FileFeatureRequest(String path,String savepath){
		
		File file=new File(path);
		
		
			
			String name = file.getName();
			String absname=file.getAbsolutePath();
			
			try {
				BufferedImage bi=ImageIO.read(file);
				RenderImage ri=new RenderImage(bi);
				SIFT sift=new SIFT();
				sift.detectFeatures(ri.toPixelFloatArray(null));
				List<KDFeaturePoint> point=sift.getGlobalKDFeaturePoints();
				System.out.println("特征点的数目："+point.size());
				KDFeaturePointListInfo kdli=new KDFeaturePointListInfo();
				kdli.setList(point);
				kdli.setImageFile(file.getName());
				kdli.setWidth(ri.getWidth());
				kdli.setHeight(ri.getHeight());
				String save=savepath+"\\"+file.getName().substring(0, file.getName().lastIndexOf("."))+"KD数据.txt";
				KDFeaturePointWriter.writeComplete(save, kdli);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
		
		
		
	}
	
	
	
	//两张图片图片对比


	public static void contrast_Two(String path,String path2,String resultpath) throws IOException{
		
		File f1=new File(path);
		
		File f2=new File(path2);
		
		BufferedImage bi1=ImageIO.read(f1);
		BufferedImage bi2=ImageIO.read(f2);
		
		RenderImage ri1=new RenderImage(bi1);
		
		RenderImage ri2=new RenderImage(bi2);
		
		SIFT sift=new SIFT();
		
		SIFT sift2=new SIFT();
		
		sift.detectFeatures(ri1.toPixelFloatArray(null));
		
		sift2.detectFeatures(ri2.toPixelFloatArray(null));
		
		List<KDFeaturePoint> p1=sift.getGlobalKDFeaturePoints();
		
		List<KDFeaturePoint> p2=sift2.getGlobalKDFeaturePoints();
		System.out.println(f1.getName()+"  特征点数目 ："+p2.size());
		System.out.println(f2.getName()+"  特征点数目 ："+p1.size());
		List<Match> ms=MatchKeys.findMatchesBBF(p1, p2);
		
		ms=MatchKeys.filterMore(ms);
		
		try {
			drawImage(bi1,bi2,resultpath,ms);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//与库中图片特征点进行比对
	
	
	public static void ExistPic(String path,String imgDao,String dao,String savepath) throws Exception {
		File fl=new File(path);
		BufferedImage img = ImageIO.read(fl);
		RenderImage ri=new RenderImage(img);
	    SIFT sift=new SIFT();
		sift.detectFeatures(ri.toPixelFloatArray(null));
		
		KDFeaturePointInfoReader reader=new KDFeaturePointInfoReader();
		KDFeaturePointListInfo kdli=new KDFeaturePointListInfo();
		List<KDFeaturePoint> al =sift.getGlobalKDFeaturePoints();
		List<KDFeaturePoint> al1 ;
		File file=new File(dao);
		
		File[] files=file.listFiles();
		int t=0;
		int i=0;
		for(File f:files) {
			
			kdli=reader.readComplete(f.getAbsolutePath());
			
			al1=kdli.getList();
			
			List<Match> ms = MatchKeys.findMatchesBBF(al1, al);
			ms = MatchKeys.filterMore(ms);
			double screen=0;
			String name1=imgDao+"\\"+f.getName().substring(0, f.getName().lastIndexOf("K"))+".jpg";
			String name2=path;
			if(ms.size()>=3) {
			
			screen=screening.Screen(ms,name1,name2);
			//System.out.println(f.getAbsolutePath()+"  轮廓差值： "+screen);
			//System.out.println();
			}
			float result=0.0f;
			float n_1=al.size();
			float n_2=al1.size();
			float n_3=ms.size();
			
			
			//特征点占比
			
			float result_1=n_3/n_1;
			float result_2=n_3/n_2;
			
			
			
			result=(float) ((result_1+result_2)/2.0);
			
			if(result_1>=0.8&&result_2>=0.8&&screen==1) {
				Info info=new ResultList().new Info();
				info.setFile(f.getName());
				info.setSample(100);
				info.setName(f.getName());
				//ResultList.result.set(ResultList.index, info);
				ResultList.index++;
			}else {

				if(((result_1>=0.6)||result_2>=0.6)&&n_3>=3&&screen==1) {
					result=1;
					Info info=new ResultList().new Info();
					info.setFile(f.getName());
					info.setSample(100);
					info.setName(f.getName());
					//ResultList.result.set(ResultList.index, info);
					ResultList.index++;
				}
				if(screen==1) {
					result=1;
					Info info=new ResultList().new Info();
					info.setFile(f.getName());
					info.setSample(50);
					info.setName(f.getName());
					//ResultList.result.set(ResultList.index, info);
					ResultList.index++;
				}
				
			}
			if(result==1&&ms.size()>=3&&screen==1) {
					
					BufferedImage img1=ImageIO.read(new File(imgDao+"\\"+f.getName().substring(0, f.getName().lastIndexOf("K"))+".jpg"));
					
					drawImage(img1, img, savepath+"\\匹配结果"+(t++)+".jpg", ms);
					System.out.println(name1+" : "+name2);
					System.out.println("匹配 ： "+ms.size());
			}
				
			//System.out.println();
			i++;
		}
	
		

	}
	
}
