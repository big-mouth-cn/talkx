package org.bigmouth.gpt.service.impl;

import com.bxm.warcar.file.upload.HttpFileManager;
import com.bxm.warcar.file.upload.HttpFileRequest;
import com.bxm.warcar.utils.DateHelper;
import com.bxm.warcar.utils.UUIDHelper;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bigmouth.gpt.autoconfigure.file.HttpFileConfiguration;
import org.bigmouth.gpt.service.FileUploadType;
import org.bigmouth.gpt.service.FileUploadService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

/**
 * @author tangxiao
 * @date 2023/7/20
 * @since 1.0
 */
@Service
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {

    public final static Set<String> ALLOW_IMG_TYPE_SET = Sets.newHashSet("jpg", "jpeg", "png", "gif");
    public final static Set<String> ALLOW_TYPE_SET = Sets.newHashSet(
            "c", "cpp", "csv", "docx", "html", "java", "json", "md", "pdf", "php", "pptx", "py", "py", "rb", "tex", "txt",
            "css", "jpeg", "jpg", "js", "gif", "png", "tar", "ts", "xlsx", "xml", "zip",
            "wav", "mp3", "m4a"
    );

    private final HttpFileManager httpFileManager;
    private final HttpFileConfiguration httpFileConfiguration;

    public FileUploadServiceImpl(HttpFileManager httpFileManager, HttpFileConfiguration httpFileConfiguration) {
        this.httpFileManager = httpFileManager;
        this.httpFileConfiguration = httpFileConfiguration;
    }

    @Override
    public FileUploadType of() {
        return FileUploadType.ALIYUN_OSS;
    }

    @Override
    public String uploadImg(MultipartFile file) {
        return this.uploadImg(file, "images");
    }

    @Override
    public String uploadImg(MultipartFile file, String parentDir) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        // 取得当前上传文件的文件名称
        String fileName = file.getOriginalFilename();
        // 如果名称不为空,说明该文件存在，否则说明该文件不存在
        if (StringUtils.isNotBlank(fileName)) {
            String[] arr = fileName.split("\\.");
            if (arr.length < 2) {
                throw new IllegalArgumentException("上传文件名格式不正确！");
            }
            String format = arr[arr.length - 1];
            if (!ALLOW_IMG_TYPE_SET.contains(StringUtils.toRootLowerCase(format))) {
                throw new IllegalArgumentException("请上传正确类型的文件！");
            }
        }

        if (file.getSize() > httpFileConfiguration.getLimitSize()) {
            throw new IllegalArgumentException("上传文件大小不能超过" + httpFileConfiguration.getLimitSize() / (1024 * 1024.0) + "M！");
        }
        String uploadPath;
        try {
            HttpFileRequest request = new HttpFileRequest(file);
            request.setFileName(parentDir + "/" + DateHelper.format("yyyy/MM/dd") + "/" + UUIDHelper.generate());
            uploadPath = httpFileManager.upload(request);
        } catch (Exception e) {
            throw new RuntimeException("上传失败", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("img upload success, the url : {} ", uploadPath);
        }
        return uploadPath;
    }

    @Override
    public String uploadFile(MultipartFile file) {
        return this.uploadFile(file, "files");
    }

    @Override
    public String uploadFile(MultipartFile file, String parentDir) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        // 取得当前上传文件的文件名称
        String fileName = file.getOriginalFilename();
        // 如果名称不为空,说明该文件存在，否则说明该文件不存在
        if (StringUtils.isNotBlank(fileName)) {
            String[] arr = fileName.split("\\.");
            if (arr.length < 2) {
                throw new IllegalArgumentException("上传文件名格式不正确！");
            }
            String format = arr[arr.length - 1];
            if (!ALLOW_TYPE_SET.contains(StringUtils.toRootLowerCase(format))) {
                throw new IllegalArgumentException("请上传正确类型的文件！");
            }
        }

        if (file.getSize() > httpFileConfiguration.getLimitSize()) {
            throw new IllegalArgumentException("上传文件大小不能超过" + httpFileConfiguration.getLimitSize() / (1024 * 1024.0) + "M！");
        }
        String uploadPath;
        try {
            HttpFileRequest request = new HttpFileRequest(file);
            request.setFileName(parentDir + "/" + DateHelper.format("yyyy/MM/dd") + "/" + UUIDHelper.generate());
            uploadPath = httpFileManager.upload(request);
        } catch (Exception e) {
            throw new RuntimeException("上传失败", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("img upload success, the url : {} ", uploadPath);
        }
        return uploadPath;
    }
}
