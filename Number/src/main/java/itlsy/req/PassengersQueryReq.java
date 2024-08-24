package itlsy.req;


import lombok.Data;

import java.util.Date;

@Data
public class PassengersQueryReq extends PageReq {
    private Long numberId;
}