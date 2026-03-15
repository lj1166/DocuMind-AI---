package cn.edu.cdu.documind;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("cn.edu.cdu.documind.mapper")
public class DocumindApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumindApplication.class, args);
    }

}
