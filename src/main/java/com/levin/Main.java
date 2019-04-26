package com.levin;


import com.levin.core.algo.CFPS;
import com.levin.core.algo.IDS;
import com.levin.core.algo.SsIDS;
import com.levin.core.entity.code.SolutionCode;
import com.levin.excel.DataLab;
import com.levin.util.FileUtils;

public class Main {
    public static void main(String[] args) {
        String path = FileUtils.getAppPath() + "/src/main/resources/";
        /*IDS solver = new SsIDS(100, DataLab.driverList(path + "vehicle.xls"), DataLab.taskList(path + "task.xls"), 100, "distance", 15, 15);
        SolutionCode solve = solver.solve();
        System.out.println("最优目标函数值：" + solve.getFitness());
        System.out.println(solve.print());*/

        int[] alpaha = new int[]{0, 50, 100, 200, 250, 300};
        int[] NN = new int[]{10, 100, 500, 1000};

        for (int a : alpaha) {
            for (int b : alpaha) {
                for (int n : NN) {
                    StringBuilder str = new StringBuilder(a + "\t" + b + "\t" + n + "\t");
                    for (int i = 0; i < 1; i++) {
                        IDS solver = new CFPS(0, DataLab.driverList(path + "vehicle.xls"), DataLab.taskList(path + "task.xls"), 1, "distance", a, b, n);
                        SolutionCode solve = solver.solve();
                        str.append(solve.getFitness()).append("\t");
                    }
                    str.append("\n");
                    System.out.println(str);
                    FileUtils.writeFile(path + "result.txt", str.toString());
                }
            }
        }
       /* IDS solver = new CFPS(0, DataLab.driverList(path + "vehicle.xls"), DataLab.taskList(path + "task.xls"), 1, "distance", 300, 300, 1000);
        SolutionCode solve = solver.solve();
        System.out.println(solve.getFitness());
        System.out.println(solve.print());*/
    }
}
