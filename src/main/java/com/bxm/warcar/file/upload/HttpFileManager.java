package com.bxm.warcar.file.upload;

/**
 * <h3>Shenjue.java基本描述</h3>
 * <p></p>
 *
 * @author hcmony
 * @since V1.0.0, 2019/01/28 13:49
 */
public interface HttpFileManager {

	String upload(HttpFileRequest request)throws Exception;
}
