package com.github.wwjwell.tap.exception;

import com.github.wwjwell.tap.RateLimitResult;

/**
 * @author : wwj
 * Create 2017/11/10
 **/
public class RejectTapThrowable extends TapThrowable {
    public RejectTapThrowable(RateLimitResult rateLimitResult) {
        super(rateLimitResult);
    }

    public RejectTapThrowable(String message, RateLimitResult rateLimitResult) {
        super(message, rateLimitResult);
    }
}
