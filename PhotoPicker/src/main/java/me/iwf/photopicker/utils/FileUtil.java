package me.iwf.photopicker.utils; /**
 * Created by reasono on 15/7/21.
 */

import android.graphics.BitmapFactory;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import me.iwf.photopicker.PhotoPagerActivity;

/**
 * @author xyzmst
 * @title FileUtil
 * @description 用于获取网络图片流
 * @modifier
 * @date
 * @since 15/8/7 下午5:22
 **/
public class FileUtil {

    public static void download(final String remoteUrl, final OnFileDownloadEvent fileDownloadEventListener) {
        final File appDir = new File(PhotoPagerActivity.CACHEDIR);
        if (!appDir.exists()) {
            if (!appDir.mkdir()) {
                return;
            }
        }
        if (TextUtils.isEmpty(remoteUrl)) {
            return;
        }
        final String localPath = appDir + "/" + remoteUrl.substring(remoteUrl.lastIndexOf("/") + 1);
        File file = new File(localPath);
        if (file.exists()) {
            if (file.length() > 0) {
                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(localPath, options);
                } catch (Exception e) {
                    file.delete();
                    downThread(remoteUrl, fileDownloadEventListener, localPath);
                }
                if (null != fileDownloadEventListener)
                    fileDownloadEventListener.OnComplete(localPath);
            } else {
                downThread(remoteUrl, fileDownloadEventListener, localPath);
            }
        } else {
            downThread(remoteUrl, fileDownloadEventListener, localPath);
        }
    }

    public static void downThread(final String remoteUrl, final OnFileDownloadEvent fileDownloadEventListener, final String localPath) {
        new Thread() {
            @Override
            public void run() {

                if (null != fileDownloadEventListener)
                    fileDownloadEventListener.OnStart();

                FileOutputStream out = null;
                InputStream is = null;
                long readSize;
                long mediaLength;
                final File cacheFile = new File(localPath);
                HttpURLConnection httpConnection;
                URL url;
                try {
                    url = new URL(remoteUrl);
                    httpConnection = (HttpURLConnection) url.openConnection();
                    if (!cacheFile.exists()) {
                        cacheFile.getParentFile().mkdirs();
                        cacheFile.createNewFile();
                    }

                    readSize = cacheFile.length();
                    out = new FileOutputStream(cacheFile, true);

                    httpConnection.setRequestProperty("Connection", "Keep-Alive");
                    httpConnection.setRequestProperty("User-Agent", "NetFox");
                    httpConnection.setRequestProperty("RANGE", "bytes="
                            + readSize + "-");

                    is = httpConnection.getInputStream();

                    mediaLength = httpConnection.getContentLength();

                    if (mediaLength <= 0) {
                        return;
                    }

                    byte buf[] = new byte[1024];
                    int size;
                    while ((size = is.read(buf)) != -1) {
                        out.write(buf, 0, size);
                        readSize += size;
                    }


                } catch (OutOfMemoryError outOfMemoryError) {
                    outOfMemoryError.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                    if (null != fileDownloadEventListener)
                        fileDownloadEventListener.OnError(e);
                } finally {
                    if (out != null) {
                        try {
                            out.flush();
                            out.getFD().sync();
                            out.close();
                        } catch (IOException e) {
                            // no impl
                        }
                    }
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            if (null != fileDownloadEventListener)
                                fileDownloadEventListener.OnError(e);
                        }
                    }
                    if (null != fileDownloadEventListener)
                        fileDownloadEventListener.OnComplete(localPath);
                }
            }
        }.start();
    }

    public static void getUriImageType(final String remoteUrl, final OnFileloadEvent event) {
        new Thread() {
            @Override
            public void run() {

                if (null != event)
                    event.OnStart();

                FileOutputStream out = null;
                InputStream is = null;
                long mediaLength;
                HttpURLConnection httpConnection;
                URL url;
                try {
                    url = new URL(remoteUrl);
                    httpConnection = (HttpURLConnection) url.openConnection();
                    httpConnection.setRequestProperty("Connection", "Keep-Alive");
                    httpConnection.setRequestProperty("User-Agent", "NetFox");
                    is = httpConnection.getInputStream();
                    mediaLength = httpConnection.getContentLength();

                    if (mediaLength <= 0) {
                        return;
                    }
                    String type = FileTypeUtil.getFileByFile(is);

                    if (null != event)
                        event.OnComplete(type);

                } catch (OutOfMemoryError outOfMemoryError) {
                    outOfMemoryError.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public static byte[] readStream(InputStream in) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len = in.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
        outputStream.close();
        in.close();
        return outputStream.toByteArray();
    }


    public interface OnFileloadEvent {
        void OnStart();

        void OnComplete(String type);

        void OnError(Exception exception);
    }

    public interface OnFileDownloadEvent {
        void OnStart();

        void OnProgress(int progress, int total);

        void OnComplete(String path);

        void OnError(Exception exception);
    }

}
