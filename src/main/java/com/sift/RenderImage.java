
package com.sift;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.sift.IPixelConverter;
import com.sift.ImagePixelArray;


public class RenderImage {

    private BufferedImage srcImage;

    public RenderImage(BufferedImage srcImage){
        this.srcImage = srcImage;
    }

    public int getWidth() {
        return this.srcImage.getWidth();
    }

    public int getHeight() {
        return this.srcImage.getHeight();
    }

    
    //略缩图
    
    
    public float scaleWithin(int dim) {
        if (this.srcImage.getWidth() <= dim && this.srcImage.getHeight() <= dim) return 1.0f;
        float xScala = (float) dim / this.srcImage.getWidth();
        float yScala = (float) dim / this.srcImage.getHeight();

        float smallestScala = xScala <= yScala ? xScala : yScala; 


        BufferedImage bmScalaed = new BufferedImage((int) (this.srcImage.getWidth() * smallestScala + 0.5),
                                                    (int) (this.srcImage.getHeight() * smallestScala + 0.5),
                                                    BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bmScalaed.createGraphics();
        g.drawImage(this.srcImage, 0, 0, (int) (this.srcImage.getWidth() * smallestScala),
                    (int) (this.srcImage.getHeight() * smallestScala), null);

        this.srcImage = bmScalaed;
        return smallestScala;
    }

    //图片存取成一维数组res，采用均值进行灰度处理
    
    public ImagePixelArray toPixelFloatArray(IPixelConverter converter) {
        int h = this.srcImage.getHeight();
        int w = this.srcImage.getWidth();
        ImagePixelArray res = new ImagePixelArray(w, h);
        int[] pix = srcImage.getRGB(0, 0, w, h, null, 0, w);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int c = pix[x + y * w];
                int R = (c >> 16) & 0xFF;
                int G = (c >> 8) & 0xFF;
                int B = (c >> 0) & 0xFF;
                if (converter == null) res.data[x + y * w] = (R + G + B) / (255.0f * 3.0f);  
                else res.data[x + y * w] = converter.convert(R, G, B);
                res.datas[x+y*w]=(int) res.data[x+y*w];
            }
        }
        return res;
    }

}

