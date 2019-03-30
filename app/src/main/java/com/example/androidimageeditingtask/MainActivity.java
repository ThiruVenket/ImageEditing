package com.example.androidimageeditingtask;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity implements View.OnClickListener {
    private TextView selectImage, editImage, cropImage;
    private ImageView imageView;
    private Context context;
    private static Uri capturedImageUri = null;
    private String strIPath = "";
    private static final int CAMERA_RESULT = 200, GALLERY_BELOW_19 = 41, GALLERY_ABOVE_19 = 40, EDITIMAGE = 201;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            initViews();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (capturedImageUri != null) {
            outState.putString("capturedImageUri", capturedImageUri.toString());
        }
        if (strIPath != null && !strIPath.equalsIgnoreCase("")) {
            outState.putString("strIPath", strIPath);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        try {
            if (savedInstanceState.getString("capturedImageUri") != null) {
                capturedImageUri = Uri.parse(savedInstanceState.getString("capturedImageUri"));
                setImage(capturedImageUri);
            }
            if (savedInstanceState.getString("strIPath") != null) {
                strIPath = savedInstanceState.getString("strIPath");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setImage(Uri capturedImageUri) throws Exception {
        try {
            if (capturedImageUri != null) {
                this.capturedImageUri = capturedImageUri;
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), capturedImageUri);
                imageView.setImageBitmap(bitmap);
                editImage.setVisibility(View.VISIBLE);
                cropImage.setVisibility(View.VISIBLE);
            } else {
                cropImage.setVisibility(View.GONE);
                editImage.setVisibility(View.GONE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    private void initViews() throws NullPointerException {
        context = this;
        selectImage = findViewById(R.id.selectImage);
        editImage = findViewById(R.id.editImage);
        cropImage = findViewById(R.id.cropImage);
        imageView = findViewById(R.id.imageView);
        selectImage.setOnClickListener(this);
        editImage.setOnClickListener(this);
        cropImage.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.selectImage:
                    showOptions();
                    break;
                case R.id.editImage:
                    callEditImageActivity();
                    break;
                case R.id.cropImage:
                    callCropImageActivity();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callEditImageActivity() {
        Intent intent = new Intent(context, Edit_Image_Activity.class);
        intent.putExtra("selectedImage", strIPath);
        startActivityForResult(intent, EDITIMAGE);
    }

    private void callCropImageActivity() {
        Intent intent = new Intent(context, Crop_Image_Activity.class);
        intent.putExtra("path", strIPath);
        startActivityForResult(intent, EDITIMAGE);
    }

    private void showOptions() throws Exception {
        try {
            final Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.image_selection_layou);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.horizontalMargin = 15;
            Window window = dialog.getWindow();
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setAttributes(lp);
            window.setGravity(Gravity.BOTTOM);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            TextView cancel1 = dialog.findViewById(R.id.cancel);
            TextView gallerySelection = dialog.findViewById(R.id.gallerySelection);
            TextView cameraSelection = dialog.findViewById(R.id.cameraSelection);
            cancel1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    dialog.dismiss();
                }
            });
            gallerySelection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    dialog.dismiss();
                    if (Build.VERSION.SDK_INT < 19) {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        startActivityForResult(intent, GALLERY_ABOVE_19);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("image/*");
                        startActivityForResult(intent, GALLERY_BELOW_19);

                    }
                }
            });
            cameraSelection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    dialog.dismiss();

                    final String path = Environment.getExternalStorageDirectory() + "/" + context.getResources().getString(R.string.app_name) + "/";
                    File directory = new File(path);
                    if (!directory.exists()) {
                        directory.mkdir();
                    }
                    strIPath = path + getFileName() + ".jpg";
                    File file = new File(strIPath);
                    if (!file.exists()) {
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    capturedImageUri = Uri.fromFile(file);
                    Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    i.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
                    startActivityForResult(i, CAMERA_RESULT);
                }
            });
        } catch (
                Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private String getFileName() {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == RESULT_OK) {
                switch (requestCode) {
                    case CAMERA_RESULT:
                        setImage(capturedImageUri);
                        break;
                    case GALLERY_BELOW_19:
                    case GALLERY_ABOVE_19:
                        if (data != null) {
                            String realPath;
                            if (Build.VERSION.SDK_INT < 11) {
                                realPath = RealPathUtil.getRealPathFromURI_BelowAPI11(this, data.getData());
                            } else if (Build.VERSION.SDK_INT < 19) {
                                realPath = RealPathUtil.getRealPathFromURI_API11to18(this, data.getData());
                            } else {
                                realPath = RealPathUtil.getRealPathFromURI_API19(this, data.getData());
                            }
                            if (data.getData() != null) {
                                final String path1 = Environment.getExternalStorageDirectory() + "/" + context.getResources().getString(R.string.app_name) + "/";
                                File directory = new File(path1);
                                if (!directory.exists()) {
                                    directory.mkdir();
                                }
                                Uri selectedImageUri = data.getData();
                                if (selectedImageUri != null) {
                                    final String path = Environment.getExternalStorageDirectory() + "/" + context.getResources().getString(R.string.app_name) + "/" + getFileName() + ".jpg";
                                    strIPath = getRealPathFromURI(selectedImageUri);
                                    Bitmap img;
                                    img = convertpathToBitmap(strIPath);
                                    if (img != null) {
                                        try {
                                            FileOutputStream out = new FileOutputStream(path);
                                            img.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                                            // PNG is a lossless format, the compression factor (100) is ignored
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        capturedImageUri = Uri.parse(path);
                                        strIPath = path;
                                    }
                                }
                                capturedImageUri = data.getData();
                                setImage(capturedImageUri);
                            }
                        }
                        break;
                    case EDITIMAGE:
                        if (data.getData() != null) {
                            if (data.hasExtra("path")) {
                                strIPath = data.getStringExtra("path");
                                capturedImageUri = Uri.parse(strIPath);
                                setImage(capturedImageUri);
                            }
                        }
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Bitmap convertpathToBitmap(String strIPath) throws Exception {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        return BitmapFactory.decodeFile((compressImage(strIPath)), bmOptions);
    }

    public String compressImage(String imageUri) {
        Bitmap scaledBitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(imageUri, options);
        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;
//      max Height and width values of the compressed image is taken as 816x612
        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;
//      width and height values are set maintaining the aspect ratio of the image
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;
            }
        }
        //      setting inSampleSize value allows to load a scaled down version of the original image
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;
//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];
        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(imageUri, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }
        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);
        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));
//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(imageUri);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out;
        try {
            out = new FileOutputStream(imageUri);
//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return imageUri;
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }
        return inSampleSize;
    }

    private String getRealPathFromURI(Uri contentUri) {
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = context.getContentResolver().query(contentUri,
                    proj, null, null, null);
            int column_index = cursor != null ? cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA) : 0;
            cursor.moveToFirst();
            String value = cursor.getString(column_index);
            cursor.close();
            return value;
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

    }
}
