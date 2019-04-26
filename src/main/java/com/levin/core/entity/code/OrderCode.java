package com.levin.core.entity.code;

import com.levin.excel.TransportTask;
import lombok.Data;

/**
 * 装箱顺序
 */
@Data
public class OrderCode {
    public enum TYPE {
        Pick(1), Deliver(2);
        private int code;

        public int getCode() {
            return code;
        }

        TYPE(int code) {
            this.code = code;
        }
    }

    /**
     * 取货还是送货（1：取，2：送）
     */
    private int type;
    /**
     * 订单
     */
    private TransportTask task;


    public OrderCode(int type, TransportTask transportTask) {
        this.type = type;
        this.task = transportTask;
    }
}
