package sss1415.di.uniba.it.avi2016chatapp;


import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.gc.materialdesign.views.Button;

import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Toast;
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
 * Questa classe java gestisce l'invio e la ricezione dei messaggi tra due utenti: mittente e destinatario
 */

public class Chat extends ListActivity {

    // Server url
    private static String url_send_message = "http://androidchatapp.altervista.org/chatApp_connect/send_message.php";
    private static String url_read_message = "http://androidchatapp.altervista.org/chatApp_connect/read_message.php";
    private static String url_notification = "http://androidchatapp.altervista.org/chatApp_connect/message_notification.php";

    ArrayList<HashMap<String, String>> messageList;

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    JSONParser jsonParser = new JSONParser();

    private Button btnSend;
    private EditText inputMsg;

    //per l'invio del messaggio
    SharedPreferences memberId;
    private static final String TAG_DID = "codice";
    private static final String TAG_MID = "codice";
    private static final String TAG_GID = "codiceGruppo";

    //per la lettura e visualizazione del messaggio
    private static final String TAG_MESSAGE1 = "testoMessaggio";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_MITTENTE = "codiceMittente";
    private static final String TAG_DESTINATARIO = "codiceDestinatario";
    private static final String TAG_NOME = "nome";
    private static final String TAG_COGNOME = "cognome";
    JSONArray messages = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        btnSend = (Button) findViewById(R.id.btnSend);
        inputMsg = (EditText) findViewById(R.id.inputMsg);
        memberId = getSharedPreferences(TAG_MID, MODE_PRIVATE);
        //deve ricevere anche l'id del destinatario

        messageList = new ArrayList<HashMap<String, String>>();
        //controlla lo stato della connessione ad Internet
        if (isNetworkAvailable()) {
            //carica eventali messaggi
            new LoadMessages().execute();
            //bottone per l'invio di nuovo messaggio
            btnSend.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    //controlla che il messaggio sia stato effettivamente digitato
                    if (inputMsg.getText().toString().length() > 0) {

                        //invio di un nuovo messaggio e salvataggio nel database
                        new SendMessage().execute();
                        // Hashmap for ListView
                        messageList = new ArrayList<HashMap<String, String>>();
                        // ricarica/aggiorna la lista dei messaggi da visualizzare
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
     * Background Async Task to send new message
     */
    class SendMessage extends AsyncTask<String, String, String> {

        //id del destinatario
        String idDestinatario = getIntent().getExtras().getString(TAG_DID);

        /**
         * Send message
         */
        protected String doInBackground(String... args) {

            //messaggio di testo da inoltrare
            String messageText = inputMsg.getText().toString();
            //id del mittente
            String idMittente = memberId.getString(TAG_MID, null);

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("idMittente", idMittente));
            params.add(new BasicNameValuePair("messageText", messageText));
            params.add(new BasicNameValuePair("idDestinatario", idDestinatario));

            // getting JSON Object
            // accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_send_message,
                    "POST", params);

            // check log cat fro response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    //per notificare il nuovo messaggio al destinatario
                    new messageNotification().execute();
                    //null
                } else {

                    runOnUiThread(new Runnable() {
                        public void run() {

                            Toast.makeText(Chat.this, "Message not send!", Toast.LENGTH_SHORT).show();
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
    Caricamento e visualizzazione dei messaggi
     */
    class LoadMessages extends AsyncTask<String, String, String> {

        // id del destinatario
        String idDestinatario = getIntent().getExtras().getString(TAG_DID);

        /**
         * caricamento messaggi
         */
        protected String doInBackground(String... args) {
            // recupero id mittente
            String idMittente = memberId.getString(TAG_MID, null);

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("idMittente", idMittente));
            params.add(new BasicNameValuePair("idDestinatario", idDestinatario));
            //lettura dei messaggi
            JSONObject json = jsonParser.makeHttpRequest(url_read_message, "POST", params);

            // check log cat fro response
            Log.d("Message detail: ", json.toString());
            // check for success tag
            try {
                // Checking for SUCCESS TAG
                int success1 = json.getInt(TAG_SUCCESS);

                if (success1 == 1) {
                    // products found
                    // Getting messages Array
                    messages = json.getJSONArray(TAG_MESSAGE);

                    // looping through All messages
                    for (int j = 0; j < messages.length(); j++) {
                        JSONObject c = messages.getJSONObject(j);

                        // Storing each json item in variable
                        String mittente = c.getString(TAG_MITTENTE);
                        String messaggio = c.getString(TAG_MESSAGE1);
                        String destinatario = c.getString(TAG_DESTINATARIO);
                        String nome = c.getString(TAG_NOME);
                        String cognome = c.getString(TAG_COGNOME);

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_MITTENTE, mittente);
                        map.put(TAG_MESSAGE1, messaggio);
                        map.put(TAG_DESTINATARIO, destinatario);
                        map.put(TAG_NOME, nome);
                        map.put(TAG_COGNOME, cognome);

                        messageList.add(map);
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

                    ListAdapter adapter = new SimpleAdapter(
                            Chat.this, messageList,
                            R.layout.list_item_message_left, new String[]{TAG_NOME, TAG_COGNOME,
                            TAG_MESSAGE1},
                            new int[]{R.id.lblMsgFrom, R.id.surname, R.id.txtMsg});
                    // updating listview
                    setListAdapter(adapter);
                }
            });

        }
    }

    class messageNotification extends AsyncTask<String, String, String> {
        String idDestinatario = getIntent().getExtras().getString(TAG_DID);

        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("idDestinatario", idDestinatario));
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
                    //
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;

        }

    }

    /*
    Controlla lo stato della connessione
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
