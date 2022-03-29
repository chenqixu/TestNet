package com.cqx.yarn.rm.test;

import com.cqx.common.utils.system.SleepUtil;
import com.cqx.yarn.rm.test.bean.TaskBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 容器
 *
 * @author chenqixu
 */
public class Container implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Container.class);
    private static final String PREFIX = "【Container】";
    private TaskBean taskBean;
    private volatile boolean ret = false;

    public Container(TaskBean taskBean) {
        this.taskBean = taskBean;
    }

    @Override
    public void run() {
        logger.info("{} 容器启动", PREFIX);
        int cnt = 5;
        while (cnt > 0) {
            logger.info("{} 容器执行中", PREFIX);
            SleepUtil.sleepSecond(1);
            cnt--;
        }
        logger.info("{} 容器执行完成", PREFIX);
        ret = true;
    }

    public boolean getRet() {
        return ret;
    }
}
