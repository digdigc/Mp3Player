package cuiz.mp3player;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cuiz.model.Mp3Info;
import cuiz.model.MyAdapter;
import cuiz.mp3player.Mp3PlayService.PlayService;
import cuiz.utils.FileUtils;

/**
 * Created by cuiz on 2016/4/10.
 */
//读取文件夹 ----读取IO操作在FIleUtils中实现
public class LocalMp3LIstActivity extends ListActivity {
    ListView listView = null;
    List<Mp3Info> mp3Infos = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local_mp3_list);
        listView = getListView();

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
        playMp3Start(Mp3Info.PLAY,mp3Infos.get(position).getMp3Name());

    }

    //播放
    private void playMp3Start(int action,String mp3Name) {
        Intent intent = new Intent();
        intent.putExtra("COMMAND",action);//KEY,VALUE；
        intent.putExtra("MP3_NAME",mp3Name);
        //附： Mp3Info实现序列化接口，可以把这个对象从内存当中变成字节码放到硬盘里面，或者通过网络发送出去。
        intent.setClass(LocalMp3LIstActivity.this, PlayService.class);
        startService(intent);
    }

    /**更新UI显示列表
     * */
      private void showMp3InfoList(){
          /**读取目录中的MP3文件的名字和大小*/
          FileUtils fileUtils = new FileUtils();
          String mp3Dir = "/storage/emulated/0/mp3/";
          mp3Infos = fileUtils.getMp3FilesList(mp3Dir);

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
