 
package com.sift;

 
public class ImagePixelArray extends FloatArray {

   
    public int width;
    public int height;

    public ImagePixelArray(int w, int h){
        this(null, w, h);
    }

    //图片像素数据数组
    
    public ImagePixelArray(float[] d, int w, int h){
        this.data = (d == null) ? new float[w * h] : d;
        this.datas= (int[]) ((d==null)? new int[w*h] : d); 
        this.width = w;
        this.height = h;
    }

 
    //clone数组备份
    
    public ImagePixelArray clone() {
        ImagePixelArray other = new ImagePixelArray(this.width, this.height);
        System.arraycopy(this.data, 0, other.data, 0, this.data.length);
        return other;
    }

    //图片初始化线性插值扩大一倍，针对特征点较少的图片
    
    public ImagePixelArray doubled() {
        if (this.width <= 2 || this.height <= 2) return null;
        int nw = this.width * 2 - 2;
        int nh = this.height * 2 - 2;
        ImagePixelArray db = new ImagePixelArray(nw, nh);
        for (int y = 0; y < this.height - 1; y++) {
            for (int x = 0; x < this.width - 1; x++) {
                db.data[2 * (x + y * nw)] = this.data[y * this.width + x]; 

                db.data[2 * (x + y * nw) + nw] = (this.data[x + y * this.width] + this.data[x + (y + 1) * this.width]) / 2.0f;  

                db.data[2 * (x + y * nw) + 1] = (this.data[x + y * this.width] + this.data[x + y * this.width + 1]) / 2.0f;  

                db.data[2 * (x + y * nw) + nw + 1] = (this.data[x + y * this.width]
                                                      + this.data[x + (y + 1) * this.width]  
                                                      + this.data[x + y * this.width + 1]   
                + this.data[x + (y + 1) * this.width + 1]) / 4.0f; 
            }
        }
        return db;
    }

    
    //降采样，即取宽高二分之一的图像
    
    public ImagePixelArray halved() {
        if (this.width / 2 == 0 || this.height / 2 == 0) return null;
        int nw = this.width / 2;
        int nh = this.height / 2;
        ImagePixelArray half = new ImagePixelArray(nw, nh);
        for (int y = 0; y < nh; y++) {
            for (int x = 0; x < nw; x++) {
                half.data[x + nw * y] = this.data[2 * (x + y * this.width)];
       
            }
        }
        return half;
    }

   
    //高斯金字塔进行相邻层相减，构建高斯差分金字塔
    
    public static ImagePixelArray minus(ImagePixelArray img1, ImagePixelArray img2) {
        if (img2.width != img1.width || img2.height != img1.height) {
            throw new IllegalArgumentException("Mismatching dimensions.");
        }

        ImagePixelArray min = new ImagePixelArray(img1.width, img1.height);
 
        for (int y = 0; y < min.height; y++) {
            for (int x = 0; x < min.width; x++) {
                min.data[x + y * min.width] = img1.data[x + y * img1.width] - img2.data[x + y * img1.width];
                min.datas[x+y*min.width]=(int) min.data[x+y*min.width];
            }
        }
        return min;
    }
}

