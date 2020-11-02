package com.xatu.easyChat.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.xatu.easyChat.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OSSUtils {
    // Endpoint以杭州为例，其它Region请按实际情况填写。
    private static String endpoint = OSSConstantPropertiesUtil.END_POINT;
    // 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建。
    private static String accessKeyId = OSSConstantPropertiesUtil.ACCESS_KEY_ID;
    private static String accessKeySecret = OSSConstantPropertiesUtil.ACCESS_KEY_SECRECT;
    private static String bucketName = OSSConstantPropertiesUtil.BUCKET_NAME;
    private static OSS ossClient = null;
    private static String resultUrl= "";
    public static String uploadMultiPartFile(String fileName,MultipartFile multipartFile) {
        try{
            ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
            InputStream inputStream = multipartFile.getInputStream();
            // 上传文件流
            //第一个参数 Bucket名称      第二个参数    上传到oss文件路径和文件名称  /aa/bb/1.jpg
            ossClient.putObject(bucketName,fileName, inputStream);
            // 关闭OSSClient。
            ossClient.shutdown();
            resultUrl = "https://" + bucketName + "." + endpoint + "/" + fileName;
            return resultUrl;
        }catch (Exception e) {
            ossClient.shutdown();
        }
        return resultUrl;

    }

}
