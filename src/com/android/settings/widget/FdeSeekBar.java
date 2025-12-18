package com.android.settings.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.SeekBar;
import com.android.settings.utils.ContentCaptureUtils;
import android.util.TypedValue;
import android.util.DisplayMetrics;
import com.android.settings.R;
import android.provider.Settings;
import android.graphics.Typeface;

public class FdeSeekBar extends SeekBar {
    private Paint paint;
    private int progress = 0 ;
    private float mTextSize = 14; 
    private String textContext = "";
    DisplayMetrics displayMetrics = new DisplayMetrics();
    Context context;

    public FdeSeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        init();
    }

    public FdeSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    public FdeSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public FdeSeekBar(Context context) {
        super(context);
        this.context = context;
        init();
    }

    private void init() {
        paint = new Paint();
        float pixelSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mTextSize, getContext().getResources().getDisplayMetrics());
        paint.setTextSize(pixelSize);
        paint.setColor(Color.BLACK);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD ));

    }

    public void updateText(int progress,String text ){
        this.progress = progress ;
        this.textContext = text;
        invalidate();
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // canvas.drawText(textContext,0, 6 * ContentCaptureUtils.getRatioHeight(),paint);

    }
}
