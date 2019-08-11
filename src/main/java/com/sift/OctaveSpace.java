 
package com.sift;

import java.util.ArrayList;

import com.sift.RefFloat;
import com.sift.FloatArray;
import com.sift.ImagePixelArray;
import com.sift.ScalePeak.LocalInfo;

 
public class OctaveSpace {

	OctaveSpace down;  //下层
	OctaveSpace up;   //上层
	ImagePixelArray baseImg;  //基础图像
	public float baseScale;  //初始尺度
 	public ImagePixelArray[] smoothedImgs;  //高斯塔
	public ImagePixelArray[] diffImags;  //差分金字塔

	private ImagePixelArray[] magnitudes;  //梯度幅值
	private ImagePixelArray[] directions;  //方向

	//得到上一组中倒数第二张图片 由于我们作完高斯塔以及后续进行提取特征点的操作 倒数第一层，以及倒数第二层
	//会被舍弃，故倒数第三层与倒数第二层的sigma最为接近 因此选取倒数第二层作为下一组的第一层
	public ImagePixelArray getLastGaussianImg() {
		if (this.smoothedImgs.length < 2) {
			throw new java.lang.IllegalArgumentException(
					"err: too few gaussian maps.");
		}
		return (this.smoothedImgs[this.smoothedImgs.length - 2]);
	}

	//高斯金字塔
	
	public void makeGaussianImgs(ImagePixelArray base, float baseScale,
			int scales, float sigma) {

		 
		smoothedImgs = new ImagePixelArray[scales + 3];
		 //开始尺度
		this.baseScale = baseScale;
		//开始像素数组
		
		ImagePixelArray prev = base;
		//第一层数据
		this.baseImg=base;
		smoothedImgs[0] = base;

		//sigma
		float w = sigma;
		//K值  每组中各层高斯金字塔的卷积程度为 Math.pow(k,n-1)*sigma;n为层数
		//k=Math.pow(2,1/Layer),layer=scaleSpaceLevels;
		//第二层为K*sigma sigma为相机尺度Math.sqrt((1.6*1.6)-0.5*0.5);
		float kTerm = (float) Math.sqrt(Math.pow(Math.pow(2.0, 1.0 / scales),
				2.0)-1.3);
		//float kTerm=(float) Math.pow(2.0, 1.0 / scales);
		//进行卷积
		for (int i = 1; i < smoothedImgs.length; i++) {
			GaussianArray gauss = new GaussianArray(w * kTerm);
			prev = smoothedImgs[i] = gauss.convolve(prev);
			w *= Math.pow(2.0, 1.0 / scales);
		}
	}
	//构建高斯差分金字塔
	public void makeGaussianDiffImgs() {
	 

		diffImags = new ImagePixelArray[smoothedImgs.length - 1];
		for (int sn = 0; sn < diffImags.length; sn++) {
			diffImags[sn] = ImagePixelArray.minus(smoothedImgs[sn + 1],
					smoothedImgs[sn]);
		}
	}

	//查找似极值点，在高斯金字塔
	
	public ArrayList<ScalePeak> findPeaks(float dogThresh) {

		ArrayList<ScalePeak> peaks = new ArrayList<ScalePeak>();

		ImagePixelArray current, above, below;

		 
		for (int level = 1; level < (this.diffImags.length - 1); level++) {
			current = this.diffImags[level];
			below = this.diffImags[level - 1];
			above = this.diffImags[level + 1];
			peaks.addAll(findPeaks4ThreeLayer(below, current, above, level,
					dogThresh));
			below = current;
		}

		return (peaks);
	}

	 

	//似极值点的筛选，去除不符合条件的点及边缘效应点，进行迭代偏移
	public ArrayList<ScalePeak> filterAndLocalizePeaks(
			ArrayList<ScalePeak> peaks, float maximumEdgeRatio,
			float dValueLowThresh, float scaleAdjustThresh,
			int relocationMaximum) {
		ArrayList<ScalePeak> filtered = new ArrayList<ScalePeak>();
		int[][] processedMap = new int[this.diffImags[0].width][this.diffImags[0].height];
		for (ScalePeak peak : peaks) {

			 //边缘效应
			if (isTooEdgelike(diffImags[peak.level], peak.x, peak.y,
					maximumEdgeRatio))
				continue;
			//迭代求精确值
			if (localizeIsWeak(peak, relocationMaximum, processedMap))
				continue;

			if (Math.abs(peak.local.scaleAdjust) > scaleAdjustThresh)
				continue;

			if (Math.abs(peak.local.dValue) <= dValueLowThresh)
				continue;

			filtered.add(peak);
		}
		return filtered;
	}

 
	//计算每个点的梯度幅值公式m点左边下x,y =Math.Pow(((x+1,y)-(x-1,y))*((x+1,y)-(x-1,y))+
	//((x,y+1)-(x,y-1))*((x,y+1)-(x,y-1)))
	//梯度方向 degree =arctan(((x,y+1)-(x,y-1))/((x+1,y)-(x-1.y)))
	//注意似极值点是在高斯差分金字塔上找，而每个像素点的梯度幅值以及梯度方向是在高斯金字塔图像上计算，
	public void pretreatMagnitudeAndDirectionImgs() {

		magnitudes = new ImagePixelArray[this.smoothedImgs.length - 1]; 
		directions = new ImagePixelArray[this.smoothedImgs.length - 1]; 
		for (int s = 1; s < (this.smoothedImgs.length - 1); s++) {
			magnitudes[s] = new ImagePixelArray(this.smoothedImgs[s].width,
					this.smoothedImgs[s].height);
			directions[s] = new ImagePixelArray(this.smoothedImgs[s].width,
					this.smoothedImgs[s].height);
			int w = smoothedImgs[s].width;
			int h = smoothedImgs[s].height;
			for (int y = 1; y < (h - 1); ++y) {
				for (int x = 1; x < (w - 1); ++x) {
					magnitudes[s].data[y * w + x] = (float) Math
							.sqrt(Math
									.pow(smoothedImgs[s].data[y * w + x + 1]
											- smoothedImgs[s].data[y * w + x
													- 1], 2.0f)
									+ Math.pow(smoothedImgs[s].data[(y + 1) * w
											+ x]
											- smoothedImgs[s].data[(y - 1) * w
													+ x], 2.0f));

					directions[s].data[y * w + x] = (float) Math.atan2(
							smoothedImgs[s].data[(y + 1) * w + x]
									- smoothedImgs[s].data[(y - 1) * w + x],
							smoothedImgs[s].data[y * w + x + 1]
									- smoothedImgs[s].data[y * w + x - 1]);
				}
			}
		}
	}

	//查找极值点，在高斯金字塔上
	
	
	public ArrayList<FeaturePoint> makeFeaturePoints(
			ArrayList<ScalePeak> localizedPeaks, float peakRelThresh,
			int scaleCount, float octaveSigma) {
		ArrayList<FeaturePoint> featurePoints = new ArrayList<FeaturePoint>();
		for (ScalePeak sp : localizedPeaks) {
			ArrayList<FeaturePoint> thisPointKeys = makeFeaturePoint(
					this.baseScale, sp, peakRelThresh, scaleCount, octaveSigma);
			
			//创建关键点描述符
			//2.0，4，8，0.2为求描述子半径的参数
			thisPointKeys = createDescriptors(thisPointKeys,
					magnitudes[sp.level], directions[sp.level], 2.0f, 4, 8,
					0.2f);
			for (FeaturePoint fp : thisPointKeys) {
				if (!fp.hasFeatures) {
					throw new java.lang.IllegalStateException(
							"should not happen");
				}

				fp.x *= fp.imgScale;
				fp.y *= fp.imgScale;
				fp.scale *= fp.imgScale;
				featurePoints.add(fp);
			}
		}
		return featurePoints;
	}

	public void clear() {
		for (int i = 0; i < this.magnitudes.length; i++)
			this.magnitudes[i] = null;
		for (int i = 0; i < this.directions.length; i++)
			this.directions[i] = null;
		magnitudes = directions = null;
	}
	
	//极值点的方向

	private ArrayList<FeaturePoint> makeFeaturePoint(float imgScale,
			ScalePeak point, float peakRelThresh, int scaleCount,
			float octaveSigma) {

		//该点所在的尺度空间，最接近的高斯金字塔层 高斯差分金字塔层加上偏移量
		float fpScale = (float) (octaveSigma * Math.pow(2.0,
				(point.level + point.local.scaleAdjust) / scaleCount));

	 
		//尺度空间进行采样 建议为3*sigma
		float sigma = 3.0f * fpScale;
		int radius = (int) (3.0 * sigma / 2.0 + 0.5);
		int radiusSq = radius * radius;

		ImagePixelArray magnitude = magnitudes[point.level];
		ImagePixelArray direction = directions[point.level];
		 
		int xMin = Math.max(point.x - radius, 1);
		int xMax = Math.min(point.x + radius, magnitude.width - 1);
		int yMin = Math.max(point.y - radius, 1);
		int yMax = Math.min(point.y + radius, magnitude.height - 1);

		// G(r) = e^{-\frac{r^2}{2 \sigma^2}}
		float gaussianSigmaFactor = 2.0f * sigma * sigma;

		float[] boxes = new float[36];  
		//进行高斯核权重相加梯度幅值并计算主方向
		for (int y = yMin; y < yMax; ++y) {
			for (int x = xMin; x < xMax; ++x) {
				int relX = x - point.x; 
				int relY = y - point.y; 
				if (relX * relX + relY * relY > radiusSq)
					continue;  

				float gaussianWeight = (float) Math.exp(-((relX * relX + relY
						* relY) / gaussianSigmaFactor));

				 
				int boxIdx = findClosestRotationBox(direction.data[y
						* direction.width + x]);

				boxes[boxIdx] += magnitude.data[y * magnitude.width + x]
						* gaussianWeight;
			}
		}
		
		
		//取附近幅值的均值
		
		averageBoxes(boxes);
		//找出最大幅值为主方向
		float maxGrad = 0.0f;
		int maxBox = 0;
		for (int b = 0; b < 36; ++b) {
			if (boxes[b] > maxGrad) {
				maxGrad = boxes[b];
				maxBox = b;
			}
		}

		RefPeakValueAndDegreeCorrection ref1 = new RefPeakValueAndDegreeCorrection();
		interpolateOrientation(boxes[maxBox == 0 ? (36 - 1) : (maxBox - 1)],
				boxes[maxBox], boxes[(maxBox + 1) % 36], ref1);

		 //大于主方向梯度幅值80%为辅方向
		boolean[] boxIsFeaturePoint = new boolean[36];
		for (int b = 0; b < 36; ++b) {
			boxIsFeaturePoint[b] = false;
			if (b == maxBox) {
				boxIsFeaturePoint[b] = true;
				continue;
			}
			if (boxes[b] < (peakRelThresh * ref1.peakValue))
				continue;
			int leftI = (b == 0) ? (36 - 1) : (b - 1);
			int rightI = (b + 1) % 36;
			if (boxes[b] <= boxes[leftI] || boxes[b] <= boxes[rightI])
				continue;  
			boxIsFeaturePoint[b] = true;
		}

		ArrayList<FeaturePoint> featurePoints = new ArrayList<FeaturePoint>();

		float oneBoxRad = (float) (2.0f * Math.PI) / 36;

		for (int b = 0; b < 36; ++b) {
			if (boxIsFeaturePoint[b] == false)
				continue;

			int bLeft = (b == 0) ? (36 - 1) : (b - 1);
			int bRight = (b + 1) % 36;

			RefPeakValueAndDegreeCorrection ref2 = new RefPeakValueAndDegreeCorrection();

			if (interpolateOrientation(boxes[bLeft], boxes[b], boxes[bRight],
					ref2) == false) {
				throw (new java.lang.IllegalStateException(
						"BUG: Parabola fitting broken"));
			}
			float degree = (float) ((b + ref2.degreeCorrection) * oneBoxRad - Math.PI);
			 
			//对角度进行正值；
			
			if (degree < -Math.PI)
				degree += 2.0 * Math.PI;
			else if (degree > Math.PI)
				degree -= 2.0 * Math.PI;
			
			//精确极值点信息存入

			FeaturePoint fp = new FeaturePoint(this.smoothedImgs[point.level],
					point.x + point.local.fineX, point.y + point.local.fineY,
					imgScale, fpScale, degree);
			featurePoints.add(fp);
		}
		return (featurePoints);
	}

	//判断角度偏移量
	
	private boolean interpolateOrientation(float left, float middle,
			float right, RefPeakValueAndDegreeCorrection ref) {
		float a = ((left + right) - 2.0f * middle) / 2.0f;
		ref.degreeCorrection = ref.peakValue = Float.NaN;
		if (a == 0.0)
			return false;
		float c = (((left - middle) / a) - 1.0f) / 2.0f;
		float b = middle - c * c * a;

		if (c < -0.5 || c > 0.5)
			throw (new IllegalStateException(
					"InterpolateOrientation: off peak ]-0.5 ; 0.5["));
		ref.degreeCorrection = c;
		ref.peakValue = b;
		return true;
	}

	//取相邻幅值的均值，进行插值处理，提高精度
	private void averageBoxes(float[] boxes) {
		 
		for (int sn = 0; sn < 4; ++sn) {
			float first = boxes[0];
			float last = boxes[boxes.length - 1];

			for (int sw = 0; sw < boxes.length; ++sw) {
				float cur = boxes[sw];
				float next = (sw == (boxes.length - 1)) ? first
						: boxes[(sw + 1) % boxes.length];

				boxes[sw] = (last + cur + next) / 3.0f;
				last = cur;
			}
		}
	}

	private int findClosestRotationBox(float angle) {
		angle += Math.PI;
		angle /= 2.0f * Math.PI;
		angle *= 36;
		int idx = (int) angle;
		if (idx == 36)
			idx = 0;
		return idx;
	}

	//构建关键点描述符
	
	private ArrayList<FeaturePoint> createDescriptors(
			ArrayList<FeaturePoint> featurePoints, ImagePixelArray magnitude,
			ImagePixelArray direction, float considerScaleFactor, int descDim,
			int directionCount, float fvGradHicap) {

		if (featurePoints.size() <= 0)
			return (featurePoints);
		//2*sigma
		considerScaleFactor *= featurePoints.get(0).scale;
		float dDim05 = ((float) descDim) / 2.0f;
		
		//关键点的描述确认描述子所需的图像区域
		//公式(consideScaleFactor*sigma*根号2*(d-1)-1)/2;d半径取4；d=descDim
		//directionCount为描述子方向数 建议取8，即八个方向;东南西北等.
		//sigma为组内尺度 本例中为featurePoints.get(0).scale
		//fvGradHicap为0.2是经验值
		int radius = (int) (((descDim + 1.0f) / 2f) * Math.sqrt(2.0f)
				* considerScaleFactor + 0.5f);

		ArrayList<FeaturePoint> survivors = new ArrayList<FeaturePoint>();

		float sigma2Sq = 2.0f * dDim05 * dDim05; 
		for (FeaturePoint fp : featurePoints) {
			float angle = -fp.orientation; 

			fp.createVector(descDim, descDim, directionCount);

			//将描述子半径区域内坐标延主方向进行旋转 ，公式为cos -sin
			//											  sin  cos 矩阵相乘下x,y
			//
			for (int y = -radius; y < radius; ++y) {
				for (int x = -radius; x < radius; ++x) {

					float yR = (float) (Math.sin(angle) * x + Math.cos(angle)
							* y);
					float xR = (float) (Math.cos(angle) * x - Math.sin(angle)
							* y);

					 
					yR /= considerScaleFactor;
					xR /= considerScaleFactor;

				 
					if (yR >= (dDim05 + 0.5) || xR >= (dDim05 + 0.5)
							|| xR <= -(dDim05 + 0.5) || yR <= -(dDim05 + 0.5))
						continue;
				 
					int currentX = (int) (x + fp.x + 0.5);
					 
					int currentY = (int) (y + fp.y + 0.5);
					 //剔除边缘点
					if (currentX < 1 || currentX >= (magnitude.width - 1)
							|| currentY < 1
							|| currentY >= (magnitude.height - 1))
						continue;
				 
					//xr,yr为坐标点离关键点的距离σ为3*σ的直方图列数一半
					//当前点的加权梯度幅值
					float magW = (float) Math.exp(-(xR * xR + yR * yR)
							/ sigma2Sq)
							* magnitude.data[currentY * magnitude.width
									+ currentX];
					yR += dDim05 - 0.5;
					xR += dDim05 - 0.5;

				 
					int[] xIdx = new int[2];
					int[] yIdx = new int[2];
					int[] dirIdx = new int[2];  
									 			 
					float[] xWeight = new float[2];
					float[] yWeight = new float[2];
					float[] dirWeight = new float[2]; 
				 
					if (xR >= 0) {
						xIdx[0] = (int) xR;
						xWeight[0] = (1.0f - (xR - xIdx[0]));
					}
					if (yR >= 0) {
						yIdx[0] = (int) yR;
						yWeight[0] = (1.0f - (yR - yIdx[0]));
					}

					if (xR < (descDim - 1)) {
						xIdx[1] = (int) (xR + 1.0f);
						xWeight[1] = xR - xIdx[1] + 1.0f;
					}
					if (yR < (descDim - 1)) {
						yIdx[1] = (int) (yR + 1.0f);
						yWeight[1] = yR - yIdx[1] + 1.0f;
					}
				 
					
					//方向角度进行旋转
					
					float dir = direction.data[currentY * direction.width
							+ currentX]
							- fp.orientation;
					if (dir <= -Math.PI)
						dir += Math.PI;

					if (dir > Math.PI)
						dir -= Math.PI;

					 
					double idxDir = (double) ((dir * directionCount) / (2.0 * Math.PI));  
				 

					if (idxDir < 0.0) {
						idxDir += directionCount;
					}

					dirIdx[0] = (int) idxDir;
					dirIdx[1] = (dirIdx[0] + 1) % directionCount;  
					dirWeight[0] = (float) (1.0 - (idxDir - dirIdx[0]));  
					dirWeight[1] = (float) (idxDir - dirIdx[0]);  
					
					//双线性插值，最终累积在每个方向上的梯度值 weight=w*dr^k*(1-dr)^(1-k)*dc^m*(1-dc)^(1-m)
					//*do^n*(1-d0)^(1-n); k,m,n,o为0或1
					for (int iy = 0; iy < 2; ++iy) {
						for (int ix = 0; ix < 2; ++ix) {
							for (int d = 0; d < 2; ++d) {
								int idx = xIdx[ix] * fp.yDim * fp.oDim
										+ yIdx[iy] * fp.oDim + dirIdx[d];
								fp.features[idx] += xWeight[ix] * yWeight[iy]
										* dirWeight[d] * magW;
							}
						}
					}
				}
			}

			capAndNormalizeFV(fp, fvGradHicap);
			survivors.add(fp);
		}

		return (survivors);
	}

	//图像灰度值漂移，邻域像素相减，去除光照影响 描述子向量门限经验值0.2
	private void capAndNormalizeFV(FeaturePoint kp, float fvGradHicap) {

		float norm = 0.0f;
		for (int n = 0; n < kp.features.length; ++n)
			norm += Math.pow(kp.features[n], 2.0); 
		//首先进行归一化处理
		norm = (float) Math.sqrt(norm); 
		if (norm == 0.0)
			throw (new IllegalStateException(
					"CapAndNormalizeFV cannot normalize with norm = 0.0"));

		for (int n = 0; n < kp.features.length; ++n) {
			kp.features[n] /= norm;
			if (kp.features[n] > fvGradHicap)
				kp.features[n] = fvGradHicap;
		}

		norm = 0.0f;
		for (int n = 0; n < kp.features.length; ++n)
			norm += Math.pow(kp.features[n], 2.0);
		norm = (float) Math.sqrt(norm);

		for (int n = 0; n < kp.features.length; ++n)
			kp.features[n] /= norm;
	}

	 
	private ArrayList<ScalePeak> findPeaks4ThreeLayer(ImagePixelArray below,
			ImagePixelArray current, ImagePixelArray above, int curLev,
			float dogThresh) {
		ArrayList<ScalePeak> peaks = new ArrayList<ScalePeak>();

		for (int y = 1; y < (current.height - 1); ++y) {
			for (int x = 1; x < (current.width - 1); ++x) {
				RefCheckMark ref = new RefCheckMark();
				ref.isMin = true;
				ref.isMax = true;
				float c = current.data[x + y * current.width];  

				if (Math.abs(c) <= dogThresh)
					continue;  

				checkMinMax(current, c, x, y, ref, true);
				checkMinMax(below, c, x, y, ref, false);
				checkMinMax(above, c, x, y, ref, false);
				if (ref.isMin == false && ref.isMax == false)
					continue;
				peaks.add(new ScalePeak(x, y, curLev));
			}
		}
		return peaks;
	}

	private void checkMinMax(ImagePixelArray layer, float c, int x, int y,
			RefCheckMark ref, boolean isCurrentLayer) {

		if (layer == null)
			return;

		if (ref.isMin) {
			if (layer.data[(y - 1) * layer.width + x - 1] <= c  
					|| layer.data[y * layer.width + x - 1] <= c  
					|| layer.data[(y + 1) * layer.width + x - 1] <= c  
					|| layer.data[(y - 1) * layer.width + x] <= c  
					|| (isCurrentLayer ? false : (layer.data[y * layer.width
							+ x] < c)) 
					|| layer.data[(y + 1) * layer.width + x] <= c  
					|| layer.data[(y - 1) * layer.width + x + 1] <= c  
					|| layer.data[y * layer.width + x + 1] <= c  
					|| layer.data[(y + 1) * layer.width + x + 1] <= c)  
				ref.isMin = false;
		}
		if (ref.isMax) {
			if (layer.data[(y - 1) * layer.width + x - 1] >= c  
					|| layer.data[y * layer.width + x - 1] >= c  
					|| layer.data[(y + 1) * layer.width + x - 1] >= c  
					|| layer.data[(y - 1) * layer.width + x] >= c  
					|| (isCurrentLayer ? false : (layer.data[y * layer.width
							+ x] > c))  
					|| layer.data[(y + 1) * layer.width + x] >= c  
					|| layer.data[(y - 1) * layer.width + x + 1] >= c  
					|| layer.data[y * layer.width + x + 1] >= c  
					|| layer.data[(y + 1) * layer.width + x + 1] >= c)  
				ref.isMax = false;
		}
	}

	
	//利用海森矩阵去除边缘效应 即我们设定的拉普他的值 (10+1)*(10+1)/10
	 //海森矩阵 d_xx,d_yy,d_xy 为个方向的导数
	private boolean isTooEdgelike(ImagePixelArray space, int x, int y, float r) {
		float d_xx, d_yy, d_xy;

		 

		d_xx = space.data[(y + 1) * space.width + x]
				+ space.data[(y - 1) * space.width + x] - 2.0f
				* space.data[y * space.width + x];
		d_yy = space.data[y * space.width + x + 1]
				+ space.data[y * space.width + x - 1] - 2.0f
				* space.data[y * space.width + x];
		d_xy = 0.25f * ((space.data[(y + 1) * space.width + x + 1] - space.data[(y + 1)
				* space.width + x - 1]) //
		- (space.data[(y - 1) * space.width + x + 1] - space.data[(y - 1)
				* space.width + x - 1]));

 
		float trHsq = d_xx + d_yy;
		trHsq *= trHsq;
		float detH = d_xx * d_yy - (d_xy * d_xy);
		float r1sq = (r + 1.0f);
		r1sq *= r1sq;
		if ((trHsq / detH) < (r1sq / r)) {
			return false;
		}
		return true;
	}

 
	private boolean localizeIsWeak(ScalePeak peak, int steps, int[][] processed) {
		boolean needToAdjust = true;
		int adjusted = steps;
		while (needToAdjust) {
			int x = peak.x;
			int y = peak.y;
			//剔除边缘点
			if (peak.level <= 0 || peak.level >= (this.diffImags.length - 1))
				return (true);
			//得到高斯差分层
			ImagePixelArray space = diffImags[peak.level];
			if (x <= 0 || x >= (space.width - 1))
				return (true);
			if (y <= 0 || y >= (space.height - 1))
				return (true);
			
			RefFloat dp = new RefFloat();
			AdjustedArray adj = getAdjustment(peak, peak.level, x, y, dp);
			//各个方向的偏移量
			float adjS = adj.data[0];
			float adjY = adj.data[1];
			float adjX = adj.data[2];
			//若任一方向偏移量大于0.5，则迭代，且像素点定位到偏移像素
			if (Math.abs(adjX) > 0.5 || Math.abs(adjY) > 0.5) {
		 
				if (adjusted == 0) {
					return (true);
				}
				adjusted -= 1;
				//偏移过大 直接舍去
				float distSq = adjX * adjX + adjY * adjY;
				if (distSq > 2.0)
					return (true);
 
				peak.x = (int) (peak.x + adjX + 0.5);
				peak.y = (int) (peak.y + adjY + 0.5);
				peak.level = (int) (peak.level + adjS + 0.5);
				continue;
			}

			if (processed[peak.x][peak.y] != 0)
				return (true);

			processed[peak.x][peak.y] = 1;
 
			LocalInfo local = new LocalInfo(adjS, adjX, adjY);
			local.dValue = space.data[peak.y * space.width + peak.x] + 0.5f
					* dp.val;
			peak.local = local;
			needToAdjust = false;
		}
		return (false);
	}

	private AdjustedArray getAdjustment(ScalePeak peak, int level, int x,
			int y, RefFloat ref) {

		ref.val = 0.0f;
		if (peak.level <= 0 || peak.level >= (this.diffImags.length - 1)) {
			throw (new IllegalArgumentException(
					"point.Level is not within [bottom-1;top-1] range"));
		}
		ImagePixelArray b = this.diffImags[level - 1]; // below
		ImagePixelArray c = this.diffImags[level]; // current
		ImagePixelArray a = this.diffImags[level + 1]; // above

		AdjustedArray h = new AdjustedArray(3, 3);
	 
		
		//二次求导公式，泰勒公式的到该点的近似值
		
		h.data[0] = b.data[y * b.width + x] - 2 * c.data[y * c.width + x]
				+ a.data[y * a.width + x]; // h.data[0][0]

		h.data[h.width] = h.data[1] = 0.25f * (a.data[(y + 1) * a.width + x] //
				- a.data[(y - 1) * a.width + x] //
		- (b.data[(y + 1) * b.width + x] - b.data[(y - 1) * b.width + x])); // h.data[0][1]

		h.data[h.width * 2] = h.data[2] = 0.25f * (a.data[y * a.width + x + 1]
				- a.data[y * a.width + x - 1] //
		- (b.data[y * b.width + x + 1] - b.data[y * b.width + x - 1]));

		h.data[1 * h.width + 1] = c.data[(y - 1) * c.width + x] - 2f
				* c.data[y * c.width + x] + c.data[(y + 1) * c.width + x];

		h.data[1 + h.width * 2] = h.data[2 + h.width] = 0.25f * (c.data[(y + 1)
				* c.width + x + 1] //
				- c.data[(y + 1) * c.width + x - 1] //
		- (c.data[(y - 1) * c.width + x + 1] //
		- c.data[(y - 1) * c.width + x - 1]));

		h.data[2 * h.width + 2] = c.data[y * c.width + x - 1] - 2
				* c.data[y * c.width + x] + c.data[y * c.width + x + 1];
		AdjustedArray d = new AdjustedArray(1, 3);
		//一阶求导

		d.data[0] = 0.5f * (a.data[y * a.width + x] - b.data[y * b.width + x]); // d.data[1][0]
																				// =>
																				// d.data[0*width+1]
																				// =
																				// d.data[1]
		d.data[1] = 0.5f * (c.data[(y + 1) * c.width + x] - c.data[(y - 1)
				* c.width + x]);
		d.data[2] = 0.5f * (c.data[y * c.width + x + 1] - c.data[y * c.width
				+ x - 1]);
		
		AdjustedArray back = d.clone();
		back.negate();
	 
		h.solveLinear(back);
		ref.val = back.dot(d);
		return (back);
	}

	private static class AdjustedArray extends FloatArray implements Cloneable {

		public int width;
		public int height;

		public AdjustedArray(int width, int height) {
			this.width = width;
			this.height = height;
			this.data = new float[width * height];
		}

		public AdjustedArray clone() {
			AdjustedArray cp = new AdjustedArray(this.width, this.height);
			System.arraycopy(this.data, 0, cp.data, 0, this.data.length);
			return cp;
		}

		 
		public float dot(AdjustedArray aa) {
			if (this.width != aa.width || this.width != 1 || aa.width != 1) {
				throw (new IllegalArgumentException(
						"Dotproduct only possible for two equal n x 1 matrices"));
			}
			float sum = 0.0f;

			for (int y = 0; y < this.height; ++y)
				sum += data[y * this.width + 0] * aa.data[y * aa.width + 0];
			return (sum);
		}
		
		
		//泰勒公式最后取倒数相反值 offset=-offset
		public void negate() {
			for (int y = 0; y < this.data.length; ++y) {
				data[y] = -data[y];
			}
		}

		//求近似值,矩阵相乘
		
		
		public void solveLinear(AdjustedArray vec) {
			if (this.width != this.height || this.height != vec.height) {
				throw (new IllegalArgumentException(
						"Matrix not quadratic or vector dimension mismatch"));
			}

		 
			for (int y = 0; y < (this.height - 1); ++y) {

				int yMaxIndex = y;
				float yMaxValue = Math.abs(data[y * this.width + y]);
				 
				for (int py = y; py < this.height; ++py) {
					if (Math.abs(data[py * this.width + y]) > yMaxValue) {
						yMaxValue = Math.abs(data[py * this.width + y]);
						yMaxIndex = py;
					}
				}

				swapRow(y, yMaxIndex);
				vec.swapRow(y, yMaxIndex);
			 //矩阵*矩阵的逆
				for (int py = y + 1; py < this.height; ++py) {
					float elimMul = data[py * this.width + y]
							/ data[y * this.width + y];
					for (int x = 0; x < this.width; ++x)
						data[py * this.width + x] -= elimMul
								* data[y * this.width + x];
					vec.data[py * vec.width + 0] -= elimMul
							* vec.data[y * vec.width + 0];
				}
			}
    
		 
			for (int y = this.height - 1; y >= 0; --y) {
				float solY = vec.data[y * vec.width + 0];
				for (int x = this.width - 1; x > y; --x)
					solY -= data[y * this.width + x]
							* vec.data[x * vec.width + 0];
				vec.data[y * vec.width + 0] = solY / data[y * this.width + y];
			}
		}

		// Swap two rows r1, r2
		private void swapRow(int r1, int r2) {
			if (r1 == r2)
				return;
			for (int x = 0; x < this.width; ++x) {
				float temp = data[r1 * this.width + x];
				data[r1 * this.width + x] = data[r2 * this.width + x];
				data[r2 * this.width + x] = temp;
			}
		}
	}
 
	static class RefCheckMark {

		boolean isMin;
		boolean isMax;
	}

	 
	static class RefPeakValueAndDegreeCorrection {

		float peakValue;
		float degreeCorrection;
	}

}
