package com.github.wwjwell.tap.utils;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author wwj
 * Create 2017/11/02
 **/
public class CommonUtilTest {
    @Test
    public void testStringIsNull() {
        String str = "";
        Assert.assertTrue(CommonUtil.isNullOrEmpty(str));
        str = " ";
        Assert.assertTrue(CommonUtil.isNullOrEmpty(str));
        str = "     ";
        Assert.assertTrue(CommonUtil.isNullOrEmpty(str));
        str = " 1 ";
        Assert.assertFalse(CommonUtil.isNullOrEmpty(str));
    }

    @Test
    public void testColIsNull() {
        Collection list = new ArrayList();
        Assert.assertTrue(CommonUtil.isNullOrEmpty(list));
        list = Lists.newArrayList();
        Assert.assertTrue(CommonUtil.isNullOrEmpty(list));
        list = Arrays.asList(1);

        Assert.assertFalse(CommonUtil.isNullOrEmpty(list));
    }

    @Test
    public void testReadAll(){
        InputStream input = Thread.currentThread().getClass().getResourceAsStream("/config_Threshold.json");
        String content = CommonUtil.readAll(input, Charset.forName("UTF-8"));
        System.out.println(content);
        Assert.assertTrue(content.length()>0);
    }

    @Test
    public void testShortClassName(){
        String name = CommonUtil.getShortClassName("ss");
        System.out.println(CommonUtil.getShortClassName("ss"));
        Assert.assertEquals(name,"String");
    }
}
