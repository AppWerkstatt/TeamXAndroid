package at.fhv.teamx.wikigeoinfo;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class FlyToActivity extends AppCompatActivity {
    FirebaseListAdapter<FlyTo> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fly_to);

        ListView flytoListview = (ListView)findViewById(R.id.flytoListview);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("flytopois");
        mAdapter = new FirebaseListAdapter<FlyTo>(this, FlyTo.class, R.layout.flyto_item, ref) {
            @Override
            protected void populateView(View view, FlyTo flytolocation, int position) {
                ((TextView)view.findViewById(R.id.label)).setText(flytolocation.getName());
                ((ImageView)view.findViewById(R.id.icon)).setImageURI(Uri.parse(flytolocation.getUrl()));
            }
        };
        flytoListview.setAdapter(mAdapter);
    }
}
