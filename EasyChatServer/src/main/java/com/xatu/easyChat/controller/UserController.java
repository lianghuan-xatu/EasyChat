package com.xatu.easyChat.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xatu.easyChat.entity.User;
import com.xatu.easyChat.entity.vo.UserVo;
import com.xatu.easyChat.service.UserService;
import com.xatu.easyChat.utils.JSONResult;
import com.xatu.easyChat.utils.MD5Utils;
import org.apache.commons.logging.Log;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@CrossOrigin
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
            if(!MD5Utils.getPwd(user.getPassword()).equals(MD5Utils.getPwd(password))) {
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
        }
            System.out.println("用户" + user.getUsername() + "登录成功");
            //注册或登录成功，将用户信息返回到前台
            UserVo userVo = new UserVo();
            BeanUtils.copyProperties(user,userVo);
            return JSONResult.ok(userVo);
    }




}
