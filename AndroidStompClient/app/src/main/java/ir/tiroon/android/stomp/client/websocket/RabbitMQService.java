package ir.tiroon.android.stomp.client.websocket;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.Charset;
import java.util.function.Consumer;

public class RabbitMQService {

    private Consumer<String> onMessageListener;
    String userName;
    String password;
    String virtualHost = "/";
    String serverIp;
    int port = 5672;
    protected Channel mChannel = null;
    protected Connection mConnection;
    Thread t;

    public RabbitMQService(String host, String userName, String password, String queueName, Consumer<String> onMessageListener) {
        this.onMessageListener = onMessageListener;

        this.userName = userName;
        this.password = password;
        this.serverIp = host;
        t = new Thread( () -> {
            try {
                final ConnectionFactory connectionFactory = new ConnectionFactory();
                connectionFactory.setUsername(userName);
                connectionFactory.setPassword(password);
                connectionFactory.setVirtualHost(virtualHost);
                connectionFactory.setHost(serverIp);
                connectionFactory.setPort(port);
                connectionFactory.setAutomaticRecoveryEnabled(true);

                mConnection = connectionFactory.newConnection();
                mChannel = mConnection.createChannel();
                mChannel.basicConsume(queueName, true, (consumerTag, message) -> {
                    String body = new String(message.getBody(), Charset.defaultCharset());
                    onMessageListener.accept(body);
                }, consumerTag -> {
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        t.start();
    }


}

