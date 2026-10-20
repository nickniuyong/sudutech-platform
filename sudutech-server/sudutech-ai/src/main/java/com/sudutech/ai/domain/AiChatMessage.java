package com.sudutech.ai.domain;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 消息表 ai_chat_message
 */
@Data
@Table("ai_chat_message")
public class AiChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long messageId;

    private Long sessionId;

    /** user/assistant */
    private String role;

    private String content;

    /** AI 产出 SQL */
    private String generatedSql;

    /** 推荐图表类型 */
    private String chartType;

    /** 结果数据缓存JSON */
    private String chartData;

    /** OK/ERROR */
    private String status;

    private LocalDateTime createTime;
}
