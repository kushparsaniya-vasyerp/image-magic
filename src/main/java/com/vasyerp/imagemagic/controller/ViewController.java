package com.vasyerp.imagemagic.controller;


import com.vasyerp.imagemagic.service.ImageService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Controller
public class ViewController {

    private static final Logger log = LoggerFactory.getLogger(ViewController.class);
    private final ImageService imageService;

    public ViewController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping
    public ModelAndView index() {
        return new ModelAndView("home");
    }

    @GetMapping("/bulk-process")
    public ModelAndView bulkProcess(HttpSession session) {
        session.removeAttribute("taken-time");
        return new ModelAndView("bulk-process");
    }

    @PostMapping("/start-bulk-process")
    public String processImages(
            @RequestParam("inputFolder") String inputFolderPath,
            @RequestParam("outputFolder") String outputFolderPath,
            @RequestParam("sizes") String[] sizes,
            @RequestParam("quality") int quality,
            @RequestParam("format") String format,
            Model model, HttpSession session) {
        try {
            File inputFolder = new File(inputFolderPath);
            for (String size : sizes) {
                try {
                    String[] dimensions = size.split("x");
                    int width = Integer.parseInt(dimensions[0]);
                    int height = Integer.parseInt(dimensions[1]);
                    imageService.convertAndResizeImagesInFolder(inputFolder, outputFolderPath, height, width, quality, format);
                } catch (Exception e) {
                    model.addAttribute("message", "Error processing images: " + e.getMessage());
                    return "bulk-process";
                }
            }
            model.addAttribute("message", "Images processed successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("message", "Error processing images: " + e.getMessage());
        }
//        session.setAttribute("taken-time", String.format("%.2f minutes", stopWatch.getTotalTimeSeconds() / 60));
        return "redirect:/bulk-process";
    }

    @PostMapping("/upload-image")
    public ResponseEntity<Resource> handleImageUpload(@RequestParam("imageFile") MultipartFile file,
                                                      @RequestParam("width") int width,
                                                      @RequestParam("height") int height,
                                                      @RequestParam("quality") int quality,
                                                      @RequestParam("format") String outputFormat
    ) throws IOException, InterruptedException {
//        String tempDir = System.getProperty("user.dir");
//        tempDir = tempDir + FileSystems.getDefault().getSeparator() + "images";
        String tempDir = System.getProperty("java.io.tmpdir");


        File tempInputFile = new File(tempDir, "upload_" + System.nanoTime() + "." + getFileExtension(file));
        File tempOutputFile = new File(tempDir, "processed_" + System.nanoTime() + "." + outputFormat);
        file.transferTo(tempInputFile);

        String outputFileName = tempOutputFile.getName();
        File processedFile = imageService.convertAndResizeImage(tempInputFile, tempOutputFile.getAbsolutePath(), height, width, quality, outputFormat);

        if (!processedFile.exists()) {
            throw new IOException("Processed file does not exist");
        }

        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(processedFile.toPath()));

        tempInputFile.delete();
        tempOutputFile.delete();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"%s\"".formatted(outputFileName))
                .header(HttpHeaders.CONTENT_TYPE, "image/" + outputFormat)
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(resource.contentLength()))
                .body(resource);
    }

    private String getFileExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        } else {
            throw new IllegalArgumentException("Invalid file: no extension found.");
        }
    }
}
