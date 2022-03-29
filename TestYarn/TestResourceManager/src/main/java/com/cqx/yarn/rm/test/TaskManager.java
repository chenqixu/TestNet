package com.cqx.yarn.rm.test;

import com.cqx.common.utils.system.SleepUtil;
import com.cqx.yarn.rm.test.bean.Task;
import com.cqx.yarn.rm.test.bean.TaskBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务管理
 *
 * @author chenqixu
 */
public class TaskManager {
    private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);
    private static final String PREFIX = "【TM】";
    private static final String PREFIX_TA = "【TM.TA】";
    private static final String PREFIX_AM = "【TM.AM】";
    private String ip;
    private String taskManagerName;
    private List<String> queueList = new ArrayList<>();

    public TaskManager(String taskManagerName) {
        this.taskManagerName = taskManagerName;
        this.ip = "10.1.8.203";
    }

    public void start() {
        logger.info("{} 启动任务管理，进行任务分配", PREFIX);
        for (String queueName : getQueueList()) {
            new Thread(new TaskAllocation(queueName)).start();
        }
    }

    public void allocationQueue(String queueName) {
        this.queueList.add(queueName);
    }

    public String toString() {
        return String.format("任务管理器%s，ip：%s", getTaskManagerName(), getIp());
    }

    public String getIp() {
        return ip;
    }

    public String getTaskManagerName() {
        return taskManagerName;
    }

    public List<String> getQueueList() {
        return queueList;
    }

    /**
     * 任务分配
     */
    class TaskAllocation implements Runnable {
        String queueName;
        volatile boolean flag = true;

        TaskAllocation(String queueName) {
            this.queueName = queueName;
        }

        @Override
        public void run() {
            //====================================
            // 任务调度管理
            //====================================
            // 向RM申请资源
            // 申请成功，提交任务到对应节点
            while (flag) {
                TaskBean taskBean = Task.poll(this.queueName);
                if (taskBean != null) {
                    logger.info("{} 刷到任务 {}", PREFIX_TA, taskBean);
                    new ApplicationManager(this.queueName, taskBean).exec();
                } else {
                    SleepUtil.sleepMilliSecond(2000);
                }
            }
        }

        public void stop() {
            this.flag = false;
        }
    }

    /**
     * 应用管理器（任务调度）
     */
    class ApplicationManager {
        String queueName;
        TaskBean taskBean;

        ApplicationManager(String queueName, TaskBean taskBean) {
            this.queueName = queueName;
            this.taskBean = taskBean;
        }

        public void exec() {
            // 从zookeeper获取当前active的RM
            ResourceManager activeRM = ZookeeperTool.getActiveRM();
            logger.info("{} 从zookeeper获取当前active的RM {}", PREFIX_AM, activeRM);
            // 向RM申请资源
            NodeManager nm = activeRM.applyResource(this.queueName);
            if (nm != null) {
                logger.info("{} 向RM申请资源成功 {}", PREFIX_AM, nm);
                // 提交任务到对应节点
                Container container = nm.submitTask(this.taskBean);
                while (!container.getRet()) {
                    SleepUtil.sleepMilliSecond(200);
                }
                // 容器执行完成，资源释放
                activeRM.releaseResource(this.queueName, nm.getNodeName());
            } else {
                logger.warn("{} 向RM申请资源失败", PREFIX_AM);
            }
        }
    }
}
