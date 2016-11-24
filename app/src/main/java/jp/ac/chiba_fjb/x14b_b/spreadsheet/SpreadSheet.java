package jp.ac.chiba_fjb.x14b_b.spreadsheet;

import android.content.Context;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by oikawa on 2016/11/22.
 */

public class SpreadSheet extends GoogleDrive {
	private final Sheets mService;
	private static final String[] SCOPES = { SheetsScopes.DRIVE, SheetsScopes.SPREADSHEETS };
	public SpreadSheet(Context con) {
		super(con, SCOPES);

		mService = new Sheets.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), getCredential())
			           .setApplicationName("Google Sheets API Android Quickstart")
			           .build();
	}
	public String create(String path){
		try {
			//フォルダ名の分解
			String folder = "";
			String name;
			int pt = path.lastIndexOf("/");
			if(pt > 0) {
				folder = path.substring(0, pt);
				name = path.substring(pt+1,path.length());
			}
			else
				name = path;

			String id = createFile(path,"application/vnd.google-apps.spreadsheet");
			return id;
		} catch (Exception e) {
			exception(e);
		}
		return null;
	}
	public static String getRangeString(int row,int col){
		return String.format("%c%d",'A'+col-1,row);
	}
	public boolean setRange(String sheetId,List<List<Object>> values,boolean raw){
		ValueRange vr = new ValueRange();
		vr.setValues(values);
		int row = values.size();
		if(row == 0)
			return false;
		int col = values.get(0).size();
		if(col == 0)
			return false;

		try {
			mService.spreadsheets().values().update(sheetId, "A1:" + getRangeString(row, col), vr).setValueInputOption(raw?"RAW":"USER_ENTERED").execute();
			return true;
		} catch (Exception e) {
			exception(e);
		}
		return false;

	}
	public boolean setRange(String sheetId,Object[][] values){

		List<List<Object>> rows = new ArrayList<List<Object>>();
		for(Object[] v : values){
			rows.add(Arrays.asList(v));
		}
		return setRange(sheetId,rows,true);
	}
	public List<List<Object>> getRange(String id){
		try {
			return mService.spreadsheets().values().get(id,"A1:Z").execute().getValues();
		} catch (Exception e) {
			exception(e);
		}
		return null;
	}
	public List<List<Object>> getLastRow(String id){
		try {
			return mService.spreadsheets().values().get(id,"A1:A").execute().getValues();
		} catch (Exception e) {
			exception(e);
		}
		return null;
	}

}
