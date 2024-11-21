/* Copyright (c) 2023 Automation Anywhere. All rights reserved.
 *
 * This software is the proprietary information of Automation Anywhere. You shall use it only in
 * accordance with the terms of the license agreement you entered into with Automation Anywhere.
 */
package aws.application.util;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;

public class JsonProtobufUtils {
    public JsonProtobufUtils() {}

    public static void fromJson(String json, Message.Builder builder)
            throws InvalidProtocolBufferException {
        JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
    }

    public static void fromJsonWithFailOnUnknown(String json, Message.Builder builder)
            throws InvalidProtocolBufferException {
        JsonFormat.parser().merge(json, builder);
    }
}
