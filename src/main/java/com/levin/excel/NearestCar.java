package com.levin.excel;


public class NearestCar {
    private Driver car;
    private double distance;

    public NearestCar(Driver car, double distance) {
        this.car = car;
        this.distance = distance;
    }

    public Driver getCar() {
        return car;
    }

    public void setCar(Driver car) {
        this.car = car;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
