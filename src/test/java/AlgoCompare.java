import com.levin.core.algo.SolverFactory;
import com.levin.core.entity.code.SolutionCode;
import com.levin.util.FileUtils;
import com.levin.web.AlgoPara;

public class AlgoCompare {
    public static void main(String[] args) {
        int scale = 10;
        String path = FileUtils.getAppPath() + "/compare.txt";
        String[] algos = new String[]{"ss", "ga", "cfps", "cfpsst"};
        AlgoPara para = new AlgoPara();
        for (int n = 2; n < scale; n++) {
            StringBuilder content = new StringBuilder(n + "\t");
            for (String algo : algos) {
                long start = System.currentTimeMillis();
                SolutionCode code = SolverFactory.solve(algo, n, para);
                double time = (System.currentTimeMillis() - start) / 1000.0;
                double fitness = code.getFitness();
                content.append(algo).append("\t").append(time).append("\t").append(fitness).append("\t");
                System.out.println(content);
            }
            content.append("\n");
            FileUtils.writeFile(path, content.toString());
        }
    }
}
