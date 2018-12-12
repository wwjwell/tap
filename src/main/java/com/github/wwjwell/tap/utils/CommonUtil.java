package com.github.wwjwell.tap.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * 工具类
 * @author wwj
 * Create 2017/10/30
 **/
public class CommonUtil {
    private static final Logger logger = LoggerFactory.getLogger(CommonUtil.class);

    /**
     * 判断字符串是否为空
     * @param str : str
     * @return true :null
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    /**
     * 判断数组是否为空
     * @param array
     * @return
     */
    public static boolean isNullOrEmpty(Object[] array) {
        return array == null || array.length==0;
    }
    /**
     * 判断集合是否为空
     * @param collection
     * @return
     */
    public static boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 判断Map是否为空
     * @param map
     * @return
     */
    public static boolean isNullOrEmpty(Map<?,?> map) {
        return map == null || map.isEmpty();
    }


    /**
     * 读取文件流所有内容
     * @param input
     * @param charset
     * @return
     */
    public static String readAll(InputStream input, Charset charset) {
        if (null == input) {
            return null;
        }
        try {
            byte[] buf = new byte[512];
            int readLen = 0;
            int len = input.read(buf);
            while (len > 0) {
                readLen += len;
                if (readLen == buf.length) {
                    buf = Arrays.copyOf(buf, readLen * 2);
                }
                len = input.read(buf, readLen, buf.length - readLen);
            }
            return new String(buf, 0, readLen, charset);
        } catch (Exception e) {
            logger.error("", e);
        }finally {
            try {
                input.close();
            } catch (IOException e) {
                logger.error("", e);
            }
        }
        return null;
    }


    public static String getShortClassName(Object object) {
        if (object == null) {
            return "null";
        }
        return object.getClass().getSimpleName();
    }
}
