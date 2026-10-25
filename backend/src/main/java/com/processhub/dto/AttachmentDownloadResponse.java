package com.processhub.dto;

public class AttachmentDownloadResponse {

    private String fileName;
    private String contentType;
    private long fileSize;
    private byte[] data;

    public AttachmentDownloadResponse() {
    }

    public AttachmentDownloadResponse(String fileName, String contentType, long fileSize, byte[] data) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.data = data;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
