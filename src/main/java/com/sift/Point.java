package com.sift;

public class Point extends PointDomain{
	int index;
	Point Map;
	Point next;
	Point prior;
	boolean used=false;
	double nextVal;
	double priorVal;
	
	public double getNextVal() {
		return nextVal;
	}
	public void setNextVal(double nextVal) {
		this.nextVal = nextVal;
	}
	public double getPriorVal() {
		return priorVal;
	}
	public void setPriorVal(double priorVal) {
		this.priorVal = priorVal;
	}
	public boolean isUsed() {
		return used;
	}
	public void setUsed(boolean used) {
		this.used = used;
	}
	public float getX() {
		return x;
	}
	public Point getMap() {
		return Map;
	}
	public void setMap(Point map) {
		Map = map;
	}
	public void setX(float x) {
		this.x = x;
	}
	public float getY() {
		return y;
	}
	public void setY(float y) {
		this.y = y;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public Point getNext() {
		return next;
	}
	public void setNext(Point next) {
		this.next = next;
	}
	public Point getPrior() {
		return prior;
	}
	public void setPrior(Point prior) {
		this.prior = prior;
	}
	
	
	
}
