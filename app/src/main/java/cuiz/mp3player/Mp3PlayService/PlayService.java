package cuiz.mp3player.Mp3PlayService;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.Message;
import android.widget.SeekBar;

import java.io.IOException;

import cuiz.Lrc.LrcProcessor;
import cuiz.model.Mp3Info;
import cuiz.mp3player.AppConstant;
import cuiz.mp3player.PlayerActivity;

/**
 * Created by cuiz on 2016/4/7.
 */
public class PlayService extends Service /*implements MediaPlayer.OnPreparedListener*/{
    private Mp3Info currentPlayingMp3 = null;
    private Mp3Info lastPlayedMp3 =null;
    public  MediaPlayer mediaPlayer = null;

    LrcProcessor lrcProcessor = null;
    boolean isTouchingSeekBar = false;

    private enum StatusOfMedia{
        isPlaying,
        isPaused,
        isStoped
    }
    StatusOfMedia statusOfMedia = null;

    private volatile Thread blinker; //安全地结束线程的一个辅助变量

    @Override
    public void onCreate() {
        System.out.println("Playservice---===OnCreate");//test
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("Playservice---===onStartCommand");//test
        //对用户点击播放的不同情况，做出相应的处理
        /*if(currentPlayingMp3 == null){ //如果是第一次进来*//**//*
            currentPlayingMp3 = (Mp3Info) intent.getSerializableExtra("MP3_INFO");
        }else{
            lastPlayedMp3 = currentPlayingMp3;
            currentPlayingMp3 = (Mp3Info) intent.getSerializableExtra("MP3_INFO");
            if(currentPlayingMp3.equals(lastPlayedMp3)){ //如果第二次和上一次点击的是同一首歌
                                                         //那么这种情况不需要重新加载Mp3文件、歌词文件进度条
            }else{                 //如果第二次点击的歌和上一次点击的歌不同
                stopMp3();//那么停止上一次播放的歌，释放上一首歌的所有资源
                return super.onStartCommand(intent, flags, startId);
            }
        }*/
        currentPlayingMp3 = (Mp3Info) intent.getSerializableExtra("MP3_INFO");
        int command = intent.getIntExtra("COMMAND", AppConstant.PlayerCmd.PLAY);

        switch (command){
            case AppConstant.PlayerCmd.PLAY:
                playMp3();
                //playUrl("http://p1.music.126.net/E2wBsgu8lMsqMmNZ05n_OQ==/3084130115961170.mp3");
                break;
            case  AppConstant.PlayerCmd.PAUSE:
                pauseMp3();
                break;
            case AppConstant.PlayerCmd.STOP:
                stopMp3();
                break;
            default:
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void playMp3Prepare(){
        String mp3Dir = Environment.getExternalStorageDirectory()+ "/mp3/"+ currentPlayingMp3.getMp3Name();
        /*String mp3Dir = "/storage/emulated/0/mp3/"+this.currentPlayMp3Name;*/
       /* mediaPlayer.setDataSource(mp3Dir);*/
        mediaPlayer = MediaPlayer.create(this, Uri.parse("file://"+mp3Dir)); //create方法中已做好了prepare
        mediaPlayer.setLooping(false);
        //mediaPlayer.prepareAsync();

        //歌词文件读取到队列
        lrcProcessor = new LrcProcessor();
        lrcProcessor.readLRC(mp3Dir);
        lrcProcessor.pollQueue(); //将队列第一行歌词信息推出来（//将缓存队列中的数据向队头推进，队头被移出来。）

        //播放进度显示，及进度调整
        managePlayerProgress();
    }

    //播放本地Mp3
    private void playMp3(){

        if(mediaPlayer == null||statusOfMedia == StatusOfMedia.isStoped){
            playMp3Prepare();
        }
        mediaPlayer.start();
        statusOfMedia = StatusOfMedia.isPlaying;
    }

    private void pauseMp3() {
        if(mediaPlayer != null){
            if(mediaPlayer.isPlaying()) {//正在播放
                mediaPlayer.pause(); //暂停
                statusOfMedia = StatusOfMedia.isPaused;
            }
        }
    }

    //在stop中，记得做好清理工作
    private void stopMp3() {
        PlayerActivity.seekBar.setProgress(0);
        if(mediaPlayer != null) {
            mediaPlayer.stop();
            statusOfMedia = StatusOfMedia.isStoped;  //设置结束标志
            //mediaPlayer.release(); // 会让程序死机
            // mediaPlayer = null; //
            blinker = null;//安全地结束进度线程
        }
    }

    public void managePlayerProgress(){


        //拖动进度条--调整播放进度
        PlayerActivity.seekBar.setOnSeekBarChangeListener(new MySeekBarChangeListener());

        //播放进度条刷新线程
        ProgressThread progressRun = new ProgressThread();
        Thread progressThread = new Thread(progressRun);
        progressThread.start();
    }

    class ProgressThread implements Runnable{
        @Override
        public void run() {
            int CurrentPosition = 0;// 设置默认进度条当前位置

            int total = mediaPlayer.getDuration();//得到持续时间
            PlayerActivity.seekBar.setMax(total);  //设置进度条MAX

            PlayerActivity.seekBar.setProgress(0); //进度条位置重置

            blinker = Thread.currentThread();
            // Update the progress bar
            while (blinker == Thread.currentThread()) {  //官方推荐的终止线程的方式
                if(mediaPlayer == null)break;
                if(CurrentPosition >= total)break;

                //获取播放的进度（已播放时间）,进度条显示进度
                try {
                    Thread.sleep(10); //10ms
                    if (mediaPlayer != null) {
                        CurrentPosition = mediaPlayer.getCurrentPosition();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                PlayerActivity.seekBar.setProgress(CurrentPosition);

                //歌词同步显示
                Long lrcPosition = lrcProcessor.getNextLrcTime();
                if(lrcPosition != null){
                    if(CurrentPosition >= lrcPosition){
                        Message msg = new Message();
                        msg.obj = lrcProcessor.getCurrentLrcContent();
                        PlayerActivity.lrcEventHander.sendMessage(msg);
                        lrcProcessor.pollQueue();//将缓存队列中的数据向队头推进，队头被移出来。
                    }
                }
            }
        }
    }

    class MySeekBarChangeListener implements SeekBar.OnSeekBarChangeListener{
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            isTouchingSeekBar = false;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(mediaPlayer==null){
                return;
            }
            /**
             * 判断：if
             * 1、确认播放没有终止
             * 2、确认是手动触碰SeekBar引起的进度条进度变化---确保播放流畅（如果没有这一步判断，播放将会很卡，因为自由播放过程中的进度变化也会进入这个回调函数）
             * {
             *     按手动触碰的位置--改变mp3的播放进度。
             * }
             * */
            if(isTouchingSeekBar && (statusOfMedia!=StatusOfMedia.isStoped)){ //
                mediaPlayer.seekTo(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isTouchingSeekBar = true;
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

    /*@Override
    public void onPrepared(MediaPlayer mp) {
        mediaPlayer.start();
    }*/

    @Override
    public void onDestroy() {
        System.out.println("Playservice---===onDestroy");//test
        super.onDestroy();

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        System.out.println("service onDestroy");
    }



    /**
     * 返回一个Binder对象
     */
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    public class MyBinder extends Binder {
        /**
         * 获取当前Service的实例
         * @return
         */
        public PlayService getService(){
            return PlayService.this;
        }
    }
}




