package itlsy.serialize;

import cn.hutool.core.util.DesensitizedUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 *身份证号脱敏反序列化
 */
public class IdCardSerializer extends JsonSerializer<String> {
    @Override
    public void serialize(String IdCard, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        // 将IdCard通过DesensitizedUtil工具类中的idCardNum方法进行处理，将中间的数字用****代替，并将结果赋值给IdCardInfo
        String IdCardInfo = DesensitizedUtil.idCardNum(IdCard, 2, 3);
        // 将处理后的IdCardInfo写入到json中
        jsonGenerator.writeString(IdCardInfo);
    }
}
