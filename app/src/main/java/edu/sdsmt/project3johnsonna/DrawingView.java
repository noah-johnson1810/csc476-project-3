package edu.sdsmt.project3johnsonna;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.location.Location;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DrawingView extends View {

    private Paint paint;
    private Path path;
    private Location originalLocation;
    private float circleLocationX;
    private float circleLocationY;

    private float scaleFactor = (float) 100000; // Adjust this value to fit your drawing area

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

    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        path = new Path();
        path.moveTo((float) getWidth() / 2, (float) this.getHeight() / 2);
        circleLocationX = (float) this.getWidth() / 2;
        circleLocationY = (float) this.getHeight() / 2;
        originalLocation = null;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, paint);

        if (originalLocation != null) {
            paint.setColor(Color.BLUE);
            canvas.drawCircle(circleLocationX, circleLocationY, 10, paint);
        } else {
            paint.setColor(Color.BLUE);
            canvas.drawCircle(this.getWidth() / 2, this.getHeight() / 2, 10, paint);
        }

        paint.setColor(Color.RED);
    }

    public void updateLocation(Location newLocation) {
        Log.i("Emily", "Updating location " + newLocation.getLongitude() + " " + newLocation.getLatitude());
        if (originalLocation == null) {
            originalLocation = newLocation;
            float centerX = getWidth() / 2f;
            float centerY = getHeight() / 2f;
            path.moveTo(centerX, centerY);
        } else {
            path.lineTo(getXFromLongitude(newLocation.getLongitude()), getYFromLatitude(newLocation.getLatitude()));
        }
        invalidate();
    }


    private float getXFromLongitude(double longitude) {
        circleLocationX = (float) ((longitude - originalLocation.getLongitude()) * scaleFactor) + (float) (this.getWidth() / 2);
        Log.i("Emily", "About to return " + circleLocationX);
        return circleLocationX;
    }

    private float getYFromLatitude(double latitude) {
        circleLocationY = (float) ((latitude - originalLocation.getLatitude()) * scaleFactor) + (float) (this.getHeight() / 2);
        Log.i("Emily", "About to return " + circleLocationY);
        return circleLocationY;
    }
}
