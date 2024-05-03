/* Copyright (c) 2023 Automation Anywhere. All rights reserved.
 *
 * This software is the proprietary information of Automation Anywhere. You shall use it only in
 * accordance with the terms of the license agreement you entered into with Automation Anywhere.
 */
package aws.application.service.splitter.impl;

import static java.text.MessageFormat.format;
import static org.apache.commons.io.FilenameUtils.concat;

import aws.application.service.splitter.DocumentSplitter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public class TIFFDocumentSplitter implements DocumentSplitter {

    @Override
    public List<String> split(
            String imageFilePath, String outputDirPath, Map<String, Object> properties)
            throws IOException {
        try {
            ImageInputStream is = ImageIO.createImageInputStream(new File(imageFilePath));
            ImageReader reader = ImageIO.getImageReaders(is).next();
            reader.setInput(is);
            List<String> paths = new ArrayList<>();
            for (int i = 0; i < reader.getNumImages(true); i++) {
                String path = concat(outputDirPath, format(IMAGE_FILENAME_PATTERN, i, IMAGE_TYPE));
                BufferedImage image = reader.read(i);
                try (OutputStream os = new FileOutputStream(path); ) {
                    ImageIO.write(image, IMAGE_TYPE, os);
                }
                paths.add(path);
            }
            return paths;
        } catch (Exception ex) {
            throw new RuntimeException("An error occurred during TIFF conversion", ex);
        }
    }
}
