package at.fhv.teamx.wikigeoinfo;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

public class ArticleViewerActivity extends AppCompatActivity {
    FirPOI mFirPoi = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_viewer);
        setTitle("");

        WebView webview = (WebView)findViewById(R.id.webview);
        if (getIntent().getSerializableExtra("firpoi") != null) {
            FirPOI firPOI = (FirPOI)getIntent().getSerializableExtra("firpoi");
            webview.loadUrl("https://en.m.wikipedia.org/w/index.php?title=Translation&curid=" + firPOI.getArticleId());
            setTitle(firPOI.getTitle());
            mFirPoi = firPOI;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.articleview_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.invite) {
            Intent share = new Intent(android.content.Intent.ACTION_SEND);
            share.putExtra(Intent.EXTRA_SUBJECT, mFirPoi.getTitle());
            share.putExtra(Intent.EXTRA_TEXT, DynamicLinksHelper.createDynamicLink(Integer.parseInt(mFirPoi.getArticleId())));
            startActivity(Intent.createChooser(share, getString(R.string.shareto)));
        }
        if (item.getItemId() == R.id.favorite) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("saved/" + user.getUid());
                ref.child(mFirPoi.getArticleId()).runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        FirPOI currentValue = mutableData.getValue(FirPOI.class);
                        if (currentValue == null) {
                            mutableData.setValue(mFirPoi);
                            Snackbar.make(getWindow().getDecorView().getRootView(), getResources().getString(R.string.addedfavs), Snackbar.LENGTH_LONG).show();
                        } else {
                            mutableData.setValue(null);
                            Snackbar.make(getWindow().getDecorView().getRootView(), getResources().getString(R.string.removedfavs), Snackbar.LENGTH_LONG).show();
                        }
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                    }
                });
            }
        }
        return super.onOptionsItemSelected(item);
    }
}