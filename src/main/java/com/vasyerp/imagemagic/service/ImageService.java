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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ImageService {

    private static final Logger log = LoggerFactory.getLogger(ImageService.class);
    @Value("${image-magic.imagemagick.path}")
    private String imageMagickPath;

    @Value("${image-magic.imagemagick.command}")
    private String imageMagickCommand;

    ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


    public File convertAndResizeImage(File inputFile, String outputPath, int height, int width, int percentage, String outputFormat) throws IOException, InterruptedException {
        log.info("image: {}, thread: {}", inputFile.getAbsolutePath(), Thread.currentThread().getName());
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

        CountDownLatch latch = new CountDownLatch(files.length);

        for (File inputFile : files) {
            executorService.submit(() -> {
                try {
                    String outputFilePath = outputFolderPath + File.separator + inputFile.getName().replaceFirst("[.][^.]+$", "") + "-" + width + "x" + height + "." + outputFormat;
                    convertAndResizeImage(inputFile, outputFilePath, height, width, percentage, outputFormat);
                } catch (IOException | InterruptedException e) {
                    log.error("Error processing image: {}", inputFile.getName(), e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        stopWatch.stop();
        log.info("TIme Take for size: {}x{} is: {} minutes", height, width, stopWatch.getTotalTimeSeconds() / 60);
    }

}


