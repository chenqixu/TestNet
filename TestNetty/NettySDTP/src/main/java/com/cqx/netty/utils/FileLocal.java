package com.cqx.netty.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * FileLocal
 *
 * @author chenqixu
 */
public class FileLocal {
    public static final String newLine = System.getProperty("line.separator");
    public static final String writeCode = "UTF-8";
    private static Logger logger = LoggerFactory.getLogger(FileLocal.class);
    private File localTmp;
    private BufferedWriter bw;
    private String sinkDir;
    private String tempDir;
    private String tempFile;
    private String fileName;
    private int fileNum;
    private long fileSize;

    public FileLocal(String fileName, String tempDir) {
        this.fileName = fileName;
        this.tempDir = tempDir;
        this.tempFile = concat(fileName, tempDir);
    }

    public FileLocal(String fileName, String sinkDir, String tempDir) {
        this(fileName, tempDir);
        this.sinkDir = sinkDir;
    }

    private String concat(String fileName, String filePath) {
        return DpiFileUtil.endWith(filePath) + fileName;
    }

    public void start() throws FileNotFoundException, UnsupportedEncodingException {
        start(false);
    }

    public void start(boolean append) throws FileNotFoundException, UnsupportedEncodingException {
        fileNum = 0;
        localTmp = new File(tempFile);
        bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(localTmp, append), writeCode));
    }

    public void write(String content) throws IOException {
        if (bw != null) {
            try {
                bw.write(content + newLine);
                fileNum++;
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                throw e;
            }
        }
    }

    public void flush() throws IOException {
        if (bw != null) {
            bw.flush();
            logger.debug("{} flush", bw);
        }
    }

    public void mvtempToSink() {
        close();
        if (localTmp != null && localTmp.exists() && localTmp.isFile()) {
            setFileSize(localTmp.length());
            DpiFileUtil.rename(tempDir, sinkDir, fileName);
        }
    }

    public void close() {
        if (bw != null) {
            try {
                bw.close();
                bw = null;
                logger.debug("{} close，= null", bw);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public String getFileName() {
        return fileName;
    }

    public int getFileNum() {
        return fileNum;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public void plusFileNum(){
        fileNum++;
    }
}
