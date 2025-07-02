package com.bxm.warcar.message;

import com.alibaba.fastjson.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * <h3>Shenjue.java基本描述</h3>
 * <p></p>
 *
 * @author hcmony
 * @since V1.0.0, 2018/07/30 20:40
 */
public abstract class AbstractMessageSender {

	protected ResponseEntity<String> postForEntity(String url,JSONObject jsonParam){
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/json; charset=UTF-8");
		HttpEntity<String> req = new HttpEntity<>(jsonParam.toJSONString(), headers);
		RestTemplate rest = new RestTemplate();
		return rest.postForEntity(url, req, String.class);
	}
}
