package com.htsc.htscprogressarc;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.htsc.htscprogressarc.update.UpdateDownloadService;
import com.htsc.htscprogressarc.widget.HtscProgressArc;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HtscProgressArc arcProgress = (HtscProgressArc) findViewById(R.id.arc_progress);
        if (arcProgress != null) {
            arcProgress.setProgress(20);
        }

        Button updateButton = (Button) findViewById(R.id.update_button);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UpdateDownloadService.class);
                intent.putExtra("apkUrl", "http://app.jikexueyuan.com/GeekAcademy_release_jikexueyuan_aligned.apk");
                startService(intent);
            }
        });
    }
}
