package to.pns.lib;


import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.widget.RemoteViews;

public class Notify
{
	class ActionData
	{
		private String mName;
		private int mID;
		ActionData(String name,int id)
		{
			mName = name;
			mID = id;
		}
		int getID()
		{
			return mID;
		}
		String getName()
		{
			return mName;
		}
	}
	
	private final static int NOTIFY_ID = 123;
	private NotificationManager mManager;
	private PendingIntent mIntent;
	private Builder mNotification;
	private RemoteViews mRemoteViews;
	private Context mContext;
	private Notification mNotify;
	private int mIcon;
	private int mIconLevel;
	private String mMessage;
	private int mCode;
	private ArrayList<ActionData> mAction = new ArrayList<ActionData>();
	public Notify(Context context,Class cs,int layoutId,int icon)
	{
		mCode = 0;
		mIcon = -1;
		mIconLevel = -1;
		mContext = context;
		//通知領域の作成
		mIntent = PendingIntent.getActivity(context,0,new Intent(context, cs),0);
		mManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		mRemoteViews = new RemoteViews(context.getPackageName(), layoutId);
		
		//mRemoteViews.setTextViewText(R.id.textTitle,context.getString(R.string.app_name));
		
		mNotification = new NotificationCompat.Builder(mContext);
		mNotification.setWhen(System.currentTimeMillis());
		mNotification.setContentIntent(mIntent);
		mNotification.setAutoCancel(false);
//		mNotification.setSmallIcon(R.drawable.ic_launcher,0);
		mNotification.setSmallIcon(icon);
		mNotification.setContentTitle("TEST");
		mNotification.setContent(mRemoteViews);
		mNotify = mNotification.build();
		//mManager.notify(NOTIFY_ID,mNotify);
	}
	public void release()
	{
		mManager.cancel(NOTIFY_ID);
		mIcon = -1;

//		mRemoteViews = new RemoteViews(mContext.getPackageName(), R.layout.status_layout);
//		mRemoteViews.setTextViewText(R.id.textTitle,mContext.getString(R.string.app_name));
//		mNotification.setContent(mRemoteViews);
		
		for(ActionData data : mAction)
		{
			int id = data.getID();
			Intent buttonIntent = new Intent();
	        buttonIntent.setAction(data.getName());
	        buttonIntent.putExtra("value",id);
	        PendingIntent pendingIntent = PendingIntent.getService(mContext, mCode++, buttonIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	        mRemoteViews.setOnClickPendingIntent(id, pendingIntent);
		}
		
	}
	public void setRemoteText(int id,String value)
	{
		mRemoteViews.setTextViewText(id,value);
	}
	public void setRemoteImage(int id,int iconID,int iconLevel)
	{
		Drawable d = mContext.getResources().getDrawable(iconID);
		d.setLevel(iconLevel);
		mRemoteViews.setImageViewBitmap(id, getBitmap(d));
	}
	public void setAction(String name,int id)
	{
		mAction.add(new ActionData(name, id));
		
		Intent buttonIntent = new Intent();
        buttonIntent.setAction(name);
        buttonIntent.putExtra("value",id);
        PendingIntent pendingIntent = PendingIntent.getService(mContext, mCode++, buttonIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        mRemoteViews.setOnClickPendingIntent(id, pendingIntent);
	}
	private static Bitmap getBitmap(Drawable drawable) {
	    int width  = drawable.getIntrinsicWidth();
	    int height = drawable.getIntrinsicHeight();
	    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
	    Canvas canvas = new Canvas(bitmap);
	    drawable.setBounds(0, 0, width, height);
	    drawable.draw(canvas);
	    return bitmap;
	}
//	public void setStatus(int iconID, int iconLevel,String msg) {
//		if(mIcon != iconID || mIconLevel != iconLevel || !msg.equals(mMessage))
//		{
//			//LogService.output(mContext, "setStatus");
//			Drawable d = mContext.getResources().getDrawable(iconID);
//			d.setLevel(iconLevel);
//			mRemoteViews.setImageViewBitmap(R.id.imageNotify, getBitmap(d));
//			mRemoteViews.setTextViewText(R.id.textMsg,msg);
//			mIcon = iconID;
//			mIconLevel = iconLevel;
//			mMessage = msg;
//
//			mNotification.setContentText(msg);
//			mNotification.setSmallIcon(iconID,iconLevel);
//			mNotify = mNotification.build();
//			mNotify.flags |= Notification.FLAG_NO_CLEAR;
//			mManager.notify(NOTIFY_ID,mNotify);
//		}
//	}
//
//	public void setRemoteMsg(String msg) {
//		if(!msg.equals(mMessage))
//		{
//			//LogService.output(mContext, "setRemoteMsg");
//			mRemoteViews.setTextViewText(R.id.textMsg,msg);
//			mNotification.setContentText(msg);
//			mMessage = msg;
//			mNotify = mNotification.build();
//			mNotify.flags |= Notification.FLAG_NO_CLEAR;
//			mManager.notify(NOTIFY_ID,mNotify);
//		}
//	}
	public void output(String msg)
	{
		mNotify.flags |= Notification.FLAG_NO_CLEAR;
		mNotify.tickerText = msg;
		mManager.notify(NOTIFY_ID,mNotify);
	}
	public void setIcon(int iconID, int iconLevel) {
		mNotification.setSmallIcon(iconID,iconLevel);
		mNotify = mNotification.build();
		mNotify.flags |= Notification.FLAG_NO_CLEAR;
		mManager.notify(NOTIFY_ID,mNotify);		
	}
}