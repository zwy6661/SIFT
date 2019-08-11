 
package com.sift;

import java.util.ArrayList;
import java.util.List;

import com.sift.FeaturePoint;
import com.sift.GaussianArray;
import com.sift.KDFeaturePoint;
import com.sift.OctaveSpace;
import com.sift.Pyramid;
import com.sift.ScalePeak;

 
public class SIFT {

 
    float preprocSigma             = 1.5f;    //论文中建议第一层的sigma 1.52 根据相机焦点
    float octaveSigma              = 1.56f;    //Lowe论文建议sigma为1.6
    int    minimumRequiredPixelsize = 32;     //图片最小尺寸
    int    scaleSpaceLevels         = 3;      //需要提取几张照片里的特征
    float dogThresh                = 0.0075f;  //寻找极值点时阈值筛选 abs(pix)>0.5*T/n; T建议取0.04；
    float dValueLowThresh          = 0.008f;  //极值点精确定位时进行的阈值筛选 value>T / n
                                              
    float maximumEdgeRatio         = 10.0f;   //海森矩阵去除边缘效应对比值 拉普他建议取10 
    float scaleAdjustThresh        = 0.50f;   
    float peakRelThresh            = 0.8f;   
    int    relocationMaximum        = 4;  //迭代次数

    //进入特征点提取函数
    
    public int detectFeatures(ImagePixelArray img) {
    	if(img.width>1500||img.height>1500) {
    		return (detectFeaturesDownscaled(img, 0, 1.0f));
    	}else {
    		
    		return (detectFeaturesDownscaled(img, -1, 1.0f));
    	}
    	
        
    }

    //开始特征提取
    public int detectFeaturesDownscaled(ImagePixelArray img, int preProcessMark, float startScale) {
    	//传入初始标记小于0，则需要线性插值，将原图像扩大一倍
        if (preProcessMark < 0) {
            img = img.doubled();
            startScale *= 0.5; //线性插值后初始尺度为0.5
            /*if(img.width<minimumRequiredPixelsize||img.height<minimumRequiredPixelsize) {
            	img = img.doubled();
                startScale *= 0.5;
            }*/
        } else if (preProcessMark > 0) {
            while (img.width > preProcessMark || img.height > preProcessMark) {
                img = img.halved();  //值大于0 进行降采样，将图像缩小一倍
                startScale *= 2.0; //初始尺度为2
            }
        }
        
        //对图像进行原始的高斯平滑 sigma取1.52
        
        if (preprocSigma > 0.0) {
            GaussianArray gaussianPre = new GaussianArray(preprocSigma);
            img = gaussianPre.convolve(img);   //二次一维高斯核卷积
        }

        //开始构造高斯差分金字塔
        //传入参数 img数组 开始尺度1.0|0.5|2  需要提取几张图片特征 需要的σ效果    要求图像的最小尺寸
        Pyramid pyr = new Pyramid();
        pyr.buildOctaves(img, startScale, scaleSpaceLevels, octaveSigma, minimumRequiredPixelsize);
        
        globalFeaturePoints = new ArrayList<FeaturePoint>();
       
        for (int on = 0; on < pyr.octaves.size(); ++on) {
            OctaveSpace osp = pyr.octaves.get(on);

            ArrayList<ScalePeak> peaks = osp.findPeaks(dogThresh); 
            ArrayList<ScalePeak> peaksFilted = osp.filterAndLocalizePeaks(peaks, maximumEdgeRatio, dValueLowThresh,
                                                                          scaleAdjustThresh, relocationMaximum);

       
            osp.pretreatMagnitudeAndDirectionImgs();
            ArrayList<FeaturePoint> faturePoints = osp.makeFeaturePoints(peaksFilted, peakRelThresh, scaleSpaceLevels,
                                                                         octaveSigma);
            osp.clear();
            globalFeaturePoints.addAll(faturePoints);
            
        }
        System.out.println("带方向极值点个数："+globalFeaturePoints.size());
        return (globalFeaturePoints.size());

    }
    
    private List<FeaturePoint> globalFeaturePoints;
    private List<KDFeaturePoint> globalKDFeaturePoints;
    
    public List<FeaturePoint> getGlobalFeaturePoints(){
    	return globalFeaturePoints;
    }
    
    public List<KDFeaturePoint> getGlobalKDFeaturePoints() {

        if (globalKDFeaturePoints != null) return (globalKDFeaturePoints);
        if (globalFeaturePoints == null) throw (new IllegalArgumentException("No featurePoints generated yet."));
        globalKDFeaturePoints = new ArrayList<KDFeaturePoint>();
        for (FeaturePoint fp : globalFeaturePoints) {
            globalKDFeaturePoints.add(new KDFeaturePoint(fp));
        }
        return globalKDFeaturePoints;
    }
}

