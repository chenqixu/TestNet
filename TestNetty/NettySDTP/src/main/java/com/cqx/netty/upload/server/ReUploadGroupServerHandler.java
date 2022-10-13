package com.cqx.netty.upload.server;

import com.alibaba.fastjson.JSON;
import com.cqx.netty.upload.client.ReportRespBean;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * 重报服务端执行器--集团
 *
 * @author chenqixu
 */
public class ReUploadGroupServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ReUploadGroupServerHandler.class);
    private static String reqJson;
    private ReUploadCfgBean reUploadCfgBean;
    private final String SUFFIXNAME = ".ok";


    public ReUploadGroupServerHandler(ReUploadCfgBean reUploadCfgBean) {
        this.reUploadCfgBean = reUploadCfgBean;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ReportRespBean resp = new ReportRespBean();
        ReportGroupReqBean req = null;
        logger.info("收到来自【{}】客户端的消息：{}", ctx.channel().remoteAddress(), msg);
        reqJson = (String) msg;
        String respJson = null;
        try {
            req = JSON.parseObject(reqJson, ReportGroupReqBean.class);

            //两种模式：1、直接查询文件名的==》又分为jk-Dpi文件格式：A
            //yyyy-MM-dd hh:mm:ss
            String startTime = req.getBegainTime();
            String endTime = req.getEndTime();
            String fileName = req.getFileName();
            resp.success(req.getId());

            File scanFile = new File(reUploadCfgBean.getSourceDir());
            StringBuilder files = new StringBuilder();
            //按照文件名扫描数据
            if (fileName != null && !"".equals(fileName)) {
                //根据文件名获取备份的时间
                String dateDir = getFileDateDir(fileName);
                File file = new File(scanFile + "/" + dateDir, fileName);
                if (file.exists()) {
                    //数据文件和校验文件都要移动
                    FileUtils.copyFile(file, new File(reUploadCfgBean.getUploadDir(), fileName));
                    FileUtils.copyFile(new File(scanFile + "/" + dateDir, fileName + SUFFIXNAME), new File(reUploadCfgBean.getUploadDir(), fileName + SUFFIXNAME));
                    logger.info("复制目录：{} 下的文件{},校验文件：{}，到上报目录{}成功！",file.getParent(),file.getName(),file.getName()+SUFFIXNAME,reUploadCfgBean.getUploadDir());
                    resp.setUploadNum(1);
                    files.append(fileName);
                }else{
                    resp.fail(req.getId(),"文件不存在！");
                }
            } else {
                //需要上报的文件列表
                List<File> uploadFiles = filterUploadFileByDate(scanFile, startTime, endTime);
                for (File file : uploadFiles) {
                    //文件直接cp，防止多次上报导致文件不存在
                    FileUtils.copyFile(file, new File(reUploadCfgBean.getUploadDir(), file.getName()));
                    FileUtils.copyFile(new File(file.getPath() + SUFFIXNAME), new File(reUploadCfgBean.getUploadDir(), file.getName() + SUFFIXNAME));
                    logger.info("复制目录：{} 下的文件{},校验文件：{}，到上报目录{}成功！",file.getParent(),file.getName(),file.getName()+SUFFIXNAME,reUploadCfgBean.getUploadDir());
                    files.append(file.getName() + ",");
                }
                resp.setUploadNum(uploadFiles.size());
            }

            resp.setUploadFileNames(files.toString());
            respJson = JSON.toJSONString(resp);
            logger.info("上报成功|" + respJson);
        } catch (Exception e) {
            logger.error("上报处理异常|", e);
            resp.fail(req.getId(), "上报方法执行异常！");
            respJson = JSON.toJSONString(resp);
        } finally {
            //返回调用结果给客户端
            ctx.writeAndFlush(respJson);
            //关闭此次连接
            ctx.close();
        }
    }

    private String getFileDateDir(String fileName) {
        return getFileDate(fileName).substring(0, 10);
    }

    private String getFileDate(String fileName) {
        //IP地址为IPv4地址：A[APP]P[nnnn]D[YYYYMMDDHHMISS]E[nnn].txt.gz
        //IP地址为IPv6地址：A[APP]P[nnnn]D[YYYYMMDDHHMISS]E[nnn]_IPv6.txt.gz
        if (fileName.startsWith("A")) {
            return fileName.substring(fileName.lastIndexOf("D")+1, fileName.lastIndexOf("E"));
        }
        //<业务功能简写>-<设备唯一标识>-<信息ID号>-<文件生成开始时间YYYYMMDDHH24MMSS>-<文件生成结束时间YYYYMMDDHH24MMSS>-< IP日志信息条数>-<文件MD5值>-<文件大小>.txt.gz
        String[] split = fileName.split("-");
        return split[3];
    }

    /**
     * 过滤需要上报的文件列表
     *
     * @param dir
     * @param startTime YYYYMMDDHH24MMSS
     * @param endTime   YYYYMMDDHH24MMSS
     * @return
     */
    private List<File> filterUploadFileByDate(File dir, final String startTime, final String endTime) {
        //jkDpi：A[APP]P[nnnn]D[YYYYMMDDHHMISS]E[nnn].txt.gz
        ////<业务功能简写>-<设备唯一标识>-<信息ID号>-<文件生成开始时间YYYYMMDDHH24MMSS>-<文件生成结束时间YYYYMMDDHH24MMSS>-< IP日志信息条数>-<文件MD5值>-<文件大小>.txt.gz
        List<File> res = new ArrayList<>();


        //开始的时间目录的值(需要根据备份周期目录的配置，截取不同的长度)
        boolean sourceDirIsDay = reUploadCfgBean.getSourceDirType().equals(ReUploadCfgBean.SOURCE_TYPE_DAY);
        final long startDir = Integer.parseInt(sourceDirIsDay? startTime.substring(0, 8) : startTime.substring(0, 10));
        //结束的时间目录的值
        final long endDir = Integer.parseInt(sourceDirIsDay?endTime.substring(0, 8):endTime.substring(0, 10));

        logger.info("上报处理参数：文件开始时间:{},文件结束时间:{}  过滤开始文件夹:{},过滤结束文件夹:{},扫描文件目录：{}"
                ,startTime,endTime,startDir,endDir,dir.getPath());


        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                //文件名
                String fileName = pathname.getName();
                //是否是目录
                if (pathname.isDirectory()) {
                    try {
                        long dirValue = Long.parseLong(fileName);
                        if (dirValue >= startDir && dirValue <= endDir) {
                            return true;
                        }
                    } catch (Exception e) {
                        //非数字类型的文件夹，则不做扫描
                        logger.warn("扫描到非时间命名的文件夹，跳过不处理:" + fileName);
                    }

                }else{//数据文件判断
                    //去除校验文件
                    if(fileName.endsWith(SUFFIXNAME)){
                        return false;
                    }
                    long fileDateDir = Long.parseLong(getFileDate(fileName));
                    if (fileDateDir >= Long.parseLong(startTime) && fileDateDir <= Long.parseLong(endTime)) {
                        return true;
                    }
                }
                return false;
            }
        });

        for (File file1 : files) {
            if (file1.isFile()) {
                res.add(file1);
            } else {
                List<File> filList = filterUploadFileByDate(file1, startTime, endTime);
                res.addAll(filList);
            }
        }
        return res;

    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        ctx.close();
    }
}
