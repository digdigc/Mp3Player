package cuiz.mp3player;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

/**
 * Created by cuiz on 2016/4/10.
 */
public class MainActivity extends TabActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //得到TabHost对象，对TabActivity的操作通常都由这个对象完成
        TabHost tabHost = getTabHost();

        //生成一个TabSpec对象，这个对象代表了一个页
        TabHost.TabSpec remoteTabSpec = tabHost.newTabSpec("remote");
        //设置该页的Indicator
        remoteTabSpec.setIndicator("remote",getResources().getDrawable(android.R.drawable.arrow_down_float));
        //设置该页的内容
        remoteTabSpec.setContent(new Intent().setClass(this,Mp3ListActivity.class));
        //将设置好的TabSpec对象添加到Tabhost中
        tabHost.addTab(remoteTabSpec);

        TabHost.TabSpec localTabSpec = tabHost.newTabSpec("local");
        localTabSpec.setIndicator("local",getResources().getDrawable(android.R.drawable.btn_dropdown));
        localTabSpec.setContent(new Intent().setClass(this,LocalMp3LIstActivity.class));
        tabHost.addTab(localTabSpec);

    }
}
