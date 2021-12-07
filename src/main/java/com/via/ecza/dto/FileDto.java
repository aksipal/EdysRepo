package com.via.ecza.dto;

import lombok.Data;

@Data
public class FileDto {
    private byte[] file;
    private String fileName;
}
