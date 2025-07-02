package org.bigmouth.gpt.service.impl;

import org.bigmouth.gpt.service.FileUploadType;
import org.bigmouth.gpt.service.FileUploadService;
import org.bigmouth.gpt.xiaozhi.forest.TalkXApi;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Allen Hu
 * @date 2025/4/18
 */
@Service
public class TalkXFileUploadService implements FileUploadService {

    private final TalkXApi talkXApi;

    public TalkXFileUploadService(TalkXApi talkXApi) {
        this.talkXApi = talkXApi;
    }

    @Override
    public FileUploadType of() {
        return FileUploadType.TALKX;
    }

    @Override
    public String uploadImg(MultipartFile file) {
        return talkXApi.uploadImage(file);
    }

    @Override
    public String uploadImg(MultipartFile file, String parentDir) {
        return talkXApi.uploadImage(file);
    }

    @Override
    public String uploadFile(MultipartFile file) {
        return talkXApi.uploadFile(file);
    }

    @Override
    public String uploadFile(MultipartFile file, String parentDir) {
        return talkXApi.uploadFile(file);
    }
}
