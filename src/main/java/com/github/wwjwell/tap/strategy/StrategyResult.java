package com.github.wwjwell.tap.strategy;

import com.github.wwjwell.tap.utils.CommonUtil;

import java.io.Serializable;

/**
 * @author : wwj
 * Create 2017/11/15
 **/
public class StrategyResult implements Serializable{
    private static final long serialVersionUID = -3236303329611484705L;
    /**
     * 策略执行结果
     */
    private Result result;

    public StrategyResult(Result result) {
        this.result = result;
    }

    /**
     * 具体执行的策略方法
     */
    private Strategy strategy;

    /**
     * 拦截信息描述
     */
    private String msg;

    public void access() {
        this.result = Result.ACCESS;
    }

    public void accessAndIgnoreAfter() {
        this.result = Result.ACCESS_IGNORE_AFTER;
    }

    public void reject() {
        this.result = Result.REJECT;
    }

    public boolean isAccess(){
        return this.result == Result.ACCESS || this.result == Result.ACCESS_IGNORE_AFTER;
    }

    public boolean isAcessAndIgnoreAfter(){
        return this.result == Result.ACCESS_IGNORE_AFTER;
    }

    public boolean isReject(){
        return !isAccess();
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public Result getResult() {
        return result;
    }

    public enum Result{
        /**
         * 不限流，并且忽略后续限流策略，直接通过
         */
        ACCESS_IGNORE_AFTER,
        /**
         * 不限流，通过
         */
        ACCESS,

        /**
         * 限流，不通过
         */
        REJECT,

    }
    @Override
    public String toString() {
        return "StrategyResult{" +
                "result=" + result +
                ", strategy=" + (null==strategy?"null": CommonUtil.getShortClassName(strategy)) +
                ", msg='" + msg + '\'' +
                '}';
    }
}
