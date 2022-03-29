package com.cqx.yarn.rm.test;

import com.cqx.yarn.rm.test.bean.Task;
import com.cqx.yarn.rm.test.bean.TaskBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * 测试服务
 *
 * @author chenqixu
 */
public class TestServer {
    private static final Logger logger = LoggerFactory.getLogger(TestServer.class);
    private static final String PREFIX = "【Server】";

    public static void main(String[] args) throws Exception {
        //====================================
        // RPC远程调用框架
        //====================================

        //====================================
        // 启动RM1，全局资源管理器(active)
        //====================================
        // 提供NM注册服务，及时调整集群可用资源
        // 提供NM管理服务，接收NM的信息汇报，如果心跳异常则从集群剔除，及时调整集群可用资源
        // 主从模式1，如果当前节点不是active，从zookeeper获取active信息，如果zookeeper中存储的RM信息超时，则向zookeeper申请active，申请成功请执行第二步
        // 主从模式2，如果当前节点是active，先从zookeeper判断有没超时，没有超时则需要定时向zookeeper汇报心跳信息，如果超时则需要执行步骤1，重新选举
        // path：/rm/active，info：ip1
        // path：/rm/active/ip1，info：心跳信息
        // path：/rm/active/ip2，info：心跳信息
        // 按配置把资源分配到队列
        // 提供队列资源查询服务
        // 提供资源申请服务，接收客户端提交的资源申请，进行资源分配，返回可用的节点给客户端，及时调整集群可用资源
        // 接收任务调度管理的资源释放申请，释放资源，并通知NM把对应的Container Kill掉
        ResourceManager rm = new ResourceManager();
        // 向zookeeper注册active RM
        ZookeeperTool.setActiveRM(rm);

        //====================================
        // 启动RM2，全局资源管理器(standby)
        //====================================

        //====================================
        // 启动NM1，节点资源管理器
        //====================================
        // 向RM(active)注册
        // 定时向RM(active)进行信息汇报
        // 和任务调度管理对接，接收任务参数，启动Container
        // 接收RM的命令，Kill对应的Container
        NodeManager nm1 = new NodeManager("node1");

        //====================================
        // 启动NM2，节点资源管理器
        //====================================

        //====================================
        // Container
        //====================================
        // 提供任务创建服务，接收NM发送来的参数
        // 定时向任务调度管理进行信息汇报


        //-----------------------------------------------------------------------------------
        //-----------------------------------------------------------------------------------
        // 【任务管理】
        //-----------------------------------------------------------------------------------
        //-----------------------------------------------------------------------------------

        //====================================
        // 启动任务分配
        //====================================
        // 任务分配和队列的关系映射
        // 从任务表中按照分配的队列获取相应任务
        //====================================
        // 任务调度管理
        //====================================
        // 向RM申请资源
        // 申请成功，提交任务到对应节点
        for (TaskManager tm : Task.getTaskManagerList()) {
            tm.start();
        }


        //-----------------------------------------------------------------------------------
        //-----------------------------------------------------------------------------------
        // 【服务】
        //-----------------------------------------------------------------------------------
        //-----------------------------------------------------------------------------------

        //====================================
        // 创建探索服务
        //====================================
        new TestServer().createDiscoveryService("root", "对低龄客户的探索");
    }

    public void createDiscoveryService(String queueName, String taskName) throws InterruptedException {
        Task.put(queueName, new TaskBean(taskName, TaskBean.TaskType.Discovery, new HashMap<String, String>()));
        logger.info("{} 创建探索服务，名称：{}，队列：{}", PREFIX, taskName, queueName);
    }
}
