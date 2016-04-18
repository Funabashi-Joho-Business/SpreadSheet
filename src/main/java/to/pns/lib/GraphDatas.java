package to.pns.lib;

import java.util.ArrayList;
import java.util.List;

public class GraphDatas
{
	String mName;
	int mColor;
	List<GraphData> mData = new ArrayList<GraphData>();


	public GraphDatas()
	{
		mName = "";
		mColor = 0xffffffff;
	}
	public GraphDatas(String name,int color)
	{
		mName = name;
		mColor = color;
	}
	public void setName(String name)
	{
		mName = name;
	}
	public void add(String label,int key,long value)
	{

		mData.add(new GraphData(label,key,value));
	}
	public void setColor(int color)
	{
		mColor = color;
	}
	public List<GraphData> getData()
	{
		return mData;
	}
	public int getColor()
	{
		return mColor;
	}
	public String getName()
	{
		return mName;
	}
}