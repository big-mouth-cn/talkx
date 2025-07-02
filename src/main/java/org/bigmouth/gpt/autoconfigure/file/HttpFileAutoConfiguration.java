package org.bigmouth.gpt.autoconfigure.file;

import com.bxm.warcar.file.upload.HttpFileManager;
import com.bxm.warcar.file.upload.alioss.AliOssHttpFileManager;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(HttpFileConfiguration.class)
public class HttpFileAutoConfiguration {

	private final HttpFileConfiguration httpFileConfiguration;

    public HttpFileAutoConfiguration(HttpFileConfiguration httpFileConfiguration) {
        this.httpFileConfiguration = httpFileConfiguration;
    }

    @Bean
	public HttpFileManager httpFileManager() {
		return new AliOssHttpFileManager(httpFileConfiguration.getEndPoint(),
				httpFileConfiguration.getAccessKeyId(),
				httpFileConfiguration.getAccessKeySecret(),
				httpFileConfiguration.getHost(),
				httpFileConfiguration.getBucketName(),
				httpFileConfiguration.getServiceName(),
				httpFileConfiguration.getLimitSize());
	}
}
