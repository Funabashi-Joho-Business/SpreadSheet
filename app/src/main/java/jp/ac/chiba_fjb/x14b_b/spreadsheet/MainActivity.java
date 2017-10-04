package jp.ac.chiba_fjb.x14b_b.spreadsheet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;

import java.util.List;

import jp.ac.chiba_fjb.x14b_b.spreadsheet.google.SpreadSheet;


public class MainActivity extends AppCompatActivity {
    private SpreadSheet mSheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //スプレットシートの生成
        mSheet = new SpreadSheet(this);
        //許可済みか確認
        if(!mSheet.connect())
            start();



    }
    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html){
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html,Html.FROM_HTML_MODE_COMPACT);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }
    void start(){
        //通信用スレッド
        new Thread(){
            @Override
            public void run() {
                super.run();
                //スプレッドシートの作成
                String id = mSheet.create("/ComData/Setting");

                if(id != null){
                    //データの書き込み
                    Object[][] values = {{"あいうえお","かきくけこ"},{"ああああ"},{"=1+2"}};
                    mSheet.setRange(id,values);

                    //全データの取得
                    List<List<Object>> data = mSheet.getRange(id);
                    System.out.println(data);
                }

            }
        }.start();


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //認証許可情報を設定
        mSheet.onActivityResult(requestCode, resultCode, data);
        start();
    }

}
