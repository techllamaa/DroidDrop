package best.com.droiddrop;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter adapter;
    private FloatingActionButton fab;
    private TextView testingText;
    private Intent myFileIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        adapter = new ViewPagerAdapter(getSupportFragmentManager());

        viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setText("Shared");
        tabLayout.getTabAt(1).setText("Received");
        tabLayout.getTabAt(2).setText("File Pick");


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                boolean flag = true;
                if(tab.getPosition() == 2){
                        testingText= findViewById(R.id.pathTxt);
                        myFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        myFileIntent.setType("*/*");
                        startActivityForResult(myFileIntent,10);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        fab = findViewById(R.id.fab);
        final TextView txt = findViewById(R.id.fab_txt);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txt.setVisibility(View.VISIBLE);
//                Intent intent = new Intent(MainActivity.this, NewMessageActivity.class);
//                startActivity(intent);
            }
        });
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.add(new SharedTab(), "Shared");
        adapter.add(new ReceivedTab(), "Received");
        adapter.add(new FilePickerTab(), "File Pick");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 10:
                if(requestCode != RESULT_OK){
                    String path = data.getData().getPath();
                    testingText= (TextView)findViewById(R.id.pathTxt);
                    testingText.setText(path);
                }
                break;
        }
    }
}
