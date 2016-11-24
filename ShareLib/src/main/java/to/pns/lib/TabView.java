package to.pns.lib;

import java.util.List;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TabView extends LinearLayout implements View.OnClickListener 
{
	private OnTabChangeListener mOnTabChangeListener;
	int mActiveIndex;
	LinearLayout mTab;
	FrameLayout mFrame;
	List<TextView> mButton;
	Context mContext;
	private float mDensity;
	public TabView(Context context) {
		super(context);
		setOrientation(LinearLayout.VERTICAL);
		
		mContext = context;
		mDensity = getResources().getDisplayMetrics().density;
		mActiveIndex = -1;
		mTab = new LinearLayout(context);
		addView(mTab,new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
		mFrame = new FrameLayout(context);
		
		int p = (int)(4.0f*mDensity+0.5f);
		mFrame.setPadding(p, p, p, 0);
		addView(mFrame,new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
	}
	public int getActiveIndex() {
		return mActiveIndex;
	}
	public void setActiveIndex(int index) 
	{
		final int activeBackColor = 0xFF444444;
		final int activeTextColor = 0xFFFFFFFF;
		final int ncBackColor = 0xFF222222;
		final int ncTextColor = 0xFF666666;		
		
		int count = mTab.getChildCount(); 
		if(index < count)
		{
			mActiveIndex = index;
			for(int i=0;i<count;i++)
			{
				TextView textView = (TextView) mTab.getChildAt(i);
				if(i==index)
				{
					textView.setTextColor(activeTextColor);
					textView.setBackgroundColor(activeBackColor);
				}
				else
				{
					textView.setTextColor(ncTextColor);
					textView.setBackgroundColor(ncBackColor);
				}
				
				View view = mFrame.getChildAt(i);
				if(view != null)
					view.setVisibility(i==index?View.VISIBLE:View.GONE);
			}
		}
	}
	public int addTab(String name,View view)
	{
		LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		p.weight = 1.0f;
		p.leftMargin = (int)(1*mDensity+0.5f);
		TextView text = new TextView(mContext);
		text.setOnClickListener(this);
		text.setText(name);
		text.setId(mTab.getChildCount());
		mTab.addView(text,p);
		int margin = (int)(6*mDensity+0.5f);
		text.setPadding(margin, margin, margin, margin);
		text.setGravity(Gravity.CENTER);
		text.setBackgroundColor(0xffaaaaaa);
		
		mFrame.addView(view,new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
		
		if(getActiveIndex() == -1)
		{
			setActiveIndex(0);
		}
		else
		{
			setActiveIndex(getActiveIndex());
		}
		
		return mTab.getChildCount()-1;
	}
	@Override
	public void onClick(View v) {
		setActiveIndex(v.getId());
		if(mOnTabChangeListener!=null)
			mOnTabChangeListener.onTabChange(this, v.getId());
		
	}

	public void setOnTabChangeListener(OnTabChangeListener onTabChangeListener) {
		mOnTabChangeListener = onTabChangeListener;
	}
	
	
}