package to.pns.lib;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.google.api.client.http.HttpMethods.HEAD;


public class LogDB extends SQLite
{
	public class LogData
	{
		public int id;
		public Date date;
		public String msg;
	}
	
	public LogDB(Context context)
	{
		super(context,"log.db",4);

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table t_log(l_id integer primary key,l_date datetime,l_msg text);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}
	public void output(String msg)
	{
		exec("begin;");
		exec("delete from t_log where l_id < (select max(l_id)-100 from t_log);");
		String sql = String.format("insert into t_log values(null,datetime('now','localtime'),'%s');",msg.replaceAll("'","''"));
		exec(sql);
		exec("commit;");
	}

	public List<LogData> getLog()
	{
		List<LogData> list = new ArrayList<LogData>();
		
		Cursor c = query("select * from t_log order by l_id desc;");
		if(c.moveToFirst())
		{
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			do
			{
				
				try {
					LogData log = new LogData();
					log.id = c.getInt(0);
					log.date = df.parse(c.getString(1));
					log.msg = c.getString(2);
					list.add(log);
				} catch (ParseException e) {}
				
			}while(c.moveToNext());
		}
		return list;
	}
}