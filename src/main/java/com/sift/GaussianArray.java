 
package com.sift;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.sift.ImagePixelArray;

 
public class GaussianArray {

    private static final Logger log = Logger.getLogger(GaussianArray.class);

    private static Map<Float, float[]> cachedMask = new HashMap<Float, float[]>();
    static {
        float[] sigmaVal = { 1.5f, 1.2262735f, 1.5450078f, 1.9465879f, 2.452547f, 3.0900156f };
        for (int i = 0; i < sigmaVal.length; i++) {
            float[] mask = makeMask(sigmaVal[i], 1 + 2 * ((int) (3.0f * sigmaVal[i])));
            cachedMask.put(sigmaVal[i], mask);
        }
    }
    public float[] mask;

    public GaussianArray(float sigma){
        if (cachedMask.containsKey(sigma)) {
            this.mask = cachedMask.get(sigma);
        } else {
            this.mask = makeMask(sigma, 1 + 2 * ((int) (3.0f * sigma)));
            log.info("重定义,sigma = " + sigma);
        }

    }

     
    public static float[] makeMask(float sigma, int dim) {
         
        dim |= 1;  
        float[] mask = new float[dim];
        float sigma2sq = 2 * sigma * sigma;
        float normalizeFactor = 1.0f / ((float) Math.sqrt(2.0 * Math.PI) * sigma);

        for (int i = 0; i < dim; i++) {
            int relPos = i - mask.length / 2;
            float G = (relPos * relPos) / sigma2sq;
            G = (float) Math.exp(-G);
            G *= normalizeFactor;
            mask[i] = G;
        }
        return mask;
    }

    public ImagePixelArray convolve(ImagePixelArray map) {
        return Filter.convolve(map, this.mask);
    }

    private static class Filter {

        public enum Direction {
            VERTICAL, HORIZONTAL
        }

        public static ImagePixelArray convolve(ImagePixelArray img, float[] mask) {

            ImagePixelArray im1 = new ImagePixelArray(img.width, img.height);
            ImagePixelArray im2 = new ImagePixelArray(img.width, img.height);
            convolve1D(im1, mask, img, Direction.VERTICAL);
            convolve1D(im2, mask, im1, Direction.HORIZONTAL);
            return im2;
        }

        public static void convolve1D(ImagePixelArray dest, float[] mask, ImagePixelArray src, Direction dir) {
            int maxN;  
            int maxP;  

            if (dir == Direction.VERTICAL) {
                maxN = src.width;
                maxP = src.height;
            } else if (dir == Direction.HORIZONTAL) {
                maxN = src.height;
                maxP = src.width;
            } else {
                throw new java.lang.IllegalArgumentException("invalid direction");
            }

            for (int n = 0; n < maxN; n++) {
                for (int p = 0; p < maxP; p++) {
                    float val = calculateConvolutionValue1D(src, mask, n, p, maxN, maxP, dir);
                    if (dir == Direction.VERTICAL) {dest.data[n + p * dest.width] = val;
                    dest.datas[n + p * dest.width]=(int) dest.data[n + p * dest.width];}
                    else { dest.data[p + n * dest.width] = val;
                    dest.datas[p + n * dest.width]=(int) dest.data[p + n * dest.width];}
                    
                    
                }
            }

        }

        private static float calculateConvolutionValue1D(ImagePixelArray src, float[] mask, int n, int p, int maxN,
                                                         int maxP, Direction dir) {
            float sum = 0.0f;
            boolean isOut = false;
            float outBound = 0.0f;
            for (int i = 0; i < mask.length; i++) {
                int curAbsP = i - (mask.length / 2) + p;
                if (curAbsP < 0 || curAbsP >= maxP) {
                    isOut = true;
                    outBound += mask[i];
                    continue;
                }
                if (dir == Direction.VERTICAL) sum += (mask[i] * src.data[curAbsP * src.width + n]);
                else sum += mask[i] * src.data[n * src.width + curAbsP];
            }
            if (isOut) sum *= 1.0 / (1.0 - outBound);
            return sum;
        }
    }
}

