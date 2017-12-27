package io.noisyfox.oplongshotxposed;

import android.app.Activity;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onHideIconClicked(View view) {
        PackageManager p = getPackageManager();
        ComponentName componentName = new ComponentName(this, "io.noisyfox.oplongshotxposed.MainActivityAlias");
        p.setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        p.getComponentEnabledSetting(componentName);

        Toast.makeText(this, R.string.hide_icon_msg, Toast.LENGTH_LONG).show();
    }
}
