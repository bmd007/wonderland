package ir.tiroon.android.messenger.client.service;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

import okhttp3.OkHttpClient;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;
import ua.naiksoftware.stomp.dto.StompMessage;

public class StompService {

    private Consumer<String> onMessageListener;
    private StompClient mStompClient;
    public  OkHttpClient okClient;
    private String ip;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public StompService(String ip, String userName, String password, String queueName, Consumer<String> onMessageListener) {
        this.onMessageListener = onMessageListener;

        String userPass = Base64.getEncoder().encodeToString(new StringBuilder().append(userName).append(":").append(password).toString().getBytes());
        okClient = new OkHttpClient.Builder().authenticator((route, response) -> response.request().newBuilder()
                .header("Authorization", "Basic "+ userPass)
                .build())
            .build();
        this.ip = ip;
        String url = "ws://" + ip + ":61613/";
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", "Basic "+ userPass);
        mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, url, headerMap, okClient);

        StompHeader header1 = new StompHeader("login", "guest");
        StompHeader header2 = new StompHeader("passcode", "guest");
        List<StompHeader> headers = new ArrayList<>();
        headers.add(header1);
        headers.add(header2);
        mStompClient.connect(headers);

        mStompClient.lifecycle().subscribe(lifecycleEvent -> {
            switch (lifecycleEvent.getType()) {
                case OPENED: {
                    Logger.getGlobal().info("Stomp connection opened");
                    mStompClient.topic(queueName).map(StompMessage::getPayload).subscribe(onMessageListener::accept);
//                    sendText("Hello", "/app/message");
                    break;
                }
                case ERROR: {
                    Logger.getGlobal().severe(lifecycleEvent.getException().getMessage());
                    lifecycleEvent.getException().printStackTrace();
                    break;
                }
                case CLOSED: {
                    Logger.getGlobal().info("Stomp connection closed");
                    break;
                }
            }
        });
    }

    public void sendText(String message, String destination) {
        mStompClient.send(destination, message).subscribe();
    }

    public void disconnect() {
        mStompClient.disconnect();
    }

}
