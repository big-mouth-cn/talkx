package org.bigmouth.gpt.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author tangxiao
 * @date 2023/7/20
 * @since 1.0
 */
public interface FileUploadService {

    FileUploadType of();

    String uploadImg(MultipartFile file);

    String uploadImg(MultipartFile file, String parentDir);

    String uploadFile(MultipartFile file);

    String uploadFile(MultipartFile file, String parentDir);
}
