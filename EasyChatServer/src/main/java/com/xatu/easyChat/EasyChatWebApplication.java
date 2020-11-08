package com.xatu.easyChat;

import com.xatu.easyChat.easyChatServer.utils.SpringUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import tk.mybatis.mapper.common.BaseMapper;

@SpringBootApplication()
@MapperScan(basePackages = "com.xatu.easyChat.mapper")
public class EasyChatWebApplication {


    @Bean
    public SpringUtil getSpringUtil(){
        return new SpringUtil();
    }
    public static void main(String args[]){
        SpringApplication.run(EasyChatWebApplication.class,args);
        }

}
