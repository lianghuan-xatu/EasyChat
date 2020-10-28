package com.xatu.easyChat.utils;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class OSSClientUtil {

    Log log = LogFactory.getLog(OSSClientUtil.class);
    // endpoint以杭州为例，其它region请按实际情况填写
    private String endpoint = "http://oss-cn-hzfinance.aliyuncs.com";
    // accessKey
    private String accessKeyId = OSSConstantPropertiesUtil.ACCESS_KEY_ID;
    private String accessKeySecret = OSSConstantPropertiesUtil.ACCESS_KEY_SECRECT;
    //空间
    private String bucketName =OSSConstantPropertiesUtil.BUCKET_NAME;


    private OSSClient ossClient;

    public OSSClientUtil(){
        ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
    }
    /**
     * 初始化
     */
    public void init(){
        ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
    }
    /**
     * 销毁
     */
    public void destory(){
        ossClient.shutdown();
    }

    /**
     * 上传到OSS服务器  如果同名文件会覆盖服务器上的
     * @param instream  文件流
     * @param fileName  文件名称 包括后缀名
     * @return  出错返回"" ,唯一MD5数字签名
     */
    public String uploadFile2OSS(InputStream instream , String fileName){
        String ret = "";
        try {
            //创建上传Object的Metadata
            ObjectMetadata objectMetadata=new ObjectMetadata();
            objectMetadata.setContentLength(instream .available());
            objectMetadata.setCacheControl("no-cache");
            objectMetadata.setHeader("Pragma", "no-cache");
            objectMetadata.setContentType(getcontentType(fileName.substring(fileName.lastIndexOf("."))));
            objectMetadata.setContentDisposition("inline;filename=" + fileName);
            //上传文件
            PutObjectResult putResult = ossClient.putObject(bucketName, fileName, instream , objectMetadata);

            ret =  putResult.getETag();
        } catch (IOException e) {
            log.error(e.getMessage(),e);
        }finally{
            try {
                instream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }
    /**
     * 从OSS获取文件
     * @param filename 文件名
     * @return InputStream 调用方法把流关闭  文件不存在返回null
     */
    public InputStream downFileFromOSS(String filename){
        boolean fileExist = ossClient.doesObjectExist(bucketName, filename);
        if(!fileExist)
            return null;
        OSSObject ossObj = ossClient.getObject(bucketName,  filename);
        return ossObj.getObjectContent();
    }

    /**
     * 根据文件名删除OSS服务器上的文件
     * @param filename
     * @return
     */
    public String deleteFile(String filename){
        boolean fileExist = ossClient.doesObjectExist(bucketName,  filename);
        if(fileExist){
            ossClient.deleteObject(bucketName, filename);
            return "delok";
        }
        else
            return filename+" not found";
    }


    /**
     * Description: 判断OSS服务文件上传时文件的contentType
     * @param FilenameExtension 文件后缀
     * @return String
     */
    public static String getcontentType(String FilenameExtension){
        if(FilenameExtension.equalsIgnoreCase("bmp")){return "image/bmp";}
        if(FilenameExtension.equalsIgnoreCase("gif")){return "image/gif";}
        if(FilenameExtension.equalsIgnoreCase("jpeg")||
                FilenameExtension.equalsIgnoreCase("jpg")||
                FilenameExtension.equalsIgnoreCase("png")){return "image/jpeg";}
        if(FilenameExtension.equalsIgnoreCase("html")){return "text/html";}
        if(FilenameExtension.equalsIgnoreCase("txt")){return "text/plain";}
        if(FilenameExtension.equalsIgnoreCase("vsd")){return "application/vnd.visio";}
        if(FilenameExtension.equalsIgnoreCase("pptx")||
                FilenameExtension.equalsIgnoreCase("ppt")){return "application/vnd.ms-powerpoint";}
        if(FilenameExtension.equalsIgnoreCase("docx")||
                FilenameExtension.equalsIgnoreCase("doc")){return "application/msword";}
        if(FilenameExtension.equalsIgnoreCase("xml")){return "text/xml";}
        return "image/jpeg";
    }



}