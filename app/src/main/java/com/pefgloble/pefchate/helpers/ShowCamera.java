package com.pefgloble.pefchate.helpers;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

import androidx.annotation.NonNull;

public class ShowCamera extends SurfaceView implements SurfaceHolder.Callback {
    Camera camera;
    SurfaceHolder holder;
    boolean mPreviewRunning;
    public ShowCamera(Context context, Camera camera) {
        super(context);
        this.camera=camera;
        holder=getHolder();
        holder.addCallback(this);


    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
/*        if (mPreviewRunning) {
            camera.stopPreview();
        }
        Camera.Parameters parameters = camera.getParameters();
        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            parameters.set("orientation", "portrait");
            camera.setDisplayOrientation(90);
            parameters.setRotation(90);
        } else {
            parameters.set("orientation", "landscape");
            camera.setDisplayOrientation(0);
            parameters.setRotation(0);
        }

        camera.setParameters(parameters);
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
        mPreviewRunning = true;*/
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
       /* camera.stopPreview();
        mPreviewRunning = false;
        camera.release();*/
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
         try {
        Camera.Parameters parameters=camera.getParameters();
        if (this.getResources().getConfiguration().orientation!= Configuration.ORIENTATION_LANDSCAPE){
            parameters.set("orientation","portrait");
            camera.setDisplayOrientation(90);
            parameters.setRotation(90);
        }
        else {
            parameters.set("orientation","landscape");
            camera.setDisplayOrientation(0);
            parameters.setRotation(0);
        }

        camera.setParameters(parameters);
         }
         catch (Exception e){
             e.printStackTrace();
         }

        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
