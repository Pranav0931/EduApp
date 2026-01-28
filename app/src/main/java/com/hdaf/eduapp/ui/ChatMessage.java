package com.hdaf.eduapp.ui;

/**
 * Model class for chat messages in EduAI conversation.
 */
public class ChatMessage {

    public enum MessageType {
        USER,
        AI,
        SYSTEM
    }

    private String id;
    private String content;
    private MessageType type;
    private long timestamp;
    private boolean isTyping;

    public ChatMessage(String content, MessageType type) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.content = content;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
        this.isTyping = false;
    }

    /**
     * Create a typing indicator message.
     */
    public static ChatMessage createTypingMessage() {
        ChatMessage message = new ChatMessage("...", MessageType.AI);
        message.setTyping(true);
        return message;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isTyping() {
        return isTyping;
    }

    public void setTyping(boolean typing) {
        isTyping = typing;
    }

    public boolean isFromUser() {
        return type == MessageType.USER;
    }

    public boolean isFromAI() {
        return type == MessageType.AI;
    }
}
