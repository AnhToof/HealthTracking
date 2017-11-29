package com.g5team.healthtracking.Views;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.g5team.healthtracking.Fragments.MeasureFragment;
import com.g5team.healthtracking.R;

/**
 * Created by Toof on 10/11/2017.
 */

public class CircleProgressBar extends View {


    /**
     * ProgressBar's line thickness
     */
    private float strokeWidth = 10;
    private float progress = 0;
    private int min = 0;
    private int max = 100;
    private boolean isFinish = false;
    private ObjectAnimator objectAnimator;

    @Override
    protected float getBottomFadingEdgeStrength() {
        return super.getBottomFadingEdgeStrength();
    }

    /**
     * Start the progress at 12 o'clock
     */
    private int startAngle = -90;
    private int color = Color.RED;
    private RectF rectF;
    private Paint backgroundPaint;
    private Paint foregroundPaint;
    private static final Matrix matrix = new Matrix();
    private static final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static Bitmap darkBitmap = null;
    private static Bitmap redBitmap = null;

    private static int parentWidth = 0;
    private static int parentHeight = 0;


    public float getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
        backgroundPaint.setStrokeWidth(strokeWidth);
        foregroundPaint.setStrokeWidth(strokeWidth);
        invalidate();
        requestLayout();//Because it should recalculate its bounds
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
        invalidate();
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
        invalidate();
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
        invalidate();
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
        backgroundPaint.setColor(adjustAlpha(color, 0.3f));
        foregroundPaint.setColor(color);
        invalidate();
        requestLayout();
    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        rectF = new RectF();
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CircleProgressBar,
                0, 0);
        //Reading values from the XML layout
        try {
            strokeWidth = typedArray.getDimension(R.styleable.CircleProgressBar_progressBarThickness, strokeWidth);
            progress = typedArray.getFloat(R.styleable.CircleProgressBar_progress, progress);
            color = typedArray.getInt(R.styleable.CircleProgressBar_progressbarColor, color);
            min = typedArray.getInt(R.styleable.CircleProgressBar_min, min);
            max = typedArray.getInt(R.styleable.CircleProgressBar_max, max);
        } finally {
            typedArray.recycle();
        }

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(Color.GRAY);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(strokeWidth);

        foregroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        foregroundPaint.setColor(color);
        foregroundPaint.setStyle(Paint.Style.STROKE);
        foregroundPaint.setStrokeWidth(strokeWidth);

        darkBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dark_icon);
        redBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.red_icon);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawOval(rectF, backgroundPaint);
        float angle = 360 * progress / max;
        canvas.drawArc(rectF, startAngle, angle, false, foregroundPaint);

        if (canvas == null) throw new NullPointerException();
        Bitmap bitmap = null;
        if (MeasureFragment.getCurrent() == MeasureFragment.TYPE.DARK) bitmap = darkBitmap;
        else bitmap = redBitmap;
        bitmap = Bitmap.createScaledBitmap(bitmap, 230, 230, false);
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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        //final int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        ///final int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);

        int min = MeasureSpec.getSize(getResources().getDimensionPixelSize(R.dimen.width_height_progress))/2;
        setMeasuredDimension(min, min);
        rectF.set(0 + strokeWidth / 2, 0 + strokeWidth / 2, min - strokeWidth / 2, min - strokeWidth / 2);
        parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(parentWidth, parentHeight);
    }

    /**
     * Lighten the given color by the factor
     *
     * @param color  The color to lighten
     * @param factor 0 to 4
     * @return A brighter color
     */
    public int lightenColor(int color, float factor) {
        float r = Color.red(color) * factor;
        float g = Color.green(color) * factor;
        float b = Color.blue(color) * factor;
        int ir = Math.min(255, (int) r);
        int ig = Math.min(255, (int) g);
        int ib = Math.min(255, (int) b);
        int ia = Color.alpha(color);
        return (Color.argb(ia, ir, ig, ib));
    }

    /**
     * Transparent the given color by the factor
     * The more the factor closer to zero the more the color gets transparent
     *
     * @param color  The color to transparent
     * @param factor 1.0f to 0.0f
     * @return int - A transplanted color
     */
    public int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public boolean isFinish(){
        return isFinish;
    }
    public void setProgressWithAnimation() {
        objectAnimator = ObjectAnimator.ofFloat(this, "progress", 0, 100);
        objectAnimator.setDuration(20000);
        objectAnimator.setInterpolator(new DecelerateInterpolator());
        objectAnimator.start();
    }
}