package com.levin.core.entity.code;

import com.levin.entity.GeneLink;
import com.levin.entity.Gene;
import com.levin.entity.SO;
import com.levin.excel.DataLab;
import com.levin.excel.Driver;
import com.levin.excel.TransportTask;

import java.util.*;

public class LayerCodeUtil {
    private static final Random random = new Random(System.currentTimeMillis());

    /**
     * 随机分段
     */
    public static List<Integer> random(int n, int L) {
        List<Integer> res = new ArrayList<>();
        int nn = n;
        for (int i = 0; i < n - 1; i++) {
            if (L <= 0) {
                res.add(0);
            } else {
                int a = Math.max(1, random.nextInt(Math.max(L / nn + 1, 1)));
                int b = random.nextInt(a) + L / nn;
                L = L - b;
                if (L <= 0) {
                    res.add(L + b);
                } else {
                    res.add(b);
                }
            }
            nn--;
        }

        res.add(L < 0 ? 0 : L);
        Collections.shuffle(res);
        return res;
    }

    public static LayerCode genCode(List<Driver> driverList, List<TransportTask> taskList, String fitnessType) {
        //乱序
        Collections.shuffle(driverList);
        Collections.shuffle(taskList);

        //将订单按照车辆数切分
        List<Integer> random = LayerCodeUtil.random(driverList.size(), taskList.size());
        List<List<TransportTask>> orders = new ArrayList<>();
        int p = 0;
        for (int k : random) {
            orders.add(taskList.subList(p, p + k));
            p += k;
        }
        List<VehicleCode> vehicleCodeList = new ArrayList<>();
        int i = 0;
        for (Driver driver : driverList) {
            List<OrderCode> codeList = new ArrayList<>();
            List<TransportTask> transportTasks = orders.get(i);
            if (transportTasks != null && transportTasks.size() > 0) {
                List<Integer> shuff = shuff(transportTasks.size());
                Set<Integer> keySet = new HashSet<>();
                for (int k : shuff) {
                    TransportTask order = transportTasks.get(k - 1);
                    if (keySet.contains(k)) {
                        codeList.add(new OrderCode(2, order));
                    } else {
                        codeList.add(new OrderCode(1, order));
                    }
                    keySet.add(k);
                }
            }
            int value = codeList.size() == 0 ? 0 : 1;
            VehicleCode vc = new VehicleCode(driver, value, codeList);
            vehicleCodeList.add(vc);
            i++;
        }
        return new LayerCode(vehicleCodeList, fitnessType);
    }

    public static List<Integer> shuff(int size) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(i + 1);
            list.add(i + 1);
        }
        Collections.shuffle(list);
        return list;
    }

    public static double ratio(String str1, String str2) {
        int d[][];
        int n = str1.length();
        int m = str2.length();
        int i;
        int j;
        char ch1;
        char ch2;
        int temp;
        if (n == 0) {
            return 0;
        }
        if (m == 0) {
            return 0;
        }
        d = new int[n + 1][m + 1];
        for (i = 0; i <= n; i++) {
            d[i][0] = i;
        }
        for (j = 0; j <= m; j++) {
            d[0][j] = j;
        }
        for (i = 1; i <= n; i++) {
            ch1 = str1.charAt(i - 1);
            for (j = 1; j <= m; j++) {
                ch2 = str2.charAt(j - 1);
                if (ch1 == ch2) {
                    temp = 0;
                } else {
                    temp = 2;
                }
                d[i][j] =
                        Math.min(Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1), Math.min(d[i][j - 1] + 1, d[i - 1][j - 1] + temp));
            }
        }
        return (double) (m + n - d[n][m]) / (double) (m + n);
    }

    /**
     * 生成交换对，把a变成和b一样，返回需要交换的下标对列表
     */
    public static ArrayList<SO> minus(Gene[] a, Gene[] b) {
        Gene[] tmp = a.clone();
        ArrayList<SO> list = new ArrayList<>();
        int index = 0;
        for (int i = 0; i < b.length; i++) {
            if (tmp[i] != b[i]) {
                //在tmp中找到和b[i]相等的值，将下标存储起来
                for (int j = i + 1; j < tmp.length; j++) {
                    if (tmp[j] == b[i]) {
                        index = j;
                        break;
                    }
                }
                SO so = new SO(i, index);
                list.add(so);
            }
        }
        return list;
    }

    /**
     * 执行交换，更新粒子
     *
     * @param path Gene[]
     * @param vii  存储的是需要交换的下标对
     */
    public static void exchange(Gene[] path, List<SO> vii) {
        Gene tmp;
        for (SO so : vii) {
            int x = so.getX();
            int y = so.getY();
            tmp = path[x];
            //车<-->车
            if (tmp.getType() == Gene.GENE_TYPE.TYPE_CAR.getCode() && path[y].getType() == Gene.GENE_TYPE.TYPE_CAR.getCode()) {
                //System.out.println("//车<-->车");
                path[so.getX()] = path[so.getY()];
                path[so.getY()] = tmp;

            } else if (tmp.getType() != Gene.GENE_TYPE.TYPE_CAR.getCode() && path[y].getType() != Gene.GENE_TYPE.TYPE_CAR.getCode()) {
                //订单<-->订单
                //System.out.println("订单<-->订单");
                int x1 = findOpp(tmp, path);
                int y1 = findOpp(path[y], path);
                if (tmp.getType() == path[y].getType()) {
                    path[so.getX()] = path[so.getY()];
                    path[so.getY()] = tmp;

                    tmp = path[x1];
                    path[x1] = path[y1];
                    path[y1] = tmp;
                } else {
                    path[so.getX()] = path[y1];
                    path[y1] = tmp;

                    tmp = path[x1];
                    path[x1] = path[so.getY()];
                    path[so.getY()] = tmp;
                }
            } else {  //订单<-->车
                //System.out.println("订单<-->车");
                if (tmp.getType() == Gene.GENE_TYPE.TYPE_CAR.getCode()) {
                    path = reGene(x, y, path);
                } else {//车<-->订单
                    path = reGene(y, x, path);
                }
            }

        }
    }


    /**
     * 修复基因
     *
     * @param x    车辆码索引
     * @param y    订单码索引
     * @param path Gene[]
     */
    private static Gene[] reGene(int x, int y, Gene[] path) {
        if (x == 0) {
            x++;
        }
        if (x == y) {
            return path;
        }

        //test
        if (x != y) {
            return path;
        }

        GeneLink<Gene> gl = new GeneLink<>(path);
        int[] yCar = finfNearCar(path, y);
        int yc1 = yCar[0];
        int yc2 = yCar[1];
        Gene xGene = path[x]; //车辆基因
        Gene yGene = path[y]; //订单基因
        Gene gc1 = path[yc1]; //替换位置之前车辆基因
        Gene gc2 = path[yc2]; //替换位置之后车辆基因
        Gene y1 = findOppGene(path[y]);
        //交换基因
        gl.exchange(xGene, yGene);
        if (path[y].getType() == Gene.GENE_TYPE.TYPE_ORDER_GET.getCode()) {  //车<-->取货码
            gl.removeNode(y1);                         //删除送货码基因
            gl.addNode(y1, gl.idx(yGene) + 1);    //将送货码至于取货码之后
        } else {//车<-->送货码
            gl.removeNode(y1);
            gl.addNode(y1, gl.idx(yGene) - 1);    //将取货码至于送货码之前
        }


        //car1(yc1)....替换位置(y)....car2(yc2)
        //修复替换位置前的基因
        yc1 = gl.idx(gc1);
        y = gl.idx(xGene);
        for (int i = yc1; i < y; i++) {
            Gene gene = gl.getNode(i).data;
            if (gene.getType() == Gene.GENE_TYPE.TYPE_ORDER_GET.getCode()) {
                Gene opp = findOppGene(gene); //找对应的送货码索引
                if (gl.idx(opp) > y) { //如果送货码索引大于替换位置索引，则将送货码前移到替换位置之前
                    gl.removeNode(opp);
                    gl.addNode(opp, y - 1);
                }
            }

        }
        //修复替换位置后的基因
        yc2 = gl.idx(gc2);
        y = gl.idx(xGene);
        for (int i = y + 1; i <= yc2; i++) {
            Gene gene = gl.getNode(i).data;
            if (gene.getType() == Gene.GENE_TYPE.TYPE_ORDER_PUT.getCode()) {
                Gene opp = findOppGene(gene); //找对应的送货码索引
                if (gl.idx(opp) < y) { //如果取货码索引小于替换位置索引，则将送货码后移到替换位置之后
                    gl.removeNode(opp);
                    gl.addNode(opp, y + 1);

                }
            }

        }
        List<Gene> genes = gl.toArray();
        Gene[] newPath = new Gene[genes.size()];
        for (int i = 0; i < genes.size(); i++) {
            newPath[i] = genes.get(i);
        }
        return newPath;
    }

    private static int[] finfNearCar(Gene[] path, int index) {
        int p = -1;
        int q = -1;
        int tmp = -1;
        for (int i = 0; i < path.length; i++) {
            Gene gene = path[i];
            if (i == index) {
                p = tmp;
            }

            if (gene.getType() == Gene.GENE_TYPE.TYPE_CAR.getCode()) {
                if (i > index) {
                    q = i;
                    break;
                }

                tmp = i;
            }
        }
        if (q == -1)
            q = path.length - 1;
        return new int[]{p, q};
    }

    private static int findOpp(Gene gene, Gene[] path) {
        int index = 0;
        for (Gene g : path) {
            if (g.getCode() == gene.getCode() && g.getType() == 5 - gene.getType()) {
                return index;
            }
            index++;
        }
        return -1;
    }

    private static Gene findOppGene(Gene gene) {
        if (gene.getType() == Gene.GENE_TYPE.TYPE_ORDER_GET.getCode()) {
            return new Gene(Gene.GENE_TYPE.TYPE_ORDER_PUT.getCode(), gene.getCode());
        } else if (gene.getType() == Gene.GENE_TYPE.TYPE_ORDER_PUT.getCode()) {
            return new Gene(Gene.GENE_TYPE.TYPE_ORDER_GET.getCode(), gene.getCode());
        } else {
            return gene;
        }
    }

    public static LayerCode toCode(Gene[] path,String fitnessType) {
        List<VehicleCode> vehicleCodeList = new ArrayList<>();
        Driver driver = DataLab.getDriver(path[0].getCode());
        List<OrderCode> taskList = new ArrayList<>();
        for (int i = 1; i < path.length; i++) {
            if (path[i].getType() == Gene.GENE_TYPE.TYPE_CAR.getCode()) {
                vehicleCodeList.add(new VehicleCode(driver, taskList.size() == 0 ? 0 : 1, taskList));
                driver = DataLab.getDriver(path[i].getCode());
                taskList = new ArrayList<>();
            } else {
                taskList.add(new OrderCode(path[i].getType() - 1, DataLab.getTask(path[i].getCode())));
            }
        }
        vehicleCodeList.add(new VehicleCode(driver, taskList.size() == 0 ? 0 : 1, taskList));
        return new LayerCode(vehicleCodeList, fitnessType);
    }
}
