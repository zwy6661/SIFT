 
package com.sift;

import java.util.Comparator;

import com.sift.KDFeaturePoint;

 
public class Match {

    int slopeArc;
    public KDFeaturePoint fp1;
    public KDFeaturePoint fp2;

    public float dist1;
    public float dist2;

    //结点匹配度
    public Match(KDFeaturePoint fp1, KDFeaturePoint fp2, float dist1, float dist2){
        this.fp1 = fp1;
        this.fp2 = fp2;
        this.dist1 = dist1;
        this.dist2 = dist2;
    }
    
    public static class MatchWeighter implements Comparator<Match> {

        private float distExp;
        private float quotExp;

        public MatchWeighter(){
            this(1.0f, 1.0f);
        }

        
        public MatchWeighter(float distExp, float quotExp){
            this.distExp = distExp;
            this.quotExp = quotExp;
        }

        public float OverallFitness(Match m) {
            float fitness = (float)(Math.pow(m.dist1, distExp) * Math.pow(1.0 / (m.dist2 - m.dist1), quotExp));
            return (fitness);
        }

        public int compare(Match o1, Match o2) {

            float fit1 = OverallFitness(o1);
            float fit2 = OverallFitness(o2);
            if (fit1 < fit2) return (-1);
            else if (fit1 > fit2) return (1);
            return (0);
        }

    }

}

