/*
 * Copyright 2016 bianxianmao.com All right reserved. This software is the confidential and proprietary information of
 * textile.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with bianxianmao.com.
 */

package com.bxm.warcar.message;

/**
 * Message sender
 * @author allen
 * @since 1.0.0
 */
public interface MessageSender {

    /**
     * send message
     * @param message
     * @return Return true if success, else false
     * @deprecated 使用 {@link #send2(Message)} 可以获取失败的原因。
     */
    @Deprecated
    boolean send(Message message);

    /**
     * Send message
     *
     * @param message message
     * @throws MessageException 如果发送失败，那么一定会抛出这个异常
     */
    void send2(Message message) throws MessageException;
}
