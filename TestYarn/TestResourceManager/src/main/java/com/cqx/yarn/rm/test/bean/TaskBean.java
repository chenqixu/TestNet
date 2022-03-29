package com.cqx.yarn.rm.test.bean;

import java.util.Map;

/**
 * 任务
 *
 * @author chenqixu
 */
public class TaskBean {
    private String taskName;
    private TaskType taskType;
    private Map<String, String> taskParams;

    public TaskBean(String taskName, TaskType taskType, Map<String, String> taskParams) {
        this.taskName = taskName;
        this.taskType = taskType;
        this.taskParams = taskParams;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public Map<String, String> getTaskParams() {
        return taskParams;
    }

    public void setTaskParams(Map<String, String> taskParams) {
        this.taskParams = taskParams;
    }

    public enum TaskType {
        Discovery,
        ;
    }
}
