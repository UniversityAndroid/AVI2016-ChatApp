package sss1415.di.uniba.it.avi2016chatapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SyncStatusObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.Button;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends Activity {
    private Button btnJoin;
    private EditText name1;
    private EditText surname1;
    private SharedPreferences memberId;
    SharedPreferences registerID;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    static HashMap<String, String> membershipList;

    // url to get all memberships list
    private static String url_membership = "http://androidchatapp.altervista.org/chatApp_connect/login.php";
    private static String url_register = "http://androidchatapp.altervista.org/chatApp_connect/registrer_notification.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MEMBERSHIPS = "memberships";
    private static final String TAG_MID = "codice";

    // products JSONArray
    JSONArray memberships = null;
    JSONParser jsonParser = new JSONParser();
    //notifiche
    private static final String BROADCAST = "com.google.android.c2dm.intent.RECEIVE";

    // inserire l'url della pagina PHP
    private static final String BACKEND_URL="http://androidchatapp.altervista.org/chatApp_connect/GCM.php";

    // nella stringa SENDER_ID inserire il Project Number del proprio progetto Google
    String SENDER_ID = "920038187319";

    GoogleCloudMessaging gcm;
    Context context;
    String regid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerID = getSharedPreferences("regid", MODE_PRIVATE);
        context = this;
        gcm = GoogleCloudMessaging.getInstance(this);

        membershipList = new HashMap<>();
        btnJoin = (Button)findViewById(R.id.btnJoin);
        name1 = (EditText) findViewById(R.id.name);
        surname1 = (EditText)findViewById(R.id.surname);
        memberId = getSharedPreferences(TAG_MID, MODE_PRIVATE);
        //per mantenere aperta la sessione di login
        String login = memberId.getString(TAG_MID, null);
        if(login != null){
            Intent home = new Intent(MainActivity.this, Home.class);
            startActivity(home);
            finish();
        }

        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //controllo cnnessione
                if (isNetworkAvailable()){
                    String name = name1.getText().toString().trim();
                String surname = surname1.getText().toString().trim();
                if (name.length() > 0 && surname.length() > 0) {

                    // Loading products in Background Thread
                    new LoadMembership().execute();

                } else {
                    Toast.makeText(getApplicationContext(),
                            "Please enter your data again", Toast.LENGTH_LONG).show();
                }
            }else{
                    Toast.makeText(getApplicationContext(),
                            "Please, enable your internet connection", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    class LoadMembership extends AsyncTask<String, String, String> {
        String name = name1.getText().toString().trim();
        String surname = surname1.getText().toString().trim();
        /**
         * getting All products from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("name", name));
            params.add(new BasicNameValuePair("surname", surname));
            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_membership, "POST", params);

            // check log cat fro response
                Log.d("Create Response", json.toString());
            // check for success tag

            try {
                int success = json.getInt(TAG_SUCCESS);

                    if (success == 1) {
                        memberships = json.getJSONArray(TAG_MEMBERSHIPS);
                        JSONObject c = memberships.getJSONObject(0);

                        // Storing each json item in variable
                        String codice = c.getString(TAG_MID);
                        // successfully login
                        //sharedPrefences
                        SharedPreferences.Editor preferencesEditor_id = memberId.edit();
                        preferencesEditor_id.putString(TAG_MID, codice);
                        preferencesEditor_id.apply();

                        registerInBackground();
                        Intent intent = new Intent();
                        intent.setAction(BROADCAST);
                        intent.putExtra("ciao", "ciao");
                        sendBroadcast(intent);

                        Intent i = new Intent(getApplicationContext(), Home.class);
                        startActivity(i);
                        // closing this screen
                        finish();
                    }else {
                        runOnUiThread(new Runnable() {
                            public void run() {

                                Toast.makeText(MainActivity.this, "Please enter again your data", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }


            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;

        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void onPause(){
        super.onPause();
        finish();
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    private void registerInBackground()
    {
        new AsyncTask<Void, Void, String>()
        {

            @Override
            protected String doInBackground(Void... params)
            {

                try {
                    if (gcm == null)
                    {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    //salvo regID
                    SharedPreferences.Editor regidId = registerID.edit();
                    regidId.putString("regid", regid);
                    regidId.apply();

                }
                catch (IOException ex)
                {
                    return null;
                }

                return regid;

            }

            @Override
            protected void onPostExecute(String regid)
            {
                if (regid!=null) {
                    sendIDToApplication(regid);
                    new saveDbData().execute();
                }
                else
                    Toast.makeText(context, "Errore: registrazione su GCM non riuscita!", Toast.LENGTH_LONG).toString();
            }
        }.execute();
    }


    private void sendIDToApplication(String regid)
    {
        new AsyncTask<String, Void, Void>()
        {
            @Override
            protected Void doInBackground(String... params)
            {
                String regid=params[0];
                HttpClient client=new DefaultHttpClient();
                HttpPost request=new HttpPost(BACKEND_URL);
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("regid", regid));
                try {
                    request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response=client.execute(request);
                    int status=response.getStatusLine().getStatusCode();
                } catch (UnsupportedEncodingException e) {
                    return null;
                } catch (ClientProtocolException e) {
                    return null;
                } catch (IOException e) {
                    return null;
                }

                return null;
            }
        }.execute(regid);
    }

    /**
     * Background Async Task to Create new regid
     * */
    class saveDbData extends AsyncTask<String, String, String> {

        /**
         * Creating group
         */
        protected String doInBackground(String... args) {

            Map<String, ?> entry_codice = memberId.getAll();
            final String[] codice = new String[entry_codice.size()];
            int i = 0;

            for(Map.Entry<String, ?> entryeach : entry_codice.entrySet()) {
                codice[i] = (String) entryeach.getValue();
                i++;
            }

            Map<String, ?> entry_codiceR = registerID.getAll();
            final String[] codiceR = new String[entry_codiceR.size()];
            int j = 0;

            for(Map.Entry<String, ?> entryeach : entry_codiceR.entrySet()) {
                codiceR[j] = (String) entryeach.getValue();
                j++;
            }
           String Id = codice[0];
           String regid = codiceR[0];
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("Id", Id));
            params.add(new BasicNameValuePair("regid", regid));

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_register,
                    "POST", params);

            // check log cat fro response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    System.out.println("Andatooooo");
                } else {
                    System.out.println("Non andatooooo");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
