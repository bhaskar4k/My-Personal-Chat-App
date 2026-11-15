package com.mypersonalchatapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mypersonalchatapp.models.ChatAdapter;
import com.mypersonalchatapp.models.ChatMessage;
import com.mypersonalchatapp.models.ChatRequest;
import com.mypersonalchatapp.models.Dto;
import com.mypersonalchatapp.models.HttpStatusCode;
import com.mypersonalchatapp.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    private ChatAdapter chatAdapter;
    private RecyclerView chatHistory;
    private EditText messageInput;
    private Button sendButton;
    private Call<Dto> pendingCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_chat);
        // Include the keyboard (ime) so the input bar is pushed up above it instead of hidden.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        chatHistory = findViewById(R.id.chatHistory);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        chatAdapter = new ChatAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatHistory.setLayoutManager(layoutManager);
        chatHistory.setAdapter(chatAdapter);

        addMessage(new ChatMessage(getString(R.string.chat_greeting), false));

        sendButton.setOnClickListener(v -> sendMessage());
    }

    @Override
    protected void onDestroy() {
        if (pendingCall != null) {
            pendingCall.cancel();
        }
        super.onDestroy();
    }

    private void sendMessage() {
        String text = messageInput.getText().toString().trim();
        if (text.isEmpty()) {
            return;
        }
        addMessage(new ChatMessage(text, true));
        messageInput.setText("");
        sendButton.setEnabled(false);

        pendingCall = ApiClient.getChatApi().chatWithModel(new ChatRequest(text));
        pendingCall.enqueue(new Callback<Dto>() {
            @Override
            public void onResponse(@NonNull Call<Dto> call, @NonNull Response<Dto> response) {
                sendButton.setEnabled(true);

                if (response.code() != HttpStatusCode.OK.getCode()) {
                    showError(getString(R.string.error_server_status, response.code()));
                    return;
                }

                Dto dto = response.body();
                if (dto == null) {
                    showError(getString(R.string.error_generic));
                    return;
                }
                if (!dto.isSuccess()) {
                    String message = dto.getMessage();
                    showError(message != null && !message.isEmpty() ? message : getString(R.string.error_generic));
                    return;
                }
                if (dto.getData() == null) {
                    showError(getString(R.string.error_generic));
                    return;
                }

                addMessage(new ChatMessage(dto.getData().toString(), false));
            }

            @Override
            public void onFailure(@NonNull Call<Dto> call, @NonNull Throwable t) {
                if (call.isCanceled()) {
                    return;
                }
                Log.e(TAG, "Chat API request failed", t);
                sendButton.setEnabled(true);
                showError(getString(R.string.error_server_unreachable));
            }
        });
    }

    private void addMessage(ChatMessage message) {
        chatAdapter.addMessage(message);
        chatHistory.scrollToPosition(chatAdapter.getItemCount() - 1);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
