package com.github.wwjwell.tap;

import com.google.common.collect.Lists;
import com.github.wwjwell.tap.channel.StrategyChannel;
import com.github.wwjwell.tap.utils.CommonUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * 策略树，根据url的路径信息建立树的结构
 * 目前只支持
 * 1、全匹配/**
 * 2、单级或多级匹配，/*\/abc 单级，/**\/abc多级
 * 3、/abc/abc* 前缀匹配，/abc/*.jsp 后缀匹配
 * /abc/123/456,
 * /abc/123,
 * /efg/hello,
 * /**,
 * /*\/abc/efg
 *                    root
 *                      |
 *      abc---|---efg---|---*---|---**
 *       |         |        |
 *      123      hello      abc
 *       |                   |
 *  456--|--123             efg
 *
 * @author wwj
 * Create 2017/10/30
 **/
public class StrategyTrie {
    private static final String PATH_SEPARATER = "/";
    private static final String MATCH_ALL_CHAR = "**";

    private Node root;

    /**
     * url最大优先级匹配
     * @param resource
     * @return
     */
    public StrategyChannel match(String resource) {
        if (root == null || CommonUtil.isNullOrEmpty(root.subNodes)) {
            return null;
        }
        Match bestMatch = NOT_MATCH;
        String[] resources = split(resource);
        for (Node subNode : root.subNodes) {
            Match match = match(subNode, resources);
            if (null != match) {
                if (match.weight > bestMatch.weight) {
                    bestMatch = match;
                }
            }
        }
        if (bestMatch.isMatch()) {
            return bestMatch.channel;
        }
        return null;
    }


    private String[] split(String resource) {
        while (resource.startsWith(PATH_SEPARATER)) {
            resource = resource.substring(PATH_SEPARATER.length());
        }
        while (resource.endsWith(PATH_SEPARATER)) {
            resource = resource.substring(0, resource.length() - PATH_SEPARATER.length());
        }
        return resource.split(PATH_SEPARATER);
    }


    static final Match NOT_MATCH = new Match(Node.NOT_MATCH, null);
    private static class Match{
        public int weight;
        public StrategyChannel channel;

        public Match(int weight, StrategyChannel channel) {
            this.weight = weight;
            this.channel = channel;
        }
        public boolean isMatch(){
            return weight > Node.NOT_MATCH;
        }

        @Override
        public String toString() {
            return "Match{" +
                    "weight=" + weight +
                    ", channel=" + channel +
                    '}';
        }
    }

    /**
     * 正向匹配，从根到叶子，寻找最大优先级
     * @param node
     * @param resources
     * @return
     */
    private Match match(Node node, String[] resources) {
        if (null == node || CommonUtil.isNullOrEmpty(resources)) {
            return null;
        }
        String resource = resources[0];
        int weight = node.match(resource);
        if (weight <= Node.NOT_MATCH) {
            return NOT_MATCH;
        }

        if (resources.length == 1 && node.channel != null) {
            return new Match(weight, node.channel);
        }
        if(CommonUtil.isNullOrEmpty(node.subNodes)){
            if (weight == Node.WEIGHT_MATCH_ALL_EQUAL) {
                return new Match(weight, node.channel);
            }
            return NOT_MATCH;
        } else {
            Match bestMatch = NOT_MATCH;
            for (Node subNode : node.subNodes) {
                Match match = NOT_MATCH;
                if(weight == Node.WEIGHT_MATCH_ALL_EQUAL) {
                    for(int i=1;i<resources.length;i++) {
                        Match subMatch = match(subNode, Arrays.copyOfRange(resources, i, resources.length));
                        if (subMatch.weight > match.weight) {
                            match = subMatch;
                        }
                    }
                    if (match == NOT_MATCH && node.channel!=null) {
                        match = new Match(weight, node.channel);
                    }
                }else{
                    match = match(subNode, Arrays.copyOfRange(resources, 1, resources.length));
                }
                if (null != match) {
                    if (match.weight > bestMatch.weight) {
                        bestMatch = match;
                    }
                }
            }
            if (bestMatch.isMatch()) {
                bestMatch.weight += weight;
                return bestMatch;
            }
        }
        return NOT_MATCH;
    }

    /**
     * 遍历策略树
     * @return
     */
    public List<StrategyChannel> traversal() {
        List<StrategyChannel> strategies = Lists.newArrayList();
        traversal(root, strategies);
        return strategies;
    }

    private void traversal(Node pNode,List<StrategyChannel> strategies) {
        if (pNode!=null) {
            if (null != pNode.channel) {
                strategies.add(pNode.channel);
            }
            if (!CommonUtil.isNullOrEmpty(pNode.subNodes)) {
                for (Node subNode : pNode.subNodes) {
                    traversal(subNode, strategies);
                }
            }
        }
    }


    /**
     * 构建树
     * @param channels
     */
    public void buildTree(Collection<StrategyChannel> channels) {
        root = new Node(Node.MATCH_CHAR, null);
        if (!CommonUtil.isNullOrEmpty(channels)) {
            for (StrategyChannel channel : channels) {
                if(channel.getResource().startsWith(PATH_SEPARATER)) {
                    String[] resources = split(channel.getResource());
                    if (CommonUtil.isNullOrEmpty(resources)) {
                        break;
                    }
                    addNode(root, resources, channel);
                }
            }
        }
    }

    public void clear() {
        root.subNodes = null;
    }

    /**
     * 递归添加策略
     * @param pNode
     * @param resources
     * @param channel
     */
    private void addNode(Node pNode, String[] resources, StrategyChannel channel) {
        if (CommonUtil.isNullOrEmpty(resources)) {
            return;
        }
        if(CommonUtil.isNullOrEmpty(pNode.subNodes)){
            pNode.subNodes = new TreeSet();
        }

        Node node = null;
        String resource = resources[0];
        for (Node subNode : pNode.subNodes) {
            if (subNode.resource.equals(resource)) {
                node = subNode;
                break;
            }
        }

        if (node == null) {
            node = new Node(resource, null);
            pNode.subNodes.add(node);
        }

        if (channel != null && resources.length == 1) {
            node.channel = channel;
        }

        if (resources.length > 1) {
            String[] subResources = Arrays.copyOfRange(resources, 1, resources.length);
            addNode(node, subResources, channel);
        }

    }


    public enum MatchType{
        /**
         * 精准匹配
         */
        All,

        /**
         * 前缀后缀匹配
         */
        PrefixSuffix,

        /**
         * 前缀匹配
         */
        Prefix,

        /**
         * 后缀
         */
        Suffix,

        /**
         *  *号匹配
         */
        Match,

        /**
         * ** 号匹配
         */
        MatchAll,

    }
    public static class Node implements Comparable{
        /**
         *  {@code **}号匹配权重
         */
        private static final int WEIGHT_MATCH_ALL_EQUAL = -1;

        /**
         * {@code *} 号匹配权重
         */
        private static final int WEIGHT_MATCH_ONE_EQUAL = 1 << 1;

        /**
         * 后缀匹配权重
         */
        private static final int WEIGHT_SUFFIX_EQUAL = 1 << 2;

        /**
         * 前缀匹配权重
         */
        private static final int WEIGHT_PREFIX_EQUAL = 1 << 3;

        /**
         *  前缀后缀匹配权重
         */
        private static final int WEIGHT_PREFIX_SUFFIX_EQUAL = 1 << 4;

        /**
         * 最高优先级匹配权重
         */
        private static final int WEIGHT_ALL_EQUAL = 1 << 5;

        private static final int NOT_MATCH = -10;

        private static final String MATCH_CHAR = "*";

        private String matchResource;
        private String resource;
        private StrategyChannel channel;
        private MatchType matchType;
        private Set<Node> subNodes;

        public Node(String resource, StrategyChannel channel) {
            this.resource = resource;
            this.channel = channel;
            genMatchResourceAndType();
        }

        /**
         * 根据resource生成匹配resource和类型
         */
        private void genMatchResourceAndType(){
            this.matchResource = resource;
            if (resource.equals(MATCH_ALL_CHAR)) {
                matchType = MatchType.MatchAll;
            } else if (resource.equals(MATCH_CHAR)) {
                matchType = MatchType.Match;
            } else if (resource.contains(MATCH_CHAR)) {
                if (resource.startsWith(MATCH_CHAR)) {
                    matchType = MatchType.Suffix;
                    this.matchResource = resource.substring(resource.indexOf(MATCH_CHAR)+1);
                } else if(resource.endsWith(MATCH_CHAR)){
                    matchType = MatchType.Prefix;
                    this.matchResource = resource.substring(0,resource.indexOf(MATCH_CHAR));
                }else{
                    this.matchResource = resource.substring(0,resource.indexOf(MATCH_CHAR));
                    matchType = MatchType.PrefixSuffix;
                }
            } else {
                matchType = MatchType.All;
            }
        }

        /**
         * 对url的匹配分为
         * 1、/** 全匹配，权重为 {@code WEIGHT_MATCH_ALL_EQUAL}
         * 2、/*  单*号匹配，匹配权重为 {@code WEIGHT_MATCH_ONE_EQUAL}
         * 3、/*.jsp 后缀匹配，匹配权重{@code WEIGHT_SUFFIX_EQUAL}
         * 4、/hello* 前缀匹配，匹配权重{@code WEIGHT_PREFIX_EQUAL}
         * 5、/hello/world 精准匹配，匹配权重为{@code WEIGHT_ALL_EQUAL}
         * @param url
         * @return
         */
        public int match(String url){
            if(this.matchType.equals(MatchType.MatchAll)){
                return WEIGHT_MATCH_ALL_EQUAL;
            }

            if (CommonUtil.isNullOrEmpty(url)) {
                return NOT_MATCH;
            }

            else if (this.matchType.equals(MatchType.Match)) {
                return WEIGHT_MATCH_ONE_EQUAL;
            } else if(this.matchType.equals(MatchType.All)){
                if (this.matchResource.equals(url)) {
                    return WEIGHT_ALL_EQUAL;
                }
            } else if (this.matchType.equals(MatchType.Prefix)) {
                if (url.startsWith(this.matchResource)) {
                    return WEIGHT_PREFIX_EQUAL + this.matchResource.length();
                }
            } else if(this.matchType.equals(MatchType.Suffix)){
                if (url.endsWith(this.matchResource)) {
                    return WEIGHT_SUFFIX_EQUAL;
                }
            } else if (this.matchType.equals(MatchType.PrefixSuffix)) {
                if (url.startsWith(this.matchResource)) {
                    String suffix = this.resource.substring(this.matchResource.length() + MATCH_CHAR.length());
                    if (url.endsWith(suffix)) {
                        return WEIGHT_PREFIX_SUFFIX_EQUAL;
                    }
                }
            }
            return NOT_MATCH;
        }

        @Override
        public int hashCode() {
            return this.matchResource.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (null == obj) {
                return false;
            }
            if (obj instanceof Node) {
                Node node = (Node) obj;
                return this.resource.equals(node.resource);
            }
            return false;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "matchResource='" + matchResource + '\'' +
                    ", resource='" + resource + '\'' +
                    ", channel=" + channel +
                    ", matchType=" + matchType +
                    ", subNodes=" + subNodes +
                    '}';
        }


        @Override
        public int compareTo(Object o) {
            Node o1 = (Node) o;
            return this.resource.compareTo(o1.resource);
        }
    }


}
