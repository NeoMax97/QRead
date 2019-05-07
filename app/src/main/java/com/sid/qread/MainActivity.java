package com.sid.qread;

import android.Manifest;
import android.content.pm.PackageManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import github.nisrulz.qreader.QRDataListener;
import github.nisrulz.qreader.QREader;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    SurfaceView camView;
    Button scanButton;
    TextView resultTextView;
    QREader qReader;
    TextToSpeech tts;
    String result;
    int camPerm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        camPerm = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        // Setup SurfaceView, Scan Button and TextViews
        camView = (SurfaceView) findViewById(R.id.camView);
        scanButton = (Button) findViewById(R.id.scanButton);
        resultTextView = (TextView) findViewById(R.id.resultTextView);
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                tts.setLanguage(Locale.ENGLISH);
            }
        });

        setupQREader();
        if (camPerm == PackageManager.PERMISSION_GRANTED) scanButton.setVisibility(View.GONE);
        scanButton.setOnClickListener(this);

    }

    void setupQREader(){
        qReader = new QREader.Builder(this, camView, new QRDataListener() {
            @Override
            public void onDetected(final String data) {
                Log.d("QREader", "Value : " + data);
                resultTextView.post(new Runnable() {
                    @Override
                    public void run() {
                        resultTextView.setText(data);
                        result = data;
                        if (!tts.isSpeaking()) tts.speak(data, TextToSpeech.QUEUE_FLUSH, null, "");
                    }
                });
            }
        }).facing(QREader.BACK_CAM)
                .enableAutofocus(false)
                .height(camView.getHeight())
                .width(camView.getWidth())
                .build();

        if (camPerm == PackageManager.PERMISSION_GRANTED) qReader.initAndStart(camView);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Initialize and Start with camView
        qReader.initAndStart(camView);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Cleanup
        qReader.releaseAndCleanup();
        if (tts != null){
            tts.stop();
            tts.shutdown();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.scanButton){
            Log.d("camPerm", "Value : " + camPerm);
            if (camPerm != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 1);
            }else{

            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1: {
                //  grantResults is empty if the request is cancelled
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    camPerm = PackageManager.PERMISSION_GRANTED;
                    scanButton.setVisibility(View.GONE);
                    finish();
                    startActivity(getIntent());
                    // onResume();
                }else{
                    Toast.makeText(this, "Camera Permission is required for QR Scanner to work", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
