package com.example.androidimageeditingtask;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.isseiaoki.simplecropview.CropImageView;
import com.isseiaoki.simplecropview.callback.CropCallback;
import com.isseiaoki.simplecropview.callback.LoadCallback;
import com.isseiaoki.simplecropview.callback.SaveCallback;
import com.isseiaoki.simplecropview.util.Logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class Crop_Image_Activity extends Activity {
    private CropImageView mCropView;
    private Uri sourceUri = null;
    private Uri saveUri = null;
    private Uri path_uri = null;
    TextView sentBtn;
    TextView back;
    String strpath;
    static String filename;
    private Bitmap.CompressFormat mCompressFormat = Bitmap.CompressFormat.JPEG;
    static String path_intent = null;
    private Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.cropimage_activity);

        try {
            initViews();
            getIntents();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (sourceUri == null) {
            sourceUri = Uri.fromFile(new File(strpath));
        }
        mCropView.setCropMode(CropImageView.CropMode.FREE);
        mCropView.load(sourceUri).execute(new LoadCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(Throwable e) {

            }
        });
        sentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Crop", "sendbtn click");
                cropImage();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mCropView.load(sourceUri).executeAsCompletable();

        mCropView.crop(sourceUri)
                .execute(new CropCallback() {
                    @Override
                    public void onSuccess(Bitmap cropped) {
                        mCropView.save(cropped)
                                .execute(saveUri, new SaveCallback() {
                                    @Override
                                    public void onSuccess(Uri uri) {

                                    }

                                    @Override
                                    public void onError(Throwable e) {

                                    }
                                });
                    }

                    @Override
                    public void onError(Throwable e) {
                    }
                });

        mCropView.crop(sourceUri)
                .executeAsSingle()
                .flatMap(new Function<Bitmap, SingleSource<Uri>>() {
                    @Override
                    public SingleSource<Uri> apply(@io.reactivex.annotations.NonNull Bitmap bitmap)
                            throws Exception {
                        return mCropView.save(bitmap)
                                .executeAsSingle(saveUri);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Uri>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull Uri uri) throws Exception {
                        // on success
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull Throwable throwable)
                            throws Exception {
                        // on error
                    }
                });
    }

    private void getIntents() throws NullPointerException {
        strpath = getIntent().getStringExtra("path");
    }

    private void initViews() throws NullPointerException {
        context = this;
        mCropView = findViewById(R.id.cropImageView);
        sentBtn = findViewById(R.id.sentBtn);
        back = findViewById(R.id.back);
    }

    private String getFolderPath() {
        String folderPath = Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/";
        return folderPath;
    }

    public static Uri getUriFromDrawableResId(Context context, int drawableResId) {
        StringBuilder builder = new StringBuilder().append(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .append("://")
                .append(context.getResources().getResourcePackageName(drawableResId))
                .append("/")
                .append(context.getResources().getResourceTypeName(drawableResId))
                .append("/")
                .append(context.getResources().getResourceEntryName(drawableResId));
        return Uri.parse(builder.toString());
    }

    public void startResultActivity(Uri uri) {
        if (isFinishing()) return;
        try {
            if (path_intent != null) {
                Intent i = new Intent();
                i.putExtra("path", path_intent);
                setResult(RESULT_OK, i);
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public Uri createSaveUri() {
        return createNewUri(this, mCompressFormat);
    }

    public static String getDirPath() {
        String dirPath = "";
        File imageDir = null;
        File extStorageDir = Environment.getExternalStorageDirectory();
        if (extStorageDir.canWrite()) {
            imageDir = new File(extStorageDir.getPath() + "/simplecropview");
        }
        if (imageDir != null) {
            if (!imageDir.exists()) {
                imageDir.mkdirs();
            }
            if (imageDir.canWrite()) {
                dirPath = imageDir.getPath();
            }
        }
        return dirPath;
    }

    public static String getFileName() {
        String strFilename = null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyyHHmmss");
            Date date = new Date();
            strFilename = dateFormat.format(date);
        } catch (Exception e) {
            // TODO Auto-generated catch block

            e.printStackTrace();
        }
        return strFilename;
    }


    public Uri createNewUri(Context context, Bitmap.CompressFormat format) {
        long currentTimeMillis = System.currentTimeMillis();
        Date today = new Date(currentTimeMillis);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String title = dateFormat.format(today);
        filename = "Crop"
                + getFileName() + ".jpg";
        path_intent = getFolderPath() + filename;
        File file = new File(path_intent);
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, title);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/" + "jpg");
        values.put(MediaStore.Images.Media.DATA, path_intent);
        long time = currentTimeMillis / 1000;
        values.put(MediaStore.MediaColumns.DATE_ADDED, time);
        values.put(MediaStore.MediaColumns.DATE_MODIFIED, time);
        if (file.exists()) {
            values.put(MediaStore.Images.Media.SIZE, file.length());
        }

        ContentResolver resolver = context.getContentResolver();
        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        return uri;
    }

    private Disposable cropImage() {
        return mCropView.crop(sourceUri)
                .executeAsSingle()
                .flatMap(new Function<Bitmap, SingleSource<Uri>>() {
                    @Override
                    public SingleSource<Uri> apply(@io.reactivex.annotations.NonNull Bitmap bitmap)
                            throws Exception {
                        return mCropView.save(bitmap)
                                .compressFormat(mCompressFormat)
                                .executeAsSingle(createSaveUri());
                    }
                })
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull Disposable disposable)
                            throws Exception {
//                        showProgress();
                    }
                })
                .doFinally(new Action() {
                    @Override
                    public void run() throws Exception {
//                        dismissProgress();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Uri>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull Uri uri) throws Exception {
                        startResultActivity(uri);

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull Throwable throwable)
                            throws Exception {
                    }
                });
    }

}


