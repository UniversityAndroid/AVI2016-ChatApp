package sss1415.di.uniba.it.avi2016chatapp;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.Toast;

import com.gc.materialdesign.views.Button;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Questa classe java gestisce l'invio e la ricezione dei messaggi ai membri iscritti ad un
 * determinato gruppo tematico.
 */
public class GroupChat extends ListActivity {

    // Server url
    private static String url_send_message_group = "http://androidchatapp.altervista.org/chatApp_connect/send_message_group.php";
    private static String url_read_message_group = "http://androidchatapp.altervista.org/chatApp_connect/read_message_group.php";
    private static String url_subscrition = "http://androidchatapp.altervista.org/chatApp_connect/send_subscrition.php";
    private static String url_isSubscribe = "http://androidchatapp.altervista.org/chatApp_connect/isSubscribe.php";
    private static String url_remove_subscribe = "http://androidchatapp.altervista.org/chatApp_connect/remove_subscribe.php";
    private static String url_notification = "http://androidchatapp.altervista.org/chatApp_connect/group_notification.php";

    ArrayList<HashMap<String, String>> groupMessageList;

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    JSONParser jsonParser = new JSONParser();

    private Button btnSend;
    private EditText inputMsg;
    private Switch switch2;

    //per l'invio del messaggio
    SharedPreferences memberId;
    private static final String TAG_MID = "codice";
    private static final String TAG_GID = "codiceGruppo";
    //per la lettura e visualizzazione dei messaggi
    private static final String TAG_MESSAGE1 = "testoMessaggio";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_MITTENTE = "codiceMittente";
    private static final String TAG_GRUPPO = "idGruppo";
    private static final String TAG_NOME = "nome";
    private static final String TAG_COGNOME = "cognome";

    JSONArray groupMessages = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        btnSend = (Button) findViewById(R.id.btnSend);
        inputMsg = (EditText) findViewById(R.id.inputMsg);
        switch2 = (Switch) findViewById(R.id.switch1);


        memberId = getSharedPreferences(TAG_MID, MODE_PRIVATE);
        //deve ricevere anche id del gruppo
        //per conversazione
        groupMessageList = new ArrayList<HashMap<String, String>>();
        //controlla la connessione
        if (isNetworkAvailable()) {
            //controlla l'effettiva iscrizione al gruppo dell'utente
            new isSubscribe().execute();
            //switch che permette l'iscrizione ad un gruppo tematico o la cancellazione dell'iscrizione
            switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (switch2.isChecked()) {
                        btnSend.setVisibility(View.VISIBLE);
                        inputMsg.setVisibility(View.VISIBLE);
                        new LoadMessages().execute();
                        new Subscribe().execute();

                    } else if (!switch2.isChecked()) {
                        btnSend.setVisibility(View.INVISIBLE);
                        inputMsg.setVisibility(View.INVISIBLE);
                        new RemoveSubscribe().execute();
                    }
                }
            });
            //bottone per l'invio di un nuovo messaggio
            btnSend.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    if (inputMsg.getText().toString().length() > 0) {
                        // Hashmap for ListView

                        //invio di un nuovo messaggio di gruppo
                        new SendMessage().execute();
                        groupMessageList = new ArrayList<HashMap<String, String>>();
                        //lettura dei messaggi relativi al gruppo
                        new LoadMessages().execute();
                        // Clearing the input filed once message was sent
                        inputMsg.setText("");
                    }

                }
            });
        } else {
            Toast.makeText(getApplicationContext(),
                    "Please, enable your internet connection", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Background Async Task to send new group message
     */
    class SendMessage extends AsyncTask<String, String, String> {

        String idGruppo = getIntent().getExtras().getString(TAG_GID);

        /**
         * Send group message
         */
        protected String doInBackground(String... args) {
            // id mittente
            Map<String, ?> entry_codice = memberId.getAll();
            final String[] codice = new String[entry_codice.size()];
            int i = 0;

            for (Map.Entry<String, ?> entryeach : entry_codice.entrySet()) {
                codice[i] = (String) entryeach.getValue();
                i++;
            }
            // messaggio di testo
            String messageText = inputMsg.getText().toString();
            String idMittente = codice[0];

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("idMittente", idMittente));
            params.add(new BasicNameValuePair("messageText", messageText));
            params.add(new BasicNameValuePair("idGruppo", idGruppo));

            // getting JSON Object
            // it accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_send_message_group,
                    "POST", params);

            // check log cat fro response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // notifica del messaggio ai componenti del gruppo
                    new messageNotification().execute();

                } else {

                    runOnUiThread(new Runnable() {
                        public void run() {

                            Toast.makeText(GroupChat.this, "Message not send!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    class LoadMessages extends AsyncTask<String, String, String> {

        // id del gruppo tematico
        String idGruppo = getIntent().getExtras().getString(TAG_GID);

        /**
         * Creating group
         */
        protected String doInBackground(String... args) {

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("idGruppo", idGruppo));
            //lettura dei messaggi
            JSONObject json = jsonParser.makeHttpRequest(url_read_message_group, "POST", params);

            // check log cat fro response
            Log.d("Message detail: ", json.toString());
            // check for success tag
            try {
                // Checking for SUCCESS TAG
                int success1 = json.getInt(TAG_SUCCESS);

                if (success1 == 1) {
                    // messages found
                    // Getting Array of messages
                    groupMessages = json.getJSONArray(TAG_MESSAGE);

                    // looping through All messages
                    for (int j = 0; j < groupMessages.length(); j++) {
                        JSONObject c = groupMessages.getJSONObject(j);

                        // Storing each json item in variable
                        String mittente = c.getString(TAG_MITTENTE);
                        String messaggio = c.getString(TAG_MESSAGE1);
                        String gruppo = c.getString(TAG_GRUPPO);
                        String nome = c.getString(TAG_NOME);
                        String cognome = c.getString(TAG_COGNOME);

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_MITTENTE, mittente);
                        map.put(TAG_MESSAGE1, messaggio);
                        map.put(TAG_GRUPPO, gruppo);
                        map.put(TAG_NOME, nome);
                        map.put(TAG_COGNOME, cognome);

                        groupMessageList.add(map);
                    }
                } else {

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }


        protected void onPostExecute(String file_url) {
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    // String mittenteMessaggio = messageList.get(idMittente);
                    ListAdapter adapter = new SimpleAdapter(
                            GroupChat.this, groupMessageList,
                            R.layout.list_item_message_right, new String[]{TAG_NOME,TAG_COGNOME,
                            TAG_MESSAGE1},
                            new int[]{R.id.lblMsgFrom, R.id.surname, R.id.txtMsg});
                    // updating listview
                    setListAdapter(adapter);
                }
            });

        }
    }

    /*
    Effettua l'iscrizione di un utente al gruppo tematico
     */
    class Subscribe extends AsyncTask<String, String, String> {

        protected String doInBackground(String... args) {

            Map<String, ?> entry_codice = memberId.getAll();
            final String[] codice = new String[entry_codice.size()];
            int i = 0;

            for (Map.Entry<String, ?> entryeach : entry_codice.entrySet()) {
                codice[i] = (String) entryeach.getValue();
                i++;
            }
            String idMittente = codice[0];
            String idGruppo = getIntent().getExtras().getString(TAG_GID);

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("idGruppo", idGruppo));
            params.add(new BasicNameValuePair("idPartecipante", idMittente));
            // getting JSON Object
            // it accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_subscrition,
                    "POST", params);

            // check log cat fro response
            Log.d("Subscribe Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {

                } else {
                    // failed to create product
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    /*
    Controlla che l'utente sia iscritto al gruppo all'apertura della conversazione di gruppo
     */
    class isSubscribe extends AsyncTask<String, String, String> {

        protected String doInBackground(String... args) {

            Map<String, ?> entry_codice = memberId.getAll();
            final String[] codice = new String[entry_codice.size()];
            int i = 0;

            for (Map.Entry<String, ?> entryeach : entry_codice.entrySet()) {
                codice[i] = (String) entryeach.getValue();
                i++;
            }
            String idMittente = codice[0];
            String idGruppo = getIntent().getExtras().getString(TAG_GID);
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("idGruppo", idGruppo));
            params.add(new BasicNameValuePair("idPartecipante", idMittente));
            // getting JSON Object
            // accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_isSubscribe, "POST", params);

            // check log cat fro response
            Log.d("IsSubscribe Response", json.toString());
            // check for success tag

            try {
                //se la switch è abilitata rende visibile i messaggi
                // altrimenti disabilita la visibilità
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            switch2.setChecked(true);
                            btnSend.setVisibility(View.VISIBLE);
                            inputMsg.setVisibility(View.VISIBLE);
                            new LoadMessages().execute();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            switch2.setChecked(false);
                            btnSend.setVisibility(View.INVISIBLE);
                            inputMsg.setVisibility(View.INVISIBLE);
                        }
                    });
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;

        }

    }

    /*
    Rimuove l'iscrizione da un gruppo tematico
     */
    class RemoveSubscribe extends AsyncTask<String, String, String> {

        protected String doInBackground(String... args) {

            Map<String, ?> entry_codice = memberId.getAll();
            final String[] codice = new String[entry_codice.size()];
            int i = 0;

            for (Map.Entry<String, ?> entryeach : entry_codice.entrySet()) {
                codice[i] = (String) entryeach.getValue();
                i++;
            }
            String idMittente = codice[0];
            String idGruppo = getIntent().getExtras().getString(TAG_GID);
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("idGruppo", idGruppo));
            params.add(new BasicNameValuePair("idPartecipante", idMittente));
            // getting JSON Object
            // it accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_remove_subscribe, "POST", params);

            // check log cat fro response
            Log.d("RemoveSub Response", json.toString());
            // check for success tag

            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    //

                } else {
                    //
                }
                finish();

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;

        }

    }

    /*
    Notifica ai membri del gruppo la ricezione dei messaggi
     */
    class messageNotification extends AsyncTask<String, String, String> {
        String idGruppo = getIntent().getExtras().getString(TAG_GID);
        String idMittente = memberId.getString(TAG_MID, null);

        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("idGruppo", idGruppo));
            params.add(new BasicNameValuePair("idMittente", idMittente));
            // getting JSON Object
            // it accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_notification, "POST", params);

            // check log cat fro response
            Log.d("Create Response notific", json.toString());
            // check for success tag

            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {

                } else {
                    runOnUiThread(new Runnable() {
                        public void run() {

                            Toast.makeText(GroupChat.this, "notifica non inviata", Toast.LENGTH_SHORT).show();
                        }
                    });
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;

        }

    }

    /*
    Controlla lo stato della connessione a internet
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }


    @Override
    protected void onPause() {

        super.onPause();
        finish();
    }

}
