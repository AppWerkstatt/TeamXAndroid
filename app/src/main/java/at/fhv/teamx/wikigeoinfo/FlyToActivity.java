package at.fhv.teamx.wikigeoinfo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.firebase.ui.database.FirebaseListAdapter;

import java.util.List;

public class FlyToActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fly_to);

        ListView flytoListview = (ListView)findViewById(R.id.flytoListview);
        FirebaseListAdapter
    }
}
