package to.pns.lib;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

class CenterLayout extends ViewGroup
{
	int mPaddingWidth;
	int mPaddingHeight;
	int mGravity = Gravity.CENTER;
	public CenterLayout(Context context)
	{
		super(context);
		mPaddingWidth = 0;
		mPaddingHeight = 0;
	}
	public int getMaxWidth()
	{
		final int count = getChildCount();
        int maxWidth = 0;
        int i;
        for (i = 0; i < count; i++)
        {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE)
            {
                maxWidth  = Math.max(maxWidth,child.getMeasuredWidth());
            }
        }	
        return maxWidth;
	}
	public void setGravity(int gravity)
	{
		mGravity = gravity;
	}
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		//子に対してサイズ調整
		measureChildren(widthMeasureSpec, heightMeasureSpec);
		
		final int count = getChildCount();
		int maxHeight = 0;
        int maxWidth = 0;
        int i;
        for (i = 0; i < count; i++)
        {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE)
            {
                maxWidth  = Math.max(maxWidth,child.getMeasuredWidth());
                maxHeight = Math.max(maxHeight,child.getMeasuredHeight());
            }
        }
		setMeasuredDimension(maxWidth,maxHeight);
	}
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b)
	{
		int i;
		int count = getChildCount();

		int parentWidth = r-l-mPaddingWidth;
		int parentHeight = b-t;
		for(i=0;i<count;i++)
		{
			View view = getChildAt(i);
			int width = view.getMeasuredWidth();
			int height = view.getMeasuredHeight();
			int x;
			int y = (parentHeight - height) / 2;
			if((mGravity & Gravity.LEFT) == Gravity.LEFT)
				x = 0;
			else if((mGravity & Gravity.RIGHT)==Gravity.RIGHT)
				x = parentWidth - width;
			else
				x = (parentWidth - width) / 2;
			view.layout(x+mPaddingWidth/2,y, x+width+mPaddingWidth,y+height);
		}
	}
	public void setPadding(float width, float height) {
		mPaddingWidth = (int)width;
		mPaddingHeight = (int)height;
	}	
}