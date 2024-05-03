/* Copyright (c) 2023 Automation Anywhere. All rights reserved.
 *
 * This software is the proprietary information of Automation Anywhere. You shall use it only in
 * accordance with the terms of the license agreement you entered into with Automation Anywhere.
 */
package aws.application.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Jackson-based JSON parsing is deprecated for removal. Instead, use JsonFormat from
 * common-protobuf.
 */
@Deprecated
public class JsonUtils {
    private static final ObjectMapper MAPPER = createMapper();
    private static final ObjectMapper MAPPER_WITH_FAIL_ON_UNKNOWN = createMapperWithFailOnUnknown();
    private static final ObjectMapper MAPPER_WITH_IGNORE_NULLS = createMapperWithIgnoreNulls();

    @Deprecated
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public static <T> T fromJsonWithFailOnUnknown(String json, Class<T> clazz) {
        try {
            return MAPPER_WITH_FAIL_ON_UNKNOWN.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public static <T> List<T> fromJsonToList(String json, Class<T> elementClass) {
        try {
            return MAPPER.readValue(
                    json,
                    MAPPER.getTypeFactory().constructCollectionType(List.class, elementClass));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public static <T> Set<T> fromJsonToSet(String json, Class<T> elementClass) {
        try {
            return MAPPER.readValue(
                    json, MAPPER.getTypeFactory().constructCollectionType(Set.class, elementClass));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public static <T, V> Map<T, V> fromJsonToMap(
            String json, Class<T> keyClass, Class<V> valueClass) {
        try {
            return MAPPER.readValue(
                    json,
                    MAPPER.getTypeFactory().constructMapType(Map.class, keyClass, valueClass));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public static String toJsonWithFailOnUnknown(Object object) {
        try {
            return MAPPER_WITH_FAIL_ON_UNKNOWN.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public static <T> T convertMap(Map properties, Class<T> type) {
        return MAPPER.convertValue(properties, type);
    }

    @Deprecated
    public static String toJson(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public static String toJsonIgnoreNulls(Object object) {
        try {
            return MAPPER_WITH_IGNORE_NULLS.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public static ObjectMapper createMapperWithIgnoreNulls() {
        ObjectMapper mapper = createMapperWithFailOnUnknown(false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    @Deprecated
    public static ObjectMapper createMapper() {
        return createMapperWithFailOnUnknown(false);
    }

    @Deprecated
    private static ObjectMapper createMapperWithFailOnUnknown() {
        return createMapperWithFailOnUnknown(true);
    }

    @Deprecated
    private static ObjectMapper createMapperWithFailOnUnknown(boolean failOnUnknown) {
        ObjectMapper mapper = new ObjectMapper();
        // ProtobufModule is needed for older services that deal with raw collections on top of
        // protobuf messages,
        // while newer services must always use wrappers on collections and do serialization with
        // protobuf supplied
        // JsonFormat.
        mapper.registerModule(new ProtobufModule());
        // mapper.registerModule(new ZonedDateTimeModule());
        /*        Hibernate5Module hibernate5Module = new Hibernate5Module();
        hibernate5Module.disable(Hibernate5Module.Feature.USE_TRANSIENT_ANNOTATION);
        mapper.registerModule(hibernate5Module);*/
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, failOnUnknown);
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        return mapper;
    }
}
