package me.iwf.photopicker.utils;

import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.UnitTransformation;

public class Delay extends UnitTransformation {
    private final int sleepTime;

    public Delay(int sleepTime) {
        this.sleepTime = sleepTime;
    }

    @Override
    public Resource transform(Resource resource, int outWidth, int outHeight) {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException ex) {
        }
        return super.transform(resource, outWidth, outHeight);
    }
} 