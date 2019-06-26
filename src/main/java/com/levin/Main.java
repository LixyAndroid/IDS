package com.levin;


import com.levin.core.algo.CFPS;
import com.levin.core.algo.IDS;
import com.levin.core.entity.code.SolutionCode;
import com.levin.excel.DataLab;
import com.levin.util.FileUtils;

public class Main {
    public static void main(String[] args) {
        String path = FileUtils.getAppPath() + "/src/main/resources/";


        for (int a = 0; a <= 500; a += 10) {
            for (int b = 0; b <= 500; b += 10) {
                StringBuilder str = new StringBuilder(a + "\t" + b + "\t");
                for (int i = 0; i < 10; i++) {
                    IDS solver = new CFPS(0, DataLab.driverList(path + "vehicle.xls"), DataLab.taskList(path + "task.xls",-1), 1, "distance", a, b, 100);
                    SolutionCode solve = solver.solve();
                    str.append(solve.getFitness()).append("\t");
                    DataLab.clear();
                }
                str.append("\n");
                FileUtils.writeFile(path + "result2.txt", str.toString());
            }
        }
    }
}
