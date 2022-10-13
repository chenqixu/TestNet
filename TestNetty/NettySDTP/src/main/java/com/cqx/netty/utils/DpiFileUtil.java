package com.cqx.netty.utils;

import com.cqx.common.utils.system.TimeCostUtil;
import com.cqx.netty.exception.DpiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * DpiFileUtil
 *
 * @author chenqixu
 */
public class DpiFileUtil {

    public static final String fileSparator = File.separator;
    private static Logger logger = LoggerFactory.getLogger(DpiFileUtil.class);
    private TimeCostUtil timeCostUtil = new TimeCostUtil();
    //记录流前文件大小和记录数
    private String fileSize;
    private String fileNum;

    public static DpiFileUtil builder() {
        return new DpiFileUtil();
    }

    public static String endWith(String path) {
        if (path.endsWith(fileSparator)) return path;
        else return path + fileSparator;
    }

    public static void rename(String source, String dist, String filename) {
        String _source = endWith(source) + filename;
        String _dist = endWith(dist) + filename;
        File sourcefile = new File(_source);
        File distfile = new File(_dist);
        if (sourcefile.exists() && sourcefile.isFile() && !distfile.exists()) {
            boolean flag = sourcefile.renameTo(distfile);
            if (!flag) {
                logger.warn("重命名文件失败：_source {}，_dist {} renameFail!", _source, _dist);
            } else {
                logger.info("_source {} [renameTo] _dist {} [result] {}", _source, _dist, flag);
            }
        } else {
            logger.warn("无法重命名文件，原因：_source：{}，_dist：{}，sourcefile.exists() ：{}, sourcefile.isFile()：{} , !distfile.exists()：{}",
                    _source, _dist, sourcefile.exists(), sourcefile.isFile(), !distfile.exists());
        }
    }

    public static void rename(String source, String dist, String filename, String distFileName) {
        String _source = endWith(source) + filename;
        String _dist = endWith(dist) + distFileName;
        File sourcefile = new File(_source);
        File distfile = new File(_dist);
        if (sourcefile.exists() && sourcefile.isFile() && !distfile.exists()) {
            boolean flag = sourcefile.renameTo(distfile);
            if (!flag) {
                logger.warn("重命名文件失败：_source {}，_dist {} renameFail!", _source, _dist);
            } else {
                logger.info("_source {} [renameTo] _dist {} [result] {}", _source, _dist, flag);
            }
        } else {
            logger.warn("无法重命名文件，原因：_source：{}，_dist：{}，sourcefile.exists() ：{}, sourcefile.isFile()：{} , !distfile.exists()：{}",
                    _source, _dist, sourcefile.exists(), sourcefile.isFile(), !distfile.exists());
        }
    }

    /**
     * 判断文件是否存在
     *
     * @param filename
     * @return
     * @throws IOException
     */
    public static boolean isExist(String filename) throws IOException {
        File file = new File(filename);
        if (file.isFile() && file.exists()) {
            return true;
        } else {
            throw new IOException("文件" + filename + "不存在！！！！");
        }
    }

    public static boolean deleteFile(String path, String filename) {
        logger.info("deleteFile，path：{}，filename：{}", path, filename);
        String deletefile = endWith(path) + filename;
        File file = new File(deletefile);
        if (file.exists() && file.isFile()) {
            return file.delete();
        }
        return false;
    }

    public String[] listFile(String path) {
        return listFile(path, null);
    }

    public String[] listFile(String path, final String keyword) {
        File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            if (keyword != null && keyword.length() > 0) {
                logger.info("listFile use keyword：{}.", keyword);
                return file.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(keyword);
                    }
                });
            } else {
                logger.info("listFile not use keyword.");
                return file.list();
            }
        } else {
            logger.warn("path：{}，file not exists：{} or file is not Directory：{}", path, file.exists(), file.isDirectory());
            return new String[0];
        }
    }

    public String[] listFileEndWith(String path, final String endWith) {
        File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            return file.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(endWith);
                }
            });
        } else {
            logger.warn("path：{}，file not exists：{} or file is not Directory：{}", path, file.exists(), file.isDirectory());
        }
        return new String[0];
    }

    /**
     * 通过关键字和文件后缀来过滤文件
     *
     * @param path
     * @param keyword
     * @param endWith
     * @return
     */
    public String[] listFile(String path, final String keyword, final String endWith) {
        File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            if (keyword != null && keyword.length() > 0) {
                logger.info("listFile use keyword：{}，endWith：{}.", keyword, endWith);
                return file.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        if(endWith!=null&&"".equals(endWith)){
                            return name.contains(keyword) && name.endsWith(endWith);
                        }
                        return name.contains(keyword);
                    }
                });
            } else {
                logger.info("listFile not use keyword.");
                return file.list();
            }
        } else {
            logger.warn("path：{}，file not exists：{} or file is not Directory：{}", path, file.exists(), file.isDirectory());
        }
        return new String[0];
    }

    public List<String> readFile(String fileName, IDpiFileDeal iDpiFileDeal) throws Exception {
        return readFile(fileName, "UTF-8", iDpiFileDeal);
    }

    public List<String> readFile(String fileName, String read_code, IDpiFileDeal iDpiFileDeal) throws Exception {
        if (iDpiFileDeal == null) throw new NullPointerException("iDpiFileDeal is null，please init first！");
        List<String> resultlist = new ArrayList<>();
        BufferedReader reader = null;
        try {
            File readFile = new File(fileName);
            setFileSize(String.valueOf(readFile.length()));
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(readFile), read_code));
            String _tmp;
            // 按批次计算消耗时间
            timeCostUtil.start();
            int count = 0;
            int allcost = 0;
            while ((_tmp = reader.readLine()) != null) {
                try {
                    // 逐行处理
                    iDpiFileDeal.run(_tmp);
                } catch (SocketException e) {
                    logger.error("服务端异常，" + e.getMessage(), e);
                    // 服务端异常，需要抛出
                    throw e;
                } catch (DpiException e) {
                    // DPI内部解析异常，压抑，错误发送给下游bolt
                    // 1、内容为空的异常，号码为空
                    // 2、时间异常，起始时间比结束时间大
                    iDpiFileDeal.sendErrorMsgToBolt(_tmp, e.getMessage());
                } catch (Exception e) {
                    // 压抑处理异常，以便可以全部处理完成
                    logger.error("iDpiFileDeal run error，content：" + _tmp + "，msg：" + e.getMessage(), e);
                    // 错误发送给下游bolt
                    iDpiFileDeal.sendErrorMsgToBolt(_tmp, e.getMessage());
                }
                count++;
                if (count % 4000 == 0) {
                    long nowcost = timeCostUtil.stopAndGet();
                    allcost += nowcost;
                    logger.debug("[cost] fileName：{}，cost：{}", fileName, nowcost);
                    timeCostUtil.start();
                }
            }
            setFileNum(String.valueOf(count));
            // 最后处理
            iDpiFileDeal.end();
            long nowcost = timeCostUtil.stopAndGet();
            allcost += nowcost;
            logger.debug("[cost] fileName：{}，last cost：{}，allcost：{}", fileName, nowcost, allcost);
        } catch (Exception e) {
            logger.error("reader " + fileName + " error，" + e.getMessage(), e);
            throw e;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.error(fileName + " close error，" + e.getMessage(), e);
                }
            }
        }
        return resultlist;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileNum() {
        return fileNum;
    }

    public void setFileNum(String fileNum) {
        this.fileNum = fileNum;
    }

}
