package cuiz.mp3player.Mp3PlayService;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;

import cuiz.model.Mp3Info;
import cuiz.mp3player.Mp3ListActivity;

/**
 * Created by cuiz on 2016/4/7.
 */
public class PlayService extends Service {
    private String currentPlayMp3Name = null;
    private boolean isThisMp3Exist = true;

    private MediaPlayer mediaPlayer = null;
    private boolean isLoop = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mediaPlayer = new MediaPlayer();
        //在播放结束时进行提示
        /*mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
            @Override
            public void onCompletion(MediaPlayer arg0) {
                Toast.makeText(PlayService.this, "播放结束", Toast.LENGTH_SHORT).show();
            }
        });*/
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        currentPlayMp3Name = intent.getStringExtra("MP3_NAME");
        int command = intent.getIntExtra("COMMAND", Mp3Info.PLAY);



        switch (command){
            case Mp3Info.PLAY:
                if(mediaPlayer.isPlaying()){ //正在播放
                    mediaPlayer.pause(); //暂停
                    break;
                }
                playMp3();
                //playUrl("http://p1.music.126.net/E2wBsgu8lMsqMmNZ05n_OQ==/3084130115961170.mp3");
                break;
            case  Mp3Info.PAUSE:
                if(mediaPlayer.isPlaying()){//正在播放
                    mediaPlayer.pause(); //暂停
                }
                break;
            case Mp3Info.STOP:
                if(mediaPlayer.isPlaying()){//正在播放
                    mediaPlayer.stop(); //停止
                }
                break;
            default:
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    //播放本地Mp3
    private void playMp3(){
        mediaPlayer.reset();
        String mp3Dir = "/storage/emulated/0/mp3/"+this.currentPlayMp3Name;

        try{
            mediaPlayer.setDataSource(mp3Dir);
            mediaPlayer.prepare();
            mediaPlayer.setLooping(isLoop);
            isThisMp3Exist = true;
        }catch (Exception e){  //
            Toast.makeText(PlayService.this,"无此文件",Toast.LENGTH_SHORT).show();
            isThisMp3Exist = false;

            //若MP3文件不存在，那么启动下载服务线程
            Intent intent = new Intent();
            intent.setClass(PlayService.this, DownloadService.class);
            startService(intent);
            //e.printStackTrace();
        }

        if(isThisMp3Exist) {

            mediaPlayer.start(); //播放

            //播放进度显示，及进度调整
            managePlayerProgress();
        }
    }

    public void managePlayerProgress(){

        //拖动进度条--调整播放进度
        Mp3ListActivity.seekBar.setOnSeekBarChangeListener(new MySeekBarChangeListener());

        //播放进度条刷新线程
        ProgressThread progressThread = new ProgressThread();
        Thread thread = new Thread(progressThread);
        thread.start();
    }

    class ProgressThread implements Runnable{
        @Override
        public void run() {
            int CurrentPosition = 0;// 设置默认进度条当前位置

            int total = mediaPlayer.getDuration();//得到持续时间
            Mp3ListActivity.seekBar.setMax(total);  //设置进度条MAX

            Mp3ListActivity.seekBar.setProgress(0); //进度条位置重置
            // Update the progress bar
            while (mediaPlayer != null && CurrentPosition < total) {
                try {
                    Thread.sleep(1000); //1s
                    if (mediaPlayer != null) {
                        CurrentPosition = mediaPlayer.getCurrentPosition();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Mp3ListActivity.seekBar.setProgress(CurrentPosition);
            }
        }
    }

    class MySeekBarChangeListener implements SeekBar.OnSeekBarChangeListener{
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mediaPlayer.seekTo(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }
    }


    public void playUrl(String videoUrl) {
        //播放进度---------------------
        managePlayerProgress();

        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(videoUrl);
            mediaPlayer.prepare();//prepare之后自动播放
            mediaPlayer.start();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        System.out.println("service onDestroy");
    }
}




