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

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.widget.TextView;
import android.widget.Toast;

import cuiz.model.Mp3Info;
import cuiz.mp3player.LocalMp3LIstActivity;

public class FileUtils{
    private String SDPATH;
    private List<Mp3Info> mp3Infos = null;

    public String getSDPATH() {
        return SDPATH;
    }
    public FileUtils() {
        //得到当前外部存储设备的目录
        // /SDCARD
        SDPATH = Environment.getExternalStorageDirectory() + "/";

        mp3Infos = new ArrayList<>();
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

        for(File fileVer:files){
            if (fileVer.getName().endsWith(".mp3"));{
                Mp3Info mp3Info = new Mp3Info();
                mp3Info.setMp3Name(fileVer.getName());
                mp3Info.setMp3Size(Long.toString(fileVer.length()));
                mp3InfoList.add(mp3Info);
            }
        }

        return mp3InfoList;
    }

    /**
     * 读取指定目录及其子目录，所有MP3文件的名字和大小
     * */

    public List<Mp3Info> getLocalMp3List(String path){
        //先新建一个FIle对象，这个File对象就代表这个文件夹、
        //File file = new File(SDPATH);
        File file = new File(path);

        LocalMp3LIstActivity.textViewFindResult.setText(""); //清空
        searchLocalMp3(file);
        return mp3Infos;
    }
    public void searchLocalMp3(File file){

        //listFiles方法---返回当前文件夹中所有的文件
        File[] files = file.listFiles();
        if(files==null)return;
        if(files.length>0) {
            for(File fileVer:files){
                if(fileVer.isFile()){
                    System.out.println("文件=========="+fileVer.getPath()+fileVer.getPath());
                    if (fileVer.getName().endsWith(".mp3")){
                        Mp3Info mp3Info = new Mp3Info();
                        mp3Info.setMp3Name(fileVer.getName());
                        mp3Info.setMp3Size(Long.toString(fileVer.length()));
                        mp3Infos.add(mp3Info);

                        LocalMp3LIstActivity.textViewFindResult.append("Found: "+fileVer.getName()+"\n");
                    }
                }else if(fileVer.isDirectory()){
                    this.searchLocalMp3(fileVer);
                    System.out.println("目录=========="+fileVer.getPath());
                }else{
                    System.out.println("不是文件也不是目录"+fileVer.getName());
                }
            }
        }

    }

    private void search(File fileold) {
        try{
            File[] files=fileold.listFiles();
            if(files.length>0)
            {
                for(int j=0;j<files.length;j++)
                {
                    if(!files[j].isDirectory())
                    {
                        if(files[j].getName().contains("mp3"))
                        {
                            System.out.println(files[j].getName().toString()+"---"+files[j].getPath().toString());
                        }
                    }
                    else{
                        this.search(files[j]);
                        System.out.println(files[j].getName().toString()+"---"+files[j].getPath().toString());
                    }
                }
            }
        }
        catch(Exception e) {
        }
    }
}