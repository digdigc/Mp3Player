package cuiz.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

import java.util.List;
import java.util.Map;

import cuiz.mp3player.R;

/**
 * Created by cuiz on 2016/4/7.
 */
public class MyAdapter extends SimpleAdapter {
    private LayoutInflater inflater = null;


    //这里为什么需要覆写
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View result = super.getView(position, convertView, parent);
        System.out.println("positon---->" + position);
        if(result != null){
            inflater.inflate(R.layout.mp3info_item, null);
        }
        return result;
    }


    /**
     * Constructor
     *
     * @param context The context where the View associated with this SimpleAdapter is running
     * @param data A List of Maps. Each entry in the List corresponds to one row in the list. The
     *        Maps contain the data for each row, and should include all the entries specified in
     *        "from"
     * @param resource Resource identifier of a view layout that defines the views for this list
     *        item. The layout file should include at least those named views defined in "to"
     * @param from A list of column names that will be added to the Map associated with each
     *        item.
     * @param to The views that should display column in the "from" parameter. These should all be
     *        TextViews. The first N views in this list are given the values of the first N columns
     *        in the from parameter.
     */
    /*Context context ：上下文
    * List<? extend Map<String,?>> data: 数据来源（列表容器）
    * int resource : 每个item表项的布局*/
    public MyAdapter(Context context, List<? extends Map<String, ?>> data,
                     int resource, String[] from, int[] to) {

        super(context, data, resource, from, to);
        inflater = LayoutInflater.from(context);
    }

}
