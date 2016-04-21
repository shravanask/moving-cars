package com.shravan.project.movingcars.model;

public class ThreadId extends ThreadLocal<Integer> {

    private int nextId;
    private int xMapSize;
    private int yMapSize;

    public ThreadId() {
        nextId = 1;
    }

    /**
     * Syncronized method to make sure a unique id incremented by 1 (starting
     * from 1) is given to each instance created.
     * 
     * @return
     */
    private synchronized Integer getNewId() {

        Integer id = new Integer(nextId);
        nextId++;
        return id;
    }

    @Override
    protected Integer initialValue() {

        return getNewId();
    }

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

//    public int getCurrentId() {
//
//        return nextId - 1;
//    }
}
