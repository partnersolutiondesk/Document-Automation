/* Copyright (c) 2023 Automation Anywhere. All rights reserved.
 *
 * This software is the proprietary information of Automation Anywhere. You shall use it only in
 * accordance with the terms of the license agreement you entered into with Automation Anywhere.
 */
package aws.application.service.splitter;

import aws.application.service.splitter.impl.BMPDocumentSplitter;
import aws.application.service.splitter.impl.NoDocumentSplitter;
import aws.application.service.splitter.impl.PDFDocumentSplitter;
import aws.application.service.splitter.impl.TIFFDocumentSplitter;
import org.apache.commons.io.FilenameUtils;

public class DocumentSplitterFactory {

    private DocumentSplitterFactory() {}

    private enum FileExtension {
        JPEG,
        JPG,
        PNG,
        PDF,
        TIF,
        TIFF,
        BMP;
    }

    public static DocumentSplitter create(String imageFilePath) {
        String filetype = FilenameUtils.getExtension(imageFilePath);
        if (filetype.toUpperCase().equals(FileExtension.PDF.name())) {
            return new PDFDocumentSplitter();
        }
        if (filetype.toUpperCase().equals(FileExtension.TIFF.name())
                || filetype.toUpperCase().equals(FileExtension.TIF.name())) {
            return new TIFFDocumentSplitter();
        }
        if (filetype.toUpperCase().equals(FileExtension.BMP.name())) {
            return new BMPDocumentSplitter();
        }
        return new NoDocumentSplitter();
    }
}
