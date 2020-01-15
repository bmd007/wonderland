package ir.tiroon.android.messenger.client.service;

import java.util.function.Consumer;
import java.util.logging.Logger;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class SimpleWebSocketEchoService {

    private Consumer<String> onMessageListener;
    private WebSocket ws;
    public  OkHttpClient okClient;

    public SimpleWebSocketEchoService(String url,Consumer<String> onMessageListener) {
        this.onMessageListener = onMessageListener;

        okClient = new OkHttpClient.Builder().authenticator((route, response) ->
            response.request().newBuilder()
            .build()).build();

        Request request = new Request.Builder().url(url).build();
        WebSocketListener listener = new EchoWebSocketListener();
        ws = okClient.newWebSocket(request, listener);
    }

    private final class EchoWebSocketListener extends WebSocketListener {

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            webSocket.send("Hello!");
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            onMessageListener.accept(text);
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            System.out.println(new String(bytes.toByteArray()));
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(NORMAL_CLOSURE_STATUS, null);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            Logger.getGlobal().severe(t.getMessage());
        }
    }

    public void sendText(String message){
        ws.send(message);
    }

    private static final int NORMAL_CLOSURE_STATUS = 1000;
    public void shutdown(){
        ws.close(NORMAL_CLOSURE_STATUS, "Goodbye !");
//        HttpClient.okClient.dispatcher().executorService().shutdown();
    }
}

//https://github.com/NaikSoftware/StompProtocolAndroid
