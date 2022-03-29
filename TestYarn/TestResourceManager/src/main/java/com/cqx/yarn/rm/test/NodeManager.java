package com.cqx.yarn.rm.test;

import com.cqx.yarn.rm.test.bean.TaskBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 节点资源管理器
 *
 * @author chenqixu
 */
public class NodeManager {
    private static final Logger logger = LoggerFactory.getLogger(NodeManager.class);
    private static final String PREFIX = "【NM】";
    private String ip;
    private String nodeName;
    private volatile int core;
    private volatile int balanceCore;

    public NodeManager(String nodeName) {
        // 获取ip
        this.ip = "10.1.8.203";
        this.nodeName = nodeName;
        // 一台主机只能启动一个NodeManager，从本地获取cpu core
        this.core = 10;
        this.balanceCore = core;
        // 从zookeeper获取当前active的RM
        ResourceManager activeRM = ZookeeperTool.getActiveRM();
        logger.info("{} 从zookeeper获取当前active的RM {}", PREFIX, activeRM);
        // 向RM注册
        activeRM.register(nodeName, this);
    }

    public String toString() {
        return String.format("名称：%s，ip：%s，总资源：%s", getNodeName(), getIp(), getCore());
    }

    public synchronized void apply() {
        balanceCore--;
        logger.info("{} 资源申请", PREFIX);
    }

    public synchronized void release() {
        balanceCore++;
        logger.info("{} 资源释放", PREFIX);
    }

    public Container submitTask(TaskBean taskBean) {
        // 和任务调度管理对接，接收任务参数，启动Container
        logger.info("{} 接收到客户端的提交任务请求 {}", PREFIX, taskBean);
        // 启动容器
        Container container = new Container(taskBean);
        new Thread(container).start();
        return container;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public int getCore() {
        return core;
    }

    public void setCore(int core) {
        this.core = core;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getBalanceCore() {
        return balanceCore;
    }
}
