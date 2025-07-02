package org.bigmouth.gpt.service;

import com.bxm.warcar.utils.AbstractBeanBus;
import org.bigmouth.gpt.ApplicationConfig;
import org.springframework.context.annotation.Configuration;

/**
 * @author Allen Hu
 * @date 2025/4/18
 */
@Configuration
public class FileUploadServiceFactory extends AbstractBeanBus<FileUploadType, FileUploadService> {

    private final ApplicationConfig applicationConfig;

    public FileUploadServiceFactory(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    @Override
    protected Class<FileUploadService> getInstanceClazz() {
        return FileUploadService.class;
    }

    @Override
    protected FileUploadType getKey(String beanName, FileUploadService bean) {
        return bean.of();
    }

    public FileUploadService get() {
        FileUploadType fileUploadType = applicationConfig.getDefaultFileUploadType();
        return get(fileUploadType);
    }
}
