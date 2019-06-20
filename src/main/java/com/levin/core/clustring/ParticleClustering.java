package com.levin.core.clustring;

import com.levin.core.entity.code.LayerCode;
import com.levin.core.entity.code.LayerCodeUtil;

import java.util.List;

/**
 * K-Media聚类
 */
public class ParticleClustering extends KMeansClustering<LayerCode> {

    public ParticleClustering(List<LayerCode> mUnits, int scale, int i) {
        super(mUnits, scale, i);
    }

    @Override
    public double similarScore(LayerCode o1, LayerCode o2) {
        return LayerCodeUtil.ratio(o1.toString(), o2.toString());
    }

    @Override
    public boolean equals(LayerCode o1, LayerCode o2) {
        if (o1 == null || o2 == null) {
            return false;
        }

        if (o1.toString() == null || o2.toString() == null) {
            return false;
        }

        return o1.toString().equals(o2.toString());
    }

    @Override
    public LayerCode getCenterT(List<LayerCode> list) {
        double min = Double.MAX_VALUE;
        LayerCode center = null;
        for (int i = 0; i < list.size(); i++) {
            double ratios = 0;
            for (int j = 0; j < list.size(); j++) {
                if (i == j) {
                    continue;
                }
                ratios += LayerCodeUtil.ratio(list.get(i).toString(), list.get(j).toString());
            }
            if (ratios < min) {
                min = ratios;
                center = list.get(i);
            }
        }
        return center;
    }
}
