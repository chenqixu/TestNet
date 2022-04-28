package com.cqx.netty.example.echo;

import java.util.ArrayList;
import java.util.List;

/**
 * SDTP队列链表
 *
 * @author chenqixu
 */
public class SDTPLinkedQueue {
    private volatile int size;
    private volatile List<LinkedQueue> linkedQueueList;
    private volatile LinkedQueue currentLQ;

    public SDTPLinkedQueue(int size) {
        if (size <= 1) throw new NullPointerException("大小不能小于等于1！");
        this.size = size;
        linkedQueueList = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            linkedQueueList.add(new LinkedQueue("" + i));
        }
        for (int i = 0; i < size; i++) {
            if ((i + 1) < size) {
                linkedQueueList.get(i).setNext(linkedQueueList.get(i + 1));
            } else {
                linkedQueueList.get(i).setNext(linkedQueueList.get(0));
            }
        }
    }

    public synchronized LinkedQueue next() {
        if (currentLQ == null) {
            currentLQ = linkedQueueList.get(0);
        } else {
            currentLQ = currentLQ.next();
        }
        return currentLQ;
    }

    public synchronized LinkedQueue get(int index) {
        if (index >= size || index < 0) throw new NullPointerException("List的索引 " + index + " 超出范围！");
        return linkedQueueList.get(index);
    }

    public int getSize() {
        return size;
    }
}
