package to.pns.lib;

public class GraphData
{
	GraphData(String label,int key,long value)
	{
		this.label = label;
		this.key = key;
		this.value = value;
	}
	public String getLabel() {
		return label;
	}
	public int getKey() {
		return key;
	}
	public long getValue() {
		return value;
	}
	private int key;
	private String label;
	private long value;
}