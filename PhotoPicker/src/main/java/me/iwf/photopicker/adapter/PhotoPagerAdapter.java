package me.iwf.photopicker.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.iwf.photopicker.PhotoPagerActivity;
import me.iwf.photopicker.PhotoPickerActivity;
import me.iwf.photopicker.R;
import me.iwf.photopicker.utils.Delay;
import me.iwf.photopicker.utils.FileTypeUtil;
import me.iwf.photopicker.utils.FileUtil;
import me.iwf.photopicker.utils.PhotoUtil;
import pl.droidsonroids.gif.GifDrawable;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

/**
 * Created by donglua on 15/6/21.
 */
public class PhotoPagerAdapter extends PagerAdapter {

    private List<String> paths = new ArrayList<>();
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    Event event;
    private GifDrawable drawable;

    public PhotoPagerAdapter(Context mContext, List<String> paths, Event event) {
        this.mContext = mContext;
        this.paths = paths;
        this.event = event;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {

        View itemView = mLayoutInflater.inflate(R.layout.item_pager, container, false);

        final ImageView imageView = (ImageView) itemView.findViewById(R.id.iv_pager);
        final ProgressBar progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);

        final String path = paths.get(position);
        if (path.startsWith("http")) {
            if (path.endsWith(".gif")) {
                showImageByGlide(imageView, progressBar, path, 0, 0);
            } else {
                PhotoUtil.showPhoto(mContext, path, imageView, new PhotoUtil.ImagePicker() {
                    @Override
                    public void OnSelected() {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void OnShow(int width, int height) {
                        showImageByGlide(imageView, progressBar, path, width, height);
                    }

                    @Override
                    public void OnEror(Exception e) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        } else {
            if ("gif".equals(FileTypeUtil.getFileByFile(new File(path)))) {
                showGif(new File(path), imageView, progressBar);
            } else {
                showImage(path, imageView, progressBar);
            }
        }


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mContext instanceof PhotoPickerActivity) {
                    if (!((Activity) mContext).isFinishing()) {
                        ((Activity) mContext).onBackPressed();
                    }
                }
            }
        });

        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (null != event)
                    event.onLongClick(position);
                return false;
            }
        });

        container.addView(itemView);

        return itemView;
    }

    private synchronized void showImageByGlide(final ImageView imageView, final ProgressBar progressBar, final String path, int width, int height) {
        final DrawableRequestBuilder<String> req = Glide
                .with(mContext.getApplicationContext())
                .fromString()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE) // disable network delay for demo
                .skipMemoryCache(true) // make sure transform runs for demo
                .crossFade(2000) // default, just stretch time for noticability
                ;
        if (width == 0)
            width = 1000;
        if (height == 0)
            height = 1000;
        req.clone()
                .load(path)
                .override(width, height)
                .placeholder(PhotoPagerActivity.PLACEHOLDERIMAGEID)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String uri, Target<GlideDrawable> target, boolean b) {
                        showImage(path, imageView, progressBar);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable glideDrawable, String uri, Target<GlideDrawable> target, boolean b, boolean b1) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .error((null != event && event.getErrorimageid() > 0) ? event.getErrorimageid() : PhotoPagerActivity.ERRORIMAGEID)
                .transform(new Delay(500))
                .into(imageView);

    }

    private void downloadShow(final ImageView imageView, final ProgressBar progressBar, final String path) {
        FileUtil.download(path, new FileUtil.OnFileDownloadEvent() {
            @Override
            public void OnStart() {

            }

            @Override
            public void OnProgress(int progress, int total) {

            }

            @Override
            public void OnComplete(final String local_path) {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if ("gif".equals(FileTypeUtil.getFileByFile(new File(local_path)))) {
                            showGif(new File(local_path), imageView, progressBar);
                        } else {
                            new File(local_path).delete();
                            showImage(path, imageView, progressBar);
                        }
                    }
                });

                Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                scanIntent.setData(Uri.fromFile(new File(path)));
                mContext.sendBroadcast(scanIntent);

            }

            @Override
            public void OnError(Exception exception) {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Picasso.with(mContext)
                                .load(PhotoPagerActivity.ERRORIMAGEID)
                                .into(imageView);
                    }
                });

            }
        });
    }

    private synchronized Observable<HashMap> showHttpImage(final String path) {

        return Observable.create(new Observable.OnSubscribe<HashMap>() {
            @Override
            public void call(final Subscriber<? super HashMap> subscriber) {
                Schedulers.io().createWorker()
                        .schedule(new Action0() {
                            @Override
                            public void call() {
                                try {
                                    Bitmap bitmap = Picasso.with(mContext).load(path).get();
                                    int width = bitmap.getWidth();
                                    int height = bitmap.getHeight();
                                    HashMap<String, Object> hashMap = new HashMap<String, Object>();
                                    hashMap.put("path", path);
                                    if (width < 4095 && height < 4095) {
                                        hashMap.put("isLong", false);
                                        subscriber.onNext(hashMap);
                                    } else {
                                        hashMap.put("isLong", true);
                                        subscriber.onNext(hashMap);
                                    }

                                } catch (final Exception e) {
                                    e.printStackTrace();
                                    subscriber.onError(e);
                                }

                            }
                        });
            }
        });

    }

    private void showImage(String path, final ImageView imageView, final ProgressBar progressBar) {
        PhotoUtil.showPhoto(mContext, path, imageView, new PhotoUtil.ImagePicker() {
            @Override
            public void OnSelected() {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void OnShow(int width, int height) {

            }

            @Override
            public void OnEror(Exception e) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void showGif(File file, ImageView imageView, ProgressBar progressBar) {
        try {
            drawable = new GifDrawable(file);
            imageView.setImageDrawable(drawable);
            drawable.start();
            progressBar.setVisibility(View.GONE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showGif(ImageView imageView, final ProgressBar progressBar, Uri uri) {
        Glide.with(mContext)
                .load(uri)
                .override(800, 800)
                .listener(new RequestListener<Uri, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .placeholder(PhotoPagerActivity.PLACEHOLDERIMAGEID)
                .error(PhotoPagerActivity.ERRORIMAGEID)
                .into(imageView);
    }


    @Override
    public int getCount() {
        return paths.size();
    }


    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public interface Event {
        void onLongClick(int position);

        int getPlaceholderimageid();

        int getErrorimageid();

    }


    private boolean isNetConnecting() {
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        try {
            ConnectivityManager connectivity = (ConnectivityManager) mContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                // 获取网络连接管理的对象
                NetworkInfo info = connectivity.getActiveNetworkInfo();
                if (info != null && info.isConnected()) {
                    // 判断当前网络是否已经连接
                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            Log.e("error", e.toString());
        }
        return false;
    }
}
