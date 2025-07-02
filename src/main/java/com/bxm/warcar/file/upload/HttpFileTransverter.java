package com.bxm.warcar.file.upload;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * <h3>多种格式转换器</h3>
 * <p></p>
 *
 * @author hcmony
 * @since V1.0.0, 2019/01/28 17:49
 */
public abstract class HttpFileTransverter implements HttpFileManager {
    protected HttpFile analysis(HttpFileRequest request) throws IOException {

        if (request == null) {
            throw new FileException("HttpFileRequest is null ");
        }
        if (request.getRequest() != null) {
            HttpFile file = requestAnalysis(request);
            file.setBucketName(request.getBucketName());
            return file;
        }
        if (request.getMultipartFile() != null) {
            HttpFile file = multipartFileAnalysis(request.getMultipartFile(), request.getFileName());
            file.setBucketName(request.getBucketName());
            return file;
        }

        HttpFile file = fileAnalysis(request.getFile());
        file.setBucketName(request.getBucketName());
        return file;
    }

    private HttpFile requestAnalysis(HttpFileRequest request1) throws IOException {
        // 创建一个通用的多部分解析器
        HttpServletRequest request = request1.getRequest();
        CommonsMultipartResolver multipartResolver =
                new CommonsMultipartResolver(request.getSession().getServletContext());
        if (!multipartResolver.isMultipart(request)) {
            throw new FileException(" is not  Multipart request");
        }

        MultipartRequest multiRequest = (MultipartRequest) request;
        MultipartFile file = multiRequest.getFile(multiRequest.getFileNames().next());
        return multipartFileAnalysis(file, request1.getFileName());
    }

    private HttpFile multipartFileAnalysis(MultipartFile file, String fixFileName) throws IOException {
        HttpFile httpFile = new HttpFile();
        String fileName = file.getOriginalFilename();
        String suffixNmae = fileName.substring(fileName.indexOf("."));
        httpFile.setFileType(suffixNmae);
        httpFile.setInputStream(file.getInputStream());
        httpFile.setFileSize(file.getSize());
        httpFile.setFileName(fixFileName);
        return httpFile;
    }

    private HttpFile fileAnalysis(File file) throws FileNotFoundException {
        HttpFile httpFile = new HttpFile();
        String fileName = file.getName();
        String suffixNmae = fileName.substring(fileName.indexOf("."));
        httpFile.setFileType(suffixNmae);
        httpFile.setInputStream(new FileInputStream(file));
        httpFile.setFileSize(file.length());
        return httpFile;
    }
}
