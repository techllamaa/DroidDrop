package best.com.droiddrop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class FileAdapter extends BaseAdapter {
    private int [] thumbnails = {R.drawable.textfileimage,R.drawable.textfileimage,R.drawable.textfileimage};
    Context context;
    FileAdapter(Context ctx){
        this.context = ctx;
    }
    public int getCount(){

        return thumbnails.length;
    }
    public long getItemId(int position){
        return position;
    }

    @Override
    public Object getItem(int position) {
        return thumbnails[position];
    }

    public View getView(int position, View convertView, ViewGroup parent){
        View gridView = convertView;
        if(gridView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            gridView = inflater.inflate(R.layout.grid_item,null);
        }
        ImageView thumbnail = gridView.findViewById(R.id.thumbnail);
        thumbnail.setImageResource(thumbnails[position]);
        return gridView;
    }
}
