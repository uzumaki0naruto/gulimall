package com.atguigu.common.constant;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName HuangXiangXiang
 * @Date 2020/7/20 16:28
 * @Version V1.0
 **/
public class WareConstant {

    public enum PurchaseStatusEnum {
        CREATE(0, "新建"),
        ASSIGNED(1, "已分配"),
        RECEIVE(2, "已领取"),
        FINISH(3, "已完成"),
        HASERROR(4, "有异常");

        @Getter
        @Setter
        private int code;

        @Getter
        @Setter
        private String msg;

        PurchaseStatusEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }
    }


    public enum PurchaseDetailStatusEnum {
        CREATE(0, "新建"),
        ASSIGNED(1, "已分配"),
        BUYING(2, "正在采购"),
        FINISH(3, "已完成"),
        HASERROR(4, "采购失败");

        @Getter
        @Setter
        private int code;

        @Getter
        @Setter
        private String msg;

        PurchaseDetailStatusEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }
    }
}
