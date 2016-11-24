package to.pns.lib;

import java.util.ArrayList;
import java.util.Vector;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LineLayout extends ViewGroup implements View.OnClickListener
{
	float mPaddingWidth;
	float mPaddingHeight;
	Vector<Integer> mWidths;
	int mBackColor = 0xff00ff00;
	int mTextColor = 0xbbffffff;
	ArrayList<CenterLayout> mlistItem = new ArrayList<CenterLayout>();
	int mLineWidth;
	int mLineHeight;
	_OnListItemListener mOnListItemListener;
	private float mLineSize;
	
	
	public LineLayout(Context context)
	{
		super(context);
		setPadding(20.0f,20.0f);
		setLineSize(1.0f);
	}
	public void setOnItemListener(_OnListItemListener listener)
	{
		mOnListItemListener = listener;
	}	
	public void setLineWidth(int size)
	{
		mLineWidth = size;
	}
	public void setBackColor(int color)
	{
		mBackColor = color;
		
		for(CenterLayout item : mlistItem)
		{
			item.setBackgroundColor(color);
		}
	}
	public void setTextColor(int color)
	{
		mTextColor = color;
	}

	public int getItemCount()
	{
		return mlistItem.size();
	}
	public int getItemWidth(int index)
	{
		return (int)(mlistItem.get(index).getMaxWidth()+mPaddingWidth);
	}
	public void setItemWidth(int index,int width)
	{
		View view = mlistItem.get(index);
		if(view != null)
		{
			int x = view.getLeft();
			int y = view.getTop();
			int x2 = x+width;
			int y2 = view.getBottom();
			view.layout(x,y,x2,y2);
		}
	}
	public void setItemGravity(int index,int gravity)
	{
		mlistItem.get(index).setGravity(gravity);;
	}
	public void setLineSize(float size)
	{
		final float scale = getContext().getResources().getDisplayMetrics().density;
		mLineSize = size * scale;
	}
	public void setPadding(float width,float height)
	{
		final float scale = getContext().getResources().getDisplayMetrics().density;
		mPaddingWidth = width * scale;
		mPaddingHeight = height * scale;
	}
	public int addItem(View view)
	{
		CenterLayout frame = new CenterLayout(getContext());
		frame.setOnClickListener(this);
		frame.setBackgroundColor(mBackColor);
		addView(frame);
		mlistItem.add(frame);
		frame.addView(view);
		return mlistItem.size();		
	}
	public int addItem(String text)
	{
		TextView textView = new TextView(getContext());
		textView.setTextColor(mTextColor);
		textView.setText(text);
		textView.setGravity(Gravity.CENTER);
		return addItem(textView);
	}
	public int setItem(int index,String text)
	{
		while(index>=mlistItem.size())
		{
			addItem("");
		}
		View o = mlistItem.get(index).getChildAt(0);
		if(o instanceof TextView)
		{
			TextView t = (TextView)o;
			t.setText(text);
		}
		else
		{
			mlistItem.get(index).removeAllViews();
			TextView textView = new TextView(getContext());
			textView.setTextColor(mTextColor);
			textView.setText(text);
			textView.setGravity(Gravity.CENTER);
			mlistItem.get(index).addView(textView);
		}
		return mlistItem.size();	
	}
	public void setItem(int index,View view)
	{
		while(index>=mlistItem.size())
		{
			addItem("");
		}
		CenterLayout layout = mlistItem.get(index);
		layout.removeAllViews();
		layout.addView(view,new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	}
	public String getItemText(int index)
	{
		return ((TextView)mlistItem.get(index).getChildAt(0)).getText().toString();
	}
	public Object getItem(int index)
	{
		return mlistItem.get(index).getChildAt(0);
	}
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		//子に対してサイズ調整
		measureChildren(widthMeasureSpec, heightMeasureSpec);
		final int count = getChildCount();
		float maxHeight = 0;
		float maxWidth = 0;
        int i;
        for (i = 0; i < count; i++)
        {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE)
            {
            	int width = (int)(child.getMeasuredWidth()+mPaddingWidth);
            	if(mWidths.get(i) < width)
            		mWidths.set(i,width);
           		maxWidth += mWidths.get(i);
            	maxHeight = Math.max(maxHeight, child.getMeasuredHeight()+mPaddingHeight+mLineSize);
            }
        }
        maxWidth += mLineSize*count;
        if(mLineWidth != 0 && mLineWidth > maxWidth)
        	maxWidth = mLineWidth;
       	setMeasuredDimension((int)maxWidth,(int)maxHeight);
        mLineHeight = (int) maxHeight;
	}
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b)
	{
		if(mWidths == null)
			return;
		
		int i;
		int count = getChildCount();

		float itemWidth = mLineSize*mWidths.size()-1;
		for(i=0;i<mWidths.size();i++)
		{
			itemWidth += mWidths.get(i);
		}
		float f = 1.0f;
		if(mLineWidth > itemWidth && mLineWidth != 0 && itemWidth != 0)
			f = mLineWidth / itemWidth;
		
		float x = 0.0f;
		for(i=0;i<count;i++)
		{
			CenterLayout view = (CenterLayout)getChildAt(i);
			float width = mWidths.get(i)*f+(i==count-1?mLineSize*2:0);
			float height = mLineHeight;
			view.setPadding(mPaddingWidth,mPaddingHeight);
			view.layout((int)x,0, (int)(x+width),(int) (height-mLineSize));
			x += width+mLineSize;
		}
	}
	public void setItemWidths(Vector<Integer> widths)
	{
		this.mWidths = widths;
	}
	@Override
	public void onClick(View v) {
		int index = 0;
		for(CenterLayout layout :mlistItem)
		{
			if(layout == v)
			{
				if(mOnListItemListener != null)
					mOnListItemListener.onItemClick(this,index);
				break;
			}
			index++;
		}		
	}
	
}