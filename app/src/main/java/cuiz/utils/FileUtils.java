package cuiz.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import android.os.Environment;
import android.support.annotation.NonNull;

import cuiz.model.Mp3Info;

public class FileUtils{
    private String SDPATH;

    public String getSDPATH() {
        return SDPATH;
    }
    public FileUtils() {
        //得到当前外部存储设备的目录
        // /SDCARD
        SDPATH = Environment.getExternalStorageDirectory() + "/";
    }
    /**
     * 在SD卡上创建文件
     *
     * @throws IOException
     */
    public File creatSDFile(String fileName) throws IOException {
        File file = new File(SDPATH + fileName);
        file.createNewFile();
        return file;
    }

    /**
     * 在SD卡上创建目录
     *
     * @param dirName
     */
    public File creatSDDir(String dirName) {
        File dir = new File(SDPATH + dirName);
        dir.mkdirs();
        return dir;
    }

    /**
     * 判断SD卡上的文件夹是否存在
     */
    public boolean isFileExist(String fileName){
        File file = new File(SDPATH + fileName);
        return file.exists();
    }

    /**
     * 将一个InputStream里面的数据写入到SD卡中
     */
    public File write2SDFromInput(String path,String fileName,InputStream input){
        File file = null;
        OutputStream output = null;
        try{
            creatSDDir(path);
            file = creatSDFile(path + fileName);
            output = new FileOutputStream(file);
            byte buffer [] = new byte[4 * 1024];
            while((input.read(buffer)) != -1){
                output.write(buffer);
            }
            output.flush();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                output.close();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * 读取目录中的MP3文件的名字和大小
     * */
    public List<Mp3Info> getMp3FilesList(String path){
        List<Mp3Info> mp3InfoList = new ArrayList<>();

        //先新建一个FIle对象，这个File对象就代表这个文件夹、
        File file = new File(path);

        //listFiles方法---返回当前文件夹中所有的文件
        File[] files = file.listFiles();

        for(int i = 0 ;i<files.length;i++){
            Mp3Info mp3Info = new Mp3Info();
            mp3Info.setMp3Name(files[i].getName());
            mp3Info.setMp3Size(Long.toString(files[i].length()));

            mp3InfoList.add(mp3Info);
        }

        return mp3InfoList;

    }

}