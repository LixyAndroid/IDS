package com.levin.core.algo;

import com.levin.core.entity.code.SolutionCode;
import com.levin.excel.DataLab;
import com.levin.util.FileUtils;
import com.levin.web.AlgoPara;

/**
 * 求解算法工厂
 */
public class SolverFactory {
    //司机和订单Excel文件路径
    private static final String path = FileUtils.getAppPath() + "/src/main/resources/";

    /**
     * 根据算法名称选择相应的算法进行求解
     *
     * @param algo 算法名称
     * @param n    订单数目
     * @param para 算法参数
     * @return
     */
    public static SolutionCode solve(String algo, int n, AlgoPara para) {
        return selectAlgo(algo, n, para).solve();
    }

    private static IDS selectAlgo(String algo, int n, AlgoPara para) {
        if (algo.equalsIgnoreCase("cfpsst")) {
            return new CFPSST(para.getMaxGen(), DataLab.driverList(path + "vehicle.xls"),
                    DataLab.taskList(path + "task.xls", n), para.getSize(), para.getFitnessType(), para.getAlpha(), para.getBeta(), para.getGama(), para.getN(), para.getB1(), para.getB2());
        } else if (algo.equalsIgnoreCase("cfps")) {
            return new CFPS(para.getMaxGen(), DataLab.driverList(path + "vehicle.xls"),
                    DataLab.taskList(path + "task.xls", n), para.getSize(), para.getFitnessType(), para.getAlpha(), para.getBeta(), para.getN());
        } else if (algo.equalsIgnoreCase("ga")) {
            return new GaIDS(para.getMaxGen(), DataLab.driverList(path + "vehicle.xls"),
                    DataLab.taskList(path + "task.xls", n), para.getSize(), para.getFitnessType(), para.getN());
        } else if (algo.equalsIgnoreCase("pso")) {
            return new PsoIDS(para.getMaxGen(), DataLab.driverList(path + "vehicle.xls"),
                    DataLab.taskList(path + "task.xls", n), para.getSize(), para.getFitnessType());
        } else if (algo.equalsIgnoreCase("abc")) {
            return new ABCIDS(para.getMaxGen(), DataLab.driverList(path + "vehicle.xls"),
                    DataLab.taskList(path + "task.xls", n), para.getFitnessType());
        }
        return new SsIDS(para.getMaxGen(), DataLab.driverList(path + "vehicle.xls"),
                DataLab.taskList(path + "task.xls", n), para.getSize(), para.getFitnessType(), para.getB1(), para.getB2(), para.getN(), true);
    }
}
