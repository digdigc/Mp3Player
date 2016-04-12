package cuiz.mp3player;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import cuiz.model.Mp3Info;
import cuiz.mp3player.Mp3PlayService.PlayService;

/**
 * Created by cuiz on 2016/4/11.
 */
public class PlayerActivity extends Activity {
    public static SeekBar seekBar = null;
    public static TextView lrcTextView = null;
    public Button playButton = null;
    public Button pauseButton = null;
    public Button stopButton = null;

    Mp3Info mp3Info = null;
    public static Handler lrcEventHander = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_mp3);
        seekBar = (SeekBar) findViewById(R.id.seekBar_id);
        lrcTextView = (TextView)findViewById(R.id.text_lrc);
        playButton = (Button) findViewById(R.id.bn_play);
        pauseButton = (Button)findViewById(R.id.bn_pause);
        stopButton = (Button)findViewById(R.id.bn_stop);

        lrcEventHander = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                String lrcContent = (String)msg.obj;
                lrcTextView.setText(lrcContent);
                super.handleMessage(msg);
            }
        };

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //启动Service播放音乐
                playMp3Start(AppConstant.PlayerCmd.PLAY, mp3Info);
            }
        });
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //启动Service暂停音乐
                playMp3Start(AppConstant.PlayerCmd.PAUSE, mp3Info);
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //启动Service停止音乐
                playMp3Start(AppConstant.PlayerCmd.STOP, mp3Info);
            }
        });
    }

    //每次从不可见到可见时，更新播放文件
    @Override
    protected void onResume() {
        //得到需要播放的MP3的文件名
        Intent intent = getIntent();
        mp3Info = (Mp3Info) intent.getSerializableExtra("MP3_INFO");
        System.out.print("PlayerActivity: -------"+ mp3Info);

        TextView playingMp3Name = (TextView) findViewById(R.id.playing_name);
        playingMp3Name.setText(mp3Info.getMp3Name());
        super.onResume();
    }

    //implement View.OnclickListener ，并没有进入下列动作；
   /* @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bn_play:
                //启动Service播放音乐
                System.out.println("-----------------=============");
                playService.mediaPlayer.start();
                break;
            case R.id.bn_pause:
                //启动Service暂停音乐
                playService.mediaPlayer.pause();
                break;
            case R.id.bn_stop:
                //启动Service停止音乐
                playService.mediaPlayer.stop();
                break;
            default:
                break;
        }

    }*/

    //启动Service播放音乐
    private void playMp3Start(int action,Mp3Info mp3Info) {
        Intent intent = new Intent();
        intent.putExtra("COMMAND",action);//KEY,VALUE；
        intent.putExtra("MP3_INFO",mp3Info);
        //附： Mp3Info实现序列化接口，可以把这个对象从内存当中变成字节码放到硬盘里面，或者通过网络发送出去。
        intent.setClass(this, PlayService.class);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
