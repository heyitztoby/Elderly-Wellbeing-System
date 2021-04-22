package com.elderly.physical.wellbeing.system.app;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.elderly.physical.wellbeing.system.R;

public class Background {

    int x = 0, y = 0;
    Bitmap background;

    Background (int screenX, int screenY, Resources res) {

        background = BitmapFactory.decodeResource(res, R.drawable.background1);
        background = Bitmap.createScaledBitmap(background, screenX, screenY, false);
    }
}
