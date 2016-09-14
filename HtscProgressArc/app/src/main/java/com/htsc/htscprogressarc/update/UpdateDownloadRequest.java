package com.htsc.htscprogressarc.update;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

/**
 * 真正执行下载的runnable
 * Created by zhangxiaoting on 16/9/8.
 */
public class UpdateDownloadRequest implements Runnable {
    private String mDownloadUrl;
    private String mLocalFilePath;
    private UpdateDownloadListener mDownloadListener;
    private boolean mIsDownloading = false;
    private long mCurrentFileLength;
    private DownloadResponseHandler mDownloadHandler;
    private Context mContext;

    private static final String TAG = "UpdateDownloadRequest";

    public UpdateDownloadRequest(Context context, String downloadUrl, String localFilePath, UpdateDownloadListener downloadListener) {
        this.mContext = context;
        this.mDownloadUrl = downloadUrl;
        this.mLocalFilePath = localFilePath;
        this.mDownloadListener = downloadListener;
        this.mIsDownloading = true;
        this.mDownloadHandler = new DownloadResponseHandler();
    }

    /**
     * 建立连接的方法
     */
    public void makeRequest() throws IOException, InterruptedException {
        if (!Thread.currentThread().isInterrupted()) {
            try {
                URL url = new URL(mDownloadUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000); // 5s timeout
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.connect(); // 开始请求 会阻塞当前线程
                mCurrentFileLength = connection.getContentLength();
                if (!Thread.currentThread().isInterrupted()) {
                    // 发送相应请求 执行下载文件
                    mDownloadHandler.sendResponseMessage(connection.getInputStream());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        try {
            makeRequest();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * 格式化浮点数 只保留两位小数
     *
     * @param value 传入的string类型的value
     * @return
     */
    private String getTwoPointFloatStr(float value) {
        DecimalFormat fNum = new DecimalFormat("0.00");
        return fNum.format(value);
    }

    public enum FailureCode {
        UnknownHost, Socket, SocketTimeout, ConnectTimeout, IO,
        HttpResponse, JSON, Interrupted
    }

    /**
     * 真正下载文件 并发送消息和回调的接口
     */
    public class DownloadResponseHandler {
        private static final int SUCCESS_MESSAGE = 0;
        private static final int FAILURE_MESSAGE = 1;
        private static final int START_MESSAGE = 2;
        private static final int FINISH_MESSAGE = 3;
        private static final int NETWORK_OFF_MESSAGE = 4;
        private static final int PROGRESS_CHANGE_MESSAGE = 5;

        private int completeSize = 0;
        private int progress = 0;

        private Handler handler;

        public DownloadResponseHandler() {
            this.handler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
//                    if (handler != null) {
//                        handler.handleMessage(msg);
//                    } else {
                    handleSelfMessage(msg);

//                    }
                }
            };
        }

        /**
         * 发送不同消息对象的方法
         */
        private void sendMessage(Message message) {
            if (handler != null) {
                handler.sendMessage(message);
            } else {
                handleSelfMessage(message);
            }
        }

        private void sendFinishMessage() {
            sendMessage(obtainMessage(FINISH_MESSAGE, null));
        }

        private void sendProgressChangedMessage(int progress) {
            sendMessage(obtainMessage(PROGRESS_CHANGE_MESSAGE, new Object[]{progress}));
        }

        private void sendFailureMessage(FailureCode failureCode) {
            sendMessage(obtainMessage(FAILURE_MESSAGE, new Object[]{failureCode}));
        }

        /**
         * 获取一个消息对象
         */
        private Message obtainMessage(int msgWhat, Object[] msgResponse) {
            Message message = null;
            if (handler != null) {
                message = handler.obtainMessage(msgWhat, msgResponse);
            } else {
                message = Message.obtain();
                message.what = msgWhat;
                message.obj = msgResponse;
            }
            return message;
        }

        protected void handleSelfMessage(Message msg) {
            Object[] response;
            switch (msg.what) {
                case FAILURE_MESSAGE:
                    response = (Object[]) msg.obj;
                    handleFailureMessage((FailureCode) response[0]);
                    break;
                case PROGRESS_CHANGE_MESSAGE:
                    response = (Object[]) msg.obj;
                    handleProgressChangedMessage(
                            ((Integer) response[0]).intValue());
                    break;
                case FINISH_MESSAGE:
                    handleFinishedMessage();
                    break;
            }
        }

        /**
         * 各种消息的处理
         */
        protected void handleFailureMessage(FailureCode failureCode) {
            mDownloadListener.onFailure();

        }

        protected void handleProgressChangedMessage(int progress) {
            mDownloadListener.onProgressChanged(progress, "");
        }

        protected void handleFinishedMessage() {
            mDownloadListener.onFinished(0, "");
        }

        protected void onFailure(FailureCode failureCode) {
            mDownloadHandler.onFailure(failureCode);

        }

        /**
         * 文件下载方法 会发送各种类型的事件
         *
         * @param is
         */
        void sendResponseMessage(InputStream is) {
            RandomAccessFile randomAccessFile = null;
            completeSize = 0;
            try {
                byte[] buffer = new byte[1024];
                int length = -1; // 每次读到的长度
                int limit = 0;
                randomAccessFile = new RandomAccessFile(mLocalFilePath, "rwd");
                while ((length = is.read(buffer)) != -1) { // 每次读取的length
                    Log.d(TAG, "Each length = " + length);
                    randomAccessFile.write(buffer, 0, length);
                    completeSize += length;
                    Log.d(TAG, "completeSize = " + completeSize + ", mCurrentFileLength = " + mCurrentFileLength);
                    if (completeSize < mCurrentFileLength) {
                        progress = (int) (Float.parseFloat(getTwoPointFloatStr((float) completeSize / mCurrentFileLength)) * 100f);
                        if (limit % 30 == 0 && progress <= 100) {
                            sendProgressChangedMessage(progress);
                        }
                        limit++;
                    }
                }
                sendFinishMessage();
            } catch (IOException e) {
                e.printStackTrace();
                sendFailureMessage(FailureCode.IO);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                    if (randomAccessFile != null) {
                        randomAccessFile.close();
                    }
                } catch (IOException e) {
                    sendFailureMessage(FailureCode.IO);
                }
            }
        }


    }
}
