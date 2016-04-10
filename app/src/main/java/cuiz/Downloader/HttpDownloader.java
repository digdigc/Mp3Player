package cuiz.Downloader;

import android.os.Message;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import cuiz.mp3player.MainActivity;
import cuiz.utils.FileUtils;

import static cuiz.mp3player.MainActivity.*;

/**
 * Created by cuiz on 2016/4/5.
 */
public class HttpDownloader {

    /**
     * 根据URL下载文件，前提是这个文件当中的内容是文本，
     * 1.创建一个URL对象
     * 2.通过URL对象，创建一个HttpURLConnection对象
     * 3.得到InputStram
     * 4.从InputStream当中读取数据
     * @param urlStr
     * ===================
     * 需要在调用此函数的线程中，处理http请求得出的网络文本数据
     * message.what = MainActivity.DOWN_XML;
        message.obj = response.toString();
     */
    public String downloadText(String urlStr) {

        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            //connection.setRequestMethod("POST");
            //BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            //bufferedWriter.write("username=admin&password=123456");


            String line;
            StringBuilder response = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                System.out.print("-------------");
                response.append(line);
            }
            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return null;
    }

    /**
     * 该函数返回整形 -1：代表下载文件出错 0：代表下载文件成功 1：代表文件已经存在
     */
	public int downFile(String urlStr, String path, String fileName) {


		InputStream inputStream = null;
		try {
			FileUtils fileUtils = new FileUtils();
            inputStream = getInputStreamFromUrl(urlStr);

			if (fileUtils.isFileExist(path+fileName)) {
				return 1;
			} else {

				File resultFile = fileUtils.write2SDFromInput(path,fileName, inputStream);
				if (resultFile == null) {
					return -1;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		} finally {
			try {
				inputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return 0;
	}

    /**
     * 根据URL得到输入流
     *
     * @param urlStr
     * @return
     * @throws MalformedURLException
     * @throws IOException
     */
    public InputStream getInputStreamFromUrl(String urlStr)
            throws MalformedURLException, IOException {
        URL url = new URL(urlStr);
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        InputStream inputStream = urlConn.getInputStream();
        return inputStream;
    }
}
