package com.xatu.easyChat.controller;

import com.xatu.easyChat.entity.User;
import com.xatu.easyChat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @RequestMapping("/getUserById/{uid}")
    public String getUserById(@PathVariable String uid, Model model){
        User user = userService.getUserById(uid);

        model.addAttribute("user",user);
        return "index";
    }
}
