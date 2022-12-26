package com.tamemm.remoteaccess.demo.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.EditText;

import com.tamemm.remoteaccess.demo.R;

import java.util.Random;

import pub.devrel.easypermissions.EasyPermissions;

import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCenter.start(getApplication(), "da3a1e9e-8f13-49d8-b113-e71865bc779d",
                Analytics.class, Crashes.class);
        // Define ActionBar object
        ActionBar actionBar;
        actionBar = getSupportActionBar();

        // Define ColorDrawable object and parse color
        // using parseColor method
        // with color hash code as its parameter
        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor("#14437b"));

        // Set BackgroundDrawable
        actionBar.setBackgroundDrawable(colorDrawable);

        final EditText serverEditText = findViewById(R.id.ServerEditText);
        final EditText roomEditText = findViewById(R.id.RoomEditText);

        String ServerAddress = serverEditText.getText().toString();

        String RoomName = LoadPreferences("RoomName");

        if (!"".equals(RoomName)) {
            Intent intent = new Intent(MainActivity.this, CallActivity.class);
            intent.putExtra("ServerAddr", ServerAddress);
            intent.putExtra("RoomName", RoomName);
            startActivity(intent);
        } else {
            String RoomNameNew = getRandomNumberString();
            SavePreferences("RoomName", RoomNameNew);
            Intent intent = new Intent(MainActivity.this, CallActivity.class);
            intent.putExtra("ServerAddr", ServerAddress);
            intent.putExtra("RoomName", RoomNameNew);
            startActivity(intent);
        }

        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "Need permissions for camera & microphone", 0, perms);
        }
    }

    private void SavePreferences(String key, String value) {
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    private String LoadPreferences(String key) {
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        String value = sharedPreferences.getString(key, "");
        return value;
    }

    public static String getRandomNumberString() {
        // It will generate 6 digit random Number.
        // from 0 to 999999
        Random rnd = new Random();
        int number = rnd.nextInt(999999);

        // this will convert any number sequence into 6 character.
        return String.format("%06d", number);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    protected void onDestroy() {
        super.onDestroy();
    }
}
