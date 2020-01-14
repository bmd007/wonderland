package ir.tiroon.android.stomp.client.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class EchoMessageRepository {

    private static EchoMessageRepository instance;
    private String echoMessage = "";

    public static EchoMessageRepository getInstance() {
        if (instance == null) {
            instance = new EchoMessageRepository();
        }
        return instance;
    }

    public void setEchoMessage(String name) {
        echoMessage = name;
    }

    public LiveData<String> getEchoMessageLiveData() {
        final MutableLiveData<String> userData = new MutableLiveData<>();
        userData.setValue(echoMessage);
        return userData;
    }

    public String getEchoMessage(){
        return echoMessage;
    }
}
