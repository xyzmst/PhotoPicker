package me.iwf.photopicker.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.lang.reflect.Field;

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
    private static int MAX_WITH ;
    private static int MAX_HEIGHT;


    public static void showPhoto(final Context context,
                                 final String path,
                                 final ImageView mImageView, final ImagePicker event) {

        new Thread() {
            @Override
            public void run() {
                boolean isLong = false;
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
                    } catch (final Exception e) {

                        e.printStackTrace();
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (event != null) {
                                    event.OnEror(e);
                                    Picasso.with(context)
                                            .load("aaaa")
                                            .error(PhotoPagerActivity.ERRORIMAGEID)
                                            .into(mImageView);
                                }
                            }
                        });

                        return;
                    }
                }

                float ratio = (float) width / (float) height;
//                MAX_WITH = getScreenWidth(context);
//                MAX_HEIGHT = getScreenHeight(context) - getStatusBarHeight(context);
                MAX_WITH = 2000;
                MAX_HEIGHT = 2000;
                width = width > MAX_SIZE ? MAX_WITH : width;
                height = (int) (width / ratio);
                height = height > MAX_SIZE ? MAX_HEIGHT : height;
                if (width > MAX_SIZE || height > MAX_SIZE)
                    isLong = true;
                width = (int) (height * ratio);

                final int descWidth = width;
                final int descHeight = height;
                final boolean finalIsLong = isLong;
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
                            if (!finalIsLong) {
                                Picasso.with(context)
                                        .load(path)
                                        .resize(descWidth, descHeight)
                                        .centerCrop()
                                        .placeholder(PhotoPagerActivity.PLACEHOLDERIMAGEID)
                                        .error(PhotoPagerActivity.ERRORIMAGEID)
                                        .into(mImageView);
                            } else {
                                if (event != null) {
                                    event.OnShow(descWidth, descHeight);
                                }
                            }


                        }
                    }
                });
            }
        }.start();
    }


    public interface ImagePicker {
        void OnSelected();

        void OnShow(int width, int height);

        void OnEror(Exception e);
    }


    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getWidth();
    }

    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getHeight();
    }

    /**
     * 获取状态栏高度
     *
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        Class<?> c;
        Object obj;
        Field field;
        int x, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }

}
