package com.shravan.project.movingcars.model;

public class ThreadId extends ThreadLocal<Integer> {

    private int xMapSize;
    private int yMapSize;

//    // Atomic integer containing the next thread ID to be assigned
//    private final AtomicInteger nextId = new AtomicInteger(0);

//    @Override
//    protected Integer initialValue() {
//
//        return nextId.getAndIncrement();
//    }

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
    
//    public Integer getCurrentId() {
//
//        return nextId.get();
//    }
}
