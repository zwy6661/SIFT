package com.sift;

import java.util.List;

public class PointTree {
	Point point;
	PointTree left;
	PointTree right;
	public static PointTree createPointTree(List<Point> Point,int index) {
		
		if(Point.size()<index) return null;
		PointTree pt=new PointTree();
		pt.point=Point.get(index);
		pt.left=createPointTree(Point, index+1);
		pt.right=createPointTree(Point, index+2);
		return pt;
	}
	
	
	
	
	
}
