package edu.sdsmt.project3johnsonna;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.os.Parcel;
import android.os.Parcelable;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DrawingView extends View {

    private Paint paint;
    private CustomPath path;
    private ArrayList<CustomPath> paths = new ArrayList<>();
    private ArrayList<String> colors = new ArrayList<>();
    private Location originalLocation;
    private float circleLocationX;
    private float circleLocationY;

    private float scaleFactor = (float) 500000; // Adjust this value to fit your drawing area

    private static final String PATH_STATE_KEY = "path_state";
    private static final String ORIGINAL_LOCATION_KEY = "original_location";
    private static final String RED_PATH = "red_path";
    private static final String GREEN_PATH = "green_path";

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
        path = new CustomPath();
        path.moveTo((float) getWidth() / 2, (float) getHeight() / 2);
        circleLocationX = (float) getWidth() / 2;
        circleLocationY = (float) getHeight() / 2;
        originalLocation = null;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            paint.setColor(Color.RED);
        } else {
            paint.setColor(Color.GREEN);
        }

        canvas.drawPath(path, paint);

        if (paths.size() > 0) {
            for (CustomPath p : paths) {
                if (colors.get(paths.indexOf(p)) == RED_PATH) {
                    paint.setColor(Color.RED);
                } else {
                    paint.setColor(Color.GREEN);
                }
                canvas.drawPath(p, paint);
            }
        }

        paint.setColor(Color.BLUE);

        if (originalLocation != null) {
            canvas.drawCircle(circleLocationX, circleLocationY, 10, paint);
        } else {
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, 10, paint);
        }
    }

    public void updateLocation(Location newLocation) {
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
        circleLocationX = (float) ((longitude - originalLocation.getLongitude()) * scaleFactor) + (float) (getWidth() / 2);
        return circleLocationX;
    }

    private float getYFromLatitude(double latitude) {
        circleLocationY = (float) ((latitude - originalLocation.getLatitude()) * scaleFactor) + (float) (getHeight() / 2);
        return circleLocationY;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (circleLocationX < 0.1 && circleLocationY < 0.1) {
            path.moveTo((float) getWidth() / 2, (float) getHeight() / 2);
        } else {
            path.moveTo(circleLocationX, circleLocationY);
        }
    }

    /* custom code ******************/

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        paths.add(path);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            colors.add(RED_PATH);
        } else {
            colors.add(GREEN_PATH);
        }

        Bundle bundle = new Bundle();
        bundle.putParcelable(PATH_STATE_KEY, super.onSaveInstanceState());
        bundle.putDouble("originalLongitude", originalLocation.getLongitude());
        bundle.putDouble("originalLatitude", originalLocation.getLatitude());
        bundle.putFloat("circleLocationX", circleLocationX);
        bundle.putFloat("circleLocationY", circleLocationY);
        bundle.putParcelableArrayList("paths", paths);
        bundle.putStringArrayList("colors", colors);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            paths = bundle.getParcelableArrayList("paths");
            colors = bundle.getStringArrayList("colors");
            originalLocation.setLatitude(bundle.getDouble("originalLatitude"));
            originalLocation.setLongitude(bundle.getDouble("originalLongitude"));
            circleLocationY = bundle.getFloat("circleLocationY");
            circleLocationX = bundle.getFloat("circleLocationX");
            state = bundle.getParcelable(PATH_STATE_KEY);
        }
        path = new CustomPath();
        super.onRestoreInstanceState(state);
    }

    public static class CustomPath extends Path implements Parcelable {

        private ArrayList<PathAction> actions = new ArrayList<>();

        public CustomPath() {
        }

        protected CustomPath(Parcel in) {
            super();
            in.readList(actions, CustomPath.class.getClassLoader());
            drawThisPath();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeList(actions);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public final Creator<CustomPath> CREATOR = new Creator<CustomPath>() {
            @Override
            public CustomPath createFromParcel(Parcel in) {
                return new CustomPath(in);
            }

            @Override
            public CustomPath[] newArray(int size) {
                return new CustomPath[size];
            }
        };

        private void drawThisPath() {
            for (PathAction p : actions) {
                if (p.getType().equals(PathAction.PathActionType.MOVE_TO)) {
                    super.moveTo(p.getX(), p.getY());
                } else if (p.getType().equals(PathAction.PathActionType.LINE_TO)) {
                    super.lineTo(p.getX(), p.getY());
                }
            }
        }

        @Override
        public void moveTo(float x, float y) {
            actions.add(new ActionMove(x, y));
            super.moveTo(x, y);
        }

        @Override
        public void lineTo(float x, float y) {
            actions.add(new ActionLine(x, y));
            super.lineTo(x, y);
        }

        public interface PathAction {
            enum PathActionType {LINE_TO, MOVE_TO}

            PathActionType getType();

            float getX();

            float getY();
        }

        public class ActionMove implements PathAction {
            private final float x, y;

            public ActionMove(float x, float y) {
                this.x = x;
                this.y = y;
            }

            @Override
            public PathActionType getType() {
                return PathActionType.MOVE_TO;
            }

            @Override
            public float getX() {
                return x;
            }

            @Override
            public float getY() {
                return y;
            }
        }

        public class ActionLine implements PathAction {
            private final float x, y;

            public ActionLine(float x, float y) {
                this.x = x;
                this.y = y;
            }

            @Override
            public PathActionType getType() {
                return PathActionType.LINE_TO;
            }

            @Override
            public float getX() {
                return x;
            }

            @Override
            public float getY() {
                return y;
            }
        }
    }
}