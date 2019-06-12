package com.levin.entity;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * 基因
 */
public class Gene {
    public enum GENE_TYPE {
        /**
         * 车辆码
         */
        TYPE_CAR(1),
        /**
         * 取货码
         */
        TYPE_ORDER_GET(2),
        /**
         * 送货码
         */
        TYPE_ORDER_PUT(3);

        private int code;

        GENE_TYPE(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    /**
     * 基因类型
     */
    private int type;
    /**
     * 基因值
     */
    private String code;

    public Gene() {
    }

    public Gene(int type, String code) {
        this.type = type;
        this.code = code;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return type + " " + code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Gene gene = (Gene) o;

        return new EqualsBuilder()
                .append(type, gene.type)
                .append(code, gene.code)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(type)
                .append(code)
                .toHashCode();
    }
}
