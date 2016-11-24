package jp.ac.chiba_fjb.x14b_b.spreadsheet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private SpreadSheet mSheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSheet = new SpreadSheet(this);
        mSheet.connect();

        new Thread(){
            @Override
            public void run() {
                super.run();
                String id = mSheet.create("/ComData/Setting");
                Object[][] values = {{"あいうえお","かきくけこ"},{"ああああ"},{"=1+2"}};
                if(id != null){
                    mSheet.setRange(id,values);
                }
                List<List<Object>> data = mSheet.getRange(id);
                System.out.println(data);
            }
        }.start();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mSheet.onActivityResult(requestCode, resultCode, data);
    }



}
