package com.sift;




import com.sift.ImagePixelArray;
 
public class FeaturePoint {

    public float          x;
    public float          y;

    public ImagePixelArray image;                   
    public float          imgScale;                
    public float          scale;

    public float          orientation;

    public float[]        features;//特征描述符
    public boolean         hasFeatures = false;

    public int xDim; 
    public int yDim;
    public int oDim;
    public FeaturePoint(){
    }

    public FeaturePoint(ImagePixelArray image, float x, float y, float imgScale, float kfScale, float orientation){
        this.image = image;
        this.x = x;
        this.y = y;
        this.imgScale = imgScale;
        this.scale = kfScale;
        this.orientation = orientation;
    }
    
    public void createVector(int xDim, int yDim, int oDim) {
        this.hasFeatures = true;
        this.xDim = xDim; 
        this.yDim = yDim; 
        this.oDim = oDim; 
        features = new float[yDim * xDim * oDim];
    }
}

