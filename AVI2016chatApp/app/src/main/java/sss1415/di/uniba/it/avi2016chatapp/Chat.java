package sss1415.di.uniba.it.avi2016chatapp;




import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.gc.materialdesign.views.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
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


public class Chat extends ListActivity {

    // url to create new group
    private static String url_send_message = "http://androidchatapp.altervista.org/chatApp_connect/send_message.php";
    private static String url_read_message = "http://androidchatapp.altervista.org/chatApp_connect/read_message.php";
    private static String url_notification = "http://androidchatapp.altervista.org/chatApp_connect/message_notification.php";
    private static final String BROADCAST = "com.google.android.c2dm.intent.RECEIVE";
    ArrayList<HashMap<String, String>> messageList;

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    JSONParser jsonParser = new JSONParser();

    private Button btnSend;
    private EditText inputMsg;
    ListView listv;
    //per l'invio del messaggio
    SharedPreferences memberId;
    private static final String TAG_DID = "codice";
    private static final String TAG_MID = "codice";
    private static final String TAG_GID = "codiceGruppo";
    //per la lettura e visualizazione
    private static final String TAG_MESSAGE1 = "testoMessaggio";
    private static final String TAG_MESSAGE ="message";
    private static final String TAG_MITTENTE ="codiceMittente";
    private static final String TAG_DESTINATARIO ="codiceDestinatario";
    private static final String TAG_NOME ="nome";
    JSONArray messages = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        btnSend = (Button) findViewById(R.id.btnSend);
        inputMsg = (EditText) findViewById(R.id.inputMsg);
        memberId =getSharedPreferences(TAG_MID, MODE_PRIVATE);
        //deve ricevere anche l'id del destinatario(se conversazione) o id del gruppo(se è gruppo)
        //per conversazione
        Map<String, ?> entry_codice = memberId.getAll();
        final String[] codice = new String[entry_codice.size()];
        int i = 0;

        for (Map.Entry<String, ?> entryeach : entry_codice.entrySet()) {
            codice[i] = (String) entryeach.getValue();
            i++;
        }
        String messageText = inputMsg.getText().toString();
        String idMittente = codice[0];
        messageList = new ArrayList<HashMap<String, String>>();
        new LoadMessages().execute();
        btnSend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(inputMsg.getText().toString().length()>0){
                    // Hashmap for ListView


                    new SendMessage().execute();
                    messageList = new ArrayList<HashMap<String, String>>();
                    new LoadMessages().execute();
                    // Clearing the input filed once message was sent
                    inputMsg.setText("");
                }

            }
        });

    }

    /**
     * Background Async Task to Create new product
     * */
    class SendMessage extends AsyncTask<String, String, String> {


        String idDestinatario = getIntent().getExtras().getString(TAG_DID);

        /**
         * Creating group
         */
        protected String doInBackground(String... args) {
            Map<String, ?> entry_codice = memberId.getAll();
            final String[] codice = new String[entry_codice.size()];
            int i = 0;

            for (Map.Entry<String, ?> entryeach : entry_codice.entrySet()) {
                codice[i] = (String) entryeach.getValue();
                i++;
            }
            String messageText = inputMsg.getText().toString();
            String idMittente = codice[0];

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("idMittente", idMittente));
            params.add(new BasicNameValuePair("messageText", messageText));
            params.add(new BasicNameValuePair("idDestinatario", idDestinatario));

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_send_message,
                    "POST", params);

            // check log cat fro response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    new messageNotification().execute();
                   //null
                } else {
                    // failed to create product
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

    class LoadMessages extends AsyncTask<String, String, String> {


        String idDestinatario = getIntent().getExtras().getString(TAG_DID);
        /**
         * Creating group
         * */
        protected String doInBackground(String... args) {
            Map<String, ?> entry_codice = memberId.getAll();
            final String[] codice = new String[entry_codice.size()];
            int i = 0;

            for(Map.Entry<String, ?> entryeach : entry_codice.entrySet()) {
                codice[i] = (String) entryeach.getValue();
                i++;
            }
            String idMittente = codice[0];

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
                            // Getting Array of Products
                            messages = json.getJSONArray(TAG_MESSAGE);

                            // looping through All Products
                            for (int j = 0; j < messages.length(); j++) {
                                JSONObject c = messages.getJSONObject(j);

                                // Storing each json item in variable
                                String mittente = c.getString(TAG_MITTENTE);
                                String messaggio = c.getString(TAG_MESSAGE1);
                                String destinatario = c.getString(TAG_DESTINATARIO);
                                String nome = c.getString(TAG_NOME);

                                // creating new HashMap
                                HashMap<String, String> map = new HashMap<String, String>();

                                // adding each child node to HashMap key => value
                                map.put(TAG_MITTENTE, mittente);
                                map.put(TAG_MESSAGE1, messaggio);
                                map.put(TAG_DESTINATARIO, destinatario);
                                map.put(TAG_NOME, nome);

                                messageList.add(map);
                            }
                        }else {
                            // failed to create product
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {

                    ListAdapter adapter = new SimpleAdapter(
                            Chat.this, messageList,
                            R.layout.list_item_message_left, new String[]{TAG_NOME,
                            TAG_MESSAGE1},
                            new int[]{R.id.lblMsgFrom, R.id.txtMsg});
                    // updating listview
                    setListAdapter(adapter);
                }
            });

         }
    }

    class messageNotification extends AsyncTask<String, String, String> {
        String idDestinatario = getIntent().getExtras().getString(TAG_DID);
        String idMittente = memberId.getString(TAG_MID, null);
        /**
         * getting All products from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("idDestinatario", idDestinatario));
            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_notification, "POST", params);

            // check log cat fro response
            Log.d("Create Response notific", json.toString());
            // check for success tag

            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {

                }else {
                    runOnUiThread(new Runnable() {
                        public void run() {

                            Toast.makeText(Chat.this, "notifica non inviata", Toast.LENGTH_SHORT).show();
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
    protected void onPause() {

        super.onPause();
        finish();
    }
}
