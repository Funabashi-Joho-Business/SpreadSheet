package to.pns.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;
class UnitParam
{
	public UnitParam(long min,long max)
	{
		//最大値と最小値の幅
		long bound = max - min;
		//幅の桁数
		int log = (int)Math.ceil(Math.log10(bound))-1;
		mUnit = (int) Math.pow(10,log);
		
		mMin = min / mUnit * mUnit;
		mMax = (max+mUnit-1) / mUnit * mUnit;		
	}
	public long getMin() {
		return mMin;
	}
	public long getMax() {
		return mMax;
	}
	public long getUnit() {
		return mUnit;
	}
	public long getCount()
	{
		return (mMax - mMin) / mUnit;
	}
	private long mMin;
	private long mMax;
	private long mUnit;
}
public class GraphView extends View {

	float mDensity;
	List<GraphDatas> mGraphData = new ArrayList<GraphDatas>();
	

	public GraphView(Context context) {
		super(context);
		mDensity = getResources().getDisplayMetrics().density;

	}
	public void clearData()
	{
		mGraphData.clear();
	}
	public void addData(GraphDatas gd)
	{
		mGraphData.add(gd);
	}
	
	static long VMIN(List<GraphData> data)
	{
		long min = Long.MAX_VALUE;
		for(GraphData v : data)
		{
			min = Math.min(min,v.getValue());
		}
		return min;
	}
	static long VMAX(List<GraphData> data)
	{
		long max = Long.MIN_VALUE;
		for(GraphData v : data)
		{
			max = Math.max(max,v.getValue());
		}
		return max;
	}
	static int IMIN(List<GraphData> data)
	{
		int min = Integer.MAX_VALUE;
		for(GraphData v : data)
		{
			min = Math.min(min,v.getKey());
		}
		return min;
	}
	static int IMAX(List<GraphData> data)
	{
		int max = Integer.MIN_VALUE;
		for(GraphData v : data)
		{
			max = Math.max(max,v.getKey());
		}
		return max;
	}
	static int ISUB(List<GraphData> data)
	{
		if(data.size() == 0)
			return Integer.MAX_VALUE;
		int sub = Integer.MAX_VALUE;
		Iterator<GraphData> it = data.iterator();
		int v = it.next().getKey();
		while(it.hasNext())
		{
			int v2 = it.next().getKey();
			sub =  Math.min(sub, Math.abs(v2 - v));
		}		
		return sub;
	}
	@Override
	protected void onDraw(Canvas c) {
		c.drawColor(Color.BLACK);
		if(mGraphData.size() == 0)
			return;
		
		// TODO 自動生成されたメソッド・スタブ
		//super.onDraw(c);
		float height = getMeasuredHeight();
		float width = getMeasuredWidth();
		float graphWidth = width;
		float graphHeight = height;
		int keyMin = Integer.MAX_VALUE;
		int keyMax = Integer.MIN_VALUE;
		long valueMin = Long.MAX_VALUE;
		long valueMax = Long.MIN_VALUE;
		int subMin = Integer.MAX_VALUE;
		for(GraphDatas gd : mGraphData)
		{
			keyMin = Math.min(keyMin,IMIN(gd.getData()));
			keyMax = Math.max(keyMax,IMAX(gd.getData()));			
			valueMin = Math.min(valueMin,VMIN(gd.getData()));
			valueMax = Math.max(valueMax,VMAX(gd.getData()));	
			subMin = Math.min(subMin,ISUB(gd.getData()));
		}
		//塗りつぶし
		Paint p = new Paint();
		p.setStyle(Paint.Style.FILL);
		p.setStrokeWidth(2.0f*mDensity+0.5f);
		p.setTextSize((int)(8.0f*mDensity+0.5f));
		
	
		if(subMin != Integer.MAX_VALUE)
		{
		
			graphHeight -= p.getTextSize()*2;
			
			UnitParam up = new UnitParam(valueMin,valueMax);
			long minPoint = up.getMin();
			long maxPoint = up.getMax();
			long countY = up.getCount();
			long unitY = up.getUnit();
			long pointY = (int)graphHeight / (countY>0?countY:1);
			
			
			//グラフ単位の作成(縦)
			float textWidthMax = 0.0f;
			float[] textWidth = new float[(int) (countY+1)];
			
			for(int i=0;i<countY+1;i++)
			{
				textWidth[i] = p.measureText(String.valueOf(maxPoint-unitY*i));
				textWidthMax = Math.max(textWidth[i],textWidthMax);
			}		
			
			p.setColor(0xff666666);
			for(int i=0;i<countY+1;i++)
			{
				float y = pointY*i;
				c.drawText(String.valueOf(maxPoint-unitY*i),textWidthMax-textWidth[i],y+p.getTextSize(),p);
				c.drawLine(textWidthMax, y+p.getTextSize()/2,textWidthMax+graphWidth,y+p.getTextSize()/2,p);
			}
			
			//ラベルの抽出
			Map<Integer,String> label = new HashMap<Integer,String>();
			for(GraphDatas gds : mGraphData)
			{	
				for(GraphData gd : gds.getData())		
				{
					label.put(gd.getKey(), gd.getLabel());
				}
			}
			
			//グラフ単位作成(横)
			graphWidth -= textWidthMax + p.getTextSize()*2;
			
			int keyCount = (keyMax - keyMin) / subMin;
			float size = graphWidth / (keyCount+1);
			
			
			for(int i = keyMin;i<=keyMax;i+=subMin)
			{
				float x = textWidthMax+p.getTextSize() + (i-keyMin)/subMin*size;
				p.setColor(Color.WHITE);
				String name = label.get(i);
				if(name != null)
					c.drawText(name,x-p.measureText(name)/2,height,p);
			}
			for(GraphDatas gds : mGraphData)
			{
				//グラフ
				p.setColor(gds.getColor());
				width -= textWidthMax + p.getTextSize()*2;
				
				
				Path path = new Path();
				boolean f = true;
				for(GraphData gd : gds.getData())
				{
					int index = gd.getKey();
					float x = textWidthMax+p.getTextSize() + (index-keyMin)/subMin*size;
					float y = p.getTextSize()/2 + graphHeight-(gd.getValue()-minPoint)*graphHeight/(maxPoint-minPoint);
					c.drawCircle(x, y,p.getTextSize()/4.0f,p);
					if(f)
					{
						path.moveTo(x, y);
						f = false;
					}
					else
					{
						path.lineTo(x, y);
					}
				}
				p.setStyle(Paint.Style.STROKE);		
				c.drawPath(path,p);
			}
		}
		//凡例
		float legendX = p.getTextSize()*2;
		float legendY = p.getTextSize();
		for(GraphDatas gds : mGraphData)
		{
			p.setColor(gds.getColor());
			p.setStyle(Paint.Style.FILL);
			c.drawRect(legendX,legendY,legendX+p.getTextSize(), legendY+p.getTextSize(),p);
			p.setColor(Color.WHITE);
			c.drawText(gds.getName(),legendX+p.getTextSize()*2,legendY+p.getTextSize(),p);
			legendY += p.getTextSize()*1.5f;
		}
	}
}
