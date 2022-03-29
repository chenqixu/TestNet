package com.cqx.yarn.rm.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局资源管理器
 *
 * @author chenqixu
 */
public class ResourceManager {
    private static final Logger logger = LoggerFactory.getLogger(ResourceManager.class);
    private static final String PREFIX = "【RM】";
    // 节点管理器
    private static ConcurrentHashMap<String, NodeManager> nodeMap = new ConcurrentHashMap<>();
    // 队列管理器
    private static ConcurrentHashMap<String, QueueManager> queueMap = new ConcurrentHashMap<>();

    static {
        // 队列分配，队列总资源不能超过节点资源总和
        QueueManager root = new QueueManager("root", 5);
        queueMap.put(root.getQueueName(), root);
        logger.info("{} 队列资源分配 {}", PREFIX, root);
        QueueManager edc_base = new QueueManager("edc_base", 2);
        queueMap.put(edc_base.getQueueName(), edc_base);
        logger.info("{} 队列资源分配 {}", PREFIX, edc_base);
    }

    private String ip;

    public ResourceManager() {
        // 获取ip
        this.ip = "10.1.8.203";
        logger.info("{} 启动：{}", PREFIX, getIp());
    }

    public NodeManager getNode(String nodeName) {
        return nodeMap.get(nodeName);
    }

    public QueueManager getQueue(String queueName) {
        return queueMap.get(queueName);
    }

    public void register(String nodeName, NodeManager nodeManager) {
        logger.info("{} NM节点 {} 进行注册", PREFIX, nodeName);
        NodeManager node = getNode(nodeName);
        if (node == null) {
            nodeMap.put(nodeName, nodeManager);
            logger.info("{} NM节点注册成功：{}", PREFIX, nodeManager);
        } else {
            logger.warn("{} NM节点 {} 已经注册过", PREFIX, node);
        }
    }

    private NodeManager getIdleNode() {
        NodeManager ret = null;
        int idle = -1;
        for (Map.Entry<String, NodeManager> stringNodeBeanEntry : nodeMap.entrySet()) {
            NodeManager now = stringNodeBeanEntry.getValue();
            int newIdle = now.getBalanceCore();
            if (newIdle > idle) {
                ret = now;
                idle = newIdle;
            }
        }
        return ret;
    }

    private int queryResosurce(String queueName) {
        return getQueue(queueName).getBalanceCore();
    }

    private NodeManager allocateResource(String queueName) {
        // 获取所有资源节点中最空闲的节点
        NodeManager node = getIdleNode();
        if (node != null) {
            // 判断节点是否有资源
            if (node.getBalanceCore() > 0) {
                // 资源申请
                node.apply();
                getQueue(queueName).apply();
            } else {
                logger.warn("{} 节点{}没有空闲资源", PREFIX, node);
            }
        } else {
            logger.warn("{} 找不到空闲节点", PREFIX);
        }
        return node;
    }

    /**
     * 资源申请
     *
     * @param queueName
     * @return
     */
    public NodeManager applyResource(String queueName) {
        NodeManager nm = null;
        // 资源查询
        int surplus = queryResosurce(queueName);
        if (surplus > 0) {
            // 分配资源
            nm = allocateResource(queueName);
        } else {
            logger.info("{} 向 {} 队列申请 {} 个资源失败，资源不足", PREFIX, queueName, 1);
        }
        return nm;
    }

    /**
     * 资源释放
     *
     * @param queueName
     */
    public void releaseResource(String queueName, String nodeName) {
        getNode(nodeName).release();
        getQueue(queueName).release();
    }

    public String getIp() {
        return ip;
    }
}
