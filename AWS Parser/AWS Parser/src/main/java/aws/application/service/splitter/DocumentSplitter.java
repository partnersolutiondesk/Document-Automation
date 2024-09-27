/* Copyright (c) 2023 Automation Anywhere. All rights reserved.
 *
 * This software is the proprietary information of Automation Anywhere. You shall use it only in
 * accordance with the terms of the license agreement you entered into with Automation Anywhere.
 */
package aws.application.service.splitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface DocumentSplitter {

    String IMAGE_FILENAME_PATTERN = "{0,number,#}.{1}";
    String IMAGE_TYPE = "png";

    List<String> split(String imageFilePath, String outputDirPath, Map<String, Object> properties)
            throws IOException;
}
