package com.cqx.yarn.rm.test.bean;

import com.cqx.yarn.rm.test.TaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 任务信息
 *
 * @author chenqixu
 */
public class Task {
    private static final String PREFIX = "【任务信息】";
    private static final Logger logger = LoggerFactory.getLogger(Task.class);
    static ConcurrentHashMap<String, LinkedBlockingQueue<TaskBean>> taskMap;
    static List<TaskManager> taskManagerList;

    static {
        taskMap = new ConcurrentHashMap<>();
        taskManagerList = new ArrayList<>();
        // 任务分配和队列的关系映射
        TaskManager tm1 = new TaskManager("tm1");
        tm1.allocationQueue("root");
        tm1.allocationQueue("edc_base");
        taskManagerList.add(tm1);
        logger.info("{} {} 分配到了 {}", PREFIX, tm1, tm1.getQueueList());
    }

    public static synchronized void put(String queueName, TaskBean taskBean) throws InterruptedException {
        LinkedBlockingQueue<TaskBean> taskBeanQueue = taskMap.get(queueName);
        if (taskBeanQueue == null) {
            taskBeanQueue = new LinkedBlockingQueue<>();
            taskMap.put(queueName, taskBeanQueue);
        }
        taskBeanQueue.put(taskBean);
    }

    public static synchronized TaskBean poll(String queueName) {
        LinkedBlockingQueue<TaskBean> taskBeanQueue = taskMap.get(queueName);
        if (taskBeanQueue != null) {
            return taskBeanQueue.poll();
        }
        return null;
    }

    public static List<TaskManager> getTaskManagerList() {
        return taskManagerList;
    }
}
