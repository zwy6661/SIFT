 
package com.sift;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.util.List;

import javax.imageio.ImageIO;

import com.sift.ModifiableConst;
import com.sift.SIFT;
import com.sift.KDFeaturePointListInfo;
import com.sift.KDFeaturePointWriter;
import com.sift.RenderImage;
import com.sift.KDFeaturePoint;

public class MakeSiftData {

    static {
        System.setProperty(ModifiableConst._TOWPNTSCALAMINUS, "8.0");
        System.setProperty(ModifiableConst._SLOPEARCSTEP, "5");
        System.setProperty(ModifiableConst._TOWPNTORIENTATIONMINUS, "0.05");

    }
    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        File logoDir = new File("C:\\Users\\22682\\Desktop\\SIFT����\\2.jpg");
        File[] logoFiles = logoDir.listFiles(new FileFilter() {

            public boolean accept(File arg0) {
                return arg0.getName().endsWith(".png");
            }
        });
        int i = 0;
        for (File logoFile : logoFiles) {
            BufferedImage img = ImageIO.read(logoFile);
            RenderImage ri = new RenderImage(img);
            SIFT sift = new SIFT();
            sift.detectFeatures(ri.toPixelFloatArray(null));
            List<KDFeaturePoint> al = sift.getGlobalKDFeaturePoints();
            KDFeaturePointListInfo info = new KDFeaturePointListInfo();
            info.setHeight(img.getHeight());
            info.setWidth(img.getWidth());
            info.setImageFile(logoFile.getName());
            info.setList(al);
            KDFeaturePointWriter.writeComplete("C:\\Users\\22682\\Desktop\\SIFT����\\7.jpg" + logoFile.getName() + ".sift",
                                               info);
            i++;
            System.out.println(i);
            if (i == 100) break;
        }
        System.out.println("total times:" + (System.currentTimeMillis() - start));
    }
}
