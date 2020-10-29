package com.xatu.easyChat.controller;



import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.xatu.easyChat.entity.User;
import com.xatu.easyChat.entity.bo.UserBo;
import com.xatu.easyChat.entity.vo.UserVo;
import com.xatu.easyChat.service.UserService;
import com.xatu.easyChat.utils.*;
import org.apache.commons.logging.Log;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;


@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    /**
     * 根据用户uid来查询用户
     * @param uid
     * @param model
     * @return
     */
    @RequestMapping("/getUserById/{uid}")
    public String getUserById(@PathVariable String uid, Model model){
        User user = userService.getUserById(uid);
        model.addAttribute("user",user);
        return "index";
    }


    /**
     * 用户登陆注册一体化方法
     * @param user
     * @return
     */
    @RequestMapping("/registerOrLogin")
    @ResponseBody
    public JSONResult registerOrLogin(@RequestBody User user) {

        //判断用户是否注册过
        User existUser = userService.queryUserIsExist(user.getUsername());
        if(existUser != null) {
            //直接登录
            String password = existUser.getPassword();
            if(!MD5Utils.getPwd(user.getPassword()).equals(password)) {
                //密码错误
                return JSONResult.errorMap("密码错误！");
            }

        }else {
            //注册用户
            user.setNickname(user.getUsername());
            user.setQrcode("");
            user.setPassword(MD5Utils.getPwd(user.getPassword()));
            user.setFaceImage("");
            user.setFaceImageBig("");
            int insert = userService.insert(user);
            if (insert < 1) {
                return JSONResult.errorMsg("注册失败！");
            }
            existUser = userService.queryUserIsExist(user.getUsername());
        }
        System.out.println("用户" + user.getUsername() + "登录成功");
        //封装返回结果
        UserVo userVo = new UserVo();
        //注册或登录成功，将用户信息返回到前台
        BeanUtils.copyProperties(existUser,userVo);
        return JSONResult.ok(userVo);
    }


    /**
     * 用户图片上传阿里云OSS
     */
    @RequestMapping("/uploadFaceBase64")
    @ResponseBody
    public JSONResult uploadFaceBase64(@RequestBody UserBo userBo) throws Exception {
        // Endpoint以杭州为例，其它Region请按实际情况填写。
        String endpoint = OSSConstantPropertiesUtil.END_POINT;
        // 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建。
        String accessKeyId = OSSConstantPropertiesUtil.ACCESS_KEY_ID;
        String accessKeySecret = OSSConstantPropertiesUtil.ACCESS_KEY_SECRECT;
        String bucketName = OSSConstantPropertiesUtil.BUCKET_NAME;
        String faceData = userBo.getFaceData();
        String userId = userBo.getUserId();
        //服务器Base64转文件 文件暂存路径
        String filePath = "D://test/" + userId + "/userFaceImage.png";
        Date curDate = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        String format = simpleDateFormat.format(curDate);
        // String fileName = format + "/" + userId + "/userFaceImage.png";
        FileUtils.base64ToFile(filePath, faceData);
        //file转 multipartFile
        MultipartFile multipartFile = FileUtils.fileToMultipart(filePath);
        String fileName = userId + "/" + format + "/" + multipartFile.getOriginalFilename();
        // 创建OSSClient实例
        OSS ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
        InputStream inputStream = null;

        try{
            inputStream = multipartFile.getInputStream();
            // 上传文件流
            //第一个参数 Bucket名称      第二个参数    上传到oss文件路径和文件名称  /aa/bb/1.jpg
            ossClient.putObject(bucketName,fileName, inputStream);
            // 关闭OSSClient。
            ossClient.shutdown();
        }catch (Exception e){
            e.printStackTrace();
        }
        String url = "https://"+bucketName+"."+endpoint+"/"+fileName;
        //保存更改头像信息到数据库
        userService.updateUserAvatar(userId,fileName);
        User userById = userService.getUserById(userId);
        return JSONResult.ok(userById);

    }

    /**
     * 用户修改昵称
     */
    @PostMapping("/updateNickName")
    @ResponseBody
    public JSONResult updateNickName(@RequestBody User user){
        User newUser = userService.updateUserInfo(user);
        //返回更新后的用户信息
        System.out.println(newUser);
        return JSONResult.ok(newUser);
    }

//    /**
//     * 用户图片上传阿里云OSS
//     */
//    @RequestMapping("/uploadFaceBase64")
//    @ResponseBody
//    public JSONResult uploadFaceBase64(@RequestBody UserBo userBo) {
//        try{
//            //获取前端传过来的base64的字符串，然后转为文件对象在进行上传
//            String faceData = userBo.getFaceData();
//            String userId = userBo.getUserId();
//            //服务器Base64转文件 文件暂存路径
//            String filePath = "D://test/" + userId + "/userFaceImage.png";
//            Date curDate = new Date();
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");
//            String format = simpleDateFormat.format(curDate);
//            String fileName = format + "/" + userId + "/userFaceImage.png";
//            FileUtils.base64ToFile(filePath, faceData);
//            //file转 multipartFile
//            MultipartFile multipartFile = FileUtils.fileToMultipart(filePath);
//            //上传到OSS
//            OSSClientUtil ossClientUtil = new OSSClientUtil();
//            ossClientUtil.init();
//            String url = ossClientUtil.uploadFile2OSS(multipartFile.getInputStream(), fileName);
//            System.out.println(url);
//            //更新用户信息
//            User user = new User();
//            user.setId(userBo.getUserId());
//            user.setFaceImage(url);
//            user.setFaceImageBig(url);
//            User result = userService.updateUserInfo(user);
//            return  JSONResult.ok(result);
//        }catch (Exception e) {
//            e.printStackTrace();
//            return JSONResult.errorMsg("time out!");
//        }
//    }
//

}
