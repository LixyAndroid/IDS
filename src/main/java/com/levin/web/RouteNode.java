package com.levin.web;

import com.levin.excel.TransportTask;
import lombok.Data;

@Data
public class RouteNode {
    public enum NODE_TYPE {
        GET(2), DELIVERY(3);
        private int code;

        NODE_TYPE(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    private TransportTask task;
    private int type;

    public RouteNode() {
    }

    public RouteNode(TransportTask task, int type) {
        this.task = task;
        this.type = type;
    }
}
