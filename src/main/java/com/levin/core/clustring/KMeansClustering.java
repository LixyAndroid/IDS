package com.levin.core.clustring;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * k-means 聚类算法
 *
 * @author : Levin
 */
public abstract class KMeansClustering<T> implements Clustring {
    private List<T> dataArray;//待分类的原始值
    private int K = 3;//将要分成的类别个数
    private int maxClusterTimes = 500;//最大迭代次数
    private List<List<T>> clusterList;//聚类的结果
    private List<T> clusteringCenterT;//质心

    public KMeansClustering() {
    }

    public KMeansClustering(List<T> dataArray, int k, int maxClusterTimes) {
        this.dataArray = dataArray;
        K = k;
        this.maxClusterTimes = maxClusterTimes;
    }

    public int getK() {
        return K;
    }

    public void setK(int K) {
        if (K < 1) {
            throw new IllegalArgumentException("K must greater than 0");
        }
        this.K = K;
    }

    public int getMaxClusterTimes() {
        return maxClusterTimes;
    }

    public void setMaxClusterTimes(int maxClusterTimes) {
        if (maxClusterTimes < 10) {
            throw new IllegalArgumentException("maxClusterTimes must greater than 10");
        }
        this.maxClusterTimes = maxClusterTimes;
    }

    public List<T> getClusteringCenterT() {
        return clusteringCenterT;
    }

    public List<List<T>> getResult() {
        return clusterList;
    }

    /**
     * 对数据进行聚类
     */
    public void clustering() {
        if (dataArray == null) {
            return;
        }
        //初始K个点为数组中的前K个点
        int size = K > dataArray.size() ? dataArray.size() : K;
        List<T> centerT = new ArrayList<T>(size);
        //对数据进行打乱
        Collections.shuffle(dataArray);
        for (int i = 0; i < size; i++) {
            centerT.add(dataArray.get(i));
        }
        clustering(centerT, 0);
    }

    /**
     * 一轮聚类
     */
    private void clustering(List<T> preCenter, int times) {
        if (preCenter == null || preCenter.size() < 2) {
            return;
        }
        //打乱质心的顺序
        Collections.shuffle(preCenter);
        List<List<T>> clusterList = getListT(preCenter.size());
        for (T o1 : this.dataArray) {
            //寻找最相似的质心
            int max = 0;
            double maxScore = similarScore(o1, preCenter.get(0));
            for (int i = 1; i < preCenter.size(); i++) {
                double score = similarScore(o1, preCenter.get(i));
                if (maxScore < score) {
                    maxScore = score;
                    max = i;
                }
            }
            clusterList.get(max).add(o1);
        }
        //计算本次聚类结果每个类别的质心
        List<T> nowCenter = new ArrayList<T>();
        for (List<T> list : clusterList) {
            nowCenter.add(getCenterT(list));
        }
        //是否达到最大迭代次数
        if (times >= this.maxClusterTimes || preCenter.size() < this.K) {
            this.clusterList = clusterList;
            return;
        }
        this.clusteringCenterT = nowCenter;
        //判断质心是否发生移动，如果没有移动，结束本次聚类，否则进行下一轮
        if (isCenterChange(preCenter, nowCenter)) {
            clear(clusterList);
            clustering(nowCenter, times + 1);
        } else {
            this.clusterList = clusterList;
        }
    }

    /**
     * 初始化一个聚类结果
     */
    private List<List<T>> getListT(int size) {
        List<List<T>> list = new ArrayList<List<T>>(size);
        for (int i = 0; i < size; i++) {
            list.add(new ArrayList<T>());
        }
        return list;
    }

    /**
     * 清空无用数组
     */
    private void clear(List<List<T>> lists) {
        for (List<T> list : lists) {
            list.clear();
        }
        lists.clear();
    }

    /**
     * 向模型中添加记录
     */
    public void addRecord(T value) {
        if (dataArray == null) {
            dataArray = new ArrayList<T>();
        }
        dataArray.add(value);
    }

    /**
     * 判断质心是否发生移动
     */
    private boolean isCenterChange(List<T> preT, List<T> nowT) {
        if (preT == null || nowT == null) {
            return false;
        }
        for (T t1 : preT) {
            boolean bol = true;
            for (T t2 : nowT) {
                if (equals(t1, t2)) {//t1在t2中有相等的，认为该质心未移动
                    bol = false;
                    break;
                }
            }
            //有一个质心发生移动，认为需要进行下一次计算
            if (bol) {
                return bol;
            }
        }
        return false;
    }

    /**
     * o1 o2之间的相似度
     */
    protected abstract double similarScore(T o1, T o2);

    /**
     * 判断o1 o2是否相等
     */
    protected abstract boolean equals(T o1, T o2);

    /**
     * 求一组数据的质心
     */
    protected abstract T getCenterT(List<T> list);
}
