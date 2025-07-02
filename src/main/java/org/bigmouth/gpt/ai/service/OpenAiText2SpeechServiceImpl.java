package org.bigmouth.gpt.ai.service;

import com.bxm.warcar.utils.JsonHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.bigmouth.gpt.Application;
import org.bigmouth.gpt.ApplicationConfig;
import org.bigmouth.gpt.ai.Text2SpeechService;
import org.bigmouth.gpt.ai.entity.SimpleHandler;
import org.bigmouth.gpt.ai.entity.voice.Text2SpeechArgument;
import org.bigmouth.gpt.ai.entity.voice.Tts1Request;
import org.bigmouth.gpt.exceptions.AiAccountException;
import org.bigmouth.gpt.exceptions.AiLimitException;
import org.bigmouth.gpt.exceptions.AiNetworkException;
import org.bigmouth.gpt.utils.Constants;
import org.bigmouth.gpt.utils.HttpClientHelper;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * @author huxiao
 * @date 2023/11/10
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class OpenAiText2SpeechServiceImpl implements Text2SpeechService {

    private final PoolingHttpClientConnectionManager connectionManager;
    private final HttpClient client;
    private final ApplicationConfig config;

    public OpenAiText2SpeechServiceImpl(ApplicationConfig config) {
        this.config = config;
        this.connectionManager = HttpClientHelper.createConnectionManager(200, 40);
        this.client = HttpClientHelper.createHttpClient(1000, 5000, 0, connectionManager);
    }

    @Override
    public byte[] speech(Text2SpeechArgument argument) {
        String reqId = argument.getId();

        String url = UriComponentsBuilder.fromUriString(config.getLlmApiHost())
                .path("/v1/audio/speech")
                .build()
                .toString();
        String requestUri = Application.getRequestUri(null, url);
        HttpPost request = new HttpPost(requestUri);
        try {
            Tts1Request tts1Request = argument.getTts1();

            request.setEntity(new StringEntity(JsonHelper.convert(tts1Request), StandardCharsets.UTF_8));
            request.addHeader("Content-Type", "application/json;charset=utf-8");
            request.addHeader("Authorization", "Bearer " + config.getLlmApiKey());

            HttpResponse response = client.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();

            if (log.isDebugEnabled()) {
                log.debug("{} - statusCode: {}, headers: {}", reqId, statusCode, response.getAllHeaders());
            }

            HttpEntity entity = response.getEntity();

            if (statusCode != HttpStatus.OK.value()) {
                // 429
                String content = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                if (content.contains(Constants.Regex.LIMIT)) {
                    throw new AiLimitException(content);
                }
                AiAccountException exception = new AiAccountException(content);
                exception.setSc(statusCode);
                throw exception;
            }

            byte[] res = new byte[0];
            OutputStream outputStream = argument.getOutputStream();
            if (Objects.nonNull(outputStream)) {
                // 不为空说明用流的方式处理

                InputStream inputStream = entity.getContent();
                byte[] buffer = new byte[1024];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                outputStream.flush();
                SimpleHandler completeRunnable = argument.getCompleteRunnable();
                if (Objects.nonNull(completeRunnable)) {
                    completeRunnable.execute();
                }
            } else {
                res = EntityUtils.toByteArray(entity);
            }
            EntityUtils.consume(entity);
            return res;
        } catch (ClientAbortException e) {
            BiConsumer<ClientAbortException, String> clientAbortExceptionStringBiConsumer = argument.getClientAbortExceptionStringBiConsumer();
            if (null != clientAbortExceptionStringBiConsumer) {
                clientAbortExceptionStringBiConsumer.accept(e, null);
            }
        } catch (IOException e) {
            throw new AiNetworkException(e);
        } finally {
            request.releaseConnection();
        }
        return null;
    }
}
