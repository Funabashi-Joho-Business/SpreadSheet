package to.pns.lib;

import java.text.SimpleDateFormat;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Gravity;
import android.view.View;

public class LogView extends ListView
{

	@Override
	protected void onWindowVisibilityChanged(int visibility)
	{
		if(visibility == View.VISIBLE)
		{
			mBr = new BroadcastReceiver()
			{
				@Override
				public void onReceive(Context context, Intent intent)
				{
					if(intent.getAction().equals(LogService.UPDATE_NAME))
					{
						update();
					}
				}
			};			
			
			mContext.registerReceiver(mBr, new IntentFilter(LogService.UPDATE_NAME));	
			update();
		}
		else
		{
			if(mBr != null)
			{
				mContext.unregisterReceiver(mBr);
				mBr = null;
			}
		}
		super.onWindowVisibilityChanged(visibility);
	}


	Context mContext;
	private BroadcastReceiver mBr;
	public LogView(Context context) {
		super(context);
		context.startService(new Intent(context, LogService.class));
		mContext = context;
		addHeader("日時");
		addHeader("MSG");
	}

	void update()
	{
		clear();
		
		LogDB db = new LogDB(mContext);
		List<LogDB.LogData> list = db.getLog();
		db.close();
		
		for(LogDB.LogData log : list)
		{
			SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			int index = addItem(df.format(log.date));
			setItem(index,1,log.msg);
			setItemGravity(index,0, Gravity.LEFT);
			setItemGravity(index,1, Gravity.LEFT);
		}
	}

	
}