package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;


    /*
    *   上传图片
    * */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){
        //file是一个临时文件，需要将其保存到服务器的某个位置，否则当请求结束时，临时文件会被删除
        log.info("上传文件:{}",file.getOriginalFilename());

        // 原始文件名
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        //uuid
        String uuidFilename = UUID.randomUUID().toString() + suffix;

        //创建目录
        File dir = new File(basePath);
        //如果目录不存在，则创建
        if(!dir.exists()){
            dir.mkdirs();
        }



        //将临时文件转存到指定位置
        try {
            file.transferTo( new File(basePath + uuidFilename));
            //到mac下载里面的绝对路径

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return R.success(uuidFilename);
    }


    /*
    *   下载图片
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){

        try {
            //输入流，通过输入流读取图片文件
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));
            //输出流，将图片响应到浏览器
            ServletOutputStream outputStream = response.getOutputStream();


            response.setContentType("image/jpeg");


            int len = 0;
            byte[] bytes = new byte[1024];
            while((len = fileInputStream.read(bytes))!= -1){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }


            outputStream.close();
            fileInputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }



    }
}
