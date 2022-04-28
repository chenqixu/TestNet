package com.cqx.netty.example.echo;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * 队列链表
 *
 * @author chenqixu
 */
public class LinkedQueue<T> {
    private String name;
    private LinkedBlockingQueue<T> linkedBlockingQueue;
    private LinkedQueue next;

    public LinkedQueue(String name) {
        this(name, 10000);
    }

    public LinkedQueue(String name, int queueSize) {
        this.name = name;
        this.linkedBlockingQueue = new LinkedBlockingQueue<>(queueSize);
    }

    @Override
    public String toString() {
        return getName();
    }

    public LinkedQueue next() {
        return next;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNext(LinkedQueue next) {
        this.next = next;
    }

    public LinkedBlockingQueue<T> getLinkedBlockingQueue() {
        return linkedBlockingQueue;
    }
}
