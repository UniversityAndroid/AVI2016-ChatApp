package sss1415.di.uniba.it.avi2016chatapp;

/**
 * Created by katia on 27/05/2015.
 */

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.gc.materialdesign.views.Button;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hp1 on 21-01-2015.
 */
public class Tab1 extends ListFragment {

    private Button addGroup;

    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> membershipsList;

    // url to get all products list
    private static String url_all_memberships = "http://androidchatapp.altervista.org/chatApp_connect/get_all_memberships.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MEMBERSHIPS = "memberships";
    private static final String TAG_MID = "codice";
    private static final String TAG_NAME = "nome";
    private static final String TAG_SURNAME = "cognome";
    // products JSONArray
    JSONArray memberships = null;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab1, container, false);

        // Hashmap for ListView
        membershipsList = new ArrayList<HashMap<String, String>>();

        // Loading products in Background Thread
        new LoadAllMemberships().execute();

        addGroup = (Button) v.findViewById(R.id.buttonFloat);

        addGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openNewGroup = new Intent(getActivity(), NewGroup.class);
                startActivity(openNewGroup);

            }
        });

        return v;
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        // Get listview
        ListView lv = getListView();

        // on seleting single product
        // launching Edit Product Screen
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting values from selected ListItem
                String pid = ((TextView) view.findViewById(R.id.mid)).getText()
                        .toString();

                // Starting new intent
                Intent in = new Intent(view.getContext(),Chat.class);
                // sending pid to next activity
                in.putExtra(TAG_MID, pid);

                // starting new activity
                startActivity(in);
            }
        });

    }

    /**
     * Background Async Task to Load all product by making HTTP Request
     * */
    class LoadAllMemberships extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Loading memberships. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting All products from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_all_memberships, "GET", params);

            // Check your log cat for JSON reponse
            Log.d("All Memberships: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // products found
                    // Getting Array of Products
                    memberships = json.getJSONArray(TAG_MEMBERSHIPS);

                    // looping through All Products
                    for (int i = 0; i < memberships.length(); i++) {
                        JSONObject c = memberships.getJSONObject(i);

                        // Storing each json item in variable
                        String id = c.getString(TAG_MID);
                        String name = c.getString(TAG_NAME);
                        String surname = c.getString(TAG_SURNAME);

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_MID, id);
                        map.put(TAG_NAME, name);
                        map.put(TAG_SURNAME, surname);

                        // adding HashList to ArrayList
                        membershipsList.add(map);
                    }
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
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            // updating UI from Background Thread
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    ListAdapter adapter = new SimpleAdapter(
                            getActivity(), membershipsList,
                            R.layout.list_item, new String[] { TAG_MID,
                            TAG_NAME, TAG_SURNAME},
                            new int[] { R.id.mid, R.id.name, R.id.surname });
                    // updating listview
                    setListAdapter(adapter);
                }
            });

        }

    }
}
