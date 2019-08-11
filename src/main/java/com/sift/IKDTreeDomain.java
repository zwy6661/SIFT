 
package com.sift;

import java.io.Serializable;

 
public abstract class IKDTreeDomain implements Serializable{
 
    private static final long serialVersionUID = -6956627943184526276L;
    public int   dim;   //维数
    public int[] descriptor;   //各维方向值
}

