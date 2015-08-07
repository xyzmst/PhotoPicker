package me.iwf.photopicker.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.io.File;

import me.iwf.photopicker.PhotoPagerActivity;

/**
 * @author mac
 * @title PhotoUtil$
 * @description
 * @modifier
 * @date
 * @since 15/8/7$ 下午4:26$
 **/
public class PhotoUtil {

    final static int MAX_SIZE = 4095;
    final static int LIMIT_SIZE = 2000;


    public static void showPhoto(final Context context,
                                 final String path,
                                 final ImageView mImageView, final ImagePicker event) {

        new Thread() {
            @Override
            public void run() {
                int width;
                int height;
                if (new File(path).exists()) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(path, options);
                    options.inJustDecodeBounds = false;
                    width = options.outWidth;
                    height = options.outHeight;
                } else {
                    try {
                        Bitmap bitmap = Picasso.with(context).load(path).get();
                        width = bitmap.getWidth();
                        height = bitmap.getHeight();
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (event != null) {
                            event.OnEror(e);
                        }
                        return;
                    }
                }

                float ratio = (float) width / (float) height;

                width = width > MAX_SIZE ? LIMIT_SIZE : width;
                height = (int) (width / ratio);
                height = height > MAX_SIZE ? LIMIT_SIZE : height;
                width = (int) (height * ratio);

                final int descWidth = width;
                final int descHeight = height;
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (event != null) {
                            event.OnSelected();
                        }


                        if (new File(path).exists()) {
                            if (descWidth > 0 || descHeight > 0) {
                                Picasso.with(context)
                                        .load(new File(path))
                                        .resize(descWidth, descHeight)
                                        .centerCrop()
                                        .placeholder(PhotoPagerActivity.PLACEHOLDERIMAGEID)
                                        .error(PhotoPagerActivity.ERRORIMAGEID)
                                        .into(mImageView);
                            } else {
                                Glide.with(context)
                                        .load(new File(path))
                                        .override(800, 800)
                                        .placeholder(PhotoPagerActivity.PLACEHOLDERIMAGEID)
                                        .error(PhotoPagerActivity.ERRORIMAGEID)
                                        .into(mImageView);
                            }
                        } else {
                            Picasso.with(context)
                                    .load(path)
                                    .resize(descWidth, descHeight)
                                    .centerCrop()
                                    .placeholder(PhotoPagerActivity.PLACEHOLDERIMAGEID)
                                    .error(PhotoPagerActivity.ERRORIMAGEID)
                                    .into(mImageView);
                        }
                    }
                });
            }
        }.start();
    }


    public interface ImagePicker {
        void OnSelected();

        void OnEror(Exception e);
    }
}
