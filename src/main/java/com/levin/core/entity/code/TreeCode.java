package com.levin.core.entity.code;

import com.levin.core.entity.fitness.Fitness;

import java.util.*;

/**
 * 树形编码
 */
public class TreeCode extends SolutionCode {

    public TreeCode(List<VehicleCode> vehicleCodeList, Fitness fitnessType) {
        super(vehicleCodeList, fitnessType);
    }

    public TreeCode(List<VehicleCode> vehicleCodeList, String fitnessType) {
        super(vehicleCodeList, fitnessType);
    }

    @Override
    public double calFitness() {
        this.fitness = fitnessType.calculate(this);
        return this.fitness;
    }

    @Override
    public SolutionCode bestSolution(int NN) {

        return bestNeighbor(NN);
    }


    /**
     * 禁忌搜索，寻找最优子结构组成的最优领域
     */
    private SolutionCode bestNeighbor(int NN) {
        Set<VehicleCode> tabuList = new HashSet<>();
        List<VehicleCode> vehicleCodes = new ArrayList<>();
        for (VehicleCode vehicleCode : this.vehicleCodeList) {
            double bestF = Double.MAX_VALUE;
            VehicleCode bestV = vehicleCode;
            // 每辆车的订单不变，只改变了其装、送顺序。达到局部最优
            VehicleCode neighbor = new VehicleCode(vehicleCode.getDriver(), vehicleCode.getValue(),
                    shuff(vehicleCode.getOrderCodeList()));
            neighbor.calFitness(fitnessType);
            for (int i = 0; i < NN; i++) {
                if (vehicleCode.getOrderCodeList() == null || vehicleCode.getOrderCodeList().size() <= 2) {
                    break;
                }
                if (!tabuList.contains(neighbor)) {
                    if (neighbor.getFitness() < bestF && neighbor.getFitness() > 0) {
                        bestF = neighbor.getFitness();
                        bestV = neighbor;
                    }
                }
                tabuList.add(neighbor);
                neighbor = new VehicleCode(neighbor.getDriver(), neighbor.getValue(),
                        shuff(neighbor.getOrderCodeList()));
                neighbor.calFitness(fitnessType);
            }
            vehicleCodes.add(bestV);
        }
        return new TreeCode(vehicleCodes, fitnessType);
    }


    /**
     * 重排装箱顺序
     *
     * @param orderCodeList 装箱顺序集合
     */
    private List<OrderCode> shuff(List<OrderCode> orderCodeList) {
        if (orderCodeList == null) {
            return null;
        }
        Set<String> tmp = new HashSet<>();
        Collections.shuffle(orderCodeList);
        List<OrderCode> codeList = new ArrayList<>();
        for (OrderCode oc : orderCodeList) {
            if (!tmp.contains(oc.getTask().getId())) {
                codeList.add(new OrderCode(1, oc.getTask()));
            } else {
                codeList.add(new OrderCode(2, oc.getTask()));
            }
            tmp.add(oc.getTask().getId());
        }
        return codeList;
    }

    @Override
    public String print() {
        int k = 1;
        StringBuilder sb = new StringBuilder();
        for (VehicleCode vc : vehicleCodeList) {
            sb.append(k).append(":\t");

            if (vc.getOrderCodeList() != null) {
                for (OrderCode oc : vc.getOrderCodeList()) {
                    if (oc != null) {
                        sb.append("-->").append(oc.getTask().getId())
                                .append("(").append(oc.getTask().getPlatenNum()).append(",").append(oc.getTask().getStart())
                                .append(",").append(oc.getTask().getEndCity()).append(")");
                    }
                }
            }

            sb.append("\n");
            k++;
        }
        return sb.toString();
    }

    @Override
    public SolutionCode repair() {
        List<VehicleCode> vehicleCodeList2 = new ArrayList<>();
        for (VehicleCode vehicleCode : this.vehicleCodeList) {
            while (vehicleCode.getFitness() < 0) {
                List<OrderCode> temp = new ArrayList<>();
                for (OrderCode oc : vehicleCode.getOrderCodeList()) {
                    if (oc.getType() == 1) {
                        temp.add(new OrderCode(1, oc.getTask()));
                        temp.add(new OrderCode(2, oc.getTask()));
                    }
                }
                vehicleCode = new VehicleCode(vehicleCode.getDriver(), vehicleCode.getValue(), temp);
                vehicleCode.calFitness(fitnessType);
            }

            vehicleCodeList2.add(vehicleCode);
        }

        return new TreeCode(vehicleCodeList2, fitnessType);
    }

    @Override
    public String toString() {
        return "TreeCode{" +
                "vehicleCodeList=" + vehicleCodeList +
                '}';
    }
}
