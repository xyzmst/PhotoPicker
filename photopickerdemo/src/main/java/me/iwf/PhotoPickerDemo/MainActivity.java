package me.iwf.PhotoPickerDemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.PhotoPagerActivity;
import me.iwf.photopicker.PhotoPickerActivity;
import me.iwf.photopicker.utils.PhotoPickerIntent;

public class MainActivity extends ActionBarActivity {


  RecyclerView recyclerView;
  PhotoAdapter photoAdapter;

  ArrayList<String> selectedPhotos = new ArrayList<>();

  public final static int REQUEST_CODE = 1;


  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
    photoAdapter = new PhotoAdapter(this, selectedPhotos);

    recyclerView.setLayoutManager(new StaggeredGridLayoutManager(4, OrientationHelper.VERTICAL));
    recyclerView.setAdapter(photoAdapter);

    findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        PhotoPickerIntent intent = new PhotoPickerIntent(MainActivity.this);
        intent.setPhotoCount(9);
        startActivityForResult(intent, REQUEST_CODE);
      }
    });


    findViewById(R.id.button_no_camera).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        PhotoPickerIntent intent = new PhotoPickerIntent(MainActivity.this);
        intent.setPhotoCount(7);
        intent.setShowCamera(false);
        startActivityForResult(intent, REQUEST_CODE);
      }
    });


    findViewById(R.id.button_one_photo).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        PhotoPickerIntent intent = new PhotoPickerIntent(MainActivity.this);
        intent.setPhotoCount(1);
        intent.setShowCamera(true);
        startActivityForResult(intent, REQUEST_CODE);
      }
    });

    findViewById(R.id.show_image2).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        startActivity(new Intent(getBaseContext(),LongActivity.class));
      }
    });

    findViewById(R.id.show_image).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        ArrayList<String> photoPaths = new ArrayList<String>();
        photoPaths.add("http://qn-cdn-img.mofunenglish.com/32/361/20160222124102640825000793.jpg");
        photoPaths.add("http://qn-cdn-img.mofunenglish.com/87/429/20160222203546088740000593.gif");
//        photoPaths.add("http://qn-cdn-img.mofunenglish.com/124/73/20151213010317107787000334.jpg");
//        photoPaths.add("http://7jptcr.com5.z0.glb.clouddn.com/69/27/20150805152754291827000981.jpg");
//        photoPaths.add("http://qn-cdn-img.mofunenglish.com/16/428/2015092115554000000121050.gif");
//        photoPaths.add("http://g.hiphotos.baidu.com/zhidao/wh%3D450%2C600/sign=4ad7bfb61bd8bc3ec65d0eceb7bb8a28/b3119313b07eca80436e336b932397dda04483d6.jpg");
//        photoPaths.add("http://7jptcr.com5.z0.glb.clouddn.com/173/32/20150808230202739974000381.jpg");
        Intent intent = new Intent(MainActivity.this, PhotoPagerActivity.class);
        intent.putExtra(PhotoPagerActivity.EXTRA_CURRENT_ITEM, 0);
        intent.putExtra(PhotoPagerActivity.EXTRA_PHOTOS, photoPaths);
        startActivity(intent);
      }
    });

  }


  public void previewPhoto(Intent intent) {
    startActivityForResult(intent, REQUEST_CODE);
  }


  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    List<String> photos = null;
    if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
      if (data != null) {
        photos = data.getStringArrayListExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS);
      }
      selectedPhotos.clear();

      if (photos != null) {

        selectedPhotos.addAll(photos);
      }
      photoAdapter.notifyDataSetChanged();
    }
  }


}
