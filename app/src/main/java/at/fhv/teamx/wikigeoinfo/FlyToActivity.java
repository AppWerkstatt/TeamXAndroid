package at.fhv.teamx.wikigeoinfo;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
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
        setTitle(getString(R.string.flytotitle));

        ListView flytoListview = (ListView)findViewById(R.id.flytoListview);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("flytopois");
        mAdapter = new FirebaseListAdapter<FlyTo>(this, FlyTo.class, R.layout.flyto_item, ref) {
            @Override
            protected void populateView(View view, final FlyTo flytolocation, int position) {
                ((TextView)view.findViewById(R.id.label)).setText(flytolocation.getName());
                ImageLoader.ImageCache imageCache = new BitmapLruCache();
                ImageLoader imageLoader = new ImageLoader(Volley.newRequestQueue(FlyToActivity.this), imageCache);
                ((NetworkImageView)view.findViewById(R.id.icon)).setImageUrl(flytolocation.getUrl(), imageLoader);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent output = new Intent();
                        output.putExtra("flyto", flytolocation);
                        setResult(RESULT_OK, output);
                        finish();
                    }
                });
            }
        };
        flytoListview.setAdapter(mAdapter);
    }
}
