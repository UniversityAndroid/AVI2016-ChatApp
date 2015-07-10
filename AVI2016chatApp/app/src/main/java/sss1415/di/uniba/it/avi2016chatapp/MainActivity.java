package sss1415.di.uniba.it.avi2016chatapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
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

/**
 * Questa classe java permette di effettuare il login all'applicazione AVI2016chatApp.
 * Il login è permesso solo ai patecipanti della conferenza, in quanto l'applicazione  stata sviluppata per tale evento.
 */

public class MainActivity extends Activity {
    private Button btnJoin;
    private EditText name1;
    private EditText surname1;
    private SharedPreferences memberId;
    //memorizza l'id di registrazione al gcm
    SharedPreferences registerID;

    static HashMap<String, String> membershipList;

    // url to get all memberships list
    private static String url_membership = "http://androidchatapp.altervista.org/chatApp_connect/login.php";
    // url to put the registration id
    //è inviato dal CGM, per permettere le notifiche push
    private static String url_register = "http://androidchatapp.altervista.org/chatApp_connect/register_notification.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MEMBERSHIPS = "memberships";
    private static final String TAG_MID = "codice";
    private static final String TAG_REGID = "regid";

    // memberships JSONArray
    JSONArray memberships = null;
    JSONParser jsonParser = new JSONParser();
    //notifiche con intent
    private static final String BROADCAST = "com.google.android.c2dm.intent.RECEIVE";

    // nella stringa SENDER_ID è inserito il Project Number del proprio progetto Google
    String SENDER_ID = "966704718766";

    GoogleCloudMessaging gcm;
    Context context;
    String regid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerID = getSharedPreferences(TAG_REGID, MODE_PRIVATE);
        context = this;
        gcm = GoogleCloudMessaging.getInstance(this);

        membershipList = new HashMap<>();
        btnJoin = (Button) findViewById(R.id.btnJoin);
        name1 = (EditText) findViewById(R.id.name);
        surname1 = (EditText) findViewById(R.id.surname);
        memberId = getSharedPreferences(TAG_MID, MODE_PRIVATE);
        //per mantenere aperta la sessione di login
        String login = memberId.getString(TAG_MID, null);
        if (login != null) {
            Intent home = new Intent(MainActivity.this, Home.class);
            startActivity(home);
            finish();
        }
        //bottone per la conferma del login
        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //controllo della connessione
                if (isNetworkAvailable()) {
                    String name = name1.getText().toString().trim();
                    String surname = surname1.getText().toString().trim();
                    //verifica che i campi non siano vuoti
                    if (name.length() > 0 && surname.length() > 0) {

                        // Loading memberships in Background Thread
                        new LoadMembership().execute();

                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Please enter your data again", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Please, enable your internet connection", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    /**
     * Richiesta asincrona per il caricamento/lettura dei partecipanti da inserire nella Home page
     */
    class LoadMembership extends AsyncTask<String, String, String> {
        //prende il nome e cognome inseriti dall'utente
        String name = name1.getText().toString().trim();
        String surname = surname1.getText().toString().trim();

        /**
         * getting All products from url
         */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("name", name));
            params.add(new BasicNameValuePair("surname", surname));
            // getting JSON Object
            // POST method
            JSONObject json = jsonParser.makeHttpRequest(url_membership, "POST", params);

            // check log cat fro response
            Log.d("Create Response", json.toString());
            // check for success tag

            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    memberships = json.getJSONArray(TAG_MEMBERSHIPS);
                    JSONObject c = memberships.getJSONObject(0);

                    // Storing each json item in variable 'codice'
                    String codice = c.getString(TAG_MID);
                    // successfully login
                    //sharedPrefences
                    SharedPreferences.Editor preferencesEditor_id = memberId.edit();
                    preferencesEditor_id.putString(TAG_MID, codice);
                    preferencesEditor_id.apply();

                    //metodo per la registrazione al GCM
                    registerInBackground();
                    //notifica push al login dell'utente
                    Intent intent = new Intent();
                    intent.setAction(BROADCAST);
                    intent.putExtra("message", "Welcome " + name + "!");
                    sendBroadcast(intent);

                    //intent alla Home page
                    Intent i = new Intent(getApplicationContext(), Home.class);
                    startActivity(i);
                    // closing this screen
                    finish();
                } else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            //i dati inseriti non sono validi per il login
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

    public void onPause() {
        super.onPause();
        finish();
    }

    /*
    Metodo che controlla lo stato della connessione a Internet
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    /*
    Registrazione al GCM e ricezione dell'id di registrazione
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {

                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    //salvo il regID
                    SharedPreferences.Editor regidId = registerID.edit();
                    regidId.putString(TAG_REGID, regid);
                    regidId.apply();

                } catch (IOException ex) {
                    return null;
                }

                return regid;

            }

            @Override
            protected void onPostExecute(String regid) {
                if (regid != null) {
                    //salva l'id
                    new saveDbData().execute();
                } else
                    Toast.makeText(context, "Errore: registrazione su GCM non riuscita!", Toast.LENGTH_LONG).toString();
            }
        }.execute();
    }


    /**
     * Background Async Task to Create new regid
     */
    class saveDbData extends AsyncTask<String, String, String> {

        protected String doInBackground(String... args) {

            //id del partecipante
            String Id = memberId.getString(TAG_MID, null);
            // id di registrazione al gcm
            String regid = registerID.getString(TAG_REGID, null);
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("Id", Id));
            params.add(new BasicNameValuePair("regid", regid));

            // getting JSON Object
            // POST method
            JSONObject json = jsonParser.makeHttpRequest(url_register,
                    "POST", params);

            // check log cat fro response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    //null
                } else {
                    //null
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
