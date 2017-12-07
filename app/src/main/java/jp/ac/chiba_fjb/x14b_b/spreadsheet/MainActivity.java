package jp.ac.chiba_fjb.x14b_b.spreadsheet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.io.IOException;
import java.util.List;

import jp.ac.chiba_fjb.x14b_b.spreadsheet.google.GoogleAccount;
import jp.ac.chiba_fjb.x14b_b.spreadsheet.google.SpreadSheet;


public class MainActivity extends AppCompatActivity {
    private SpreadSheet mSheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //スプレットシートの生成
        mSheet = new SpreadSheet(this);
        //mSheet.resetAccount();
        mSheet.execute(new GoogleAccount.GoogleRunnable() {
            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }

            @Override
            public void run() throws IOException {
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
        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //認証許可情報を設定
        mSheet.onActivityResult(requestCode, resultCode, data);
    }

}
