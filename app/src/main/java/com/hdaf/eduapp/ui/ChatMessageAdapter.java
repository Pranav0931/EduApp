package com.hdaf.eduapp.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hdaf.eduapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying chat messages in EduAI conversation.
 */
public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 0;
    private static final int VIEW_TYPE_AI = 1;

    private List<ChatMessage> messages;

    public ChatMessageAdapter() {
        this.messages = new ArrayList<>();
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);
        return message.isFromUser() ? VIEW_TYPE_USER : VIEW_TYPE_AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        
        if (viewType == VIEW_TYPE_USER) {
            View view = inflater.inflate(R.layout.item_chat_message_user, parent, false);
            return new UserMessageViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_chat_message_ai, parent, false);
            return new AIMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        
        if (holder instanceof UserMessageViewHolder) {
            ((UserMessageViewHolder) holder).bind(message);
        } else if (holder instanceof AIMessageViewHolder) {
            ((AIMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    /**
     * Add a message to the chat.
     */
    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    /**
     * Remove typing indicator if present.
     */
    public void removeTypingIndicator() {
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i).isTyping()) {
                messages.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    /**
     * Update the last AI message (used for replacing typing indicator).
     */
    public void updateLastAIMessage(String content) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatMessage message = messages.get(i);
            if (message.isFromAI()) {
                message.setContent(content);
                message.setTyping(false);
                notifyItemChanged(i);
                break;
            }
        }
    }

    /**
     * Clear all messages.
     */
    public void clearMessages() {
        messages.clear();
        notifyDataSetChanged();
    }

    /**
     * Get all messages.
     */
    public List<ChatMessage> getMessages() {
        return messages;
    }

    /**
     * ViewHolder for user messages.
     */
    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageText;

        UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
        }

        void bind(ChatMessage message) {
            messageText.setText(message.getContent());
        }
    }

    /**
     * ViewHolder for AI messages.
     */
    static class AIMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageText;

        AIMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
        }

        void bind(ChatMessage message) {
            if (message.isTyping()) {
                messageText.setText("Soch raha hoon... 🤔");
            } else {
                messageText.setText(message.getContent());
            }
        }
    }
}
