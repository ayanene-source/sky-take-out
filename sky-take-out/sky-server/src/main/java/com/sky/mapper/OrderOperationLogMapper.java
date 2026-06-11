package com.sky.mapper;

import com.sky.entity.OrderOperationLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderOperationLogMapper {

    @Insert("insert into order_operation_log (order_id, action, before_status, after_status, operator_type, operator_id, remark, create_time) " +
            "values (#{orderId}, #{action}, #{beforeStatus}, #{afterStatus}, #{operatorType}, #{operatorId}, #{remark}, #{createTime})")
    void insert(OrderOperationLog operationLog);

    @Select("select * from order_operation_log where order_id = #{orderId} order by create_time asc, id asc")
    List<OrderOperationLog> listByOrderId(Long orderId);
}
