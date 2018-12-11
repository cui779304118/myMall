package cup.cw.mall.service;

import cup.cw.mall.common.ServerResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * created by cuiwei on 2018/10/28
 * 文件服务类
 */
public interface IFileService {

    ServerResponse upload(MultipartFile file,String filePath);
}
