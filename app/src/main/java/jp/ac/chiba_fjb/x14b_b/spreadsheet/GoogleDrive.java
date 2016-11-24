package jp.ac.chiba_fjb.x14b_b.spreadsheet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;


/**
 * Created by oikawa on 2016/09/26.
 */

public class GoogleDrive extends GoogleAccount {

    interface OnConnectListener{
        public void onConnected(boolean flag);
    }
    private class PathFile{
        String mFolder;
        String mName;
        public PathFile(String path){
            //フォルダ名の分解
            mFolder = "";
            int pt = path.lastIndexOf("/");
            if(pt > 0) {
                mFolder = path.substring(0, pt);
                mName = path.substring(pt+1,path.length());
            }
            else
                mName = path;
        }
        public String getFolder(){return mFolder;}
        public String getName(){return mName;}

    }

    private OnConnectListener mListener;
    private Drive mDrive;
    private String mRootId;
    public GoogleDrive(Context con){
        super(con,null);
        mDrive = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), getCredential()).build();
    }
    public GoogleDrive(Context con,String[] scopes){
        super(con,scopes);
        mDrive = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), getCredential()).build();
    }

    public void setOnConnectedListener(OnConnectListener listener){
        mListener = listener;
    }
    public boolean connect(){
        requestAccount();
        try {
            if(getCredential().getToken() != null)
                return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    protected void onExec()  {
        mRootId = getRootId();
        if(mListener != null)
            mListener.onConnected(mRootId != null);
    }

    @Override
    protected void onError() {
        super.onError();
        if(mListener != null)
            mListener.onConnected(false);
    }

    public String getRootId(){
        try {
            if(mRootId == null)
                mRootId = mDrive.files().get("root").setFields("id").execute().getId();
        } catch (Exception e) {
            exception(e);
        }
        return mRootId;
    }
    public FileList getFileList(String id){
        try {
            return mDrive.files().list().setQ(String.format("trashed=false and '%s' in parents",id)).execute();
        } catch (IOException e) {
            return null;
        }
    }
    public FileList getFolderList(String id){
        try {
            return mDrive.files().list().setQ(String.format("trashed=false and '%s' in parents and mimeType = 'application/vnd.google-apps.folder'",id)).execute();
        } catch (IOException e) {
            return null;
        }
    }
    public String getItemId(String parent,String name){
        try {
            FileList list = mDrive.files().list().setQ(String.format("trashed=false and '%s' in parents and name='%s'", parent, name)).execute();
            if(list.getFiles().size() > 0)
                return list.getFiles().get(0).getId();
        } catch (IOException e) {}
        return null;
    }


    public String getItemId(String path){
        String[] folders = path.split("/", 0);
        String id = getRootId();
        for(String f : folders){
            if(f.length() == 0)
                continue;
            String id2 = getItemId(id,f);
            if(id2 == null)
                return null;
            id = id2;
        }
        return id;
    }
    public String createFile(String path,String mime){
        String id = getItemId(path);
        if(id != null)
            return id;

        PathFile pathFile = new PathFile(path);
        String parentId = getItemId(pathFile.getFolder());
        if(parentId == null)
            return null;

        File fileMetadata = new File();
        fileMetadata.setParents(Collections.singletonList(parentId));
        fileMetadata.setMimeType(mime);
        fileMetadata.setName(pathFile.getName());
        try {
            File file = mDrive.files().create(fileMetadata).setFields("id, parents").execute();
            if(file != null)
                return file.getId();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean moveFileFromId(String targetFolder,String srcId){
        try {
            String targetId = getFolderId(targetFolder);
            if(targetId == null)
                return false;
            File file = mDrive.files().get(srcId).setFields("parents").execute();
            if(file == null)
                return false;
            if(file.getParents() != null) {
                StringBuilder sb = new StringBuilder();
                for(String s : file.getParents())
                    sb.append(sb.length() > 0?","+s:s);
                mDrive.files().update(srcId, null).setAddParents(targetId).setRemoveParents(sb.toString()).execute();
            }else
                mDrive.files().update(srcId, null).setAddParents(targetId).execute();
            return true;
        } catch (Exception e) {
            exception(e);
        }
        return false;
    }
    public String getFolderId(String name){
        return getFolderId(name,false);
    }
    public String getFolderId(String name,boolean cflag){
        String[] folders = name.split("/", 0);
        String id = getRootId();
        for(String f : folders){
            if(f.length() == 0)
                continue;
            String id2 = getFolderId(id,cflag);
            if(id2 == null && cflag)
                id = createFolder(id,f);
            else
                id = id2;

        }
        return id;
    }

    public String createFolder(String id,String name){
        File fileMetadata = new File();
        fileMetadata.setName(name);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        if(id != null)
            fileMetadata.setParents(Collections.singletonList(id));
        try {
            File file = mDrive.files().create(fileMetadata).setFields("id, parents").execute();
            if(file != null)
                return file.getId();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public String upload(String dest,String src,String type){
        try {
            java.io.File fileDest = new java.io.File(dest);
            String pid = getFolderId(fileDest.getParent(),true);

            File fileMetadata = new File();
            fileMetadata.setName(fileDest.getName());
            fileMetadata.setMimeType(type);
            if(pid != null)
                fileMetadata.setParents(Collections.singletonList(pid));


            java.io.File filePath = new java.io.File(src);
            FileContent mediaContent = new FileContent(type, filePath);
            File file = mDrive.files().create(fileMetadata, mediaContent)
                    .setFields("id, parents")
                    .execute();
            return file.getId();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public Bitmap downloadImage(String id){
        try {
            HttpResponse response = mDrive.files().get(id).executeMedia();
            Bitmap bitmapm = BitmapFactory.decodeStream(response.getContent());
            return bitmapm;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public Bitmap downloadThumbnail(String id){
        try {
            File file = (File)mDrive.files().get(id).setFields("thumbnailLink").execute();
            URL imageUrl = new URL(file.getThumbnailLink());
            Bitmap bitmapm = BitmapFactory.decodeStream(imageUrl.openStream());

            return bitmapm;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
