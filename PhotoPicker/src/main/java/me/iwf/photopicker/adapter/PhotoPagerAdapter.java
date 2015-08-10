package me.iwf.photopicker.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.PhotoPagerActivity;
import me.iwf.photopicker.PhotoPickerActivity;
import me.iwf.photopicker.R;
import me.iwf.photopicker.utils.FileTypeUtil;
import me.iwf.photopicker.utils.FileUtil;
import me.iwf.photopicker.utils.PhotoUtil;
import pl.droidsonroids.gif.GifDrawable;

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
            Glide.with(mContext)
                    .load(path)
//                    .dontAnimate()
                    .override(1800, 1800)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String uri, Target<GlideDrawable> target, boolean b) {
                            showImage(path, imageView, progressBar);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable glideDrawable, String uri, Target<GlideDrawable> target, boolean b, boolean b1) {
                            progressBar.setVisibility(View.GONE);
                            imageView.requestLayout();
                            imageView.invalidate();
                            return false;
                        }
                    })
                    .placeholder((null != event && event.getPlaceholderimageid() > 0) ? event.getPlaceholderimageid() : PhotoPagerActivity.PLACEHOLDERIMAGEID)
                    .error((null != event && event.getErrorimageid() > 0) ? event.getErrorimageid() : PhotoPagerActivity.ERRORIMAGEID)
                    .dontTransform()
                    .into(imageView);
//            downloadShow(imageView, progressBar, path);
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

    private void showImage(String path, ImageView imageView, final ProgressBar progressBar) {
        PhotoUtil.showPhoto(mContext, path, imageView, new PhotoUtil.ImagePicker() {
            @Override
            public void OnSelected() {
                progressBar.setVisibility(View.GONE);
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


}
