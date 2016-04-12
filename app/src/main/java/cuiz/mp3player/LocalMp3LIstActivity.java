package cuiz.mp3player;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cuiz.model.Mp3Info;
import cuiz.model.MyAdapter;
import cuiz.utils.FileUtils;

/**
 * Created by cuiz on 2016/4/10.
 *
 * 1：显示本地指定文件夹下的mp3的信息列表
 * 2：设置点击监听，启动PlayActivity，显示播放界面
 */
//读取文件夹 ----读取IO操作在FIleUtils中实现
public class LocalMp3LIstActivity extends ListActivity {
    ListView listView = null;
    public static TextView textViewFindResult = null;
    List<Mp3Info> mp3Infos = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local_mp3_list);
        listView = getListView();
        textViewFindResult = (TextView) findViewById(R.id.text_show_find_files_result);

    }

    //activity可见时，数据显示到listview中。
    @Override
    protected void onResume() {
        super.onResume();
        showMp3InfoList();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Mp3Info mp3Info = mp3Infos.get(position);

        Intent intent = new Intent();
        intent.putExtra("MP3_INFO",mp3Info);
        intent.setClass(this,PlayerActivity.class);
        startActivity(intent);

    }

    /**更新UI显示列表
     * */
      private void showMp3InfoList(){
          /**读取目录中的MP3文件的名字和大小*/
          try{
              FileUtils fileUtils = new FileUtils();
              //String mp3Dir = "/storage/emulated/0/mp3/";
              String mp3Dir = Environment.getExternalStorageDirectory()+ "/mp3/";
              mp3Infos = fileUtils.getLocalMp3List(mp3Dir);
          }catch (Exception e){
              Toast.makeText(this,"空",Toast.LENGTH_SHORT).show();
              e.printStackTrace();
              return;
          }

          //数据源
          List<HashMap<String,String>> mp3InfoListForView = new ArrayList<>();
          for(Mp3Info mp3Info:mp3Infos){
              HashMap<String,String> item = new HashMap<>();
              item.put("NAME",mp3Info.getMp3Name());
              item.put("SIZE",mp3Info.getMp3Size());
              mp3InfoListForView.add(item);
          }
          MyAdapter myAdapter = new MyAdapter(this,mp3InfoListForView,R.layout.mp3info_item,new String[]{"NAME","SIZE"},new int[]{R.id.mp3_name,R.id.mp3_size});
          /**使用listView显示*/
          listView.setAdapter(myAdapter);
    }

}
