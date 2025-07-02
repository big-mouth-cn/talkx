/*
 * Copyright 2016 bianxianmao.com All right reserved. This software is the confidential and proprietary information of
 * textile.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with bianxianmao.com.
 */

package com.bxm.warcar.message;

import java.io.Serializable;
import java.util.List;

/**
 * Message
 * @author allen
 * @since 1.0.0
 */
public class Message implements Serializable {

    private static final long serialVersionUID = 7953257301233581347L;

    private String from;
    private String content;
    private List<String> tos;

    /**
     * 主题。一般用于邮件标题。
     */
    private String subject;

    public Message() {
    }

    public Message(String content) {
        this.content = content;
    }

    public Message(String content, List<String> tos) {
        this.content = content;
        this.tos = tos;
    }

    public Message(String from, String content, List<String> tos) {
        this.from = from;
        this.content = content;
        this.tos = tos;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getTos() {
        return tos;
    }

    public void setTos(List<String> tos) {
        this.tos = tos;
    }

    public String getSubject() {
        return subject;
    }

    public Message setSubject(String subject) {
        this.subject = subject;
        return this;
    }
}
