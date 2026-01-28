package com.hdaf.eduapp.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.hdaf.eduapp.R;
import com.hdaf.eduapp.accessibility.TTSManager;
import com.hdaf.eduapp.accessibility.VoiceRecognitionManager;
import com.hdaf.eduapp.ai.EduAIService;
import com.hdaf.eduapp.ai.EduIntent;
import com.hdaf.eduapp.ai.NavigationController;

/**
 * Bottom sheet dialog for EduAI chat interface.
 * Provides voice and text input for interacting with EduAI.
 */
public class EduAIChatBottomSheet extends BottomSheetDialogFragment {

    private static final String TAG = "EduAIChatBottomSheet";
    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 100;

    private RecyclerView messagesRecyclerView;
    private TextInputEditText messageInput;
    private FloatingActionButton voiceInputButton;
    private FloatingActionButton sendButton;
    private ImageButton closeButton;
    private TextView statusText;
    private Chip chipOpenClass, chipStartQuiz, chipPlayAudio, chipDailyChallenge;

    private ChatMessageAdapter adapter;
    private EduAIService eduAIService;
    private NavigationController navigationController;
    private VoiceRecognitionManager voiceRecognitionManager;
    private TTSManager ttsManager;
    private Handler mainHandler;

    private boolean isListening = false;

    public static EduAIChatBottomSheet newInstance() {
        return new EduAIChatBottomSheet();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.Theme_EduApp_BottomSheet);
        
        eduAIService = EduAIService.getInstance();
        navigationController = new NavigationController();
        voiceRecognitionManager = VoiceRecognitionManager.getInstance();
        ttsManager = TTSManager.getInstance();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_eduai_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupRecyclerView();
        setupListeners();
        initializeVoiceRecognition();
        
        // Add welcome message
        addAIMessage(eduAIService.getGreeting());
    }

    private void initializeViews(View view) {
        messagesRecyclerView = view.findViewById(R.id.messagesRecyclerView);
        messageInput = view.findViewById(R.id.messageInput);
        voiceInputButton = view.findViewById(R.id.voiceInputButton);
        sendButton = view.findViewById(R.id.sendButton);
        closeButton = view.findViewById(R.id.closeButton);
        statusText = view.findViewById(R.id.statusText);
        
        chipOpenClass = view.findViewById(R.id.chipOpenClass);
        chipStartQuiz = view.findViewById(R.id.chipStartQuiz);
        chipPlayAudio = view.findViewById(R.id.chipPlayAudio);
        chipDailyChallenge = view.findViewById(R.id.chipDailyChallenge);
    }

    private void setupRecyclerView() {
        adapter = new ChatMessageAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        closeButton.setOnClickListener(v -> dismiss());
        
        sendButton.setOnClickListener(v -> sendMessage());
        
        voiceInputButton.setOnClickListener(v -> toggleVoiceInput());
        
        messageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Update send button state
                sendButton.setEnabled(s != null && s.length() > 0);
            }
        });
        
        // Quick action chips
        chipOpenClass.setOnClickListener(v -> sendQuickAction("Mujhe class selection dikhao"));
        chipStartQuiz.setOnClickListener(v -> sendQuickAction("Mujhe ek quiz start karna hai"));
        chipPlayAudio.setOnClickListener(v -> sendQuickAction("Mujhe audio lesson sunna hai"));
        chipDailyChallenge.setOnClickListener(v -> sendQuickAction("Aaj ka daily challenge start karo"));
    }

    private void initializeVoiceRecognition() {
        if (getContext() != null) {
            voiceRecognitionManager.initialize(getContext());
        }
    }

    private void sendMessage() {
        String message = messageInput.getText() != null ? 
                messageInput.getText().toString().trim() : "";
        
        if (message.isEmpty()) return;
        
        // Add user message
        addUserMessage(message);
        messageInput.setText("");
        
        // Show typing indicator
        addTypingIndicator();
        
        // Send to EduAI
        eduAIService.sendMessage(message, new EduAIService.ResponseCallback() {
            @Override
            public void onResponse(String responseMessage, EduIntent intent) {
                mainHandler.post(() -> {
                    // Remove typing indicator and add response
                    adapter.removeTypingIndicator();
                    addAIMessage(responseMessage);
                    
                    // Speak response
                    if (ttsManager.isReady()) {
                        ttsManager.speak(responseMessage);
                    }
                    
                    // Handle navigation if needed
                    if (intent != null && intent.isNavigationIntent()) {
                        handleNavigation(intent);
                    }
                });
            }

            @Override
            public void onError(String error) {
                mainHandler.post(() -> {
                    adapter.removeTypingIndicator();
                    addAIMessage("Oops! Kuch gadbad ho gayi 😅 Phir se try karo!");
                });
            }
        });
    }

    private void sendQuickAction(String action) {
        messageInput.setText(action);
        sendMessage();
    }

    private void toggleVoiceInput() {
        if (!checkAudioPermission()) {
            requestAudioPermission();
            return;
        }
        
        if (isListening) {
            stopVoiceInput();
        } else {
            startVoiceInput();
        }
    }

    private void startVoiceInput() {
        isListening = true;
        updateVoiceButtonState();
        statusText.setText(R.string.eduai_status_listening);
        
        voiceRecognitionManager.startListening(new VoiceRecognitionManager.VoiceCallback() {
            @Override
            public void onReadyForSpeech() {
                mainHandler.post(() -> {
                    statusText.setText(R.string.eduai_status_listening);
                });
            }

            @Override
            public void onBeginningOfSpeech() {
                // Voice detected
            }

            @Override
            public void onEndOfSpeech() {
                mainHandler.post(() -> {
                    isListening = false;
                    updateVoiceButtonState();
                    statusText.setText(R.string.eduai_status_online);
                });
            }

            @Override
            public void onResults(String text) {
                mainHandler.post(() -> {
                    isListening = false;
                    updateVoiceButtonState();
                    statusText.setText(R.string.eduai_status_online);
                    
                    if (text != null && !text.isEmpty()) {
                        messageInput.setText(text);
                        sendMessage();
                    }
                });
            }

            @Override
            public void onPartialResults(String text) {
                mainHandler.post(() -> {
                    if (text != null && !text.isEmpty()) {
                        messageInput.setText(text);
                    }
                });
            }

            @Override
            public void onError(int errorCode, String errorMessage) {
                mainHandler.post(() -> {
                    isListening = false;
                    updateVoiceButtonState();
                    statusText.setText(R.string.eduai_status_online);
                    
                    if (errorCode != 7) { // Ignore NO_MATCH errors
                        Toast.makeText(getContext(), 
                                "Voice error: " + errorMessage, 
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void stopVoiceInput() {
        isListening = false;
        updateVoiceButtonState();
        statusText.setText(R.string.eduai_status_online);
        voiceRecognitionManager.stopListening();
    }

    private void updateVoiceButtonState() {
        if (isListening) {
            voiceInputButton.setImageResource(R.drawable.ic_mic);
            voiceInputButton.setBackgroundTintList(
                    ContextCompat.getColorStateList(requireContext(), R.color.error_red));
        } else {
            voiceInputButton.setImageResource(R.drawable.ic_mic);
            voiceInputButton.setBackgroundTintList(
                    ContextCompat.getColorStateList(requireContext(), R.color.primary_magenta));
        }
    }

    private void addUserMessage(String message) {
        ChatMessage chatMessage = new ChatMessage(message, ChatMessage.MessageType.USER);
        adapter.addMessage(chatMessage);
        scrollToBottom();
    }

    private void addAIMessage(String message) {
        ChatMessage chatMessage = new ChatMessage(message, ChatMessage.MessageType.AI);
        adapter.addMessage(chatMessage);
        scrollToBottom();
    }

    private void addTypingIndicator() {
        ChatMessage typingMessage = ChatMessage.createTypingMessage();
        adapter.addMessage(typingMessage);
        scrollToBottom();
    }

    private void scrollToBottom() {
        if (adapter.getItemCount() > 0) {
            messagesRecyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
        }
    }

    private void handleNavigation(EduIntent intent) {
        if (getContext() != null) {
            // Delay navigation slightly to let the user see the response
            mainHandler.postDelayed(() -> {
                if (navigationController.navigate(getContext(), intent)) {
                    dismiss();
                }
            }, 1000);
        }
    }

    private boolean checkAudioPermission() {
        return getContext() != null && 
                ContextCompat.checkSelfPermission(getContext(), 
                        Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAudioPermission() {
        if (getActivity() != null) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_RECORD_AUDIO);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (isListening) {
            voiceRecognitionManager.stopListening();
        }
    }
}
