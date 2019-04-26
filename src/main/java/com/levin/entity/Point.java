package com.levin.entity;

/**
 * 坐标点
 */
public class Point {
    /**
     * 维度
     */
    private double lat;
    /**
     * 经度
     */
    private double lng;

    /**
     * 位置名称
     */
    private String addr;


    public Point() {
    }

    public Point(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public Point(double lat, double lng, String saddr) {
        this.lat = lat;
        this.lng = lng;
        this.addr = saddr;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    @Override
    public String toString() {
        return "Point{" +
                "lat=" + lat +
                ", lng=" + lng +
                '}';
    }
}
