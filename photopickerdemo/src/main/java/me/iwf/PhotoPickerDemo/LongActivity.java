package me.iwf.PhotoPickerDemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;

import me.iwf.photopicker.largeimage.LongImageView;

/**
 * @author mac
 * @title LongActivity$
 * @description
 * @modifier
 * @date
 * @since 15/12/14$ 上午12:15$
 **/
public class LongActivity extends ActionBarActivity {
    private LongImageView long_image;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.long_image);
        long_image = (LongImageView) findViewById(R.id.long_image);
        long_image.setImage("http://qn-cdn-img.mofunenglish.com/124/73/20151213010317107787000334.jpg");

    }
}
