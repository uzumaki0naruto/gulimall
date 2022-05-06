package com.atguigu.common.to.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 库存锁定成功to
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class stockLockedTo implements Serializable {
    private Long id; //库存工作单
    private stockDetailTo stockDetailTo; //详情id
}
