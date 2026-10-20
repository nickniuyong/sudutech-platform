package com.sudutech.ai.domain;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 会话表 ai_chat_session
 */
@Data
@Table("ai_chat_session")
public class AiChatSession implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long sessionId;

    private String sessionTitle;

    private Long datasourceId;

    private Long userId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
