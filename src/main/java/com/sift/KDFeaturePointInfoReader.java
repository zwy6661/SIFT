 
package com.sift;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.sift.KDFeaturePoint;
 
public class KDFeaturePointInfoReader {

    private final static Logger logger = Logger.getLogger(KDFeaturePointInfoReader.class);

    public static KDFeaturePointListInfo readComplete(String filePath) {

        ObjectInputStream fis = null;
        try {
            File f = new File(filePath);
            fis = new ObjectInputStream(new FileInputStream(f));
            int count = fis.readInt();
            if (count == 0) return null;
            List<KDFeaturePoint> al = new ArrayList<KDFeaturePoint>();
            for (int i = 0; i < count; i++) {
                KDFeaturePoint kp = (KDFeaturePoint) fis.readObject();
                al.add(kp);
            }
            int w = fis.readInt();
            int h = fis.readInt();
            KDFeaturePointListInfo kfl = new KDFeaturePointListInfo();
            kfl.setImageFile(f.getName());
            kfl.setList(al);
            kfl.setWidth(w);
            kfl.setHeight(h);
            return kfl;
        } catch (Exception e) {
            e.printStackTrace();
            logger.equals(e.getMessage());
            return null;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    logger.equals(e.getMessage());
                }
            }
        }

    }

    public static KDFeaturePointListInfo readComplete(ObjectInputStream fis, String name) {

        try {
            int count = fis.readInt();
            if (count == 0) return null;
            List<KDFeaturePoint> al = new ArrayList<KDFeaturePoint>();
            for (int i = 0; i < count; i++) {
                KDFeaturePoint kp = (KDFeaturePoint) fis.readObject();
                al.add(kp);
            }
            int w = fis.readInt();
            int h = fis.readInt();
            KDFeaturePointListInfo kfl = new KDFeaturePointListInfo();
            kfl.setImageFile(name);
            kfl.setList(al);
            kfl.setWidth(w);
            kfl.setHeight(h);
            return kfl;
        } catch (Exception e) {
            logger.equals(e.getMessage());
            return null;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    logger.equals(e.getMessage());
                }
            }
        }
    }
}

