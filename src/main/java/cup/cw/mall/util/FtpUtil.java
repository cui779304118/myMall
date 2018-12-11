package cup.cw.mall.util;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * created by cuiwei on 2018/10/27
 * 接受List集合，把list上传到自定义的ftp服务器上面去
 */
public class FtpUtil {
    private static Logger logger = LoggerFactory.getLogger(FtpUtil.class);

    private static String ip = PropertiesUtil.getProperty("ftp.server.ip");
    private static String username = PropertiesUtil.getProperty("ftp.server.username");
    private static String password = PropertiesUtil.getProperty("ftp.server.password");
    private static int port = 21;
    private static FTPClient ftpClient;

    public static boolean uploadFile(List<File> fileList){
        return uploadFile("pub",fileList);
    }

    public static boolean uploadFile(String remotePath, List<File> fileList){
        logger.info("开始连接ftp服务器...");
        boolean uploaded = false;
        FileInputStream fis = null;
        if(!connectServer(ip,port,username,password)){
            return uploaded;
        }
        try {
            makeDirs(ftpClient,remotePath);
            ftpClient.setBufferSize(1024);
            ftpClient.setControlEncoding("UTF-8");
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.enterLocalActiveMode();
            for (File file : fileList){
                fis = new FileInputStream(file);
                uploaded = ftpClient.storeFile(file.getName(),fis);
            }
        } catch (IOException e) {
            logger.error("上传文件到ftp服务器异常",e);
        }finally {
            if (fis != null){
                try {
                    fis.close();
                    ftpClient.disconnect();
                } catch (IOException e) {
                    logger.error("关闭FileInputStream连接或者ftpClient异常！");
                }
            }
        }
        logger.info("上传到ftp服务器结束，上传结果：{}",uploaded);
        return uploaded;
    }

    private static boolean connectServer(String ip, int port, String username, String password){
        boolean isSuccess = false;
        ftpClient = new FTPClient();
        try {
            ftpClient.connect(ip);
            isSuccess = ftpClient.login(username,password);
            if (isSuccess)
                logger.info("连接FTP服务器成功！ ip:{},username:{}",new Object[]{ip,username});
            else
                logger.info("连接FTP服务器失败！ ip:{},username:{}",new Object[]{ip,username});
        } catch (IOException e) {
            logger.error("连接FTP服务器异常",e);
        }
        return isSuccess;
    }

    private static boolean makeDirs(FTPClient ftpClient,String path){
        String[] dirs = path.split("/");
        try {
            for (String dir : dirs){
                ftpClient.makeDirectory(dir);
                ftpClient.changeWorkingDirectory(dir);
            }
            return true;
        } catch (IOException e) {
            logger.error("ftp创建目录失败，path:{}",path,e);
        }
       return false;
    }

    public static void main(String[] args) {
        File file = new File("C:\\Users\\Lebron\\Desktop\\Fimage.exe");
        List<File> fileList = new ArrayList<>();
        fileList.add(file);
        FtpUtil.uploadFile("productPic/2018/10/30",fileList);
    }
}
