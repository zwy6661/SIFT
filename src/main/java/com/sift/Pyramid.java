 
package com.sift;

import java.io.IOException;
import java.util.ArrayList;

import com.sift.ImagePixelArray;

 
public class Pyramid {

    public ArrayList<OctaveSpace> octaves;  

    //OctaveSpace为尺度空间集合，即每组中高斯金字塔以及高斯差分金字塔的集合
    
    
    public int buildOctaves(ImagePixelArray source, float scale, int levelsPerOctave, float octaveSigm, int minSize) {
        this.octaves = new ArrayList<OctaveSpace>();
        OctaveSpace downSpace = null;
        ImagePixelArray prev = source;

        //开始高斯金字塔以及高斯差分金字塔
        
        while (prev != null && prev.width >= minSize && prev.height >= minSize) {
            OctaveSpace osp = new OctaveSpace();

            
            osp.makeGaussianImgs(prev, scale, levelsPerOctave, octaveSigm);//高斯金字塔
            osp.makeGaussianDiffImgs();//高斯差分金字塔
            octaves.add(osp);//添加到尺度空间
            prev = osp.getLastGaussianImg().halved();
            if (downSpace != null) downSpace.up = osp;
            osp.down = downSpace;
            downSpace = osp;
            scale *= 2.0;
        }
        /*try {
			DrawImage.drawImage(octaves);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
        return (octaves.size());
    }
}

