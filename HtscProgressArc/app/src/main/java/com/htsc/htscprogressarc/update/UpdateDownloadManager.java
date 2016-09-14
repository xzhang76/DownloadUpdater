package com.htsc.htscprogressarc.update;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 下载调度管理器 调用UpdateDownloadRequest的方法执行下载操作
 * 可以通过两种方式：
 * 1.启动一个线程 去执行UpdateDownloadRequest这个runnable
 * 2.创建一个线程池 将UpdateDownloadRequest这个runnable扔进去执行
 * <p>
 * Created by zhangxiaoting on 16/9/8.
 */
public class UpdateDownloadManager {
    private static UpdateDownloadManager downloadManager;
    private ThreadPoolExecutor mThreadPoolExecutor;
    private UpdateDownloadRequest mDownloadRequest;

    // 单例模式 构造函数是私有的
    private UpdateDownloadManager() {
        mThreadPoolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }

    public static UpdateDownloadManager getInstance() {
        if (null == downloadManager) {
            downloadManager = new UpdateDownloadManager();
        }
        return downloadManager;
    }

    public void startDownload(Context context, String downloadUrl, String filePath, UpdateDownloadListener listener) {
        if (mDownloadRequest != null) {
            return;
        }
        checkLocalFilePath(filePath);
        // 开始下载文件
        mDownloadRequest = new UpdateDownloadRequest(context, downloadUrl, filePath, listener);
        Future<?> future = mThreadPoolExecutor.submit(mDownloadRequest);
    }

    private void checkLocalFilePath(String filePath) {
        File dir = new File(filePath.substring(0, filePath.lastIndexOf("/") + 1));
        if (!dir.exists()) {
            dir.mkdir();
        }
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
