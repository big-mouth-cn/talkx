package com.bxm.warcar.file.upload;

import java.io.InputStream;

/**
 * <h3>Shenjue.java基本描述</h3>
 * <p></p>
 *
 * @author hcmony
 * @since V1.0.0, 2019/01/28 16:56
 */
public class HttpFile {
	/**
	 * 设置文件名。支持目录。不要设置文件类型，通过 fileType 来设置。
	 * 如：images/2023/11/12/file
	 */
	private String fileName;
	/**
	 * 文件类型。需要有"."
	 * 如：.jpg
	 */
	private String fileType ;
	private InputStream inputStream;
	private String bucketName;
	private long fileSize;

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public String getBucketName() {
		return bucketName;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
