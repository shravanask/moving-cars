package com.shravan.project.movingcars.model;

public class ThreadId extends ThreadLocal<Integer> {

    private int nextId;

    public ThreadId() {
        nextId = 1;
    }

    private synchronized Integer getNewId() {

        Integer id = new Integer(nextId);
        nextId++;
        return id;
    }

    @Override
    protected Integer initialValue() {

        return getNewId();
    }
}
