package com.example.zola.capchem;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.parse.ParseUser;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity{

    static final int SELECT_PICTURE = 1;
    static final int TAKE_PICTURE = 2;
    private String mImageFullPathAndName = "";
    private String localImagePath = "";
    private static final int OPTIMIZED_LENGTH = 1024;
    public static String compoundData;

    private final String idol_ocr_service = "https://api.idolondemand.com/1/api/async/ocrdocument/v1?";
    private final String idol_ocr_job_result = "https://api.idolondemand.com/1/job/result/";
    private String jobID = "";

    CommsEngine commsEngine;

    ImageView ivSelectedImg;
    FrameLayout llResultContainer;
    LinearLayout llCameraButtons;
    LinearLayout llOperations;
    ProgressBar pbOCRRecognizing;
    EditText edTextResult;
    Button logOutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivSelectedImg = (ImageView) findViewById(R.id.imageView);
        llCameraButtons = (LinearLayout) findViewById(R.id.llcamerabuttons);
        llOperations = (LinearLayout) findViewById(R.id.lloptions);

        edTextResult = (EditText) findViewById(R.id.etresult);
        llResultContainer = (FrameLayout) findViewById(R.id.llresultscontainer);
        pbOCRRecognizing = (ProgressBar) findViewById(R.id.pbocrrecognizing);

        CreateLocalImageFolder();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (commsEngine == null) {
            commsEngine = new CommsEngine();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void DoCloseApp(View v) {
        ParseUser.getCurrentUser().logOut();
        startActivity(new Intent(MainActivity.this, DispatchActivity.class));
    }

    @Override
    public void onBackPressed() {
        if (llResultContainer.getVisibility() == View.VISIBLE) {
            llResultContainer.setVisibility(View.GONE);
            ivSelectedImg.setVisibility(View.VISIBLE);
            return;
        } else
            finish();
    }

    public void DoStartOCR(View v) {
        pbOCRRecognizing.setVisibility(View.VISIBLE);
        if (jobID.length() > 0)
            getResultByJobId();
        else if (!mImageFullPathAndName.isEmpty()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("file", mImageFullPathAndName);
            String fileType = "image/jpeg";
            map.put("mode", "document_photo");
            commsEngine.ServicePostRequest(idol_ocr_service, fileType, map, new OnServerRequestCompleteListener() {
                @Override
                public void onServerRequestComplete(String response) {
                    try {
                        JSONObject mainObject = new JSONObject(response);
                        if (!mainObject.isNull("jobID")) {
                            jobID = mainObject.getString("jobID");
                            getResultByJobId();
                        } else
                            ParseSyncResponse(response);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onErrorOccured(String errorMessage) {
                }
            });
        } else {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_LONG).show();
        }


    }

    private void getResultByJobId() {
        String param = idol_ocr_job_result + jobID + "?";
        commsEngine.ServiceGetRequest(param, "", new OnServerRequestCompleteListener() {
            @Override
            public void onServerRequestComplete(String response) {
                ParseAsyncResponse(response);
            }

            @Override
            public void onErrorOccured(String errorMessage) {

            }
        });
    }

    public void DoCloseResult(View v) {
        ivSelectedImg.setVisibility(View.VISIBLE);
        llResultContainer.setVisibility(View.GONE);
    }

    private void ParseSyncResponse(String response) {
        pbOCRRecognizing.setVisibility(View.GONE);
        if (response == null) {
            Toast.makeText(this, "Error!!!!", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            JSONObject mainObject = new JSONObject(response);
            JSONArray textBlockArray = mainObject.getJSONArray("text_block");
            int count = textBlockArray.length();
            if (count > 0) {
                for (int i = 0; i < count; i++) {
                    JSONObject texts = textBlockArray.getJSONObject(i);
                    String text = texts.getString("text");
                    ivSelectedImg.setVisibility(View.GONE);
                    llResultContainer.setVisibility(View.VISIBLE);
                    edTextResult.setText(text);
                    Log.d("WORKING", text);
                    compoundData = text;
                }
            } else
                Toast.makeText(this, "Nope", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ParseAsyncResponse(String response) {
        pbOCRRecognizing.setVisibility(View.GONE);
        if (response == null) {
            Toast.makeText(this, "Unknown Error", Toast.LENGTH_LONG).show();
        }

        try {
            JSONObject mainObject = new JSONObject(response);
            JSONArray textBlockArray = mainObject.getJSONArray("actions");
            int count = textBlockArray.length();
            if (count > 0) {
                for (int i = 0; i < count; i++) {
                    JSONObject actions = textBlockArray.getJSONObject(i);
                    String action = actions.getString("action");
                    String status = actions.getString("status");
                    JSONObject result = actions.getJSONObject("result");
                    JSONArray textArray = result.getJSONArray("text_block");
                    count = textArray.length();
                    if (count > 0) {
                        for (int n = 0; n < count; n++) {
                            JSONObject texts = textArray.getJSONObject(n);
                            String text = texts.getString("text");
                            ivSelectedImg.setVisibility(View.GONE);
                            llResultContainer.setVisibility(View.VISIBLE);
                            edTextResult.setText(text);
                            Log.d("WORKING", text);
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Error!!!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void CreateLocalImageFolder() {

        if (localImagePath.length() == 0) {
            localImagePath = getFilesDir().getAbsolutePath() + "/orc/";
            File folder = new File(localImagePath);
            boolean success = true;
            if (!folder.exists()) {
                success = folder.mkdir();
            }
            if (!success)
                Toast.makeText(this, "Cannot create the folder yo", Toast.LENGTH_LONG).show();
        }
    }

    public Bitmap decodeFile(File file) {
        Toast.makeText(this, "I HEAR YOU", Toast.LENGTH_LONG).show();
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = 1;
        int mImageRealWidth = options.outWidth;
        int mImageRealHeight = options.outHeight;
        Bitmap pic = null;
        try {
            pic = BitmapFactory.decodeFile(file.getPath(), options);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pic;
    }

    public Bitmap rescaleBitmap(Bitmap bm, int newWidth, int newHeight) {
        Toast.makeText(this, "I HEAR YOU", Toast.LENGTH_LONG).show();
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    private Bitmap rotateBitmap(Bitmap pic, int deg) {
        Toast.makeText(this, "I HEAR YOU", Toast.LENGTH_LONG).show();
        Matrix rotate90DegAntiClock = new Matrix();
        rotate90DegAntiClock.preRotate(deg);
        Bitmap newPic = Bitmap.createBitmap(pic, 0, 0, pic.getWidth(), pic.getHeight(), rotate90DegAntiClock, true);
        return newPic;
    }

    private String SaveImage(Bitmap image) {
        String fileName = localImagePath + "imagetoocr.jpg";
        try {
            File file = new File(fileName);
            FileOutputStream fileStream = new FileOutputStream(file);

            image.compress(Bitmap.CompressFormat.JPEG, 100, fileStream);
            try {
                fileStream.flush();
                fileStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return fileName;
    }

    public void DoTakePhoto(View v) {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivityForResult(intent, TAKE_PICTURE);
    }

    public void DoShowSelectImage(View v) {
        Intent i = new Intent(
                Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, SELECT_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && null != data) {
            Bitmap mCurrentSelectedBitmap;
            if (requestCode == SELECT_PICTURE && null != data) {
                Uri selectedimage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedimage, filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                mImageFullPathAndName = cursor.getString(columnIndex);
                cursor.close();
                jobID = "";
                File file = new File(mImageFullPathAndName);
                mCurrentSelectedBitmap = decodeFile(file);

                if (mCurrentSelectedBitmap != null) {

                    ivSelectedImg.setImageBitmap(mCurrentSelectedBitmap);


                    int w = mCurrentSelectedBitmap.getWidth();
                    int h = mCurrentSelectedBitmap.getHeight();

                    int length = (w > h) ? w : h;
                    if (length > OPTIMIZED_LENGTH) {
                        float ratio = (float) w / h;
                        int newW, newH = 0;

                        if (ratio > 1.0) {
                            newW = OPTIMIZED_LENGTH;
                            newH = (int) (OPTIMIZED_LENGTH / ratio);
                        } else {
                            newH = OPTIMIZED_LENGTH;
                            newW = (int) (OPTIMIZED_LENGTH * ratio);
                        }
                        mCurrentSelectedBitmap = rescaleBitmap(mCurrentSelectedBitmap, newW, newH);
                    }

                    mImageFullPathAndName = SaveImage(mCurrentSelectedBitmap);

                }
            } else if (requestCode == TAKE_PICTURE) {
                Bundle extras = data.getExtras();
                mCurrentSelectedBitmap = (Bitmap) extras.get("data");
                if (mCurrentSelectedBitmap != null) {

                    ivSelectedImg.setImageBitmap(mCurrentSelectedBitmap);


                    int w = mCurrentSelectedBitmap.getWidth();
                    int h = mCurrentSelectedBitmap.getHeight();

                    int length = (w > h) ? w : h;
                    if (length > OPTIMIZED_LENGTH) {
                        float ratio = (float) w / h;
                        int newW, newH = 0;

                        if (ratio > 1.0) {
                            newW = OPTIMIZED_LENGTH;
                            newH = (int) (OPTIMIZED_LENGTH / ratio);
                        } else {
                            newH = OPTIMIZED_LENGTH;
                            newW = (int) (OPTIMIZED_LENGTH * ratio);
                        }
                        mCurrentSelectedBitmap = rescaleBitmap(mCurrentSelectedBitmap, newW, newH);
                    }

                    mImageFullPathAndName = SaveImage(mCurrentSelectedBitmap);
                }
            }

        }

    }

    public void setCompoundData(String compoundData) {this.compoundData = compoundData;}
    public String getCompoundData() { return compoundData;}

}

