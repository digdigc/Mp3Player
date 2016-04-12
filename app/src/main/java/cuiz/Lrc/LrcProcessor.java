package cuiz.Lrc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by cuiz on 2016/4/12.
 */
public class LrcProcessor {

    private Queue<String> lrcContentQ;
    private Queue<Long> lrcTimeQ;

    private Long lrcTime = null;
    private String lrcContent = null;

    /**
     * 无参构造函数用来实例化对象
     */
    public LrcProcessor() {
        lrcContentQ = new LinkedList<>();
        lrcTimeQ = new LinkedList<>();
    }

    /**
     * 读取歌词
     * @param path
     * @return
     */
    public String readLRC(String path) {
        //定义一个StringBuilder对象，用来存放歌词内容
        StringBuilder stringBuilder = new StringBuilder();
        File file = new File(path.replace(".mp3", ".lrc"));

        try {
            //创建一个文件输入流对象
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String tempLine = null;
            while((tempLine = br.readLine()) != null) {
                //替换字符
                tempLine = tempLine.replace("[", "");
                tempLine = tempLine.replace("]", "@");

                //分离“@”字符
                String splitLrcData[] = tempLine.split("@");
                if(splitLrcData.length > 1) {
                    //处理歌词取得歌曲的时间
                    lrcTimeQ.offer(time2Long(splitLrcData[0]));

                    lrcContentQ.offer(splitLrcData[1]);

                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            stringBuilder.append("木有歌词文件，赶紧去下载！...");
        } catch (IOException e) {
            e.printStackTrace();
            stringBuilder.append("木有读取到歌词哦！");
        }
        return stringBuilder.toString();
    }
    /**
     * 解析歌词时间
     * 歌词内容格式如下：
     * [00:02.32]陈奕迅
     * [00:03.43]好久不见
     * [00:05.22]歌词制作  王涛
     * @param timeStr
     * @return
     */
    public Long time2Long(String timeStr) {
        timeStr = timeStr.replace(":", ".");
        timeStr = timeStr.replace(".", "@");

        String timeData[] = timeStr.split("@"); //将时间分隔成字符串数组

        //分离出分、秒并转换为整型
        Long minute = Long.parseLong(timeData[0]);
        Long second = Long.parseLong(timeData[1]);
        Long millisecond = Long.parseLong(timeData[2]);

        //计算上一行与下一行的时间转换为毫秒数
        Long currentTime = (minute * 60 + second) * 1000 + millisecond * 10;
        return currentTime;
    }

    public void pollQueue(){
        if(lrcTimeQ.peek() != null){
            lrcTime = lrcTimeQ.poll();
            System.out.println("::"+lrcTime);
        }

        if(lrcContentQ.peek()!=null){
            lrcContent = lrcContentQ.poll();
            System.out.println("::"+lrcContent);
        }
    }


    public Long getNextLrcTime(){
        return lrcTime;
    }

    public String getCurrentLrcContent() {
        return lrcContent;
    }
}
