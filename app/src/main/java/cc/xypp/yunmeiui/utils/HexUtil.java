package cc.xypp.yunmeiui.utils;

public class HexUtil {
    public static String hex2String(byte[] b, int off, int len){
        StringBuilder sb = new StringBuilder();
        for(int i = off; i < off + len; i++){
            sb.append((char)b[i]);
        }
        return sb.toString();
    }
}
