package com.htsc.htscprogressarc.update;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import com.htsc.htscprogressarc.R;

import java.io.File;

/**
 * 启动service调用UpdateDownloadManager的方法进行下载
 * <p>
 * Created by zhangxiaoting on 16/9/8.
 */
public class UpdateDownloadService extends Service {
    private String mApkFileUrl;
    public static String mFilePath;
    private NotificationManager notificationManager;
    private Notification mNotification;

    private UpdateDownloadListener mListener = new UpdateDownloadListener() {
        @Override
        public void onStarted() {

        }

        @Override
        public void onProgressChanged(int progress, String downloadUrl) {
            notifyUser(getString(R.string.update_download_processing), getString(R.string.update_download_processing), progress);
        }

        @Override
        public void onFinished(int completeSize, String downloadUrl) {
            notifyUser(getString(R.string.update_download_finish), getString(R.string.update_download_finish_msg), 100);
            stopSelf();
        }

        @Override
        public void onFailure() {
            notifyUser(getString(R.string.update_download_failed), getString(R.string.update_download_failed_msg), 0);
            stopSelf();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mFilePath = Environment.getExternalStorageDirectory() + "/imooc/QjFound.apk";

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            notifyUser(getString(R.string.update_download_failed), getString(R.string.update_download_failed_msg), 0);
            stopSelf();
        } else {
            mApkFileUrl = intent.getStringExtra("apkUrl");
            notifyUser(getString(R.string.update_download_start), getString(R.string.update_download_start_msg), 0);
            startDownload();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void startDownload() {
        UpdateDownloadManager.getInstance().startDownload(this ,mApkFileUrl, mFilePath, mListener);
    }

    /**
     * 通知用户 notification
     *
     * @param result 结果
     * @param reason 原因
     */
    private void notifyUser(String result, String reason, int progress) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.notification_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.notification_icon))
                .setContentTitle("下载更新");
        if (progress > 0 && progress < 100) {
            builder.setProgress(100, progress, false);
        } else {
            builder.setProgress(0, 0, false);
        }
        builder.setAutoCancel(true);
        builder.setWhen(System.currentTimeMillis());
        builder.setTicker(result);
        builder.setContentIntent(progress >= 100 ? getContentIntent()
                : PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT));
        mNotification = builder.build();
        notificationManager.notify(0, mNotification);

    }

    private PendingIntent getContentIntent() {
        File fileApk = new File(mFilePath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse("file://" + fileApk.getAbsolutePath()), "application/vnd.android.package-archive");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
