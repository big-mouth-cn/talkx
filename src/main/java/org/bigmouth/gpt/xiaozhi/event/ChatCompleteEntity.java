package org.bigmouth.gpt.xiaozhi.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bigmouth.gpt.ai.entity.Message;
import org.bigmouth.gpt.entity.Session;

import java.util.List;

/**
 * @author Allen Hu
 * @date 2025/3/5
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatCompleteEntity {

    private String sessionId;
    private Session session;
    private List<Message> messages;
    private String userPrompt;
    private String completion;
}
