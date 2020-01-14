package ir.tiroon.android.stomp.client.view;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import ir.tiroon.android.stomp.client.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow();
        }
    }
}
