package com.cqx.netty.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * 工具类
 */
public class Utils {
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);
    private Class cls = Utils.class;

    /**
     * 通过反射创建类
     *
     * @param cls
     * @param params
     * @return
     * @throws Exception
     */
    public static IServerHandler genrate(Class cls, Map<String, String> params) throws Exception {
        IServerHandler result;
        String clsName = cls.getName();
        String classSplit = "$";
        int classSplitIndex = clsName.indexOf(classSplit);
        if (classSplitIndex > 0) {//内部类
            //获取父类
            String superClsName = clsName.substring(0, classSplitIndex);
            Class superCls = Class.forName(superClsName);
            Object superObject = superCls.newInstance();
            Constructor<?> constructor = cls.getDeclaredConstructor(superCls);
            result = (IServerHandler) constructor.newInstance(superObject);
        } else {
            result = (IServerHandler) cls.newInstance();
        }
//        result.setParams(params);
        return result;
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String getNow() {
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(now);
    }

    public void classTest() throws IllegalAccessException, InstantiationException {
        System.out.println(cls);
        System.out.println(cls.newInstance());
    }

    public void readBuf(ByteBuf byteBuf) {
        byte[] arr1 = new byte[10240];
        byte[] arr2 = new byte[10240];
        byte[] arr3 = new byte[10240];
        byteBuf.readBytes(arr1);
        byteBuf.readBytes(arr2);
        byteBuf.readBytes(arr3);
        System.out.println("arr1：" + new String(arr1));
        System.out.println("arr2：" + new String(arr2));
        System.out.println("arr3：" + new String(arr3));
    }

    /**
     * 英文数字1个字节，中文3个字节，这个是字符编码的原因照成的字节差异
     *
     * @return
     */
    public ByteBuf writeBuf() {
        ByteBuf buf = Unpooled.buffer(3);
        byte[] dest = new byte[10240];
        byte[] src = "test1".getBytes();
        System.arraycopy(src, 0, dest, 0, src.length);
//        System.out.println(Arrays.toString(dest));
//        System.out.println(new String(dest));
        System.out.println("t1：" + "t1".getBytes().length);
        System.out.println("你：" + "你".getBytes().length);
        System.out.println("你好：" + "你好".getBytes().length);
        System.out.println("你好!：" + "你好!".getBytes().length);
        System.out.println("t1!@：" + "t1!@".getBytes().length);
        System.out.println("t1!@：：" + "t1!@：".getBytes().length);
        buf.writeBytes(dest);
        return buf;
    }

    public static <T> T setValDefault(Map param, String paramKey, T defaultValue) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException {
        if (defaultValue == null) throw new NullPointerException(paramKey + "默认值不能为空！");
        Object value = param.get(paramKey);
        T t = defaultValue;
        if (value != null) {
            String className = defaultValue.getClass().getName();
            Class cls = defaultValue.getClass();
            //参数列表
            Class<?>[] parameterTypes = {String.class};
            //获取参数对应的构造方法
            Constructor<T> constructor = cls.getConstructor(parameterTypes);
            //根据类型设置参数
            switch (className) {
                case "java.lang.Long":
                    //带参构造
                    t = constructor.newInstance(String.valueOf(((Number) value).longValue()));
                    break;
                case "java.lang.Integer":
                    //带参构造
                    t = constructor.newInstance(String.valueOf(((Number) value).intValue()));
                    break;
                case "java.lang.String":
                    t = constructor.newInstance((String) value);
                    break;
                case "java.lang.Boolean":
                    t = constructor.newInstance(String.valueOf(value));
                    break;
                default:
                    break;
            }
        } else {
            logger.info("获取{}配置为空，使用默认值：{}", paramKey, defaultValue);
        }
        return t;
    }
}
