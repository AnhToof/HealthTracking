package com.g5team.healthtracking.Views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.g5team.healthtracking.Fragments.MeasureFragment;
import com.g5team.healthtracking.R;

/**
 * Created by Toof on 10/12/2017.
 */

public class HeartBeatView extends View {

    private static final Matrix matrix = new Matrix();
    private static final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static Bitmap darkBitmap = null;
    private static Bitmap redBitmap = null;

    private static int parentWidth = 0;
    private static int parentHeight = 0;

    public HeartBeatView(Context context, AttributeSet attr) {
        super(context, attr);

        darkBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_unbeat);
        redBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_beat);
    }

    public HeartBeatView(Context context) {
        super(context);

        darkBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_unbeat);
        redBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_beat);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(parentWidth, parentHeight);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDraw(Canvas canvas) {
        if (canvas == null) throw new NullPointerException();

        Bitmap bitmap = null;
        if (MeasureFragment.getCurrent() == MeasureFragment.TYPE.DARK) bitmap = darkBitmap;
        else bitmap = redBitmap;

        int bitmapX = bitmap.getWidth() / 2;
        int bitmapY = bitmap.getHeight() / 2;

        int parentX = parentWidth / 2;
        int parentY = parentHeight / 2;

        int centerX = parentX - bitmapX;
        int centerY = parentY - bitmapY;

        matrix.reset();
        matrix.postTranslate(centerX, centerY);
        canvas.drawBitmap(bitmap, matrix, paint);
    }
}
