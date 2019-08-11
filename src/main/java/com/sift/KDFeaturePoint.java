 
package com.sift;

import com.sift.IKDTreeDomain;

 
public class KDFeaturePoint extends IKDTreeDomain implements Cloneable {
 
    private static final long serialVersionUID = 814942706491557514L;
   
    public float              x, y;
    public float              scale;
    public float              orientation;

    public KDFeaturePoint(){
    }
    
    //KD树关键点描述符
    
    public KDFeaturePoint(FeaturePoint fp){
        if (!fp.hasFeatures) throw (new IllegalArgumentException(
                                                                 "While trying to generate integer "
                                                                         + "vector: source FeaturePoint has no feature vector yet"));
        x = fp.x;
        y = fp.y;
        scale = fp.scale;
        orientation = fp.orientation;
        dim = fp.features.length;
        descriptor = new int[dim];
        for (int d = 0; d < dim; ++d) {
            descriptor[d] = (int) (255.0 * fp.features[d]);
            if (descriptor[d] < 0 || descriptor[d] > 255) {
                throw (new IllegalArgumentException("Resulting integer descriptor k is not 0 <= k <= 255"));
            }
        }
    }

    public KDFeaturePoint clone() {
        KDFeaturePoint other = new KDFeaturePoint();
        other.dim = dim;
        other.x = x;
        other.y = y;
        other.scale = scale;
        other.orientation = orientation;
        other.descriptor = new int[dim];
        System.arraycopy(descriptor, 0, other.descriptor, 0, dim);
        return other;
    }
}

