package com.app.debrove.tinpandog.groups;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.app.debrove.tinpandog.R;
import com.app.debrove.tinpandog.groups.datebase.ChatInformation;
import com.app.debrove.tinpandog.groups.datebase.GroupsMemberInformation;
import com.hyphenate.chat.EMClient;

import com.hyphenate.chat.EMMessage;


/**
 * Created by NameTooLong on 2017/10/29.
 *
 * 群聊实现的{@link Activity}
 * 实现功能：
 *     1.消息的发送与本地保存（图片、文字）；
 *     2.群聊的创建 @see {@link GroupsCreateActivity}
 */

public class GroupsActivity extends Activity implements View.OnClickListener{
    /**
     * 向{@link Activity#startActivity(Intent)}的Intent中传入
     *       <Field>mGroupsName,mUserName,mProfileImagePath</Field>时所用的FLAG
     */
    public static final String GROUPS_NAME="groupsName";
    public static final String USER_NAME="userName";
    public static final String USER_PROFILE_PATH="userProfilePath";

    private static final int CHOOSE_PHOTO_FROM_ALBUM=0;
    private static final int ASK_PERMISSION=1;

    /**
     * @param mGroupsName 群聊的群名
     * @param mUserName 用户名
     * @param mProfileImagePath 用户头像图片的绝对路径名
     *    在{@link Activity#startActivity(Intent)}时，向Intent中传入
     */
    private String mGroupsName="GroupsExample-1.0",
            mUserName="601976748@qq.com",mProfileImagePath;

    private RecyclerView mRecyclerView;
    private EditText mEditText;
    private ImageButton mImageButton;
    private Button mButton_sendImage;

    private ChatItemAdapter mChatItemAdapter;
    private MessageListener mMessageListener;
    private LinearLayout mView_activity,mLinearLayout_key;
    private int height_key=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activty_groups);

        mGroupsName=getIntent().getStringExtra(GROUPS_NAME);
        mUserName=getIntent().getStringExtra(USER_NAME);
        mProfileImagePath=getIntent().getStringExtra(USER_PROFILE_PATH);

        mView_activity=(LinearLayout) findViewById(R.id.groups_activity_viewActivity);
        mView_activity.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom != oldBottom) {
                    if(mChatItemAdapter.width_content<=0){
                        mChatItemAdapter.width_content=right-left;
                    }
                    if (bottom < oldBottom) {
                        mChatItemAdapter.moveToNewItem();
                        Log.e(TAG, "keyboard appear");
                    }
                    height_key = (height_key < mLinearLayout_key.getHeight()) ? mLinearLayout_key.getHeight() : height_key;
                    //Log.e(TAG, "h=" + height_key);
                    if (height_key > 0) {
                        mLinearLayout_key.setLayoutParams(new LinearLayout.LayoutParams(mLinearLayout_key.getWidth(), height_key));
                        mLinearLayout_key.setY(bottom - height_key);
                    }
                }
            }
        });
        mLinearLayout_key=(LinearLayout)findViewById(R.id.groups_activity_linerLayout_key);

        mEditText=(EditText)findViewById(R.id.groups_activity_editText);

        mRecyclerView=(RecyclerView)findViewById(R.id.groups_activity_recyclerView);
        mImageButton=(ImageButton) findViewById(R.id.groups_activity_button_sendMessage);
        mImageButton.setOnClickListener(this);
        mButton_sendImage=(Button)findViewById(R.id.groups_activity_button_sendImage);
        mButton_sendImage.setOnClickListener(this);

        mChatItemAdapter=new ChatItemAdapter();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mChatItemAdapter);
        mChatItemAdapter.setRecyclerView(mRecyclerView);

        new LoadChatTask(mUserName,mGroupsName,mChatItemAdapter).execute();

        EMClient.getInstance().chatManager().addMessageListener(mMessageListener);
    }

    @Override
    public void onClick(View v) {
        /**
         * 图片消息的发送单独处理
         */
        if(v.getId()==R.id.groups_activity_button_sendImage){
            if(ContextCompat.checkSelfPermission(GroupsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(GroupsActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},ASK_PERMISSION);
            else
                openAlbum();
            return;
        }
        ChatInformation chatInformation=new ChatInformation();
        chatInformation.setProfileUri(mProfileImagePath);
        chatInformation.setTime(System.currentTimeMillis());
        chatInformation.setGroupsName(mGroupsName);
        chatInformation.setIs_others(false);
        EMMessage emMessage=null;
        ChatItem chatItem=new ChatItem(mProfileImagePath,false);
        switch (v.getId()){
            case R.id.groups_activity_button_sendMessage:
                String s=mEditText.getText().toString();
                emMessage=EMMessage.createTxtSendMessage(s,mGroupsName);
                if(emMessage==null) {
                    Log.e(TAG, "null emMessage");
                    return;
                }
                mEditText.setText("");
                chatInformation.setContent(s);
                chatInformation.setChatType(ChatInformation.TEXT);
                chatItem.setContent(s, ChatItem.ChatType.TEXT);
                sendMess(chatInformation,chatItem,emMessage);
                break;
        }
    }

    private static final String TAG = "GroupsActivity";

    private void sendMess(ChatInformation chatInformation,ChatItem chatItem,EMMessage emMessage){
        chatInformation.save();
        emMessage.setChatType(EMMessage.ChatType.GroupChat);
        emMessage.setAttribute(GroupsMemberInformation.GROUPS_NAME,mGroupsName);
        mChatItemAdapter.addNewChatItem(chatItem);
        SendEMMessageThread sendEMMessageThread=new SendEMMessageThread(emMessage);
        sendEMMessageThread.start();
    }

    private void sendImageMess(String providedImagePath){
        String imagePath=ImageLoader.saveImageFromLocal(providedImagePath);
        ChatInformation chatInformation=new ChatInformation();
        chatInformation.setProfileUri(mProfileImagePath);
        chatInformation.setTime(System.currentTimeMillis());
        chatInformation.setGroupsName(mGroupsName);
        chatInformation.setIs_others(false);
        ChatItem chatItem=new ChatItem(mProfileImagePath,false);
        EMMessage emMessage=EMMessage.createImageSendMessage(imagePath,false,mGroupsName);
        chatInformation.setContent(imagePath);
        chatInformation.setChatType(ChatInformation.IMAGE);
        chatItem.setContent(imagePath, ChatItem.ChatType.IMAGE);
        sendMess(chatInformation,chatItem,emMessage);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EMClient.getInstance().chatManager().removeMessageListener(mMessageListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults) {
        switch (requestCode){
            case ASK_PERMISSION:
                if(grantResults.length>0&&grantResults[0]== PackageManager.PERMISSION_GRANTED)
                    openAlbum();
                else
                    Toast.makeText(this,"Permission denied!",Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void openAlbum(){
        Intent intent=new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,CHOOSE_PHOTO_FROM_ALBUM);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data==null||data.getData()==null)
            return;
        switch (requestCode){
            case CHOOSE_PHOTO_FROM_ALBUM:
                if(Build.VERSION.SDK_INT>=19)
                    handleImageOnKitKat(data);
                else
                    handleImageBeforeKitKat(data);
                break;
        }
    }

    @TargetApi(21)
    private void handleImageOnKitKat(Intent data){
        String imagePath=null;
        Uri uri=data.getData();
        if(DocumentsContract.isDocumentUri(this,uri)){
            String docId=DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id=docId.split(":")[1];
                String selection= MediaStore.Images.Media._ID+"="+id;
                imagePath=getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }
            else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri= ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
                imagePath=getImagePath(contentUri,null);
            }
        }
        else if("content".equalsIgnoreCase(uri.getScheme()))
            imagePath=getImagePath(uri,null);
        else if("file".equalsIgnoreCase(uri.getScheme()))
            imagePath=uri.getPath();
        sendImageMess(imagePath);
    }

    private String getImagePath(Uri uri,String selection){
        String imagePath=null;
        Cursor cursor=getContentResolver().query(uri,null,selection,null,null);
        if(cursor!=null){
            if(cursor.moveToFirst())
                imagePath=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();
        }
        return imagePath;
    }

    private void handleImageBeforeKitKat(Intent data){
        Uri uri=data.getData();
        String imagePath=getImagePath(uri,null);
        sendImageMess(imagePath);
    }

    private class SendEMMessageThread extends Thread{
        EMMessage mEMMessage;
        SendEMMessageThread(EMMessage emMessage){
            mEMMessage=emMessage;
        }

        @Override
        public void run() {
            EMClient.getInstance().chatManager().sendMessage(mEMMessage);
        }
    }
}
