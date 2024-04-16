package com.ushareit.dstask.utils;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DecimalFormat;

/**
 * user gson to do with json
 * Created by dongjiejie on 2017/2/16.
 */
public class GsonUtil {

    public static <T> T parse(String jsonStr, Class<T> typeClass) {
        Gson gson = buildGson();
        return gson.fromJson(jsonStr, typeClass);
    }

    /**
     * 主要是为了解决不用传类型解析字符串，解决FastJson的坑
     *
     * @param jsonStr
     *
     * @return
     *
     * @throws ClassNotFoundException
     */
    public static Object parse(String jsonStr) throws ClassNotFoundException {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(jsonStr).getAsJsonObject();
        String name = jsonObject.get("@type").getAsString();
        if (StringUtils.isBlank(name)) {
            return null;
        }
        return parse(jsonStr, Class.forName(name));
    }

    /**
     * 形如 {"@type":"java.lang.String","value"="xx"}
     *
     * @param obj
     * @param withClassName
     *
     * @return
     */
    public static String toJson(Object obj, boolean withClassName) {
        Gson gson = buildGson();
        if (withClassName) {
            JsonElement jsonElement = gson.toJsonTree(obj);
            jsonElement.getAsJsonObject().addProperty("@type", obj.getClass().getName());
            return jsonElement.toString();
        }
        return gson.toJson(obj);
    }

    public static Gson buildGson() {
        return buildGson(false);
    }

    public static Gson buildGson(boolean prettyPrinting) {
        GsonBuilder builder = new GsonBuilder()
                .disableHtmlEscaping()
                .registerTypeAdapter(Date.class, SQL_DATE_JSON_DESERIALIZER)
                .registerTypeAdapter(Date.class, SQL_DATE_JSON_SERIALIZER)
                .registerTypeAdapter(Time.class, SQL_TIME_SERIALIZER)
                .registerTypeAdapter(Time.class, SQL_TIME_DESERIALIZER)
                .registerTypeAdapter(Timestamp.class, SQL_TIMESTAMP_SERIALIZER)
                .registerTypeAdapter(Timestamp.class, SQL_TIMESTAMP_DESERIALIZER)
                .registerTypeAdapter(java.util.Date.class, UTIL_DATA_DESERIALIZER)
                .registerTypeAdapter(java.util.Date.class, UTIL_DATE_SERIALIZER)
                .registerTypeAdapter(BigDecimal.class, BIG_DECIMAL_JSON_DESERIALIZER)
                .registerTypeAdapter(BigDecimal.class, BIG_DECIMAL_JSON_SERIALIZER)
                .serializeNulls();
        if (prettyPrinting) {
            builder.setPrettyPrinting();
        }
        return builder.create();
    }
    private static final TypeAdapter<String> STRING = new TypeAdapter<String>()
    {
        @Override
        public String read(JsonReader reader) throws IOException
        {
            if (reader.peek() == JsonToken.NULL)
            {
                reader.nextNull();
                return "";
            }
            return reader.nextString();
        }

        @Override
        public void write(JsonWriter writer, String value) throws IOException
        {
            if (value == null)
            {
                // 在这里处理null改为空字符串
                writer.value("");
                return;
            }
            writer.value(value);
        }
    };


    public static Gson builderGsonFormatDateForEs(){
        GsonBuilder builder = new GsonBuilder()
                .disableHtmlEscaping()
                .registerTypeAdapter(Date.class, SQL_DATE_JSON_DESERIALIZER)
                .registerTypeAdapter(Date.class, SQL_DATE_JSON_SERIALIZER)
                .registerTypeAdapter(Time.class, SQL_TIME_SERIALIZER)
                .registerTypeAdapter(Time.class, SQL_TIME_DESERIALIZER)
                .registerTypeAdapter(Timestamp.class, SQL_TIMESTAMP_SERIALIZER)
                .registerTypeAdapter(Timestamp.class, SQL_TIMESTAMP_DESERIALIZER)
                .registerTypeAdapter(BigDecimal.class, BIG_DECIMAL_JSON_DESERIALIZER)
                .registerTypeAdapter(BigDecimal.class, BIG_DECIMAL_JSON_SERIALIZER)
                .registerTypeAdapter(String.class,STRING)
                .setDateFormat("yyyyMMddHHmmssSSS")
                .serializeNulls();
        return builder.create();
    }
    private final static String BIG_DECIMAL_FORMAT_STR = "0.######";

    private final static ThreadLocal<DecimalFormat> DECIMAL_FORMAT_THREAD_LOCAL = new ThreadLocal<>();

    private final static JsonSerializer<Date> SQL_DATE_JSON_SERIALIZER =
            (date, typeOfSrc, context) -> date == null ? null : new JsonPrimitive(date.getTime());

    private final static JsonDeserializer<Date> SQL_DATE_JSON_DESERIALIZER =
            (json, typeOfSrc, context) -> json == null ? null : new Date(json.getAsLong());

    private final static JsonSerializer<Time> SQL_TIME_SERIALIZER =
            (time, typeOfSrc, context) -> time == null ? null : new JsonPrimitive(time.getTime());

    private final static JsonDeserializer<Time> SQL_TIME_DESERIALIZER =
            (json, typeOfT, context) -> json == null ? null : new Time(json.getAsLong());

    private final static JsonSerializer<Timestamp> SQL_TIMESTAMP_SERIALIZER =
            (timestamp, typeOfSrc, context) -> timestamp == null ? null : new JsonPrimitive(timestamp.getTime());

    private final static JsonDeserializer<Timestamp> SQL_TIMESTAMP_DESERIALIZER =
            (json, typeOfT, context) -> json == null ? null : new Timestamp(json.getAsLong());

    private final static JsonSerializer<java.util.Date> UTIL_DATE_SERIALIZER =
            (date, typeOfSrc, context) -> new JsonPrimitive(date.getTime());

    private final static JsonDeserializer<java.util.Date> UTIL_DATA_DESERIALIZER =
            (json, typeOfT, context) -> new java.util.Date(json.getAsLong());

    private final static JsonSerializer<BigDecimal> BIG_DECIMAL_JSON_SERIALIZER =
            (bigDecimal, typeOfSrc, context) -> {
                DecimalFormat decimalFormat = DECIMAL_FORMAT_THREAD_LOCAL.get();
                if (decimalFormat == null) {
                    decimalFormat = new DecimalFormat(BIG_DECIMAL_FORMAT_STR);
                    DECIMAL_FORMAT_THREAD_LOCAL.set(decimalFormat);
                }
                return new JsonPrimitive(decimalFormat.format(bigDecimal));
            };

    private final static JsonDeserializer<BigDecimal> BIG_DECIMAL_JSON_DESERIALIZER =
            (json, typeOfT, context) -> new BigDecimal(json.getAsString());

    public static <T> T parseFromJson(String str, Type type) {
        Gson gson = buildGson();
        return gson.fromJson(str, type);
    }
}
