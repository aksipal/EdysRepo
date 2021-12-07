package com.via.ecza.api.admin;


import com.via.ecza.dto.*;
import com.via.ecza.service.DrugCardService;
import com.via.ecza.util.ApiPath;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@RestController
@RequestMapping(ApiPath.AdminDrugCtrl.CTRL)
public class DrugCardApi {


    @Autowired
    private DrugCardService drugCardService;


    @PostMapping("/find-by-searching")
    public ResponseEntity<List<DrugCardDto>> findBySearching(
            @RequestHeader("Authorization") String authHeader,@RequestBody DrugCardParams params  ) throws NotFoundException {
        return ResponseEntity.ok(drugCardService.findBySearching(params.getDrugName()));
    }




}
