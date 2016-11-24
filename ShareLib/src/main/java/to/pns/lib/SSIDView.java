package to.pns.lib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.view.Window;

public abstract class SSIDView extends Dialog implements OnListItemListener
{
	private ListView listView;
	private Context mContext;
	protected SSIDView(Context context) {
		super(context);
		mContext = context;
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		listView = new ListView(context);
		setContentView(listView);

		listView.addHeader("SSID");

		listView.setOnItemListener(this);
		reload();
	}

	@Override
	public void onItemClick(ListView v, int index1, int index2) {
		
		if(index1 > 0)
		{
			onSelect(index1 == 1?"":v.getItemText(index1,0));
		}
		cancel();
	}
	public abstract void onSelect(String value);
	void reload()
	{
		listView.clear();
	    WifiManager manager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
		List<WifiConfiguration> wifiConfigurationList = manager.getConfiguredNetworks();
		listView.addItem("キャンセル");		
		listView.addItem("自動");		
    	
    	//ソート処理
    	List<String> names = new ArrayList<String>();
    	for(WifiConfiguration c : wifiConfigurationList)
    	{
    		names.add(c.SSID.replace("\"", ""));
    	}
    	
    	Collections.sort(names);
		for(String name : names)
        {
			listView.addItem(name);		
   
        }
	}	
}