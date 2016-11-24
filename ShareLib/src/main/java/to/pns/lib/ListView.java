package to.pns.lib;

import java.util.ArrayList;
import java.util.Vector;

import to.pns.lib.SlantingScrollView.OnScrollListener;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

interface _OnListItemListener
{
	public void onItemClick(ViewGroup v,int index);
}
public class ListView extends LinearLayout implements _OnListItemListener
{

	float mLineSize;
	float mPaddingWidth = 10.0f;
	float mPaddingHeight = 5.0f;
	Vector<Integer> mMaxWidths = new Vector<Integer>();
	
	LinearLayout mLinearClient;
	SlantingScrollView mScrollItems;
	LineLayout mHeaders;
	ArrayList<LineLayout> mListItem;
	OnListItemListener mOnListItemListener;

	public void setOnItemListener(OnListItemListener listener)
	{
		mOnListItemListener = listener;
	}
	public ListView(Context context) {
		super(context);
		init(context);
	}
	public ListView(Context context, AttributeSet attrs) {

		super(context,attrs);
		init(context);
	}
	private void init(Context context)
	{
		//setVisibility(View.GONE);
		setOrientation(LinearLayout.VERTICAL);
		setBackgroundColor(0x0);

		mLinearClient = new LinearLayout(context);
		mLinearClient.setOrientation(LinearLayout.VERTICAL);
		addView(mLinearClient,new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		mHeaders = new LineLayout(context);
		mHeaders.setBackColor(0x88666666);
		mLinearClient.addView(mHeaders,new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		//縦スクロール領域の作成
		mScrollItems = new SlantingScrollView(context);
		mScrollItems.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onSlantingScroll() {
				mHeaders.setScrollX(mScrollItems.getScrollX());
				
			}
		});
		mLinearClient.addView(mScrollItems,new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));	
		mListItem = new ArrayList<LineLayout>();
		
		setLineColor(0x88ffffff);
		setHeaderPadding(10.0f,10.0f);
		setLineSize(1.0f);
	}
	public void setLineSize(float size)
	{
		final float scale = getContext().getResources().getDisplayMetrics().density;
		mLineSize = size*scale;
		int paddingSize = (int)(size * scale);
		mLinearClient.setPadding(paddingSize,paddingSize,paddingSize,0);
	}
	public void setLineColor(int color)
	{
		mLinearClient.setBackgroundColor(color);
	}
	public void setHeaderPadding(float width,float height)
	{
		mHeaders.setPadding(width,height);
	}
	public void setPadding(float width,float height)
	{
		mPaddingWidth = width;
		mPaddingHeight = height;
	}

	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		// TODO 自動生成されたメソッド・スタブ
		super.onVisibilityChanged(changedView, visibility);
		if(this.getVisibility() != View.GONE)
		{
			mHeaders.requestLayout();
		}
	}	
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		measureChildren(
				MeasureSpec.makeMeasureSpec(widthMeasureSpec, MeasureSpec.UNSPECIFIED), 
				MeasureSpec.makeMeasureSpec(heightMeasureSpec, MeasureSpec.UNSPECIFIED));
		//幅をアイテムに通知
		{
		int i,j;
		int count = mHeaders.getItemCount();
		int width;
		width = resolveSize(getMeasuredWidth(), widthMeasureSpec);//-(int)(mLineSize*2);
		if(width < getMeasuredWidth())
			width = getMeasuredWidth();
		width -= (int)(mLineSize*2);
		for(i=0;i<count;i++)
		{
			mHeaders.setLineWidth(width);
			mHeaders.requestLayout();
			int itemCount = mListItem.size();
			for(j=0;j<itemCount;j++)
			{
				LineLayout item = mListItem.get(j);
				item.setLineWidth(width);
				item.requestLayout();
			}
		}
		}

		int width;
		int height;
		
		if(MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED)
			width = MeasureSpec.makeMeasureSpec(widthMeasureSpec, MeasureSpec.UNSPECIFIED);
		else
			width = MeasureSpec.makeMeasureSpec(widthMeasureSpec, MeasureSpec.EXACTLY);
		
		int mode = MeasureSpec.getMode(heightMeasureSpec);
		if(mode == MeasureSpec.UNSPECIFIED)
			height = MeasureSpec.makeMeasureSpec(heightMeasureSpec, MeasureSpec.UNSPECIFIED);
		else
			height = MeasureSpec.makeMeasureSpec(heightMeasureSpec, MeasureSpec.EXACTLY);
			
		//子に対してサイズ調整
		measureChildren(
				width, 
				height);

		final int count = getChildCount();
		int maxHeight = 0;
        int maxWidth = 0;
        int i;
        for (i = 0; i < count; i++)
        {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE)
            {
                maxWidth  = Math.max(maxWidth, child.getMeasuredWidth());
                maxHeight += child.getMeasuredHeight();
            }
        }
        setMeasuredDimension(maxWidth,maxHeight);
	}

	public void addHeader(String text)
	{
		mMaxWidths.add(0);
		mHeaders.setItemWidths(mMaxWidths);
		mHeaders.addItem(text);
	}
	public void clear()
	{
		//headers.setItemWidths(null);
		mScrollItems.removeAllViews();
		mListItem.clear();
		
		//幅の調整
		int count = mHeaders.getItemCount();
		int i;
		for(i=0;i<count;i++)
			mMaxWidths.set(i,mHeaders.getItemWidth(i));	
		
	}
	public void setItemBackColor(int index,int color)
	{
		if(index < mListItem.size())
			mListItem.get(index).setBackColor(color);
	}
	private LineLayout createItem()
	{
		LineLayout data = new LineLayout(getContext());
		data.setOnItemListener(this);
		data.setPadding(mPaddingWidth,mPaddingHeight);
		data.setBackColor(0x88226622);
		data.setItemWidths(mMaxWidths);
		mListItem.add(data);		
		mScrollItems.addView(data,new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		return data;
	}
	public int addItem(View view)
	{
		mHeaders.requestLayout();
		LineLayout data = createItem();
		data.addItem(view);
		return mListItem.size()-1;
	}
	public int addItem(String text)
	{
		mHeaders.requestLayout();
		LineLayout data = createItem();
		data.addItem(text);
		return mListItem.size()-1;
	}
	public void setItem(int index1,int index2,String text)
	{
		mHeaders.requestLayout();
		if(index1 < mListItem.size())
		{
			LineLayout data = mListItem.get(index1);
			data.setItem(index2,text);
		}
	}
	public void setItem(int index1,int index2,View view)
	{
		mHeaders.requestLayout();
		if(index1 < mListItem.size())
		{
			LineLayout data = mListItem.get(index1);
			data.setItem(index2,view);
		}
	}
	public String getItemText(int index1,int index2)
	{
		return mListItem.get(index1).getItemText(index2);
	}
	public Object getItem(int index1,int index2)
	{
		return mListItem.get(index1).getItem(index2);
	}
	public void setItemGravity(int index1,int index2,int gravity)
	{
		mListItem.get(index1).setItemGravity(index2,gravity);
	}
	public int getSize()
	{
		return mListItem.size();
	}


	@Override
	public void onItemClick(ViewGroup v, int index2) {
		int index = 0;
		for(LineLayout layout :mListItem)
		{
			if(layout == v)
			{
				if(mOnListItemListener != null)
					mOnListItemListener.onItemClick(this,index,index2);
				break;
			}
			index++;
		}		
	}
	public void setHeader(int i, String text) {
		// TODO 自動生成されたメソッド・スタブ
		mHeaders.setItem(i,text);
	}
	public void setItemScroll(int x, int y) {
		// TODO 自動生成されたメソッド・スタブ
		mScrollItems.scrollTo(x, y);
	}
	public int getItemScrollX() {
		// TODO 自動生成されたメソッド・スタブ
		return mScrollItems.getScrollX();
	}
	public int getItemScrollY() {
		// TODO 自動生成されたメソッド・スタブ
		return mScrollItems.getScrollY();
	}
}