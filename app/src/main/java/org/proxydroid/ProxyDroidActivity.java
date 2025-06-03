package org.proxydroid;

import androidx.appcompat.app.AppCompatActivity;

public class ProxyDroidActivity extends AppCompatActivity {
    // add ProxyDroid Fragment
    @Override
    public void onStart() {
        super.onStart();
        if (getSupportFragmentManager().findFragmentByTag("proxydroid_fragment") == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new ProxyDroid(), "proxydroid_fragment")
                .commit();
        }
    }
}
