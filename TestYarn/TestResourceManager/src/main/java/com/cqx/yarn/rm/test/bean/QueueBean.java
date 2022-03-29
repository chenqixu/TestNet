package com.cqx.yarn.rm.test.bean;

/**
 * 队列
 *
 * @author chenqixu
 */
public class QueueBean {
    private String queueName;
    private int core;

    public QueueBean(String queueName, int core) {
        this.queueName = queueName;
        this.core = core;
    }

    public String toString() {
        return String.format("队列名：%s，队列总资源：%s", getQueueName(), getCore());
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public int getCore() {
        return core;
    }

    public void setCore(int core) {
        this.core = core;
    }
}
