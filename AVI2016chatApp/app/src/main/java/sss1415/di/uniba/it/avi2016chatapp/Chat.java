package sss1415.di.uniba.it.avi2016chatapp;




import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.gc.materialdesign.views.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;



public class Chat extends ActionBarActivity {


    private Button btnSend;
    private EditText inputMsg;
    ListView listv;
    SharedPreferences memberId;
    private static final String TAG_MID = "codice";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        listv = (ListView) findViewById(R.id.list_view_messages);
        btnSend = (Button) findViewById(R.id.btnSend);
        inputMsg = (EditText) findViewById(R.id.inputMsg);
        //prelevo id del mittente
        memberId = getSharedPreferences(TAG_MID, MODE_PRIVATE);
        //deve ricevere anche l'id del destinatario(se conversazione) o id del gruppo(se è gruppo)

        btnSend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                // Clearing the input filed once message was sent
                //inputMsg.setText("");
            }
        });

    }
}
