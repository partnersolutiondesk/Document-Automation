/* Copyright (c) 2023 Automation Anywhere. All rights reserved.
 *
 * This software is the proprietary information of Automation Anywhere. You shall use it only in
 * accordance with the terms of the license agreement you entered into with Automation Anywhere.
 */
package aws.application.service.splitter.impl;

import static java.text.MessageFormat.format;
import static org.apache.commons.io.FilenameUtils.concat;

import aws.application.service.splitter.DocumentSplitter;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

public class PDFDocumentSplitter implements DocumentSplitter {

    private static final long MAX_MAIN_PDFBOX_MEMORY = 104857600L;
    private static final long MAX_TOTAL_PDFBOX_MEMORY = 1099511627776L;

    private static final int RENDER_DPI = 300;

    public static final String MAX_MAIN_PDFBOX_MEMORY_PARAM = "maxMainPDFBoxMemory";
    public static final String MAX_TOTAL_PDFBOX_MEMORY_PARAM = "maxTotalPDFBoxMemory";
    public static final String RENDER_DPI_PARAM = "renderDPI";

    @Override
    public List<String> split(
            String imageFilePath, String outputDirPath, Map<String, Object> properties)
            throws IOException {
        try {
            ImageIO.scanForPlugins();
            List<String> paths = new ArrayList<>();
            try (InputStream is = new FileInputStream(imageFilePath);
                    PDDocument doc =
                            PDDocument.load(
                                    is,
                                    MemoryUsageSetting.setupMixed(
                                            getMaxMainPDFBoxMemory(properties),
                                            getMaxTotalPDFBoxMemory(properties)))) {
                PDFRenderer renderer = new PDFRenderer(doc);

                for (int i = 0; i < doc.getNumberOfPages(); i += 1) {
                    String path =
                            concat(outputDirPath, format(IMAGE_FILENAME_PATTERN, i, IMAGE_TYPE));
                    paths.add(path);

                    try (OutputStream os = new FileOutputStream(path)) {
                        ImageIO.write(
                                renderer.renderImageWithDPI(i, getRenderDIP(properties)),
                                IMAGE_TYPE,
                                os);
                    }
                }
            }

            return paths;
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    private long getMaxMainPDFBoxMemory(Map<String, Object> properties) {
        return (long) properties.getOrDefault(MAX_MAIN_PDFBOX_MEMORY_PARAM, MAX_MAIN_PDFBOX_MEMORY);
    }

    private long getMaxTotalPDFBoxMemory(Map<String, Object> properties) {
        return (long)
                properties.getOrDefault(MAX_TOTAL_PDFBOX_MEMORY_PARAM, MAX_TOTAL_PDFBOX_MEMORY);
    }

    private int getRenderDIP(Map<String, Object> properties) {
        return (int) properties.getOrDefault(RENDER_DPI_PARAM, RENDER_DPI);
    }
}
