package com.sift;

import java.io.File;
import java.io.IOException;

public class Compared_Enter {
	
	private String path;
	
	private String savepath;
	private int type;
	
	public static void main(String[] argv) {
		
		String path2="C:\\Users\\22682\\Desktop\\图片测试\\wszz201804013.pdf_3.jpg";
		String path3="C:\\Users\\22682\\Desktop\\图片测试特征点文件";
		String path4="D:\\testmaven\\照片1特征点";
		String path  ="C:\\Users\\22682\\Desktop\\图片测试";
		String resultpath="C:\\Users\\22682\\Desktop\\结果7";
		File file=new File(resultpath);
		
		if(!file.exists()) {
			
			file.mkdirs();
		}
		
		try {
			Main.ExistPic(path2, path, path3, resultpath);
			//Main.FilesFeatureRequest(path, resultpath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
	}
	
}
