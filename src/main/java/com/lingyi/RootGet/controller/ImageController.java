package com.lingyi.RootGet.controller;

import com.lingyi.RootGet.tools.Constant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@RestController
public class ImageController {
    @GetMapping("/image")
    public void getImage(@RequestParam("filePath") String filePath, HttpServletResponse response) {
        File file = new File( filePath);
        response.setHeader("Content-Type", "image/" + Constant.getFileType(file));
        response.setContentLengthLong(file.length());
        ServletOutputStream outputStream;
        try {
            outputStream = response.getOutputStream();
            writeStream(file, outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void writeStream(File file, OutputStream outputStream) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        int len;
        byte[] bytes = new byte[2048];
        while((len=bis.read(bytes))!=-1){
            outputStream.write(bytes,0,len);
        }
        bis.close();
    }
}
