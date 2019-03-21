package best.com.droiddrop;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity{
    private boolean open = true;

    //ideally wanna get this number from backend
    private int numOfDevicesFound = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FloatingActionButton fab = findViewById(R.id.fabNearBy);
        final FloatingActionButton foundDeviceOne = findViewById(R.id.fab_user);
        final TextView device1 = findViewById(R.id.device1);
        fab.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                if(open){
                    foundDeviceOne.show();
                    device1.setVisibility(View.VISIBLE);
                    open = false;
                }else{
                    foundDeviceOne.hide();
                    open = true;
                }
            }
        });
    }


    protected void createContacts(){



    }
}