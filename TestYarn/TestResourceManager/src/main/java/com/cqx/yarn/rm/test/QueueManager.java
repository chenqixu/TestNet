package com.cqx.yarn.rm.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 队列管理器
 *
 * @author chenqixu
 */
public class QueueManager {
    private static final Logger logger = LoggerFactory.getLogger(QueueManager.class);
    private static final String PREFIX = "【Queue】";
    private String queueName;
    private int core;
    private volatile int balanceCore;

    public QueueManager(String queueName, int core) {
        this.queueName = queueName;
        this.core = core;
        this.balanceCore = core;
    }

    public String toString() {
        return String.format("队列名：%s，队列总资源：%s", getQueueName(), getCore());
    }

    public synchronized void apply() {
        balanceCore--;
        logger.info("{} 资源申请", PREFIX);
    }

    public synchronized void release() {
        balanceCore++;
        logger.info("{} 资源释放", PREFIX);
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

    public int getBalanceCore() {
        return balanceCore;
    }
}
