package com.sniper.Draw;

import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

public class SniperDrawStartActivity extends GraphicsActivity implements ColorPickerDialog.OnColorChangedListener {

	 private Paint       mPaint;
	 private MaskFilter  mEmboss;
	 private MaskFilter  mBlur;
	
	 @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(new MyView(this));

	        this.mPaint = new Paint();
	        this.mPaint.setAntiAlias(true);
	        this.mPaint.setDither(true);
	        this.mPaint.setColor(0xFF00FF00);//background starting color
	        this.mPaint.setStyle(Paint.Style.STROKE);
	        this.mPaint.setStrokeJoin(Paint.Join.ROUND);
	        this.mPaint.setStrokeCap(Paint.Cap.ROUND);//a draw shape
	        this.mPaint.setStrokeWidth(14);//weight of draw

	        this.mEmboss = new EmbossMaskFilter(new float[] { 1, 1, 1 },
	                                       0.4f, 6, 3.5f);
	        this.mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);
	    }

	    public void colorChanged(int color) {
	        this.mPaint.setColor(color);//set color
	    }

	    public class MyView extends View {

	        private static final float MINP = 0.25f;
	        private static final float MAXP = 0.75f;

	        private Bitmap  mBitmap;
	        private Canvas  mCanvas;
	        private Path    mPath;
	        private Paint   mBitmapPaint;

	        public MyView(Context c) {
	            super(c);

	            mPath = new Path();
	            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
	        }

	        @Override
	        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
	            super.onSizeChanged(w, h, oldw, oldh);
	            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
	            mCanvas = new Canvas(mBitmap);
	        }

	        @Override
	        protected void onDraw(Canvas canvas) {
	            canvas.drawColor(0xFFFF6215);//set background of pad
	            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
	            canvas.drawPath(mPath, mPaint);
	        }

	        private float mX, mY;
	        private static final float TOUCH_TOLERANCE = 4;

	        private void touch_start(float x, float y) {//when drawing start
	            mPath.reset();
	            mPath.moveTo(x, y);
	            mX = x;
	            mY = y;
	        }
	        private void touch_move(float x, float y) {//when drawing move
	            float dx = Math.abs(x - mX);
	            float dy = Math.abs(y - mY);
	            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
	                mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
	                mX = x;
	                mY = y;
	            }
	        }
	        private void touch_up() {//when finger up
	            mPath.lineTo(mX, mY);
	            // commit the path to our off screen
	            mCanvas.drawPath(mPath, mPaint);
	            // kill this so we don't double draw
	            mPath.reset();
	        }

	        @Override
	        public boolean onTouchEvent(MotionEvent event) {
	            float x = event.getX();
	            float y = event.getY();

	            switch (event.getAction()) {
	                case MotionEvent.ACTION_DOWN:
	                    touch_start(x, y);
	                    invalidate();
	                    break;
	                case MotionEvent.ACTION_MOVE:
	                    touch_move(x, y);
	                    invalidate();
	                    break;
	                case MotionEvent.ACTION_UP:
	                    touch_up();
	                    invalidate();
	                    break;
	            }
	            return true;
	        }
	    }
	    //constants for the menu
	    private static final int COLOR_MENU_ID = Menu.FIRST;
	    private static final int EMBOSS_MENU_ID = Menu.FIRST + 1;
	    private static final int BLUR_MENU_ID = Menu.FIRST + 2;
	    private static final int ERASE_MENU_ID = Menu.FIRST + 3;
	    private static final int SRCATOP_MENU_ID = Menu.FIRST + 4;

	    @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	        super.onCreateOptionsMenu(menu);

	        menu.add(0, COLOR_MENU_ID, 0, "Color").setShortcut('3', 'c');
	        menu.add(0, EMBOSS_MENU_ID, 0, "Emboss").setShortcut('4', 's');
	        menu.add(0, BLUR_MENU_ID, 0, "Blur").setShortcut('5', 'z');
	        menu.add(0, ERASE_MENU_ID, 0, "Erase").setShortcut('5', 'z');
	        menu.add(0, SRCATOP_MENU_ID, 0, "SrcATop").setShortcut('5', 'z');

	        /****   Is this the mechanism to extend with filter effects?
	        Intent intent = new Intent(null, getIntent().getData());
	        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
	        menu.addIntentOptions(
	                              Menu.ALTERNATIVE, 0,
	                              new ComponentName(this, NotesList.class),
	                              null, intent, 0, null);
	        *****/
	        return true;
	    }

	    @Override
	    public boolean onPrepareOptionsMenu(Menu menu) {
	        super.onPrepareOptionsMenu(menu);
	        return true;
	    }

	    @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	        mPaint.setXfermode(null);
	        mPaint.setAlpha(0xFF);

	        switch (item.getItemId()) {
	            case COLOR_MENU_ID:
	                new ColorPickerDialog(this, this, mPaint.getColor()).show();
	                return true;
	            case EMBOSS_MENU_ID:
	                if (mPaint.getMaskFilter() != mEmboss) {
	                    mPaint.setMaskFilter(mEmboss);
	                } else {
	                    mPaint.setMaskFilter(null);
	                }
	                return true;
	            case BLUR_MENU_ID:
	                if (mPaint.getMaskFilter() != mBlur) {
	                    mPaint.setMaskFilter(mBlur);
	                } else {
	                    mPaint.setMaskFilter(null);
	                }
	                return true;
	            case ERASE_MENU_ID:
	                mPaint.setXfermode(new PorterDuffXfermode(
	                                                        PorterDuff.Mode.CLEAR));
	                return true;
	            case SRCATOP_MENU_ID:
	                mPaint.setXfermode(new PorterDuffXfermode(
	                                                    PorterDuff.Mode.SRC_ATOP));
	                mPaint.setAlpha(0x80);
	                return true;
	        }
	        return super.onOptionsItemSelected(item);
	    }
}