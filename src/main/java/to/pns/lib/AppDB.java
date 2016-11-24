package to.pns.lib;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class AppDB extends SQLite {

    public AppDB(Context context, String dbName, int version) {
        super(context, dbName, version);
    }

    @Override

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE m_setting(s_name text,s_value text);");
    }


    public static String createSqlCreateClass(Class c,String className,String primary){
        Map<Class,String> map = new HashMap<Class,String>();
        map.put(String.class,"text");
        map.put(int.class,"int");
        map.put(Date.class,"date");

        StringBuilder sb = new StringBuilder();
        for(int i=0;i<c.getFields().length;i++) {
            Field f = c.getFields()[i];
            if(f.getName().charAt(0) == '$')
                continue;
            if(sb.length()>0)
                sb.append(",");

            sb.append(String.format("%s %s",f.getName(),map.get(f.getType())));
            if(f.getName().equals(primary))
                sb.append(" primary key");
        }
        return String.format("create table %s (%s)",className,sb.toString());
    }

    public static String createSqlReplaceClass(Object obj,String className) {
        try {
            Class c = obj.getClass();
            StringBuilder sbValue = new StringBuilder();
            StringBuilder sbName = new StringBuilder();
            for(int i=0;i<c.getFields().length;i++) {
                Field f = c.getFields()[i];
                if(f.getName().charAt(0) == '$')
                    continue;

                if(sbName.length()>0) {
                    sbName.append(",");
                    sbValue.append(",");
                }
                sbName.append(f.getName());

                String value;
                Object v = f.get(obj);
                if(f.getType() == Date.class)
                    value = new java.sql.Timestamp(((Date)v).getTime()).toString();
                else
                    value = v.toString();

                sbValue.append(String.format("'%s'",STR(value)));
            }

            return String.format("replace into %s(%s) values(%s)",className,sbName.toString(),sbValue.toString());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setSetting(String name,int value)
    {
        setSetting(name,String.valueOf(value));
    }
    public void setSetting(String name,long value)
    {
        setSetting(name,String.valueOf(value));
    }
    public void setSetting(String name,String value)
    {
        exec("begin;");
        String sql = String.format(
                "delete from m_setting where s_name='%s';",name);
        exec(sql);
        sql = String.format("insert into m_setting values('%s','%s');",name,value);
        exec(sql);
        exec("commit;");
    }

    public String getSetting(String name)
    {
        String sql = String.format("select s_value from m_setting where s_name='%s';", name);
        Cursor c = query(sql);
        if(!c.moveToFirst())
            return null;
        String s = c.getString(0);
        c.close();
        return s;

    }
    public String getSetting(String name,String defValue)
    {
        String v = getSetting(name);
        if(v == null)
            return defValue;
        return v;
    }
    public int getSetting(String name,int defValue)
    {
        String v = getSetting(name);
        if(v == null)
            return defValue;
        return Integer.valueOf(v);
    }
    public long getSetting(String name,long defValue)
    {
        String v = getSetting(name);
        if(v == null)
            return defValue;
        return Long.valueOf(v);
    }



    public List<Map<String,String>> getTableData(String tableName)
    {
        String sql;
        sql = String.format("select * from %s;",tableName);
        return queryMap(sql);
    }
    public  List<Map<String,String>> queryMap(String sql)
    {
        //System.out.println(sql+"\n");
        Cursor c = query(sql);
        int col = c.getColumnCount();

        List<Map<String,String>> list = new ArrayList<Map<String,String>>();
        if(c.moveToFirst())
        {
            do
            {
                HashMap<String,String> map = new HashMap<String,String>();
                for(int i = 0;i<col;i++)
                {
                    String name = c.getColumnName(i);
                    String data = c.getString(i);
                    map.put(name,data);
                    //System.out.println(name+","+data);
                }
                list.add(map);
            }while(c.moveToNext());
        }
        c.close();
        return list;
    }
    public void vacuum()
    {

        exec("BEGIN TRANSACTION;");
        exec("create table tmp_traffic(a,b,c)");
        exec("insert into tmp_traffic select strftime('%Y-%m-01 00:00:00', tra_date)  as mdate,apn_id,sum(tra_size) from m_traffic where date(tra_date) < date('now','localtime','-31 days')  group by apn_id,strftime('%Y-%m-01 00:00:00', tra_date) union all select * from m_traffic where date(tra_date) >= date('now','localtime','-31 days') order by mdate,apn_id");
        exec("delete from m_traffic;");
        exec("insert into m_traffic select * from tmp_traffic;");
        exec("drop table tmp_traffic;");
        exec("COMMIT;");
        //exec("vacuum;");
    }


}
