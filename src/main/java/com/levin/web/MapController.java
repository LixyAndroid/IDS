package com.levin.web;

import com.levin.core.algo.SolverFactory;
import com.levin.core.entity.code.OrderCode;
import com.levin.core.entity.code.SolutionCode;
import com.levin.core.entity.code.VehicleCode;
import com.levin.excel.DataLab;
import com.levin.excel.Driver;
import com.levin.excel.TransportTask;
import com.levin.util.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/map")
public class MapController {
    @GetMapping({"", "/index"})
    public String map() {
        return "map";
    }


    @RequestMapping("/data")
    @ResponseBody
    public Result route(String algo, AlgoPara para) {
        para = new AlgoPara();
        long start = System.currentTimeMillis();
        SolutionCode solve = SolverFactory.solve(algo, para);
        return process(solve, start);
    }

    private Result process(SolutionCode solve, long start) {
        List<Route> routeList = new ArrayList<>();
        for (VehicleCode vc : solve.vehicleCodeList) {
            if (vc.getValue() == 1) {
                List<RouteNode> nodeList = new ArrayList<>();
                for (OrderCode oc : vc.getOrderCodeList()) {
                    nodeList.add(new RouteNode(oc.getTask(), oc.getType()));
                }
                routeList.add(new Route(vc.getDriver(), nodeList));
            }
        }

        Result result = new Result();
        result.setFitness(solve.getFitness());
        result.setTime((System.currentTimeMillis() - start) / 1000.0);
        result.setRouteList(routeList);
        return result;
    }

    @RequestMapping("/driver")
    @ResponseBody
    public List<Driver> driver() {
        return DataLab.driverList(FileUtils.getAppPath() + "/src/main/resources/vehicle.xls");
    }

    @RequestMapping("/task")
    @ResponseBody
    public List<TransportTask> task() {
        return DataLab.taskList(FileUtils.getAppPath() + "/src/main/resources/task.xls");
    }
}
