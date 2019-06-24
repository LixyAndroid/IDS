package com.levin.core.entity.code;

import com.levin.core.entity.fitness.Fitness;
import com.levin.entity.Gene;

import java.util.ArrayList;
import java.util.List;

/**
 * 双链多层编码
 */
public class LayerCode extends SolutionCode {

    public LayerCode(List<VehicleCode> vehicleCodeList, String fitnessType) {
        super(vehicleCodeList, fitnessType);
    }

    public LayerCode(List<VehicleCode> vehicleCodeList, Fitness fitnessType) {
        super(vehicleCodeList, fitnessType);
    }

    public LayerCode() {
    }

    @Override
    public double calFitness() {
        this.fitness = fitnessType.calculate(this);
        return fitness;
    }


    @Override
    public String print() {
        return null;
    }

    @Override
    public SolutionCode repair() {
        return null;
    }

    public Gene[] toGeneArray() {
        List<Gene> geneList = new ArrayList<>();
        for (VehicleCode vc : vehicleCodeList) {
            Gene gene = new Gene(Gene.GENE_TYPE.TYPE_CAR.getCode(), vc.getDriver().getId());
            geneList.add(gene);
            for (OrderCode oc : vc.getOrderCodeList()) {
                Gene g;
                if (oc.getType() == 1) {
                    g = new Gene(Gene.GENE_TYPE.TYPE_ORDER_GET.getCode(), oc.getTask().getId());
                } else {
                    g = new Gene(Gene.GENE_TYPE.TYPE_ORDER_PUT.getCode(), oc.getTask().getId());
                }
                geneList.add(g);
            }

        }

        Gene[] res = new Gene[geneList.size()];
        int i = 0;
        for (Gene g : geneList) {
            res[i++] = g;
        }
        return res;
    }

    @Override
    public String toString() {
        Gene[] mPath = this.toGeneArray();
        StringBuilder path = new StringBuilder();
        for (Gene g : mPath) {
            path.append(g.getCode());
        }
        return path.toString();
    }
}
