package com.sift;
import static java.lang.System.*;

import java.io.IOException;
import java.util.*;

import com.brief.gray;

public class screening {
	
	
	public static double Screen(List<Match> ms,String f1,String f2) {
		
		List<Point> points=new ArrayList<Point>();
		int index=0;
		KDFeaturePoint p1 =new KDFeaturePoint();
		KDFeaturePoint p2 = new KDFeaturePoint();
		Entry[] dist=new Entry[ms.size()];
		double distance=0;
		for(int i=0;i<ms.size();i++) {
			Point point = new Point();
			Point MapPoint=new Point();
			 dist[i]=new Entry();
			 p1 = ms.get(i).fp1;
			 p2 = ms.get(i).fp2;	
			 distance=Math.sqrt(p1.x*p1.x+p1.y*p1.y);
			 
			 dist[i].dist=distance;
			 dist[i].setX(p1.x);
			 dist[i].setY(p1.y);;
			 MapPoint.setX(p2.x);
			 MapPoint.setY(p2.y);
			 dist[i].setMap(MapPoint);
			 
		}
		index=0;
		for(int i=0;i<dist.length;i++) {
			for(int j=i+1;j<dist.length;j++) {
				if(dist[i].dist>dist[j].dist) {
					Entry temp=dist[i];
					dist[i]=dist[j];
					dist[j]=temp;
				}	
			}
			
		}
		
		
		for(int i=0;i<dist.length;i++) {
			Point point = new Point();
			Point MapPoint=new Point();
			Point next=new Point();
			Point prior=new Point();
			if(i==0) {
				next.setX(dist[i+1].getX());
				next.setY(dist[i+1].getY());
				dist[i].setNext(next);
				prior.setX(dist[i+2].getX());
				prior.setY(dist[i+2].getY());
				dist[i].setPrior(prior);
				MapPoint=dist[i+1].getMap();
				next.x=MapPoint.x;
				next.y=MapPoint.y;
				dist[i].getMap().setNext(next);
				
				MapPoint=dist[i+2].getMap();
				prior.x=MapPoint.x;
				prior.y=MapPoint.y;
				dist[i].getMap().setPrior(prior);
			}else if(i==dist.length-1) {
				next.x=dist[i-1].x;
				next.y=dist[i-1].y;
				dist[i].setNext(next);
				prior.x=dist[i-2].x;
				prior.y=dist[i-2].y;
				dist[i].setPrior(prior);
				MapPoint=dist[i-1].getMap();
				next.x=MapPoint.x;
				next.y=MapPoint.y;
				dist[i].Map.setNext(next);
				MapPoint=dist[i-2].getMap();
				prior.x=MapPoint.x;
				prior.y=MapPoint.y;
				dist[i].Map.setPrior(prior);
			}else {
				next.x=dist[i+1].x;
				next.y=dist[i+1].y;
				dist[i].setNext(next);
				prior.x=dist[i-1].x;
				prior.y=dist[i-1].y;
				dist[i].setPrior(prior);
				MapPoint=dist[i+1].getMap();
				next.x=MapPoint.x;
				next.y=MapPoint.y;
				dist[i].Map.setNext(next);
				MapPoint=dist[i-1].getMap();
				prior.x=MapPoint.x;
				prior.y=MapPoint.y;
				
				dist[i].Map.setPrior(prior);
			}
			//System.out.println(dist[i].x+" : "+dist[i].next.x+"    "+dist[i].y+"  :  "+dist[i].next.y);
			//System.out.println(dist[i].getMap().x+" : "+dist[i].getMap().next.x+"    "+dist[i].getMap().y+"  :  "+dist[i].getMap().next.y);
			dist[i].nextVal=Math.sqrt((dist[i].x-dist[i].next.x)*(dist[i].x-dist[i].next.x)
					+(dist[i].y-dist[i].next.y)*(dist[i].y-dist[i].next.y));
			dist[i].priorVal=Math.sqrt((dist[i].x-dist[i].prior.x)*(dist[i].x-dist[i].prior.x)
					+(dist[i].y-dist[i].prior.y)*(dist[i].y-dist[i].prior.y));
			dist[i].Map.nextVal=Math.sqrt((dist[i].Map.x-dist[i].Map.next.x)*(dist[i].Map.x-dist[i].Map.next.x)
					+(dist[i].Map.y-dist[i].Map.next.y)*(dist[i].Map.y-dist[i].Map.next.y));
			dist[i].Map.priorVal=Math.sqrt((dist[i].Map.x-dist[i].Map.prior.x)*(dist[i].Map.x-dist[i].Map.prior.x)
					+(dist[i].Map.y-dist[i].Map.prior.y)*(dist[i].Map.y-dist[i].Map.prior.y));
			//System.out.println("distNext : "+dist[i].nextVal+" distPrior : "+dist[i].priorVal);
		}
		
		double sum=0.8;
		int tongji=0;
		for(int i=0;i<dist.length;i++) {	
			
			double dist1=Math.abs(dist[i].nextVal-dist[i].Map.nextVal);
			double dist2=Math.abs(dist[i].priorVal-dist[i].Map.priorVal);
			//System.out.println("dist1 : "+dist1+" dist2 : "+dist2);
			dist[i].distance=Math.abs(dist1-dist2);
			if(dist[i].distance<sum) {
				tongji++;
			}
		}
		double rencent=(double)tongji/(double)ms.size();
		
		if(rencent>0.7) {
			int n=0;
			try {
				n=gray.Compared(f1, f2);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(n<=30) {
				return 1;	
			}
			
		}else {
			return 0;
		}
		return 0;
		
	}
	
	

}
