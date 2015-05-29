package sss1415.di.uniba.it.avi2016chatapp;

/**
 * Created by katia on 27/05/2015.
 */
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gc.materialdesign.views.Button;

/**
 * Created by hp1 on 21-01-2015.
 */
public class Tab2 extends Fragment {
private Button addGroup;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab2,container,false);
        addGroup = (Button)v.findViewById(R.id.buttonFloat);

        addGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openNewGroup = new Intent(getActivity(), NewGroups.class);
                startActivity(openNewGroup);

            }
        });

        return v;



    }
}