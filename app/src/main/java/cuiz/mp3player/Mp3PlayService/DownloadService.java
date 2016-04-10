package cuiz.mp3player.Mp3PlayService;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import cuiz.Downloader.HttpDownloader;
import cuiz.mp3player.Mp3ListActivity;
/**
 * Created by cuiz on 2016/4/7.
 */
public class DownloadService extends Service{
    private String currentDownMp3Name = null;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //存放当前点击的MP3名字
        currentDownMp3Name = Mp3ListActivity.currentSelectedMp3Name;
        System.out.println("service:--->>>"+ currentDownMp3Name); //测试

        //开启下载线程
        DownloadThead downloadThead = new DownloadThead();
        Thread thread = new Thread(downloadThead);
        thread.start();

        return super.onStartCommand(intent, flags, startId);
    }

    class DownloadThead implements Runnable{
        @Override
        public void run() {

            //下载地址：http://192.168.1.104:8080/mp3/a1.mp3
            //根据Mp3的名字生成下载地址
            String mp3Url = "http://192.168.1.104:8080/mp3/"+currentDownMp3Name;
            //生成下载文件所用的对象
            HttpDownloader httpDownloader = new HttpDownloader();
            //将文件下载下来，并存储到SDCard当中
            int result = httpDownloader.downFile(mp3Url,"mp3/",currentDownMp3Name);
            String resultMsg = null;
            if(result==0){
                resultMsg = "下载成功";
            }else if (result == -1){
                resultMsg = "下载失败";
            }else if (result == 1){
                resultMsg = "文件已经存在，请不要重复下载";
            }
            System.out.println("Service:--->"+resultMsg);



            //发送下载完成的消息给UI线程，让UI线程去通知用户
            Message message = new Message();
            message.what = Mp3ListActivity.DOWN_MP3;
            message.obj = resultMsg;
            Mp3ListActivity.myHandler.sendMessage(message);

            //所有UI操作都必须放在UI线程中
            //Toast.makeText(DownloadService.this, resultMsg,Toast.LENGTH_SHORT).show();
        }
    }
}
