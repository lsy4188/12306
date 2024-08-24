package itlsy.serialize;

import cn.hutool.core.util.DesensitizedUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 *  手机号脱敏手机号脱敏反序列化
 */
public class PhoneSerializer extends JsonSerializer<String> {
    @Override
    public void serialize(String Phone, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        String PhoneNumber = DesensitizedUtil.mobilePhone(Phone);
        jsonGenerator.writeString(PhoneNumber);
    }
}
