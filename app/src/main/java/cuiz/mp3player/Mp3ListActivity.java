package cuiz.mp3player;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParserFactory;

import cuiz.Downloader.HttpDownloader;
import cuiz.model.MyAdapter;
import cuiz.mp3player.Mp3PlayService.DownloadService;
import cuiz.mp3player.Mp3PlayService.PlayService;
import cuiz.model.Mp3Info;
import cuiz.xml.Mp3ListContentHandler;


public class Mp3ListActivity extends AppCompatActivity {
    //普通变量

    public static final String KEY_DOWN = "DOWNLOAD";

    /**线程间通信，异步信息*/
    public static final int DOWN_PARSE_XML_OK = 0;
    public static final int DOWN_MP3 = 1; 

    /**菜单点击item序号*/
    private static final int update = 1;
    private static final int about = 2;

    /**线程间通信处理器*/
    public static Handler myHandler = null;

    /**由xml文件生成的Mp3文件的信息*/
    public static List<Mp3Info> mp3Infos = null;
    /**用户点击选择的位置*/
    public static int clickPosition = 0;
    public static String currentSelectedMp3Name = null;

    //UI控件
    /**UI容器，每个item装载一个Mp3文件的信息（name size）*/
    private ListView listView = null;
    public static SeekBar seekBar = null;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remote_mp3_list);
        listView = (ListView)findViewById(R.id.mp3info_list);
        seekBar = (SeekBar) findViewById(R.id.seekBar_id);
        /***异步事件处理
         * DOWN_PARSE_XML_OK  :XML文件下载并解析完成，在此完成UI列表更新，并通知用户;
         * DOWN_MP3           :MP3文件下载结果，在此通知用户.
         * */
        myHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                System.out.println("[Handler]----->>"+msg);
                if ((msg.what == DOWN_PARSE_XML_OK)) {
                    //UI列表更新--Mp3信息
                    showMp3InfoView();
                    Toast.makeText(Mp3ListActivity.this, "更新完成", Toast.LENGTH_SHORT).show();
                }else if(msg.what == DOWN_MP3){
                    //提示用户--MP3文件下载情况
                    Toast.makeText(getApplicationContext(),(String)msg.obj,Toast.LENGTH_SHORT).show();
                }
                super.handleMessage(msg);
            }
        };

        /**下载resource.xml(含mp3信息),并解析，生成Mp3对象放入Mp3InfoList
         * */
        updateMp3InfoFrowXML();

        /**click，播放指定的mp3文件
         * */
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Mp3ListActivity.clickPosition = position;
                Mp3ListActivity.currentSelectedMp3Name = mp3Infos.get(position).getMp3Name();
                playMp3Start(Mp3Info.PLAY);
            }
        });

        /**长按--下载mp3文件
         * */
       listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Mp3ListActivity.clickPosition = position;
                Mp3ListActivity.currentSelectedMp3Name = mp3Infos.get(position).getMp3Name();

                AlertDialog.Builder dialog = new AlertDialog.Builder(Mp3ListActivity.this);
                dialog.setTitle("是否下载");
                dialog.setMessage("Something important.");
                dialog.setCancelable(false);
                dialog.setPositiveButton("下载", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(Mp3ListActivity.this,"正在下载",Toast.LENGTH_SHORT).show();
                        downMp3Start();
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(Mp3ListActivity.this,"已取消下载",Toast.LENGTH_SHORT).show();

                    }
                });
                dialog.show();
                return false;
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void playMp3Start(int action) {
        Intent intent = new Intent();
        intent.putExtra("COMMAND",action);//KEY,VALUE；
        intent.putExtra("MP3_NAME",currentSelectedMp3Name);
        //附： Mp3Info实现序列化接口，可以把这个对象从内存当中变成字节码放到硬盘里面，或者通过网络发送出去。
        intent.setClass(Mp3ListActivity.this, PlayService.class);
        startService(intent);
    }

    public void downMp3Start() {
        //在Service中下载Mp3文件
        Intent intent = new Intent();
        intent.setClass(Mp3ListActivity.this, DownloadService.class);
        startService(intent);
    }



    /**XML文件下载、读取、解析---新建线程进行网络下载及耗时的非UI操作
     * */
    private void updateMp3InfoFrowXML(){
        Toast.makeText(Mp3ListActivity.this, "正在更新", Toast.LENGTH_SHORT).show();
        new Thread(){
            @Override
            public void run() {
                //下载xml文件
                String xmlStr = new HttpDownloader().downloadText("http://192.168.1.104:8080/mp3/resources.xml");

                //解析xml文件，并将解析结果放置到Mp3Info对象中，最后将这些对象放入List中
                mp3Infos = parse(xmlStr);

                if(mp3Infos != null){
                    Message message = new Message();
                    message.what = DOWN_PARSE_XML_OK;
                    myHandler.sendMessage(message);
                }

                super.run();
            }
        }.start();
    }

    /**解析xml文件
     * */
    private List<Mp3Info> parse(String xmlStr) {
        List<Mp3Info> infos = new ArrayList<Mp3Info>();
        try {
            //创建一个SAXParserFactory
            SAXParserFactory factory = SAXParserFactory.newInstance();
            XMLReader reader = factory.newSAXParser().getXMLReader();
            //为XMLReader设置内容处理器
            reader.setContentHandler(new Mp3ListContentHandler(infos));
            //开始解析文件
            reader.parse(new InputSource(new StringReader(xmlStr)));
            //测试点2：将XML文件解析出来放入list之后，使用迭代的方式，将list打印出来，测试一下，看看是否解析成功。
            /*for (Iterator iterator = infos.iterator();iterator.hasNext();){
                Mp3Info mp3Info = (Mp3Info) iterator.next();
                System.out.println("【解析resources.xml】成功--->>>"+mp3Info);
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        return infos;
    }

    /**更新UI显示列表
     * */
    private void showMp3InfoView(){
        //将生成的MyAdapter对象设置到listView中
        listView.setAdapter(buildMyAdapter());
    }

    /**根据mp3Infos生成MyAdapter对象
     * */
    private MyAdapter buildMyAdapter(){
        //生成一个List对象，并按照MyAdapter（即SimpleAdapter）的标准，将mp3Infos中的数据添加到list中
        List<Map<String,String>> mp3InfoViewlist = new ArrayList<>();
        for(Iterator iterator = mp3Infos.iterator();iterator.hasNext();){
            Mp3Info mp3Info = (Mp3Info) iterator.next();

            Map<String,String> item = new HashMap<>();
            item.put("MP3.NAME",mp3Info.getMp3Name());
            item.put("MP3.SIZE",mp3Info.getMp3Size());

            mp3InfoViewlist.add(item);
        }
        //创建一个MyAdapter对象
        MyAdapter myAdapter = new MyAdapter(Mp3ListActivity.this,
                mp3InfoViewlist,
                R.layout.mp3info_item,
                new String[]{"MP3.NAME","MP3.SIZE"},
                new int[]{R.id.mp3_name,R.id.mp3_size});
        return myAdapter;
    }

    //--------------------------------------------------------------
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        System.out.println("item'id is--> " + item.getItemId());

        switch (item.getItemId()) {
            //用户点击了更新
            case update:
                updateMp3InfoFrowXML();
                break;
            case about:

                break;
            default:
                break;
        }


        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.add(0, 1, 1, R.string.mp3list_update);
        menu.add(0, 2, 2, R.string.mp3list_about);
        return super.onCreateOptionsMenu(menu);
    }

    //----------------------------------------------------------
    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://cuiz.mp3player/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://cuiz.mp3player/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

}


        /*//a、内容填充--old版本
        Map<String,String> item0 = new HashMap<>();
        Map<String,String> item1 = new HashMap<>();
        item0.put("MP3.NAME",mp3InfoList.get(0).getMp3Name());
        item0.put("MP3.SIZE",mp3InfoList.get(0).getMp3Size());

        item1.put("MP3.NAME",mp3InfoList.get(1).getMp3Name());
        item1.put("MP3.SIZE",mp3InfoList.get(1).getMp3Size());

        mp3InfoViewlist.add(item0);
        mp3InfoViewlist.add(item1);*/

         /* for(int i=0;i<mp3InfoList.size();i++){
            Map<String,String> item = new HashMap<>();
            item.put("MP3.NAME",mp3InfoList.get(i).getMp3Name());
            item.put("MP3.SIZE",mp3InfoList.get(i).getMp3Size());

            mp3InfoViewlist.add(item);
        }*/

