package com.elderly.physical.wellbeing.system.app;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

import java.util.ArrayList;

public class Bird extends BaseObject {
    private ArrayList<Bitmap> arrBMS = new ArrayList<>();
    private int count, vFlap, idCurrentBitmap;
    private float drop;
    public Bird() {
        this.count = 0;
        this.vFlap = 5;
        this.idCurrentBitmap = 0;
        this.drop = 0;
    }

    public void draw(Canvas canvas) {
        drop();
        canvas.drawBitmap(this.getBm(), this.x, this.y, null);
    }

    private void drop() {
        this.drop += 0.04;
        this.y += this.drop;
    }

    public ArrayList<Bitmap> getArrBMS() {
        return arrBMS;
    }

    public void setArrBMS(ArrayList<Bitmap> arrBMS) {
        this.arrBMS = arrBMS;
        for (int i = 0; i < arrBMS.size(); i++)
        {
            this.arrBMS.set(i, Bitmap.createScaledBitmap(this.arrBMS.get(i), this.width, this.height, true));
        }
    }

    @Override
    public Bitmap getBm() {
        count++;
        if (this.count == this.vFlap) {
            for (int i = 0; i < arrBMS.size(); i++) {
                if (i == arrBMS.size() - 1) {
                    this.idCurrentBitmap = 0;
                    break;
                }

                else if (this.idCurrentBitmap == i) {
                    idCurrentBitmap = i + 1;
                    break;
                }
            }

            count = 0;
        }

        if (this.drop < 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(-25);
            return Bitmap.createBitmap(arrBMS.get(idCurrentBitmap), 0, 0, arrBMS.get(idCurrentBitmap).getWidth(),
                    arrBMS.get(idCurrentBitmap).getHeight(), matrix, true);
        }
        else if (drop >= 0) {
            Matrix matrix = new Matrix();
            if (drop < 30) {
                matrix.postRotate(-25 + (drop));
            }
            else {
                matrix.postRotate(0);
            }

            return Bitmap.createBitmap(arrBMS.get(idCurrentBitmap), 0, 0, arrBMS.get(idCurrentBitmap).getWidth(),
                    arrBMS.get(idCurrentBitmap).getHeight(), matrix, true);
        }
        return this.arrBMS.get(idCurrentBitmap);
    }

    public float getDrop() {
        return drop;
    }

    public void setDrop(float drop) {
        this.drop = drop;
    }
}
