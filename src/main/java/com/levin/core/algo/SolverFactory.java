package com.levin.core.algo;

import com.levin.core.entity.code.SolutionCode;
import com.levin.excel.DataLab;
import com.levin.util.FileUtils;
import com.levin.web.AlgoPara;

public class SolverFactory {
    private static final String path = FileUtils.getAppPath() + "/src/main/resources/";

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
