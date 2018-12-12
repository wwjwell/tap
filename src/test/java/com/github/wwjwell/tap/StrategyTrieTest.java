package com.github.wwjwell.tap;

import com.github.wwjwell.tap.channel.StrategyChannel;
import com.github.wwjwell.tap.strategy.StrategyResult;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author wwj
 * @author  2017/11/03
 **/
public class StrategyTrieTest {
    @Test
    public void matchTest(){
        StrategyTrie.Node node = new StrategyTrie.Node("*abc", null);
        String path = "cabc";
        System.out.println(node.match(path));
    }


    @Test
    public void testBestMatch(){
        String[] resources = {
                "/*",
                "/*/*",
                "/**",
                "/**/helloWorld",
                "/order/**",
                "/order/category/**",
                "/order/category/subcategory/**",
                "/order/*",
                "/order/*/*",
                "/**/r/*",
                "/order/category/subcategory/l2*.action",
                "/order/category/subcategory/*/l2/m*",
                "/order/category/subcategory/**/l2/m*",
                "/order/category/subcategory/l1/l2/m2",
                "/**/a/b/c/d",
                "/**/b/c/d",
                "/**/a/c/d",
                "/**/c/d"
        };
        List<StrategyChannel> channelList = Lists.newArrayList();

        for (final String resource : resources) {
            StrategyChannel channel = new StrategyChannel() {
                @Override
                public void init() {
                }

                @Override
                public List<StrategyResult> acquire(String resource, Map<String, Object> attachment) {
                    return null;
                }

                @Override
                public String getResource() {
                    return resource;
                }

                @Override
                public boolean isEnable() {
                    return false;
                }


                @Override
                public void destroy() throws Exception {

                }
            };
            channelList.add(channel);
        }

        StrategyTrie tree = new StrategyTrie();
        tree.buildTree(channelList);

        Map<String, String> urlMatchMap = Maps.newLinkedHashMap();
        urlMatchMap.put("/hello", "/*");
        urlMatchMap.put("/a/a", "/*/*");
        urlMatchMap.put("/a/a/a", "/**");
        urlMatchMap.put("/hello/hello/hello", "/**");
        urlMatchMap.put("/hello/hello/hello/helloWorld", "/**/helloWorld");
        urlMatchMap.put("/a/helloWorld", "/**/helloWorld");
        urlMatchMap.put("/order/l1/l2/l3/l4", "/order/**");
        urlMatchMap.put("/order/l3/l4/l5", "/order/**");
        urlMatchMap.put("/order/l4/l5", "/order/*/*");
        urlMatchMap.put("/order/x/l2", "/order/*/*");
        urlMatchMap.put("/order/l3", "/order/*");
        urlMatchMap.put("/order/category/l2", "/order/category/**");
        urlMatchMap.put("/order/x", "/order/*");
        urlMatchMap.put("/order/category/subcategory/a/b/c", "/order/category/subcategory/**");
        urlMatchMap.put("/order/category/subcategory/l2a.action", "/order/category/subcategory/l2*.action");
        urlMatchMap.put("/order/category/subcategory/xxx/l2/mc", "/order/category/subcategory/*/l2/m*");
        urlMatchMap.put("/order/category/subcategory/xxx/xx/l2/mc", "/order/category/subcategory/**/l2/m*");
        urlMatchMap.put("/order/category/subcategory/a/b/c/d/l2/", "/order/category/subcategory/**");
        urlMatchMap.put("/x/a/b/c/d", "/**/a/b/c/d");
        urlMatchMap.put("/x/z/b/c/d", "/**/b/c/d");
        urlMatchMap.put("/x/z/y/c/d", "/**/c/d");
        urlMatchMap.put("/xsdc/d", "/*/*");
        urlMatchMap.put("/xsdc/dds/d", "/**");
        urlMatchMap.put("/order/dds/d", "/order/*/*");
        urlMatchMap.put("/order/dds/d/d", "/order/**");
        urlMatchMap.put("/order/category/d", "/order/category/**");
        urlMatchMap.put("/order/category/subcategory/d", "/order/category/subcategory/**");
        urlMatchMap.put("/xxxx/x/api/dsdsd", "/**");
        urlMatchMap.put("/xxxxxsd/category/subcategory/d/sdsdsd", "/**");
        urlMatchMap.put("/xxxxxsd", "/*");

        for (Map.Entry<String, String> entry : urlMatchMap.entrySet()) {
            String match = tree.match(entry.getKey()).getResource();
            System.out.println(entry.getKey() + "\t" + entry.getValue() + "\t" + match);
            Assert.assertTrue(entry.getValue().equals(match));
        }
    }

    @Test
    public void arraySubTest(){
        String[] resources = {"1", "2", "3"};
        String[] strings = Arrays.copyOfRange(resources, 0, resources.length - 1);
        System.out.println(strings.length);
        Assert.assertTrue(resources.length == strings.length+1);

    }
}
