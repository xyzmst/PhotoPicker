package me.iwf.photopicker.largeimage;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup.LayoutParams;

public class LongImageView2 extends LargeImageView {

    public LongImageView2(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public LongImageView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public LongImageView2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LongImageView2(Context context) {
        super(context);
    }

    @Override
    public void onImageLoadFinished(final int imageWidth, final int imageHeight) {
        super.onImageLoadFinished(imageWidth, imageHeight);
        post(new Runnable() {
            @Override
            public void run() {
                setLayout(imageWidth, imageHeight);
            }
        });
    }

    private void setLayout(int imageWidth, int imageHeight) {
        LayoutParams layoutParams = getLayoutParams();
        layoutParams.width = (int) (1.0f * imageWidth * getHeight() / imageHeight);
        setLayoutParams(layoutParams);
    }




}
