package best.com.droiddrop;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity{
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter adapter;
    private FloatingActionButton fab;
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
    private void setupViewPager(ViewPager viewPager){
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.add(new SharedTab(), "Shared");
        adapter.add(new ReceivedTab(), "Received");
        viewPager.setAdapter(adapter);
    }
}
    //        FloatingActionButton fab = findViewById(R.id.fabNearBy);
//        final FloatingActionButton foundDeviceOne = findViewById(R.id.fab_user);
//        final TextView device1 = findViewById(R.id.device1);
//        fab.setOnClickListener(new View.OnClickListener(){
//            public void onClick(View view){
//                if(open){
//                    foundDeviceOne.show();
//                    device1.setVisibility(View.VISIBLE);
//                    open = false;
//                }else{
//                    foundDeviceOne.hide();
//                    open = true;
//                }
//            }
//        });
//}