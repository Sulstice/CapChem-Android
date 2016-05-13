package com.example.zola.capchem;

import android.nfc.Tag;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Zola on 4/16/2016.
 */

interface OnServerRequestCompleteListener {
    void onServerRequestComplete(String response);
    void onErrorOccured(String errorMessage);
}
public class CommsEngine {

    private final String apikey = "8ddca276-13f8-46b1-b254-cb9b744850f3";

    HttpClient httpClient = null;
    HttpGet httpGet = null;
    HttpPost httpPost = null;

    // State our methods of GET and POST
    public enum HTTP_METHOD {GET, POST};
    public HTTP_METHOD httpMethod = HTTP_METHOD.GET;

    OnServerRequestCompleteListener mListener;
    public CommsEngine() {

        //Establish a connection if there is none
        if (httpClient == null) {
            HttpParams httpParameters = new BasicHttpParams();
            int timeoutConnection = 20000;
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            httpClient = new DefaultHttpClient();
        }
        if (httpGet == null)
            httpGet = new HttpGet();
        if (httpPost == null)
            httpPost = new HttpPost();
    }

    //Get for the request
    public void ServiceGetRequest(String serviceType, String param, OnServerRequestCompleteListener listener) {
        mListener = listener;
        httpMethod = HTTP_METHOD.GET;
        new MakeAsyncActivitiesTask().execute(serviceType, param);
    }

    //Post the request
    public void ServicePostRequest(String serviceType, String fileType, Map<String, String> param,OnServerRequestCompleteListener listener ) {
        mListener = listener;
        httpMethod = HTTP_METHOD.POST;
        new MakeAsyncActivitiesTask().execute(serviceType, fileType, param);
    }

    private void ParseResponse(String response) {
        mListener.onServerRequestComplete(response);
    }

    class MakeAsyncActivitiesTask extends AsyncTask<Object, String, String> {

        @Override
        protected String doInBackground(Object... params)
        {
            String url = "";
            URI uri;
            if (httpMethod == HTTP_METHOD.GET) {
                url = params[0] + "apikey=" + apikey;
                url += (String) params[1];
                try {
                    uri = new URI(url);
                    httpGet.setURI(uri);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    mListener.onErrorOccured(e.getMessage());
                }

                try {
                    HttpResponse response = httpClient.execute(httpGet);
                    StatusLine statusLine = response.getStatusLine();
                    if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                        HttpEntity entity = response.getEntity();
                        String responseStr = EntityUtils.toString(entity);
                        if (responseStr != null)
                            return responseStr;
                    } else {
                        mListener.onErrorOccured("Error!!!");
                    }
                } catch (IOException e) {
                    mListener.onErrorOccured(e.getMessage());
                }
            }

            else if (httpMethod == HTTP_METHOD.POST) {
                try {
                    url = (String) params[0];
                    uri = new URI(url);
                    httpPost.setURI(uri);
                    MultipartEntityBuilder reqEntity = MultipartEntityBuilder.create();

                    reqEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                    Map<String,String> map = (Map) params[2];
                    reqEntity.addPart("apikey", new StringBody(apikey, ContentType.TEXT_PLAIN));
                    for (Map.Entry<String, String> e : map.entrySet()) {
                        String key = e.getKey();
                        String value = e.getValue();
                        if (key.equals("file")) {
                            ContentType type = ContentType.create((String) params[1], Consts.ISO_8859_1);
                            reqEntity.addBinaryBody("file", new File(value), type, "");
                        } else
                            reqEntity.addPart(key, new StringBody(value, ContentType.TEXT_PLAIN));

                    }
                    httpPost.setEntity(reqEntity.build());

                    } catch (URISyntaxException e) {

                            mListener.onErrorOccured(e.getMessage());
                    }

                try {
                    HttpResponse response = httpClient.execute(httpPost);
                    StatusLine statusLine = response.getStatusLine();
                    if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                        HttpEntity entities = response.getEntity();
                        String responseStr = EntityUtils.toString(entities);
                        if (responseStr != null)
                            return responseStr;
                        else
                            mListener.onErrorOccured("Error!!!");
                    } else {
                        mListener.onErrorOccured("Error!!!");
                    }
                } catch (IOException e) {
                    mListener.onErrorOccured(e.getMessage());

                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... unused) {

        }

        @Override
        protected void  onPostExecute(String sResponse) {
            ParseResponse(sResponse);
        }
    }
}
