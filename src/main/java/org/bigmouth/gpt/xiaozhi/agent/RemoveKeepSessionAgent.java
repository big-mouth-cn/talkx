package org.bigmouth.gpt.xiaozhi.agent;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;
import org.bigmouth.gpt.ai.agent.Agent;
import org.bigmouth.gpt.ai.agent.AgentRequest;
import org.bigmouth.gpt.entity.Session;
import org.bigmouth.gpt.service.ISessionService;
import org.bigmouth.gpt.xiaozhi.udp.UdpClientContext;
import org.bigmouth.gpt.xiaozhi.udp.UdpClientContextHolder;
import org.springframework.context.annotation.Configuration;

/**
 * @author Allen Hu
 * @date 2025/3/5
 */
@Deprecated
//@Configuration
public class RemoveKeepSessionAgent implements Agent<RemoveKeepSessionAgent.RemoveSessionRequest> {

    private final UdpClientContextHolder udpClientContextHolder;
    private final ISessionService sessionService;

    public RemoveKeepSessionAgent(UdpClientContextHolder udpClientContextHolder, ISessionService sessionService) {
        this.udpClientContextHolder = udpClientContextHolder;
        this.sessionService = sessionService;
    }

    @Override
    public String getFunctionName() {
        return "remove_keep_session";
    }

    @Override
    public String getFunctionDescription() {
        return "删除与用户当前对话之前的聊天记录，便于忘记之前交谈的话题，重新开始新的话题";
    }

    @Override
    public Object apply(RemoveSessionRequest removeSessionRequest) {
        try {
            String sessionId = removeSessionRequest.getSessionId();
            if (sessionId == null) {
                throw new NullPointerException();
            }
            UdpClientContext context = udpClientContextHolder.get(sessionId);
            if (context == null) {
                throw new NullPointerException();
            }
            Session session = context.getSession();
            if (session == null) {
                throw new NullPointerException();
            }
            sessionService.removeKeepSession(session.getUserId(), session.getFriendId(), session.getProductId());
        } catch (NullPointerException ignore) {
        }
        return "[DONE]";
    }

    @Data
    public static class RemoveSessionRequest implements AgentRequest {
        @JsonProperty(required = true)
        @JsonPropertyDescription("本次对话的会话ID")
        private String sessionId;
    }
}
