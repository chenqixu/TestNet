package com.cqx.netty.utils;

/**
 * IDpiFileDeal
 *
 * @author chenqixu
 */
public interface IDpiFileDeal {
    void run(String value) throws Exception;

    void end() throws Exception;

    void sendErrorMsgToBolt(String value, String errMsg);
}
