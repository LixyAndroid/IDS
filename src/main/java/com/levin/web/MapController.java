package com.levin.web;

import com.levin.core.algo.CFPSST;
import com.levin.core.algo.SolverFactory;
import com.levin.core.draw.MinimumBoundingPolygon;
import com.levin.core.entity.code.OrderCode;
import com.levin.core.entity.code.SolutionCode;
import com.levin.core.entity.code.VehicleCode;
import com.levin.core.entity.dto.PartitionDto;
import com.levin.entity.Point;
import com.levin.excel.DataLab;
import com.levin.excel.Driver;
import com.levin.excel.TransportTask;
import com.levin.util.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.LinkedList;
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
        long start = System.currentTimeMillis();
        SolutionCode solve = SolverFactory.solve(algo, 30, para);
        DataLab.clear();
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
        return DataLab.taskList(FileUtils.getAppPath() + "/src/main/resources/task.xls", 30);
    }

    @RequestMapping("/clustering")
    @ResponseBody
    public List<List<Point>> clustering() {
        String path = FileUtils.getAppPath() + "/src/main/resources/";
        CFPSST cfpsst = new CFPSST(0, DataLab.driverList(path + "vehicle.xls"), DataLab.taskList(path + "task.xls",30), 1, "distance", 50, 200, 100, 100, 10, 30);
        List<PartitionDto> partition = cfpsst.partition2();

        List<List<Point>> result = new ArrayList<>();
        for (PartitionDto partitionDto : partition) {
            List<Driver> driverList = partitionDto.getDrivers();
            List<TransportTask> taskList = partitionDto.getTaskList();
            List<Point> all = new ArrayList<>();
            for (Driver driver : driverList) {
                all.add(new Point(driver.getLat(), driver.getLng()));
            }

            for (TransportTask task : taskList) {
                all.add(new Point(task.getLat1(), task.getLng1()));
                all.add(new Point(task.getLat2(), task.getLng2()));
            }

            LinkedList<Point> smallestPolygon = MinimumBoundingPolygon.findSmallestPolygon(all);
            if (smallestPolygon != null) {
                result.add(smallestPolygon);
            }
        }
        return result;
    }
}
