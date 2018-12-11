package cup.cw.mall.util;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * created by cuiwei on 2018/10/14
 */
public class MD5Util {

    private static final Logger logger = LoggerFactory.getLogger(MD5Util.class);

    public static void main(String[] args) {
        String str = "cuiwei";
        System.out.println(MD5Encode(str,"UTF"));
    }

    public static String MD5Encode(String str,String charsetName){
        if (StringUtils.isEmpty(str)) {
            logger.warn("the param is empty！");
            return null;
        }
        String MD5Str = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            if (StringUtils.isEmpty(charsetName)){
                    MD5Str = byteArr2HexStr(md.digest(str.getBytes("UTF-8")));
            }else{
                    MD5Str = byteArr2HexStr(md.digest(str.getBytes(charsetName)));
            }
        } catch (NoSuchAlgorithmException e) {
            logger.error("no such algorithm",e);
        }catch (UnsupportedEncodingException e) {
           logger.error("un support such encoding charset",e);
        }
        return MD5Str;
    }

    private static String byteArr2HexStr(byte[] byteArr){
        StringBuilder hexBuilder = new StringBuilder();
        for (int i = 0; i < byteArr.length; i++) {
            String temHexStr = Integer.toHexString(0xFF & byteArr[i]);//将byte转化为二进制表现形式
            System.out.print(temHexStr + " ");
            hexBuilder.append(temHexStr);
        }
        return hexBuilder.toString();
    }

}
