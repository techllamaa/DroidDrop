package best.com.droiddrop;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FilePickerTab extends Fragment {
    public FilePickerTab() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.filepicker_tab, container, false);

        return view;
    }

//    public void activity(View view){
//        pathTxt = view.findViewById(R.id.pathTxt);
//        myFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
//        myFileIntent.setType("*/*");
//        startActivityForResult(myFileIntent,10);
//    }
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        switch (requestCode){
//            case 10:
//                if(requestCode == RESULT_OK){
//                    System.out.println("IM IN HERE");
//                    String path = data.getData().getPath();
//                    pathTxt.setText(path);
//                }
//                break;
//        }
//    }


}
