package cn.hwjcode.straberryguild;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("cn.hwjcode.straberryguild.**.mapper")
public class StraberryGuildApplication {

    public static void main(String[] args) {
        SpringApplication.run(StraberryGuildApplication.class, args);
    }
}
