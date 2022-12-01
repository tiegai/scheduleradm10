package com.nike.springboottemplate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.nike.wingtips.springboot.WingtipsSpringBootConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

//import okhttp3.OkHttpClient;

@SpringBootApplication
@EnableTransactionManagement
@ComponentScan(basePackages = {"com.nike"})
@Import({ WingtipsSpringBootConfiguration.class })
public class Application {

	public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    //@Bean
    //OkHttpClient okHttpClient() {
    //    return new OkHttpClient();
    //}
}
