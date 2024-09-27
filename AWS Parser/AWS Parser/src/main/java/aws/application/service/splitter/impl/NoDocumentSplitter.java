/* Copyright (c) 2023 Automation Anywhere. All rights reserved.
 *
 * This software is the proprietary information of Automation Anywhere. You shall use it only in
 * accordance with the terms of the license agreement you entered into with Automation Anywhere.
 */
package aws.application.service.splitter.impl;

import aws.application.service.splitter.DocumentSplitter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class NoDocumentSplitter implements DocumentSplitter {

    @Override
    public List<String> split(
            String imageFilePath, String outputDirPath, Map<String, Object> properties)
            throws IOException {
        return List.of(imageFilePath);
    }
}
