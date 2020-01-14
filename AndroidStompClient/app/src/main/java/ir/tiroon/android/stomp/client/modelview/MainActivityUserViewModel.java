package ir.tiroon.android.stomp.client.modelview;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import ir.tiroon.android.stomp.client.repository.EchoMessageRepository;

public class MainActivityUserViewModel extends ViewModel {

    private MutableLiveData<String> echoMessage;

    public void populateWithData() {
        echoMessage = (MutableLiveData<String>) EchoMessageRepository.getInstance().getEchoMessageLiveData();
    }

    public LiveData<String> getEchoMessage() { return echoMessage;}

    public void updateEchoMessage(String newEchoMessage){
        echoMessage.postValue(newEchoMessage);
        EchoMessageRepository.getInstance().setEchoMessage(newEchoMessage);
    }

}


