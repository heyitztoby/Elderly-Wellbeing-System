package com.elderly.physical.wellbeing.system;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.elderly.physical.wellbeing.system.app.GameActivity1Test;

public class GamePageActivity extends AppCompatActivity {

    ImageButton btn_game1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_game_page);

        btn_game1 = findViewById(R.id.btn_Game1);

        btn_game1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GamePageActivity.this, GameActivity1Test.class);
                startActivity(intent);
            }
        });
    }
}