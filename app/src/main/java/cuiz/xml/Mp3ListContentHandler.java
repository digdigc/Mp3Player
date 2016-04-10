package cuiz.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.List;

import cuiz.model.Mp3Info;

/**
 * Created by cuiz on 2016/4/6.
 */
public class Mp3ListContentHandler extends DefaultHandler{


    private List<Mp3Info> infos = null; //每个mp3就是一个Info对象  //建立引用，由外部传入
    private Mp3Info mp3Info = null;
    private String tagName = null;

    public Mp3ListContentHandler(List<Mp3Info> infos) {
        this.infos = infos;
    }

    public List<Mp3Info> getInfos() {
        return infos;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String temp = new String(ch,start,length);

        if(tagName.equals("id")){
            mp3Info.setId(temp);
        }else if(tagName.equals("mp3name")){
            mp3Info.setMp3Name(temp);
        }else if(tagName.equals("mp3size")){
            mp3Info.setMp3Size(temp);
        }else if(tagName.equals("lrcname")){
            mp3Info.setLrcName(temp);
        }else if(tagName.equals("lrcsize")){
            mp3Info.setLrcSize(temp);
        }
    }


    @Override
    public void startDocument() throws SAXException {
        System.out.println("the start of XML");
    }

    @Override
    public void endDocument() throws SAXException {
        System.out.println("the end of XML");
    }



    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        if(localName.equals("resource")){
            mp3Info = new Mp3Info();
        }

        this.tagName = localName; //记录正在读取的标签的标签名

    }
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(localName.equals("resource")){  //这里使用localName ，而不是tagName
            //测试点：在这打断点，检查list容器是否有添加info对象，info对象是否无误。
            infos.add(mp3Info);
        }

        this.tagName = "";//清除记录的标签名

    }

}
