package com.cqx.netty.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * FileFilterUtils
 *
 * @author chenqixu
 */
public class FileFilterUtils {

    public static FileFilterUtils builder() {
        return new FileFilterUtils();
    }

    /***
     * @description: 列出给定时间区间内的文件
     * @param path
     * @param begainTime
     * @param endTime
     */
    public List<File> listFileByTime(String path, String begainTime, String endTime) {
        FilenameFilter fileter = new AssignFileTimeFilter(begainTime, endTime);
        File file = new File(path);
        if (file.exists()) {
            return new ArrayList<>(Arrays.asList(Objects.requireNonNull(file.listFiles(fileter))));
        }
        return null;
    }

    /***
     * @description: 列出给定文件名前缀的文件
     * @param path
     * @param startsWith
     */
    public List<File> listFiles(String path, String startsWith) {
        File file = new File(path);
        List<File> fileList = new ArrayList<>();
        for (File tmp : Objects.requireNonNull(file.listFiles())) {
            if (tmp.getName().startsWith(startsWith)) {
                fileList.add(tmp);
            }
        }
        return fileList;
    }

    class AssignFileTimeFilter implements FilenameFilter {
        private static final String SEPARATION = "-";
        private static final String A = "A";
        private static final String E = "E";
        private long begainTime;
        private long endTime;

        public AssignFileTimeFilter(String begainTime, String endTime) {
            this.begainTime = Long.parseLong(begainTime);
            this.endTime = Long.parseLong(endTime);
        }

        @Override
        public boolean accept(File dir, String name) {
            try {
                if (name.startsWith(A)) {  //DPI文件
                    long time = Long.parseLong(name.substring(name.lastIndexOf(E) - 14, name.lastIndexOf(E)));
                    if (time >= begainTime && time <= endTime) {
                        return true;
                    }
                } else {
                    long begin = Long.parseLong(name.split(SEPARATION)[3]);
                    long end = Long.parseLong(name.split(SEPARATION)[4]);
                    if (begin >= begainTime && end <= endTime) {
                        return true;
                    }
                }
            } catch (Exception e) {
                return false;
            }
            return false;
        }
    }
}
