package com.example.androidimageeditingtask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ParseException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Edit_Image_Activity extends Activity implements View.OnClickListener {
    private TextView save_btn, cancel;
    private DrawingCanvasVIew drawingView;
    private Context context;
    private float brushSize = 5;
    private Uri SelectedImageUri;
    private String SelectedImagePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editing_activity);
        try {
            initViews();
            getIntents();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getIntents() throws Exception {
        if (getIntent() != null) {
            if (getIntent().hasExtra("selectedImage")) {
                SelectedImagePath = getIntent().getStringExtra("selectedImage");
                setImageUri();
            }
        }
    }

    private void setImageUri() throws NullPointerException, ParseException {
        if (SelectedImagePath != null && !SelectedImagePath.equalsIgnoreCase("")) {
            SelectedImageUri = Uri.parse(SelectedImagePath);
            setImageToDrawingView();
        }
    }

    private void initViews() throws NullPointerException {
        context = this;
        save_btn = findViewById(R.id.save_btn);
        cancel = findViewById(R.id.cancel);
        drawingView = findViewById(R.id.drawing);
        drawingView.setBrushSize(brushSize);
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

    private void setImageToDrawingView() {
        Bitmap board;

        /*String[] filePathColumn = {MediaStore.Images.Media.DATA};
//        Cursor cursor = getContentResolver().query(SelectedImageUri, filePathColumn, null, null, null);
        Cursor cursor = managedQuery(SelectedImageUri, filePathColumn, null, null, null);
        assert cursor != null;
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();*/

        board = BitmapFactory.decodeFile(SelectedImagePath);
        int h = drawingView.getHeight(); // 320; // Height in pixels
        int w = drawingView.getWidth();// 480; // Width in pixels
        board = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(SelectedImagePath), w, h, true);
        drawingView.setImage(board);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.save_btn:
                    if (drawingView.count() > 0) {
                        saveNote();
                    } else {
                        Toast.makeText(getApplicationContext(), "Please draw a sketch", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.cancel:
                    finish();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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


    private String getFolderPath() {
        String folderPath = Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/";
        return folderPath;
    }

    private void saveNote() {
        drawingView.setDrawingCacheEnabled(true);
        File dir = new File(getFolderPath());
        if (!dir.exists()) {
            dir.mkdir();
        }

        String filename = getFolderPath() + "Sketch_file"
                + getFileName() + ".jpg";
        String imgSaved =
                MediaStore.Images.Media.insertImage(
                        getContentResolver(),
                        drawingView.getDrawingCache(), filename, "drawing");
        Bitmap img = drawingView.getDrawingCache();
        if (img != null) {
            BufferedOutputStream stream;
            try {
                stream = new BufferedOutputStream(new FileOutputStream(new File(filename)));
                img.compress(Bitmap.CompressFormat.JPEG, 75, stream);

                imgSaved = "saved";

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        if (imgSaved != null) {

        } else {
            Toast unsavedToast = Toast.makeText(
                    getApplicationContext(),
                    "Oops! Image could not be saved.",
                    Toast.LENGTH_SHORT);
            unsavedToast.show();
        }
        drawingView.destroyDrawingCache();
        try {
            Intent i = new Intent();
            i.putExtra("path", filename);
            setResult(RESULT_OK, i);
            finish();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //}
        //});
        /*saveDialog.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
		saveDialog.show();*/

    }
}
