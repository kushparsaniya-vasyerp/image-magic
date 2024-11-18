package com.vasyerp.imagemagic.service;

import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.core.ImageMagickCmd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.io.File;
import java.io.IOException;

@Service
public class ImageService {

    private static final Logger log = LoggerFactory.getLogger(ImageService.class);
    @Value("${image-magic.imagemagick.path}")
    private String imageMagickPath;

    @Value("${image-magic.imagemagick.command}")
    private String imageMagickCommand;

    public File convertAndResizeImage(File inputFile, String outputPath, int height, int width, int percentage, String outputFormat) throws IOException, InterruptedException {
        IMOperation operation = new IMOperation();

        operation.addImage(inputFile.getAbsolutePath());
        if (height > 0 && width > 0) {
            operation.resize(height, width);
        } else {
            throw new IllegalArgumentException("Height and width must be greater than 0");
        }
        operation.quality((double) percentage);
        operation.format(outputFormat);
        operation.addImage(outputPath);

        ImageMagickCmd cmd = new ImageMagickCmd(imageMagickCommand);
        try {
            cmd.run(operation);
        } catch (IM4JavaException e) {
            e.printStackTrace();
            throw new IOException("Error during image conversion", e);
        }

        File outputFile = new File(outputPath);
        if (!outputFile.exists()) {
            throw new IOException("Output file was not created");
        }
        return outputFile;
    }

    @Async
    public void convertAndResizeImagesInFolder(File inputFolder, String outputFolderPath, int height, int width, int percentage, String outputFormat) throws IOException, InterruptedException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        log.info("Converting and resizing images in folder: {}", inputFolder.getAbsolutePath());
        if (!inputFolder.isDirectory()) {
            throw new IllegalArgumentException("Input path must be a directory");
        }

        File outputFolder = new File(outputFolderPath);
        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }

        File[] files = inputFolder.listFiles((dir, name) -> {
            String lowerName = name.toLowerCase();
            return lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".png") || lowerName.endsWith(".webp");
        });


        if (files == null) {
            log.warn("No images found in folder");
            return;
        }
        log.info("Found {} images in folder", files.length);
        log.info("Start converting and resizing images for size: {}x{}", width, height);
        for (File inputFile : files) {
            String outputFilePath = "";
            try {
                outputFilePath = outputFolderPath + File.separator + inputFile.getName().replaceFirst("[.][^.]+$", "") + "-" + width + "x" + height + "." + outputFormat;
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (outputFilePath.isBlank()) {
                throw new IllegalArgumentException("Output file path is invalid");
            }
            convertAndResizeImage(inputFile, outputFilePath, height, width, percentage, outputFormat);
        }
        log.info("Finished converting and resizing images for size: {}x{}", width, height);
        log.info("Total time taken for size: {}x{}: {} minutes", width, height, stopWatch.getTotalTimeSeconds());
    }

}


