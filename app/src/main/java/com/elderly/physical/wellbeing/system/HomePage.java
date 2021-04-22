package com.elderly.physical.wellbeing.system;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.elderly.physical.wellbeing.system.app.GameActivity1Test;

import java.util.Calendar;
import java.util.Date;

public class HomePage extends AppCompatActivity {

    ImageButton btn_Game1;
    Button btn_startGame;
    private String lastLaunch;
    private TextView txt_lastLaunch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_home_page);

        Date currentTime = Calendar.getInstance().getTime();


        SharedPreferences sp = this.getPreferences(Context.MODE_PRIVATE);

        txt_lastLaunch = findViewById(R.id.txt_lastlaunch);
        lastLaunch = sp.getString("launch", "First launch!");

        txt_lastLaunch.setText("Last Launched: " + lastLaunch);

        SharedPreferences.Editor editor = sp.edit();
        editor.putString("launch", currentTime.toString());
        editor.commit();

        btn_Game1 = findViewById(R.id.btn_Game1);

        btn_startGame = findViewById(R.id.btn_startGame);

        btn_Game1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, GameActivity1Test.class);
                startActivity(intent);
            }
        });

        btn_startGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, GamePageActivity.class);
                startActivity(intent);
            }
        });
    }
}