import com.levin.core.algo.GaIDS;
import com.levin.core.entity.code.SolutionCode;
import com.levin.excel.DataLab;
import com.levin.util.FileUtils;
import com.levin.web.AlgoPara;

public class GaTest {
    //司机和订单Excel文件路径
    private static final String path = FileUtils.getAppPath() + "/src/main/resources/";

    public static void main(String[] args) {
        staticValidate();
    }

    private static void staticValidate() {
        AlgoPara para = new AlgoPara();
        para.setMaxGen(1000);
        GaIDS ga = new GaIDS(para.getMaxGen(), DataLab.driverList(path + "vehicle1.xls"),
                DataLab.taskList(path + "task1.xls", -1), para.getSize(), para.getFitnessType(), para.getN());
        SolutionCode solve = ga.solve();
        System.out.println(solve.print());
        System.out.println(solve.getFitness());
    }

}
