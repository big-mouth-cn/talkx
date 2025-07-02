package org.bigmouth.gpt.controller;

import lombok.extern.slf4j.Slf4j;
import org.bigmouth.gpt.service.FileUploadService;
import org.bigmouth.gpt.service.FileUploadServiceFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 文件上传 前端控制器
 * </p>
 *
 * @author zhengwangeng
 * @since 2019-12-17
 */
@Slf4j
@RestController
@RequestMapping("/upload")
public class FileUploadController {
    private final FileUploadServiceFactory fileUploadServiceFactory;

    public FileUploadController(FileUploadServiceFactory fileUploadServiceFactory) {
        this.fileUploadServiceFactory = fileUploadServiceFactory;
    }

    @PostMapping("/img")
    public String uploadImg(@RequestParam("file") MultipartFile file) {
        return fileUploadServiceFactory.get().uploadImg(file);
    }

    @PostMapping("/file")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        return fileUploadServiceFactory.get().uploadFile(file);
    }
}
