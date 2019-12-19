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
    public String print() {
        return super.print();
        /*int k = 1;
        StringBuilder sb = new StringBuilder();
        for (VehicleCode vc : vehicleCodeList) {
            sb.append(k).append(":\t");

            if (vc.getOrderCodeList() != null) {
                for (OrderCode oc : vc.getOrderCodeList()) {
                    if (oc != null) {
                        sb.append("-->").append(oc.getTask().getId())
                                .append("(").append(oc.getType()).append(",").append(oc.getTask().getPlatenNum()).append(",").append(oc.getTask().getStart())
                                .append(",").append(oc.getTask().getEndCity()).append(")");
                    }
                }
            }

            sb.append("\n");
            k++;
        }
        return sb.toString();*/
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
