package com.levin.core.draw;

import com.levin.core.algo.CFPSST;
import com.levin.core.entity.dto.PartitionDto;
import com.levin.entity.Point;
import com.levin.excel.DataLab;
import com.levin.excel.Driver;
import com.levin.excel.TransportTask;
import com.levin.util.FileUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class DrawPath extends JPanel {
    private int width = 650;
    private int height = 450;

    private int r = 2;
    private MyFrame frame;

    private Random random = new Random(System.currentTimeMillis());

    public DrawPath(int width, int height, MyFrame frame) {
        this.width = width;
        this.height = height;
        this.setSize(width, height);
        this.frame = frame;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.setBackground(Color.white);
        g.setFont(new Font("宋体", Font.BOLD, 16));
        frame.setTitle("聚类结果");

        //聚类分区
        String path = FileUtils.getAppPath() + "/src/main/resources/";
        CFPSST cfpsst = new CFPSST(0, DataLab.driverList(path + "vehicle.xls"), DataLab.taskList(path + "task.xls"), 1, "distance", 50, 200, 100,100, 10, 30);
        List<PartitionDto> partition = cfpsst.partition2();

        /*int t = 19;
        int i = 0;*/
        //画图
        for (PartitionDto partitionDto : partition) {
            /*if (i++ != t)
                continue;*/
            List<Driver> driverList = partitionDto.getDrivers();
            List<TransportTask> taskList = partitionDto.getTaskList();
            //System.out.println(i++ + "," + driverList.size() + " , " + taskList.size());
            List<com.levin.core.draw.Point> all = new ArrayList<>();
            for (Driver driver : driverList) {
                drawVehicle(g, new Point(driver.getLat(), driver.getLng()));
                all.add(new com.levin.core.draw.Point(driver.getLat(), driver.getLng()));
            }

            for (TransportTask task : taskList) {
                drawOrder(g, new Point(task.getLat1(), task.getLng1()), new Point(task.getLat2(), task.getLng2()));
                all.add(new com.levin.core.draw.Point(task.getLat1(), task.getLng1()));
                all.add(new com.levin.core.draw.Point(task.getLat2(), task.getLng2()));
            }

            LinkedList<com.levin.core.draw.Point> smallestPolygon = MinimumBoundingPolygon.findSmallestPolygon(all);
            if (smallestPolygon != null) {
                //drawBound(g, smallestPolygon);
            }
            //break;
        }

        //savePic(this, "D:/dp.png");
    }

    private void draLx(Graphics g, Point point) {
        int rr = 4;
        double x = point.getLat();
        double y = point.getLng();

        int[] xx = new int[5];
        int[] yy = new int[5];

        xx[0] = (int) (x - rr / Math.sqrt(2));
        xx[2] = (int) (x + rr / Math.sqrt(2));
        xx[1] = (int) (x);
        xx[3] = (int) (x);
        xx[4] = (int) (x - rr / Math.sqrt(2));

        yy[0] = (int) y;
        yy[2] = (int) y;
        yy[1] = (int) (y - rr / Math.sqrt(2));
        yy[3] = (int) (y + rr / Math.sqrt(2));
        yy[4] = (int) (y);

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.red);
        g2d.drawPolygon(xx, yy, 5);
        g2d.fillPolygon(xx, yy, 5);
    }

    private void drawTriangle(Graphics g, Point point) {
        int rr = 4;
        double x = point.getLat();
        double y = point.getLng();

        int[] xx = new int[4];
        int[] yy = new int[4];

        xx[0] = (int) (x - rr / 2);
        xx[1] = (int) (x + rr / 2);
        xx[2] = (int) (x);
        xx[3] = (int) (x - rr / 2);

        yy[0] = (int) (y - Math.sqrt(3) * rr / 6);
        yy[1] = (int) (y - Math.sqrt(3) * rr / 6);
        yy[2] = (int) (y - Math.sqrt(3) * rr * 5 / 6);
        yy[3] = (int) (y - Math.sqrt(3) * rr / 6);

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.green);
        g2d.drawPolygon(xx, yy, 4);
        g2d.fillPolygon(xx, yy, 4);
    }

    private Point trans(Point point) {
        double lat = -point.getLat() * width / 30.0 + 130 * width / 30.0;
        double lng = point.getLng() * height / 25.0 - 45 * height / 25.0;
        //System.out.println(lat + "\t" + lng);
        return new Point((lat / 2 - 800) * 2.5 + 250, (lng / 2 - 500) * 2.5 + 50);
    }

    private void drawBound(Graphics g, LinkedList<com.levin.core.draw.Point> points) {
        Graphics2D g2d = (Graphics2D) g.create();
        points.add(points.get(0));

        // 抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 设置画笔颜色
        g2d.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
        float[] dash = new float[]{5, 10};
        BasicStroke bs2 = new BasicStroke(
                1,                      // 画笔宽度/线宽
                BasicStroke.CAP_SQUARE,
                BasicStroke.JOIN_MITER,
                10.0f,
                dash,                   // 虚线模式数组
                0.0f
        );
        g2d.setStroke(bs2);

        int nPoints = points.size();
        int[] xPoints = new int[nPoints];
        int[] yPoints = new int[nPoints];

        for (int i = 0; i < nPoints; i++) {
            com.levin.core.draw.Point point = points.get(i);
            Point trans = trans(new Point(point.getX(), point.getY()));
            xPoints[i] = (int) trans.getLat();
            yPoints[i] = (int) trans.getLng();
        }

        g2d.drawPolyline(xPoints, yPoints, nPoints);
    }

    private void drawVehicle(Graphics g, Point point) {
        Graphics2D g2d = (Graphics2D) g.create();

        // 抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 设置画笔颜色
        g2d.setColor(Color.BLACK);

        Point point1 = trans(point);
        int starx = (int) point1.getLat() - r;
        int starty = (int) point1.getLng() - r;
        g2d.drawRect(starx, starty, 2 * r, 2 * r);
        g2d.fillRect(starx, starty, 2 * r, 2 * r);

    }

    private void drawOrder(Graphics g, Point start, Point end) {
        // 创建 Graphics 的副本, 需要改变 Graphics 的参数,
        // 这里必须使用副本, 避免影响到 Graphics 原有的设置
        Graphics2D g2d = (Graphics2D) g.create();

        // 抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 设置画笔颜色
        g2d.setColor(Color.DARK_GRAY);

        Point start1 = trans(start);
        Point end1 = trans(end);

        int sx = (int) start1.getLat();
        int sy = (int) start1.getLng();
        int ex = (int) end1.getLat();
        int ey = (int) end1.getLng();

        // 1. 两点绘制线段: 点(20, 50), 点(200, 50)
        g2d.setStroke(new BasicStroke(1));

        g2d.drawLine(sx, sy, ex, ey);

        drawTriangle(g, start1);
        draLx(g, end1);

        /*g2d.setColor(Color.RED);
        g2d.drawRect(sx - r, sy - r, 2 * r, 2 * r);
        g2d.fillRect(sx - r, sy - r, 2 * r, 2 * r);

        g2d.setColor(Color.GREEN);
        g2d.drawRect(ex - r, ey - r, 2 * r, 2 * r);
        g2d.fillRect(ex - r, ey - r, 2 * r, 2 * r);*/

        // 自己创建的副本用完要销毁掉
        g2d.dispose();
    }

    /**
     * 保存图片到指定文件
     */
    public static boolean savePic(DrawPath dp, String path) {
        Dimension imageSize = dp.getSize();
        BufferedImage image = new BufferedImage(imageSize.width, imageSize.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        dp.paint(graphics);
        graphics.dispose();

        try {
            File file = new File(path);
            if (!FileUtils.checkFileExists(file.getPath())) {
                FileUtils.createDir(file.getParent());
            }

            return ImageIO.write(image, "png", new File(path));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
