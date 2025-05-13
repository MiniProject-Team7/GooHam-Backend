package com.uplus.ureka.service.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AmazonS3 amazonS3;

    // 파일 업로드 후 JSON 문자열 반환
    public String uploadFile(List<MultipartFile> multipartFiles, String pathPrefix) {
        List<String> fileNameList = new ArrayList<>();
        logger.info("uploadFile() called with {} files, pathPrefix='{}'", multipartFiles.size(), pathPrefix);

        for (MultipartFile file : multipartFiles) {
            String originalName = createFileName(file.getOriginalFilename());
            String fileName = pathPrefix + "/" + originalName;
            logger.info("Preparing to upload file: original='{}', targetKey='{}'", file.getOriginalFilename(), fileName);


            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(file.getSize());
            objectMetadata.setContentType(file.getContentType());

            try (InputStream inputStream = file.getInputStream()) {
                amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata));
                logger.info("Uploaded successfully: key='{}'", fileName);
            } catch (IOException e) {
                logger.error("IOException during upload of '{}'", fileName, e);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다.");
            }

            fileNameList.add(fileName);
        }

        String json;
        try {
            json = objectMapper.writeValueAsString(fileNameList);
            logger.info("Returning imageJson: {}", json);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize fileNameList to JSON", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "JSON 변환에 실패했습니다.");
        }

        return json;

    }

    // 파일명을 난수화하기 위해 UUID 를 활용하여 난수를 돌린다.
    public String createFileName(String fileName){
        return UUID.randomUUID().toString().concat(getFileExtension(fileName));
    }

    //  "."의 존재 유무만 판단
    private String getFileExtension(String fileName){
        try{
            return fileName.substring(fileName.lastIndexOf("."));
        } catch (StringIndexOutOfBoundsException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 형식의 파일" + fileName + ") 입니다.");
        }
    }


    public void deleteFile(String fileName){
        log.info("Deleting file: {}", fileName);
        amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileName));
        System.out.println(bucket);
    }

}

