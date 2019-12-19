import com.levin.core.algo.SolverFactory;
import com.levin.core.entity.code.SolutionCode;
import com.levin.web.AlgoPara;

public class ParaAnanyse {
    //算法比较
    private static void algoCompare() {
        String[] algos = new String[]{"ss", "ga", "pso", "abc"};
        AlgoPara algoPara = new AlgoPara();
        for (String algo : algos) {
            for (int i = 0; i < 10; i++) {
                SolutionCode solve = SolverFactory.solve(algo, 30, algoPara);
                System.out.println("目标函数：" + solve.getFitness());
                System.out.println("车辆数目：" + solve.getCarNumUsed());
                //System.out.println(solve.print());
            }
        }
    }

    public static void main(String[] args) {
        try {
            algoCompare();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
