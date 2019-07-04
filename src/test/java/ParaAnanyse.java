import com.levin.core.algo.SolverFactory;
import com.levin.core.entity.code.SolutionCode;
import com.levin.web.AlgoPara;

public class ParaAnanyse {
    public static void main(String[] args) {
        String algo = "pso";
        AlgoPara algoPara = new AlgoPara();
        algoPara.setMaxGen(100);
        algoPara.setFitnessType("profit");
        SolutionCode solve = SolverFactory.solve(algo, 30, algoPara);
        System.out.println("目标函数："+solve.getFitness());
        System.out.println("车辆数目："+solve.getCarNumUsed());
        //System.out.println(solve.print());
    }
}
