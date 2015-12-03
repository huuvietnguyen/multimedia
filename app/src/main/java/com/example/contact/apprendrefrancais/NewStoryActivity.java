package com.example.contact.apprendrefrancais;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.contact.apprendrefrancais.FilePath.FilePathActivity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class NewStoryActivity extends Activity {
    String UTF8 = "utf8";
    int BUFFER_SIZE = 8192;
    // Progress Dialog
    private ProgressDialog pDialog;
    String curFileName;
    private static final int REQUEST_PATH_FILE = 1;
    private static final int REQUEST_PATH_TEXT = 2;
    JSONParser jsonParser = new JSONParser();
    EditText inputName;
    EditText inputDesc;
    Button recordStory;
    ImageView micrpRecord;
    MediaPlayer player;
    private RadioGroup radioCategorie;
    private RadioButton radioCategorieChoice;
    private MediaRecorder myRecorder;
    private ImageButton fileDescription;
    private String outputFile = null;
    long totalSize = 0;
    String nameRecord = "";
    private boolean isPressed = false;
    private boolean isRecord = false;

    // url to create new product
    private static String url_create_product = "http://bide.byethost32.com/firstapp/ApprendreFrancais/create_product.php";
    private static String url_create_file = "http://bide.byethost32.com/firstapp/AndroidFileUpload/fileUpload.php";
    // JSON Node names
    private static final String TAG_SUCCESS = "success";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_story);
        nameRecord = generalNameRecord();
        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/"+ nameRecord +".mp3";
        myRecorder = new MediaRecorder();
        myRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        myRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        myRecorder.setOutputFile(outputFile);



        // Edit Text
        fileDescription = (ImageButton) findViewById(R.id.fileDescription);
        micrpRecord = (ImageView) findViewById(R.id.microRecord);
        inputName = (EditText) findViewById(R.id.inputName);
        inputDesc = (EditText) findViewById(R.id.inputDesc);
        recordStory = (Button) findViewById(R.id.record_story);
        fileDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FilePathActivity.class);
                startActivityForResult(intent, REQUEST_PATH_TEXT);
            }
        });
        recordStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPressed == false) {
                    recordStory.setText("Recording ...");
                    Drawable micro= getResources().getDrawable(R.drawable.micro);
                    micrpRecord.setBackgroundDrawable(micro);
                    actionRecord();
                    micrpRecord .setVisibility(View.VISIBLE);
                    isPressed = true;
                    isRecord = true;
                }
                else {
                    recordStory.setText("Record");
                    Drawable cassette= getResources().getDrawable(R.drawable.cassette);
                    micrpRecord.setBackgroundDrawable(cassette);
                    stopRecord();
                    isPressed = false;
                }

            }
        });
        micrpRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPressed == false) {
                    if (isRecord == true) {
                        try {
                            player = new MediaPlayer();
                            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            player.setDataSource(outputFile);
                            player.prepare();
                            player.start();
                        } catch (Exception e) {
                        // TODO: handle exception
                        }
                    }
                    if (isRecord == false) {
                        Intent intent = new Intent(getApplicationContext(), FilePathActivity.class);
                        startActivityForResult(intent, REQUEST_PATH_FILE);
                    }
                }
            }
        });
        radioCategorie = (RadioGroup) findViewById(R.id.radioCategorie);
        // Create button
        Button btnCreateProduct = (Button) findViewById(R.id.btnCreateProduct);

        // button click event
        btnCreateProduct.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // creating new product in background thread
                new CreateNewProduct().execute();
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        // See which child activity is calling us back.
        if (requestCode == REQUEST_PATH_FILE){
            if (resultCode == RESULT_OK) {
                curFileName  = data.getStringExtra("GetFileName");
                outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/"+ curFileName;
                nameRecord = curFileName.substring(0,curFileName.length() - 4) ;
                Log.d("FileName", outputFile);
                Log.d("FileName 1111", nameRecord);
                Drawable acceptfile= getResources().getDrawable(R.drawable.fileaccept1);
                micrpRecord.setBackgroundDrawable(acceptfile);
            }
        }
        if (requestCode == REQUEST_PATH_TEXT) {
            if (resultCode == RESULT_OK) {
                curFileName  = data.getStringExtra("GetFileName");
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/"+ curFileName);
                StringBuilder text = new StringBuilder();

                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));//BufferedReader br = new BufferedReader(new FileReader(file));
                    String line;

                    while ((line = br.readLine()) != null) {
                        text.append(line);
                        text.append('\n');
                    }
                    br.close();
                }
                catch (IOException e) {
                    //You'll need to add proper error handling here
                }
                Drawable acceptfile= getResources().getDrawable(R.drawable.fileaccept);
                fileDescription.setBackgroundDrawable(acceptfile);
                inputDesc.setText(text);
            }
        }
    }

    /**
     * Background Async Task to Create new product
     * */
    class CreateNewProduct extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(NewStoryActivity.this);
            pDialog.setMessage("Creating Product..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }



        /**
         * Creating product
         * */
        protected String doInBackground(String... args) {
            String name = inputName.getText().toString();
            String description = inputDesc.getText().toString();
            Log.d("description" , description);
            int selectedId = radioCategorie.getCheckedRadioButtonId();
            radioCategorieChoice = (RadioButton) findViewById(selectedId);
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("name", name));
            params.add(new BasicNameValuePair("record", nameRecord));
            params.add(new BasicNameValuePair("description", description));
            params.add(new BasicNameValuePair("categorie", radioCategorieChoice.getText().toString()));
            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_create_product,
                    "POST", params);

            // check log cat fro response
            Log.d("Create Response", json.toString());

            // check for success tag


            String responseString = null;

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url_create_file);
            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new AndroidMultiPartEntity.ProgressListener() {


                            @Override
                            public void transferred(long num) {
                                publishProgress(toString().valueOf((num / (float) totalSize) * 100));
                            }
                        });

                File sourceFile = new File(outputFile);
                Log.d("file name thuc su " , outputFile);
                // Adding file data to http body
                entity.addPart("image", new FileBody(sourceFile));

                // Extra parameters if you want to pass to server
                entity.addPart("website",
                        new StringBody("www.androidhive.info"));
                entity.addPart("email", new StringBody("abc@gmail.com"));

                totalSize = entity.getContentLength();
                httpPost.setEntity(entity);

                // Making server call
                HttpResponse response = httpClient.execute(httpPost);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // Server response
                    responseString = EntityUtils.toString(r_entity);
                } else {
                    responseString = "Error occurred! Http Status Code: "
                            + statusCode;
                }
                boolean deleted = sourceFile.delete();
                Log.d("delete",toString().valueOf(deleted));

            } catch (ClientProtocolException e) {
                responseString = e.toString();
            } catch (IOException e) {
                responseString = e.toString();
            }


            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // successfully created product
                    Intent i = new Intent(getApplicationContext(), NewStoryActivity.class);
                    startActivity(i);

                    // closing this screen
                    finish();
                } else {
                    // failed to create product
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return responseString;

        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            pDialog.dismiss();
        }


    }

    public void actionRecord() {
        try {
            myRecorder.prepare();
            myRecorder.start();
        }
        catch (IllegalStateException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecord() {
        try {
            myRecorder.stop();
            myRecorder.release();
            myRecorder = null;
        }
        catch (IllegalStateException e) {
            e.printStackTrace();
        }
        catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    private static String generalNameRecord() {
        String id;
        Calendar cal = Calendar.getInstance();
        id = "LOCAL-" + cal.get(Calendar.YEAR) + cal.get(Calendar.MONTH) + cal.get(Calendar.DATE) + cal.get(Calendar.HOUR) + cal.get(Calendar.MINUTE) + cal.get(Calendar.SECOND);
        return  id;
    }
}