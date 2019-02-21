package best.com.droiddrop;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity{
    private boolean connection = false;
    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /** called when user presses send button */
    public void connectButton(View view) {
        TextView textView = findViewById(R.id.textView2);
        //call connection here, give feedback flag connection
        if(connection){
            textView.setText("Connected");
        }
        else{
            textView.setText("Not Connected");
        }
    }
}