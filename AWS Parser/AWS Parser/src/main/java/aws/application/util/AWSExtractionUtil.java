/* Copyright (c) 2023 Automation Anywhere. All rights reserved.
 *
 * This software is the proprietary information of Automation Anywhere. You shall use it only in
 * accordance with the terms of the license agreement you entered into with Automation Anywhere.
 */
package aws.application.util;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;

public class AWSExtractionUtil {

    public static String toJson(MessageOrBuilder object) {
        if (object == null) {
            return null;
        }
        try {
            return JsonFormat.printer().includingDefaultValueFields().print(object);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Failed to convert dto to string", e);
        }
    }
}
