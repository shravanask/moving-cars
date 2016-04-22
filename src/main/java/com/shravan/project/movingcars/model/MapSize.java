package com.shravan.project.movingcars.model;

public class MapSize extends ThreadLocal<Integer> {

    private int xMapSize;
    private int yMapSize;

    public int getxMapSize() {

        return xMapSize;
    }

    public void setxMapSize(int xMapSize) {

        this.xMapSize = xMapSize;
    }

    public int getyMapSize() {

        return yMapSize;
    }

    public void setyMapSize(int yMapSize) {

        this.yMapSize = yMapSize;
    }
}
