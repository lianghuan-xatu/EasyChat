package com.xatu.easyChat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.mapper.common.BaseMapper;

@SpringBootApplication()
@MapperScan(basePackages = "com.xatu.easyChat.mapper")
public class EasyChatWebApplication {
    public static void main(String args[]){
        SpringApplication.run(EasyChatWebApplication.class,args);
        }
}
