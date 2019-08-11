
package com.sift;

import java.util.ArrayList;
import java.util.List;

import com.sift.RefFloat;
import com.sift.RefInt;

 
public class KDTree {

    IKDTreeDomain dr;         //结点
    int           splitDim;  //分割维

    KDTree        left;      //左KD
    KDTree        right;	 //右KD

    @SuppressWarnings("rawtypes")
    public static class BestEntry implements Comparable {

        private float dist;      //距离
        private int   distSq;	 //距离

        IKDTreeDomain neighbour;  //邻近结点

        public IKDTreeDomain getNeighbour() {
            return this.neighbour;
        }

        public int getDistSq() {
            return distSq;
        }

        public void setDistSq(int distSq) {
            this.distSq = distSq;
        }

        public float getDist() {
            return dist;
        }

        public void setDist(float dist) {
            this.dist = dist;
        }

        BestEntry(IKDTreeDomain neighbour, int distSq){
            this.neighbour = neighbour;
            this.distSq = distSq;
        }

        BestEntry(IKDTreeDomain neighbour, float dist){
            this.neighbour = neighbour;
            this.dist = dist;
        }

        
        //比较关键点
        //挑选最近邻
        public int compareTo(Object o) {
            BestEntry be = (BestEntry) o;
            if (distSq < be.distSq) return -1;
            if (distSq > be.distSq) return 1;
            return 0;
        }

    }

    //两个特征的描述符的距离，欧氏距离,SIFT中128维向量的距离
    
    public static int distanceSq(IKDTreeDomain t1, IKDTreeDomain t2) {
        int distance = 0;
        for (int n = 0; n < t1.dim; ++n) {
            int dimDist = t1.descriptor[n] - t2.descriptor[n];
            distance += dimDist * dimDist;
        }
        return (distance);
    }

    static class HyperRectangle implements Cloneable {

        int[] leftTop;       //左子节点
        int[] rightBottom;	 //右子节点
        int   dim;			 //维数

        private HyperRectangle(int dim){
            this.dim = dim;
            leftTop = new int[dim];
            rightBottom = new int[dim];
        }

        public Object clone() {
            HyperRectangle rec = new HyperRectangle(dim);

            for (int n = 0; n < dim; ++n) {
                rec.leftTop[n] = leftTop[n];
                rec.rightBottom[n] = rightBottom[n];
            }

            return (rec);
        }

        //创建矩形区域对比,左子节点小，右子节点大
        
        
        static HyperRectangle createUniverseRectangle(int dim) {
            HyperRectangle rec = new HyperRectangle(dim);

            for (int n = 0; n < dim; ++n) {
                rec.leftTop[n] = Integer.MIN_VALUE;
                rec.rightBottom[n] = Integer.MAX_VALUE;
            }

            return (rec);
        }

        //寻找维度空间
        //所在维度，以及中值。
        HyperRectangle splitAt(int splitDim, int splitVal) {
            if (leftTop[splitDim] >= splitVal || rightBottom[splitDim] < splitVal) {
                throw (new IllegalArgumentException("SplitAt with splitpoint outside rec"));
            }
            HyperRectangle r2 = (HyperRectangle) this.clone();
            rightBottom[splitDim] = splitVal;
            r2.leftTop[splitDim] = splitVal;
            return (r2);
        }
        
        //该结点是否在所划分的区域内
        
        boolean isIn(IKDTreeDomain target) {
            if (target.dim != dim) throw (new IllegalArgumentException("isIn dimension mismatch"));

            for (int n = 0; n < dim; ++n) {
                int targD = target.descriptor[n];

                if (targD < leftTop[n] || targD >= rightBottom[n]) return (false);
            }

            return (true);
        }

        
        //该结点的距离是否达到要求
        
        boolean isInReach(IKDTreeDomain target, float distRad) {
            return (distance(target) < distRad);
        }

        //判断所查结点与当前结点左右结点的距离
        float distance(IKDTreeDomain target) {
            int closestPointN;
            int distance = 0;

             
            for (int n = 0; n < dim; ++n) {
                int tI = target.descriptor[n];
                int hrMin = leftTop[n];
                int hrMax = rightBottom[n];

                closestPointN = 0;
                if (tI <= hrMin) {
                    closestPointN = hrMin;
                } else if (tI > hrMin && tI < hrMax) {
                    closestPointN = tI;
                } else if (tI >= hrMax) {
                    closestPointN = hrMax;
                }

                int dimDist = tI - closestPointN;
                distance += dimDist * dimDist;
            }

            return (float) (Math.sqrt((float) distance));
        }
    }

    
    //实体样本
    
    static class HREntry implements Comparable<HREntry> {

        float          dist;  //距离
        IKDTreeDomain  pivot; //结点
        HyperRectangle rect;  //区域
        KDTree         tree;  //KD树

        public float getDist() {
            return this.dist;
        }

        public IKDTreeDomain getPivot() {
            return this.pivot;
        }

        public HyperRectangle getRect() {
            return this.rect;
        }

        public KDTree getTree() {
            return this.tree;
        }

        public HREntry(HyperRectangle rect, KDTree tree, IKDTreeDomain pivot, float dist){
            this.dist = dist;
            this.pivot = pivot;
            this.tree = tree;
            this.rect = rect;
        }

        //比较最近邻
        
        public int compareTo(HREntry o) {
            HREntry hre = (HREntry) o;
            if (dist < hre.dist) return -1;
            if (dist > hre.dist) return 1;
            return 0;
        }
    }

     
    //返回最近邻结点

    public IKDTreeDomain nearestNeighbour(IKDTreeDomain target, RefFloat ref) {
        HyperRectangle hr = HyperRectangle.createUniverseRectangle(target.dim);

        IKDTreeDomain nearest = nearestNeighbourI(target, hr, Float.POSITIVE_INFINITY, ref);
        // Math.sqrt(ref.val);
        return (nearest);
    }

    
    //递归查找最近邻结点
    
    private IKDTreeDomain nearestNeighbourI(IKDTreeDomain target, HyperRectangle hr, float maxDistSq, RefFloat resDistSq) {
        // Console.WriteLine ("C NearestNeighbourI called");

        resDistSq.val = Float.POSITIVE_INFINITY;

        IKDTreeDomain pivot = dr;

        HyperRectangle leftHr = hr;
        HyperRectangle rightHr = leftHr.splitAt(splitDim, pivot.descriptor[splitDim]);

        HyperRectangle nearerHr, furtherHr;
        KDTree nearerKd, furtherKd;

        // step 5-7
        if (target.descriptor[splitDim] <= pivot.descriptor[splitDim]) {
            nearerKd = left;
            nearerHr = leftHr;
            furtherKd = right;
            furtherHr = rightHr;
        } else {
            nearerKd = right;
            nearerHr = rightHr;
            furtherKd = left;
            furtherHr = leftHr;
        }

        // step 8
        IKDTreeDomain nearest = null;

        RefFloat distSq = new RefFloat();
        if (nearerKd == null) {
            distSq.val = Float.POSITIVE_INFINITY;
        } else {
            nearest = nearerKd.nearestNeighbourI(target, nearerHr, maxDistSq, distSq);
        }

        // step 9
        maxDistSq = (float) Math.min(maxDistSq, distSq.val);

        // step 10
        if (furtherHr.isInReach(target, (float) Math.sqrt(maxDistSq))) {
            float ptDistSq = KDTree.distanceSq(pivot, target);
            if (ptDistSq < distSq.val) {
                // steps 10.1.1 to 10.1.3
                nearest = pivot;
                distSq.val = ptDistSq;
                maxDistSq = distSq.val;
            }

            // step 10.2
            RefFloat tempDistSq = new RefFloat();
            IKDTreeDomain tempNearest = null;
            if (furtherKd == null) {
                tempDistSq.val = Float.POSITIVE_INFINITY;
            } else {
                tempNearest = furtherKd.nearestNeighbourI(target, furtherHr, maxDistSq, tempDistSq);
            }

            // step 10.3
            if (tempDistSq.val < distSq.val) {
                nearest = tempNearest;
                distSq = tempDistSq;
            }
        }

        resDistSq = distSq;
        return (nearest);
    }

    
    
    //创建KD树
    
    public static KDTree createKDTree(List<? extends IKDTreeDomain> exset) {
        if (exset.size() == 0) return (null);
        KDTree cur = new KDTree();    //KD树
        RefInt splitDim = new RefInt();   //维数
        splitDim.val = cur.splitDim;    
        cur.dr = goodCandidate(exset, splitDim);   //偏向的维数线
        cur.splitDim = splitDim.val;// 当前点所偏向维数
        ArrayList<IKDTreeDomain> leftElems = new ArrayList<IKDTreeDomain>();
        ArrayList<IKDTreeDomain> rightElems = new ArrayList<IKDTreeDomain>();

        //下标维数
        float bound = cur.dr.descriptor[splitDim.val];
        for (IKDTreeDomain dom : exset) {
           
            if (dom == cur.dr) continue;

            if (dom.descriptor[splitDim.val] <= bound) {
                leftElems.add(dom);
            } else {
                rightElems.add(dom);
            }
        }

        //递归构建出KD树
        cur.left = createKDTree(leftElems);
        cur.right = createKDTree(rightElems);
        return (cur);
    }

    //偏向的维数线
    
    private static IKDTreeDomain goodCandidate(List<? extends IKDTreeDomain> exset, RefInt splitDim) {
        IKDTreeDomain first = exset.get(0);
        if (first == null) {
            throw (new java.lang.NullPointerException("Not of type IKDTreeDomain "));
        }
        int dim = first.dim;

        
        float[] minHr = new float[dim];
        float[] maxHr = new float[dim];
        for (int k = 0; k < dim; ++k) {
            minHr[k] = Float.POSITIVE_INFINITY;
            maxHr[k] = Float.NEGATIVE_INFINITY;
        }

        //找出所有维度上最大最小值
        
        for (IKDTreeDomain dom : exset) {
            for (int k = 0; k < dim; ++k) {
                float dimE = dom.descriptor[k];
                if (dimE < minHr[k]) minHr[k] = dimE;
                if (dimE > maxHr[k]) maxHr[k] = dimE;
            }
        }

        //找出每个维数的最大最小值的差值
        //并标记最大差值的维数
        
        float[] diffHr = new float[dim];
        int maxDiffDim = 0;
        float maxDiff = 0.0f;
        for (int k = 0; k < dim; ++k) {
            diffHr[k] = maxHr[k] - minHr[k];
            if (diffHr[k] > maxDiff) {
                maxDiff = diffHr[k];
                maxDiffDim = k;
            }
        }

        
        //设立中位值
        
        float middle = 0.0f;
        try {
            middle = (maxDiff / 2.0f) + minHr[maxDiffDim];
        } catch (Exception e) {
            System.out.println(dim);
            System.out.println(minHr.length);
        }

        //定义结点
        
        IKDTreeDomain exemplar = null;
        float exemMinDiff = Float.POSITIVE_INFINITY;

        //遍历结点，找出中位结点
        for (IKDTreeDomain dom : exset) {
            float curDiff = Math.abs(dom.descriptor[maxDiffDim] - middle);
            if (curDiff < exemMinDiff) {
                exemMinDiff = curDiff;
                exemplar = dom;
            }
        }

    
        //标记所在维数
        splitDim.val = maxDiffDim;
        return (exemplar);
    }

    
    //最近邻BBF算法
    public ArrayList<BestEntry> nearestNeighbourListBBF(IKDTreeDomain kp, int q, int searchSteps) {
        HyperRectangle hr = HyperRectangle.createUniverseRectangle(kp.dim);

        SortedLimitedList<BestEntry> best = new SortedLimitedList<BestEntry>(q);
        SortedLimitedList<HREntry> searchHr = new SortedLimitedList<HREntry>(searchSteps);

        RefInt dummyDist = new RefInt();
        RefInt step = new RefInt();
        dummyDist.val = 0;
        step.val = searchSteps;
        nearestNeighbourListBBFI(best, q, kp, hr, Integer.MAX_VALUE, dummyDist, searchHr, step);
        for (BestEntry be : best)
            be.dist = (float) Math.sqrt(be.distSq);
        return (best);
    }

    
    //K近邻BBF算法查询
    private IKDTreeDomain nearestNeighbourListBBFI(SortedLimitedList<BestEntry> best, int q, IKDTreeDomain target,
                                                   HyperRectangle hr, int maxDistSq, RefInt resDistSq,
                                                   SortedLimitedList<HREntry> searchHr, RefInt searchSteps) {
        resDistSq.val = Integer.MAX_VALUE;

        IKDTreeDomain pivot = dr;
        best.add(new BestEntry(dr, KDTree.distanceSq(target, dr)));

        HyperRectangle leftHr = hr;
        HyperRectangle rightHr = leftHr.splitAt(splitDim, pivot.descriptor[splitDim]);

        HyperRectangle nearerHr, furtherHr;
        KDTree nearerKd, furtherKd;

        // step 5-7
        if (target.descriptor[splitDim] <= pivot.descriptor[splitDim]) {
            nearerKd = left;
            nearerHr = leftHr;
            furtherKd = right;
            furtherHr = rightHr;
        } else {
            nearerKd = right;
            nearerHr = rightHr;
            furtherKd = left;
            furtherHr = leftHr;
        }

       
        IKDTreeDomain nearest = null;
        RefInt distSq = new RefInt();

        searchHr.add(new HREntry(furtherHr, furtherKd, pivot, furtherHr.distance(target)));

      
        if (nearerKd == null) {
            distSq.val = Integer.MAX_VALUE;
        } else {
            nearest = nearerKd.nearestNeighbourListBBFI(best, q, target, nearerHr, maxDistSq, distSq, searchHr,
                                                        searchSteps);
        }

        
        if (best.size() >= q) {
            maxDistSq = ((BestEntry) best.get(q - 1)).getDistSq();
        } else maxDistSq = Integer.MAX_VALUE;

        if (searchHr.size() > 0) {
            HREntry hre = (HREntry) searchHr.get(0);
            searchHr.remove(0);

            furtherHr = hre.rect;
            furtherKd = hre.tree;
            pivot = hre.pivot;
        }

     
        searchSteps.val -= 1;
        if (searchSteps.val > 0 && furtherHr.isInReach(target, (float) Math.sqrt(maxDistSq))) {
            int ptDistSq = KDTree.distanceSq(pivot, target);
            if (ptDistSq < distSq.val) {
              
                nearest = pivot;
                distSq.val = ptDistSq;
                maxDistSq = distSq.val;
            }

         
            RefInt tempDistSq = new RefInt();
            IKDTreeDomain tempNearest = null;
            if (furtherKd == null) {
                tempDistSq.val = Integer.MAX_VALUE;
            } else {
                tempNearest = furtherKd.nearestNeighbourListBBFI(best, q, target, furtherHr, maxDistSq, tempDistSq,
                                                                 searchHr, searchSteps);
            }

         
            if (tempDistSq.val < distSq.val) {
                nearest = tempNearest;
                distSq = tempDistSq;
            }
        }

        resDistSq = distSq;
        return (nearest);
    }

}

