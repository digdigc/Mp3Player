package cuiz.mp3player.Mp3PlayService;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import cuiz.Downloader.HttpDownloader;
import cuiz.model.Mp3Info;
import cuiz.mp3player.AppConstant;
import cuiz.mp3player.OnlineListActivity;
/**
 * Created by cuiz on 2016/4/7.
 */
public class DownloadService extends Service{
    private Mp3Info currentDownMp3 = null;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //存放当前点击的MP3名字
        currentDownMp3 = (Mp3Info) intent.getSerializableExtra("MP3_INFO");
        System.out.println("service:--->>>"+ currentDownMp3.getMp3Name()); //测试

        //开启下载Mp3线程
        DownloadThead downloadMp3 = new DownloadThead(currentDownMp3.getLrcName());
        Thread downloadMp3Thread = new Thread(downloadMp3);
        downloadMp3Thread.start();

        //开启下载Mp3 Lrc线程
        DownloadThead downloadMp3Lrc = new DownloadThead(currentDownMp3.getMp3Name());
        Thread downloadMp3LrcThread = new Thread(downloadMp3Lrc);
        downloadMp3LrcThread.start();

        return super.onStartCommand(intent, flags, startId);
    }

    class DownloadThead implements Runnable{
        String downFileName = null;

        DownloadThead(String downFileName){
            this.downFileName = downFileName;
        }

        @Override
        public void run() {

            //下载地址：http://192.168.1.104:8080/mp3/a1.mp3
            //根据Mp3的名字生成下载地址
            String mp3Url = AppConstant.BASE_HTTP_URL + downFileName;
            //生成下载文件所用的对象
            HttpDownloader httpDownloader = new HttpDownloader();
            //将文件下载下来，并存储到SDCard当中
            int result = httpDownloader.downFile(mp3Url,"mp3/", downFileName);
            String resultMsg = null;
            if(result==0){
                resultMsg = downFileName+"下载成功";
            }else if (result == -1){
                resultMsg = "下载失败";
            }else if (result == 1){
                resultMsg = downFileName+"已经存在，请不要重复下载";
            }
            System.out.println("Service:--->"+resultMsg);



            //发送下载完成的消息给UI线程，让UI线程去通知用户
            Message message = new Message();
            message.what = OnlineListActivity.DOWN_MP3;
            message.obj = resultMsg;
            OnlineListActivity.myHandler.sendMessage(message);

            //所有UI操作都必须放在UI线程中
            //Toast.makeText(DownloadService.this, resultMsg,Toast.LENGTH_SHORT).show();
        }
    }
}
