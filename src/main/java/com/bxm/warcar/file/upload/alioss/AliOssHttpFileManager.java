package com.bxm.warcar.file.upload.alioss;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.bxm.warcar.file.upload.FileException;
import com.bxm.warcar.file.upload.HttpFile;
import com.bxm.warcar.file.upload.HttpFileRequest;
import com.bxm.warcar.file.upload.HttpFileTransverter;
import com.bxm.warcar.utils.DateHelper;
import com.bxm.warcar.utils.UUIDHelper;
import org.apache.commons.lang3.StringUtils;

/**
 * <h3>阿里云实现OSS上传文件，返回格式</h3>
 *
 * @author hcmony
 * @since V1.0.0, 2019/01/28 14:48
 */
public class AliOssHttpFileManager extends HttpFileTransverter {

    private String endPoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String host;
    private String bucketName = "oss-image";
    //区分是哪个服务上传的文件
    private String serviceName = "default";
    //上传文件大小,默认4M
    private long limitSize = 1024 * 1024 * 4;

    public AliOssHttpFileManager(String endPoint,
                                 String accessKeyId,
                                 String accessKeySecret,
                                 String host,
                                 String bucketName,
                                 String serviceName,
                                 long limitSize) {
        this.endPoint = endPoint;
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.host = host;
        this.bucketName = bucketName;
        this.serviceName = serviceName;
        this.limitSize = limitSize;
    }

    @Override
    public String upload(HttpFileRequest request) throws Exception {
        HttpFile httpFile = analysis(request);
        return upload(httpFile);
    }

    private String upload(HttpFile httpFile) throws FileException {
        if (httpFile == null) {
            throw new FileException("httpFile is null");
        }

        if (httpFile.getFileSize() > limitSize) {
            throw new FileException("The file is too large ,size is " + httpFile.getFileSize());
        }

        if (StringUtils.isBlank(host)) {
            throw new FileException("host is null");
        }

        String fileName = fileName(httpFile);
        OSSClient ossClient = null;
        try {
            ossClient = bulidOSSClient();
            ossClient.putObject(StringUtils.isNoneBlank(bucketName) ? bucketName : this.bucketName, fileName, httpFile.getInputStream());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        return StringUtils.join(host, fileName);
    }

    private String fileName(HttpFile httpFile) {
        String fileName = httpFile.getFileName();
        String fileType = httpFile.getFileType();
        if (StringUtils.isBlank(fileName)) {
            fileName = DateHelper.format("yyyy/MM/dd") + "/" + serviceName + "/" + UUIDHelper.generate();
        }
        return StringUtils.join(fileName, fileType);
    }

    private OSSClient bulidOSSClient() {
        return new OSSClient(endPoint, new DefaultCredentialProvider(accessKeyId, accessKeySecret), null);
    }
}
