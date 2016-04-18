package to.pns.lib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class SlantingScrollView extends ViewGroup
{
	public interface OnScrollListener
	{
		public void onSlantingScroll();
	}
	
	static final int TAG_ZODER = 0x20000000;
	static final int BAR_SIZE = 4;
	
	static final int	ANIMATION_PERIOD = 20;
	static final float	ANIMATION_DECELERATION = 0.99f;
	
	private OnScrollListener mScrollListener;
	private float mLastMotionX;
	private float mLastMotionY;
	private float mScrollAnimeX;
	private float mScrollAnimeY;
	private boolean mScrollRangeFlag = false;
	private int mScrollRangeX;
	private int mScrollRangeY;

	private Timer mScrollTimer;
	private long mLastTime;
	private ArrayList<Integer> mZList;
	public SlantingScrollView(Context context) {
		super(context);
		setFocusable(true);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        setWillNotDraw(false);
        setChildrenDrawingOrderEnabled(true);
	}
	public SlantingScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public void setOnScrollListener(OnScrollListener listener)
	{
		mScrollListener = listener;
	}
	void sort()
	{
		ArrayList<Integer> list = new ArrayList<Integer>();
		int count = getChildCount();
		for(int i=0;i<count;i++)
		{
			list.add(i);
		}
		Collections.sort(list,new Comparator<Integer>() {
			public int compare(Integer lhs, Integer rhs) {
				View view1 = getChildAt(lhs);
				View view2 = getChildAt(rhs);
				Integer zoder1 = (Integer) view1.getTag(TAG_ZODER);
				Integer zoder2 = (Integer) view2.getTag(TAG_ZODER);
				zoder1 = zoder1==null?0:zoder1;
				zoder2 = zoder2==null?0:zoder2;
				return zoder1 - zoder2;
			};
		});
		mZList = list;
	}
	@Override
	protected int getChildDrawingOrder(int childCount, int i) {
		if(mZList == null || mZList.size() < childCount)
			return i;
		else
			return mZList.get(i);
    }
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch(ev.getAction())
		{
		case MotionEvent.ACTION_DOWN:
			mLastMotionX = ev.getRawX(); 
			mLastMotionY = ev.getRawY(); 
			mLastTime = System.nanoTime();
			break;
		case MotionEvent.ACTION_UP:
			break;
		case MotionEvent.ACTION_MOVE: 
			final float x = ev.getRawX(); 
			final float y = ev.getRawY(); 
			final long time = System.nanoTime();
			final long r = time - mLastTime;
			final float deltaX = (mLastMotionX - x)*1000*1000*ANIMATION_PERIOD / r;
			final float deltaY = (mLastMotionY - y)*1000*1000*ANIMATION_PERIOD / r;
			mLastMotionX = x; 
			mLastMotionY = y; 
			mLastTime = time;
			//if((deltaX*deltaX+deltaY*deltaY) > 0)
			{
				scrollAnime(deltaX,deltaY);
			}
			break;
		}
		return false;
	}


	private void scrollAnime(float x,float y)
	{
		synchronized(this)
		{
			mScrollAnimeX = x;
			mScrollAnimeY = y;
			
			if(mScrollTimer == null)
			{
				final Handler handler = new Handler();
				final Runnable proc = new Runnable() {
					@Override
					public void run() {
						final float x = mScrollAnimeX;
						final float y = mScrollAnimeY;
						scrollBy((int)x,(int)y);
						
						if(mScrollTimer != null && Math.abs(mScrollAnimeX) < 1.0f && Math.abs(mScrollAnimeY) < 1.0f)
						{
							mScrollTimer.cancel();
							mScrollTimer = null;
						}
						mScrollAnimeX *= ANIMATION_DECELERATION;
						mScrollAnimeY *= ANIMATION_DECELERATION;
					}
				};
				mScrollTimer = new Timer();
				mScrollTimer.schedule(new TimerTask() {
					@Override
					public void run() {
						handler.post(proc);
					}
				}, 1,ANIMATION_PERIOD);
			}
		}
	}


	@Override
	protected void dispatchDraw(Canvas canvas) {
		int bold = (int)(BAR_SIZE*getResources().getDisplayMetrics().density);
		int width = getWidth();
		int height = getHeight();
		int rangeX = getScrollRangeX();
		int rangeY = getScrollRangeY();
		
		int scrollX = computeHorizontalScrollOffset();
		int scrollY = computeVerticalScrollOffset();
		Paint paint = new Paint();
		paint.setColor(Color.argb(120, 10, 10, 10));
		Paint paint2 = new Paint();
		paint2.setColor(Color.argb(120, 200, 200, 200));
		
        sort();
		super.dispatchDraw(canvas);

		canvas.translate(scrollX,scrollY);

		width -= bold;
		height -= bold;
		if(rangeX > width+bold)
		{
			//横
			canvas.drawRect(0, height, width, height, paint);
			int barWidth = width*width / rangeX;
			int barX = (width-barWidth)*scrollX / (rangeX-width);
			if(barWidth < bold)
				barWidth = bold;		
			canvas.drawRect(barX, height, barX+barWidth, height+bold, paint2);
		}
		if(rangeY > height+bold)
		{
			//縦
			canvas.drawRect(width, 0, width+bold, height, paint);
			int barHeight = height*height / rangeY;
			int barY = (height-barHeight)*scrollY / (rangeY-height);
			if(barHeight < bold)
				barHeight = bold;
			canvas.drawRect(width, barY, width+bold, barY+barHeight, paint2);
		}

	}
	@Override
	protected int computeHorizontalScrollRange()
	{
		int range = getScrollRangeX() - getWidth();
		return range < 0?0:range;
	}
	@Override
	protected int computeVerticalScrollRange()
	{
		int range = getScrollRangeY() - getHeight();
		return range < 0?0:range;
	}	
    private int getScrollRangeY() {
    	if(mScrollRangeFlag)
    		return mScrollRangeY;
        int scrollRange = 0;
		final int count = getChildCount();
        for (int i = 0; i < count; i++)
        {
            final View child = getChildAt(i);
            int height = child.getMeasuredHeight();
            scrollRange += height>0?height:child.getHeight();
        }
        mScrollRangeY = scrollRange;
        return scrollRange;
    }
    private int getScrollRangeX() {
       	if(mScrollRangeFlag)
    		return mScrollRangeX;
       int scrollRange = 0;
		final int count = getChildCount();
        for (int i = 0; i < count; i++)
        {
            final View child = getChildAt(i);
            int width = child.getMeasuredWidth();
            scrollRange = Math.max(scrollRange,width>0?width:child.getWidth());
        }
        mScrollRangeX = scrollRange;
        return scrollRange;
    }

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		//子に対してサイズ調整
		measureChildren(
				MeasureSpec.makeMeasureSpec(widthMeasureSpec, MeasureSpec.UNSPECIFIED), 
				MeasureSpec.makeMeasureSpec(heightMeasureSpec, MeasureSpec.UNSPECIFIED));
		mScrollRangeFlag = false;
		int rangeX = getScrollRangeX();
		int rangeY = getScrollRangeY();
		mScrollRangeFlag = true;
		int minWidth = 0;
		int minHeight = (int)(400*getResources().getDisplayMetrics().density);
		int height;


		height = MeasureSpec.getSize(heightMeasureSpec);
		if(MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED){
			if(height == 0 || minHeight < height)
				height = minHeight;
			if(height == 0 || rangeY < height)
				height = rangeY;
		}


		if(widthMeasureSpec > 0)
			minWidth = widthMeasureSpec;
		else
			minWidth = rangeX < minWidth?rangeX:minWidth;

		
        setMeasuredDimension(
        		resolveSize(rangeX, widthMeasureSpec),
        		height);
        //Log.d("",String.format("%d,%d", resolveSize(rangeX, widthMeasureSpec),height));

	}
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int top = 0;
		final int count = getChildCount();
        for (int i = 0; i < count; i++)
        {
            final View child = getChildAt(i);
        	int height = child.getHeight();
        	if(height == 0)
        		height = child.getMeasuredHeight();
            child.layout(0, top, child.getMeasuredWidth(), top+height);
            top += height;
        }
	}	

	public void scrollTo(int x,int y)
	{
		int scrollX = x;
		int scrollY = y;
		if(scrollX < 0)
			scrollX = 0;
		if(scrollY < 0)
			scrollY = 0;
		final int rangeX = computeHorizontalScrollRange();
		final int rangeY = computeVerticalScrollRange();
		if(scrollX > rangeX)
			scrollX = rangeX;
		if(scrollY > rangeY)
			scrollY = rangeY;	

		super.scrollTo(scrollX, scrollY);
	}
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if(mScrollListener != null)
			mScrollListener.onSlantingScroll();
	}

}