package com.github.wwjwell.tap.exception;

import com.github.wwjwell.tap.RateLimitResult;

/**
 * 拒绝异常
 * @author wwj
 * Create 2017/11/08
 **/
public class RejectTapException extends TapException {
    public RejectTapException(RateLimitResult rateLimitResult) {
        super(rateLimitResult);
    }

    public RejectTapException(String message, RateLimitResult rateLimitResult) {
        super(message, rateLimitResult);
    }
}
