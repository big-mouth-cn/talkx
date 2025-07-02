package com.bxm.warcar.file.upload;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.Serializable;

/**
 * <h3>Shenjue.java基本描述</h3>
 * <p></p>
 *
 * @author hcmony
 * @since V1.0.0, 2019/01/28 14:35
 */
public class HttpFileRequest implements Serializable {

    private static final long serialVersionUID = -6679618799203778257L;

    private HttpServletRequest request;
    private File file;
    private MultipartFile multipartFile;
    //oss 分桶名
    private String bucketName;
    /**
     * 设置文件名。支持目录。不要设置文件类型，通过 fileType 来设置。
     * 如：images/2023/11/12/file
     */
    private String fileName;

    public HttpFileRequest() {
    }

    public HttpFileRequest(HttpServletRequest request) {
        this.request = request;
    }

    public HttpFileRequest(MultipartFile multipartFile) {
        this.multipartFile = multipartFile;
    }

    public HttpFileRequest(File file) {
        this.file = file;
    }

    public HttpFileRequest(HttpServletRequest request, String bucketName) {
        this.request = request;
        this.bucketName = bucketName;
    }

    public HttpFileRequest(HttpServletRequest request, File file, MultipartFile multipartFile,
                           String bucketName) {
        this.request = request;
        this.file = file;
        this.multipartFile = multipartFile;
        this.bucketName = bucketName;
    }

    public HttpFileRequest(HttpServletRequest request, File file, MultipartFile multipartFile, String bucketName, String fileName) {
        this.request = request;
        this.file = file;
        this.multipartFile = multipartFile;
        this.bucketName = bucketName;
        this.fileName = fileName;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public MultipartFile getMultipartFile() {
        return multipartFile;
    }

    public void setMultipartFile(MultipartFile multipartFile) {
        this.multipartFile = multipartFile;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
