package cup.cw.mall.service.impl;

import com.google.common.collect.Lists;
import cup.cw.mall.common.ServerResponse;
import cup.cw.mall.service.IFileService;
import cup.cw.mall.util.DateTimeUtil;
import cup.cw.mall.util.FtpUtil;
import cup.cw.mall.util.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * created by cuiwei on 2018/10/28
 */
@Service("fileService")
public class FileServiceImpl implements IFileService {
    private static Logger logger  = LoggerFactory.getLogger(FileServiceImpl.class);
    private static String TEMP_DIR = "./temp/";

    /**
     * TODO 还需存储到数据库
     * @param file
     * @param filePath
     * @return
     */
    @Override
    public ServerResponse upload(MultipartFile file,String filePath) {
        String fileName = file.getOriginalFilename();
        if (!checkFile(fileName)){
            return ServerResponse.createByError("上传失败！不支持该种类型文件！");
        }
        //扩展名
        String uploadFileName = wrapFileName(fileName);
        String uploadDir = filePath + "/" +  DateTimeUtil.dateToStr(new Date(),"yyyy/MM/dd/");
        String uploadPath = uploadDir + uploadFileName;
        logger.info("文件开始上传，源文件名：{}，存储路径：{}",fileName,uploadPath);

        File fileDir = new File(TEMP_DIR);
        if (!fileDir.exists()){
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        File targetFile = new File(TEMP_DIR,uploadFileName);
        try {
            file.transferTo(targetFile);
            logger.info("文件临时上传到本地成功！源文件名：{},存储路径：{}",fileName,uploadFileName);
            //文件已经上传成功
            boolean isUpload = FtpUtil.uploadFile(uploadDir,Lists.newArrayList(targetFile));
            //已经将targetFile上传到FTP服务器上面
            boolean isDelete = targetFile.delete();
            //上传完后，删除upload下面的文件
            if (isUpload && isDelete){
                String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + uploadPath;
                Map<String,String> fileMap = new HashMap<>();
                fileMap.put("url",url);
                logger.info("文件上传成功！源文件名：{},存储路径：{}",fileName,uploadFileName);
                return ServerResponse.createBySuccess(fileMap);
            }
        } catch (IOException e) {
            logger.error("文件上传异常，源文件名：{},存储路径：{}",fileName,uploadFileName,e);
        }
        return ServerResponse.createByError("上传图片失败！");
    }

    private static String wrapFileName(String fileName){
        String fileNameSuffix = getSuffix(fileName);
        String uuidFileName = UUID.randomUUID().toString() + "." + fileNameSuffix;
        return uuidFileName.replace("-","");
    }

    private static boolean checkFile(String fileName){
        String fileSuffix = getSuffix(fileName).toLowerCase();
//        return fileSuffix.matches("(jpg|jpeg|png|bmp|gif)");
        return true;
    }

    private static String getSuffix(String fileName){
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    public static void main(String[] args) {
        System.out.println(FileServiceImpl.checkFile("cuiwei.jpg"));
    }
}
