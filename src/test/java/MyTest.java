
public class MyTest {
    public static void main(String[] args) {
        int[] maxgens = new int[]{10, 20, 30};
        int[] np = new int[]{50, 100, 300};
        int[] alpaha = new int[]{0, 50, 100, 300};
        int[] NN = new int[]{0, 10, 100, 1000};
        int[] B = new int[]{10, 15, 30};

        for (int gen : maxgens) {
            for (int n : np) {
                for (int a : alpaha) {
                    for (int b : alpaha) {
                        for (int nn : NN) {
                            for (int b1 : B) {
                                for (int b2 : B) {
                                    System.out.println(gen + "\t" + n + "\t" +
                                            a + "\t" + b + "\t" + nn + "\t" +
                                            b1 + "\t" + b2);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
