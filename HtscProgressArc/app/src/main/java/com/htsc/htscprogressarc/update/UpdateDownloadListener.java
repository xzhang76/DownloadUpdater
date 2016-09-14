package com.htsc.htscprogressarc.update;

/**
 * 下载更新的事件监听(回调)
 * Created by zhangxiaoting on 16/9/8.
 */
public interface UpdateDownloadListener {
    /**
     * 更新下载的开始回调
     */
    public void onStarted();

    /**
     * 进度更新回调
     */
    public void onProgressChanged(int progress, String downloadUrl);

    /**
     * 更新下载完毕的回调
     */
    public void onFinished(int completeSize, String downloadUrl);

    /**
     * 更新下载失败的回调
     */
    public void onFailure();
}
