package jp.ac.chiba_fjb.x14b_b.spreadsheet.google;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAuthIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.util.ExponentialBackOff;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by oikawa on 2016/10/11.
 */

public class GoogleAccount {

    public interface GoogleRunnable {
        public void run() throws IOException;
        public void onError(Exception e);
    }

    private static final int REQUEST_ACCOUNT_PICKER = 998;
    private static final int REQUEST_AUTHORIZATION = 999;
    private static final String EXTRA_NAME = "SCRIPT_INFO";
    private static final String PREF_ACCOUNT_NAME = "ScriptUser";

    private Context mContext;
    private GoogleAccountCredential mCredential;
    private String mAccountName;
    private static final String[] SCOPES = {"https://www.googleapis.com/auth/drive"};
    List<GoogleRunnable> mRunnables = new ArrayList<>();

    public GoogleAccount(Context con, String[] scope) {
        //Activityの保存
        mContext = con;
        //認証用クラスの生成
        mCredential = GoogleAccountCredential.usingOAuth2(
                con, Arrays.asList(scope==null?SCOPES:scope))
                .setBackOff(new ExponentialBackOff());
        //登録済みアカウント名を取得
        mAccountName = mContext.getSharedPreferences("GOOGLE", Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null);
        mCredential.setSelectedAccountName(mAccountName);
    }
    public GoogleAccountCredential getCredential(){
        return mCredential;
    }
    public String getAccount(){
        return mAccountName;
    }
    public void resetAccount(){
        //登録アカウントの解除
        SharedPreferences settings =
                mContext.getSharedPreferences("GOOGLE", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREF_ACCOUNT_NAME, null);
        editor.apply();
        mAccountName = null;
        //mCredential.setSelectedAccountName(null);
        mCredential.setSelectedAccount(null);
    }
    public void requestAccount(){
        //ユーザ選択
        if(mAccountName==null&& mContext instanceof Activity)
            ((Activity)mContext).startActivityForResult(mCredential.newChooseAccountIntent(),REQUEST_ACCOUNT_PICKER);
        else
            call();
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == REQUEST_ACCOUNT_PICKER) {
            if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
                mAccountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

                if (mAccountName != null) {
                    Account account = new Account(mAccountName,"com.google");
                    //アカウント選択確定
                    mCredential.setSelectedAccount(account);
                    SharedPreferences settings =
                            mContext.getSharedPreferences("GOOGLE", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(PREF_ACCOUNT_NAME, mAccountName);
                    editor.apply();
                }else{
                    requestAccount();
                }

                call();		//実行要求
            }
//            else
//
//                onError();	//実行不能時の
//            // 処理
        }
        else if(requestCode == REQUEST_AUTHORIZATION) {
            if (resultCode == Activity.RESULT_OK)
                call();			//実行要求
//            else
//                onError();	//実行不能時の処理
        }
    }
    protected boolean exception(Exception e){
        if (e instanceof UserRecoverableAuthIOException) {
            //権限要求の呼び出し
            if(mContext instanceof Activity) {
                Intent intent = ((UserRecoverableAuthIOException) e).getIntent();
                ((Activity) mContext).startActivityForResult(intent, REQUEST_AUTHORIZATION);
                return true;
            }
        } else if (e instanceof IllegalArgumentException) {
            //アカウント要求
            requestAccount();
            return true;
        } else if (e instanceof GoogleJsonResponseException) {
            System.err.println(((GoogleJsonResponseException)e).getMessage());
        } else if(e instanceof GoogleAuthIOException){
            //登録系エラー
            Log.e("登録エラー",getAppName(mContext)+":"+getAppFinger(mContext));
        }
        else{


            e.printStackTrace();

        }
        return false;
    }
    public void call(){
        final Handler handler = new Handler();
        new Thread() {
            @Override
            public void run() {
                synchronized(mRunnables){
                    while(mRunnables.size()>0){
                        try {
                            mRunnables.get(0).run();
                            mRunnables.remove(0);
                        }catch (final Exception e) {
                            if(exception(e)==false) {
                                final GoogleRunnable runnable = mRunnables.get(0);
                                mRunnables.remove(0);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        runnable.onError(e);
                                    }
                                });
                            }else
                                break;
                        }
                    }
                }


            }
        }.start();
    }
    public void execute(GoogleRunnable runnable){
        mRunnables.add(runnable);
        call();
    }
    public static String getAppName(Context con){
        try {
            PackageInfo packageInfo = con.getPackageManager().getPackageInfo(con.getPackageName(), PackageManager.GET_SIGNATURES);
            return packageInfo.packageName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
    static public String getAppFinger(Context con){
        try {
            PackageInfo packageInfo = con.getPackageManager().getPackageInfo(con.getPackageName(), PackageManager.GET_SIGNATURES);
            InputStream input = new ByteArrayInputStream(packageInfo.signatures[0].toByteArray());
            Certificate c = CertificateFactory.getInstance("X509").generateCertificate(input);
            byte[] publicKey = MessageDigest.getInstance("SHA1").digest(c.getEncoded());

            StringBuffer hexString = new StringBuffer();
            for (int i=0;i<publicKey.length;i++)
                hexString.append(String.format("%02x",publicKey[i]));
            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
