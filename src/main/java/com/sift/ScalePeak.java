 
package com.sift;

 
public class ScalePeak {

    public int       x;         //
    public int       y;         //
    public int       level;     //高斯差分
    public LocalInfo local;     //偏移量

    public static class LocalInfo {
    	//精确极值点，偏移量
        public float fineX;
        public float fineY;
        public float scaleAdjust;
        public float dValue;

        public LocalInfo(float scaleAdjust,float fineX, float fineY){
            this.fineX = fineX;
            this.fineY = fineY;
            this.scaleAdjust = scaleAdjust;
        }
    }
    //似极值点
    public ScalePeak(int x, int y, int level){
        this.x = x;
        this.y = y;
        this.level = level;
    }
}

