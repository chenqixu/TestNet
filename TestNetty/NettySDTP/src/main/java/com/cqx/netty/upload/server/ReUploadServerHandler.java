package com.cqx.netty.upload.server;

import com.alibaba.fastjson.JSON;
import com.cqx.netty.upload.client.ReportReqBean;
import com.cqx.netty.upload.client.ReportRespBean;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 集中存储
 *
 * @author chenqixu
 */
public class ReUploadServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ReUploadServerHandler.class);
    private static String reqJson;
    private ReUploadCfgBean reUploadCfgBean;
    private final String SUFFIXNAME = ".CHK";


    private final String TYPE_KEY = "WLAN、HOME、IDC";
    private DateFormat dateFormat = new SimpleDateFormat("YYYYMMDDHHMMSS");

    public ReUploadServerHandler(ReUploadCfgBean reUploadCfgBean) {
        this.reUploadCfgBean = reUploadCfgBean;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ReportRespBean resp = new ReportRespBean();
        ReportReqBean req = null;
        logger.info("收到来自【{}】客户端的消息：{}", ctx.channel().remoteAddress(), msg);
        reqJson = (String) msg;
        String respJson = null;
        try {
            req = JSON.parseObject(reqJson, ReportReqBean.class);
            String beginFilename = req.getBeginFilename();
            String endFilename = req.getEndFilename();
            //将文件上的时间统一转成数字方便比较
            long startFileTime = getFileTime(beginFilename);
            long endFileTime = getFileTime(endFilename);
            int startFileNum = getFileNum(beginFilename);
            int endFileNum = getFileNum(endFilename);
            long startDirTime = getDirTime(startFileTime);
            long endDirTime = getDirTime(endFileTime);
            logger.info("上报处理过滤参数：文件开始时间：{},文件结束时间：{}，文件开始编号：{}，文件结束编号：{}，过滤开始文件夹：{}，过滤结束文件夹：{}"
                    , startFileTime, endFileTime, startFileNum, endFileNum, startDirTime, endDirTime);
            //根据源文件备份的类型（按小时分文件夹或则按天分文件夹），截取时间的长度，用于与文件夹比较

            //扫描目录
            File scanFile = new File(reUploadCfgBean.getSourceDir());
            if (!scanFile.exists()) {
                logger.error("源文件目录{}不存在", reUploadCfgBean.getSourceDir());
                return;
            }
            List<File> uploadFiles = new ArrayList<>();
            StringBuilder files = new StringBuilder();
            if (beginFilename.equals(endFilename)) {
                File file = new File(reUploadCfgBean.getSourceDir()+""+getDirTime(getFileTime(endFilename)),endFilename);
                if(file.exists()) {
                    uploadFiles.add(file);
                }
            } else {
                //TODO 目前外送数据只按照规范中的时间+文件编号进行重报（不对数据小类型进行判断）
                //需要上报的文件列表
                uploadFiles = filterUploadFile(scanFile, startDirTime, endDirTime, startFileTime, endFileTime, startFileNum, endFileNum);
            }
            for (File file : uploadFiles) {
                //校验文件也需要移动
                FileUtils.copyFile(file, new File(reUploadCfgBean.getUploadDir(), file.getName()));
                FileUtils.copyFile(file, new File(reUploadCfgBean.getUploadDir(), file.getName() + SUFFIXNAME));
                files.append(file.getName() + ",");
            }

            logger.info("找到符合上报条件的文件数：{}，文件名{}", uploadFiles.size(), files.toString());
            resp.success(req.getId());
            resp.setUploadNum(uploadFiles.size());
            resp.setUploadFileNames(files.toString());
            respJson = JSON.toJSONString(resp);
            logger.info("上报成功|" + respJson);
        } catch (Exception e) {
            logger.error("上报处理异常|", e);
            resp.fail(req.getId(), "上报方法执行异常");
            respJson = JSON.toJSONString(resp);
        } finally {
            //返回调用结果给客户端
            ctx.writeAndFlush(respJson);
            //关闭此次连接,连接不关闭==》需要做成对象
            ctx.close();
        }
    }

    /**
     * 过滤需要上报的文件列表
     *
     * @param dir           源目录
     * @param startDirTime  子目录下的起始文件名
     * @param endDirTime    子目录下的结束文件名
     * @param startFileTime 过滤文件名开始时间
     * @param endFileTime   过滤文件名结束时间
     * @param startFileNum  过滤文件名开始编号
     * @param endFileNum    过滤文件名结束编号
     * @return
     */
    private List<File> filterUploadFile(File dir, final long startDirTime, final long endDirTime,
                                        final long startFileTime, final long endFileTime, final int startFileNum, final int endFileNum) {
        List<File> res = new ArrayList<>();

        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                //文件名
                String fileName = pathname.getName();
                //是否是目录
                if (pathname.isDirectory()) {
                    try {
                        int dirValue = Integer.parseInt(fileName);
                        if (dirValue >= startDirTime && dirValue <= endDirTime) {
                            return true;
                        }
                    } catch (Exception e) {
                        //非数字类型的文件夹，则不做扫描
                        logger.warn("扫描到非时间命名的文件夹，跳过不处理:" + fileName);
                    }
                    return false;
                }
                //去除校验文件
                if (fileName.endsWith(SUFFIXNAME)) {
                    return false;
                }

                //非文件夹
                long fileTime = getFileTime(fileName);
                long fileNum = getFileNum(fileName);
                //开始时间和结束时间一致
                if (startFileTime == endFileTime) {
                    if (startFileTime == fileTime && fileNum >= startFileNum && fileNum <= endFileNum) {
                        return true;
                    }
                    return false;
                } else {
                    //开始和结束时间不一致，文件时间在两则之间，不包含等于
                    if (startFileTime < fileTime && fileTime < endFileTime) {
                        return true;
                    }
                    //开始结束时间不一致时，文件等于开始时间，文件编号必须在大于开始编号
                    if (startFileTime == fileTime && fileNum >= startFileNum) {
                        return true;
                    }
                    //开始结束时间不一致时，文件等于结束时间，文件编号必须在小于开始编号
                    if (endFileTime == fileTime && fileNum <= endFileNum) {
                        return true;
                    }
                    return false;
                }
            }
        });

        for (File file1 : files) {
            if (file1.isFile()) {
                res.add(file1);
            } else {
                List<File> filList = filterUploadFile(file1, startDirTime, endDirTime, startFileTime, endFileTime, startFileNum, endFileNum);
                res.addAll(filList);
            }
        }
        return res;

    }

    /**
     * //根据源文件备份的类型（按小时分文件夹或则按天分文件夹），截取时间的长度，用于与文件夹比较
     *
     * @param beginFilename yyyymmddhhmmss
     * @return
     */
    private long getDirTime(long beginFilename) {
        String type = reUploadCfgBean.getSourceDirType();
        if (reUploadCfgBean.SOURCE_TYPE_DAY.equals(type)) {
            return beginFilename / 1000000;
        } else if (reUploadCfgBean.SOURCE_TYPE_HOUR.equals(type)) {
            return beginFilename / 10000;
        }
        return -1;
    }

    /**
     * 根据文件名获取其文件时间（去除后缀，防止时最后一个）
     * WLAN：WLAN上网日志留存系统;
     * HOME: 家宽上网日志留存系统;
     * IDC：IDC/ISP信息安全管理系统;
     * 以上的数据小类型不包含下划线，其他类型包含下划线，时间字段和编号所在的位置不一致
     * 文件上的时间格式：省份_城市_日志留存系统类型_设备编号_日志类型_文件生成时间_文件编号.gz
     * 省份_城市_日志留存系统类型_设备编号_日志类型_文件生成时间.后缀
     * 文件生成时间格式：YYYYMMDDHHMISS
     *
     * @param beginFilename
     * @return
     */
    private long getFileTime(String beginFilename) {
        String type = getFileType(beginFilename);
        String fileTime;
        if (TYPE_KEY.indexOf(type.toUpperCase()) != -1) {
            fileTime =beginFilename.split("_")[5];
        }
        fileTime = beginFilename.split("_")[6];

        return Long.parseLong(fileTime.split("\\.",-1)[0]);

    }




    /**
     * 根据文件名获取其文件编号，去除后缀
     * <p>
     * WLAN：WLAN上网日志留存系统;
     * HOME: 家宽上网日志留存系统;
     * IDC：IDC/ISP信息安全管理系统;
     * 以上的数据小类型不包含下划线，其他类型包含下划线，时间字段和编号所在的位置不一致
     *
     * @param beginFilename
     * @return
     */
    private int getFileNum(String beginFilename) {
        String type = getFileType(beginFilename);
        if(beginFilename.lastIndexOf(".")!=-1) {
            beginFilename = beginFilename.substring(0, beginFilename.lastIndexOf("."));
        }
        if (TYPE_KEY.indexOf(type.toUpperCase()) != -1) {
            return Integer.parseInt(beginFilename.split("_")[6]);
        }
        return Integer.parseInt(beginFilename.split("_")[7]);

    }

    private String getFileType(String beginFilename) {
        return beginFilename.split("_")[2];
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        ctx.close();
    }
}
