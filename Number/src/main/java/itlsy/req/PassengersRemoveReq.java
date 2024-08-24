package itlsy.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PassengersRemoveReq {
    /**
     * 乘车人id
     */
    @NotBlank(message = "乘车人id不能为空")
    private String id;
}
