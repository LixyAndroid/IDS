package com.levin.entity;

public class CarProp {
    /**
     * 额定载重
     */
    private double G;
    /**
     * 额定载方
     */
    private double V;
    /**
     * 固定成本
     */
    private double C;
    /**
     * 满载单位行驶成本
     */
    private double FC;
    /**
     * 空载单位行驶成本
     */
    private double NC;

    /**
     * 速度
     */
    private double speed;

    public CarProp() {

    }

    public CarProp(double g, double v, double c, double FC, double NC,double speed) {
        G = g;
        V = v;
        C = c;
        this.FC = FC;
        this.NC = NC;
        this.speed = speed;
    }

    public double getG() {
        return G;
    }

    public void setG(double g) {
        G = g;
    }

    public double getV() {
        return V;
    }

    public void setV(double v) {
        V = v;
    }

    public double getC() {
        return C;
    }

    public void setC(double c) {
        C = c;
    }

    public double getFC() {
        return FC;
    }

    public void setFC(double FC) {
        this.FC = FC;
    }

    public double getNC() {
        return NC;
    }

    public void setNC(double NC) {
        this.NC = NC;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
}
