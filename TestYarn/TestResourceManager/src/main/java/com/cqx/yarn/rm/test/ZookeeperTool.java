package com.cqx.yarn.rm.test;

/**
 * zookeeper工具
 *
 * @author chenqixu
 */
public class ZookeeperTool {
    static ResourceManager activeRM;

    public static ResourceManager getActiveRM() {
        return activeRM;
    }

    public static void setActiveRM(ResourceManager activeRM) {
        ZookeeperTool.activeRM = activeRM;
    }
}
