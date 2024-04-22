package edu.sdsmt.project3johnsonna;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DrawingView extends View  {

    private Paint paint;
    private double currentLatitude;
    private double currentLongitude;
    private double lastLatitude;
    private double lastLongitude;

    public DrawingView(Context context) {
        super(context);
        init();
    }

    public DrawingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        // TODO: draw line
        lastLatitude = currentLatitude;
        lastLongitude = currentLongitude;
    }

    public void updateCoordinates(double longitude, double latitude) {
        Log.i("Emily", "Updated longitude to " + longitude);
        Log.i("Emily", "Updated latitude to " + latitude);
        currentLatitude = longitude;
        currentLongitude = latitude;
        invalidate();
    }
}
