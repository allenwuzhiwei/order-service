package com.nusiss.orderservice.service;

import com.nusiss.orderservice.entity.OrderPayment;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/*
 OrderPaymentService 接口 - 定义订单支付相关的业务逻辑
 */
public interface OrderPaymentService {

    // 基础功能

    /*
     创建支付记录
     @param payment 要创建的支付记录对象
     @return 创建成功后的支付记录
     */
    OrderPayment createPayment(OrderPayment payment);

    /*
     根据支付ID获取支付记录详情
     @param paymentId 支付ID
     @return 支付记录对象（如果存在）
     */
    Optional<OrderPayment> getPaymentById(Long paymentId);

    /*
     获取所有支付记录
     @return 支付记录列表
     */
    List<OrderPayment> getAllPayments();

    /*
     更新支付信息
     @param payment 要更新的支付记录
     @return 是否更新成功
     */
    boolean updatePayment(OrderPayment payment);

    /*
     删除支付记录
     @param paymentId 要删除的支付记录ID
     @return 是否删除成功
     */
    boolean deletePayment(Long paymentId);

    // 扩展功能

    /*
     根据订单ID查询该订单的支付记录（可能有多笔）
     一个订单可能被用户分多次支付（比如分期、部分付款）
     @param orderId 订单ID
     @return 对应订单的支付记录列表
     */
    List<OrderPayment> getPaymentsByOrderId(Long orderId);

    /*
     多条件筛选支付记录（状态、支付时间范围、支付方式等）
     @param status       支付状态（可选）
     @param method       支付方式（可选）
     @param startDate    起始支付时间（可选）
     @param endDate      结束支付时间（可选）
     @return 满足条件的支付记录列表
     */
    List<OrderPayment> filterPayments(String status, String method, Date startDate, Date endDate);

    /*
     获取某订单的总支付金额（用于核对总额）
     @param orderId 订单ID
     @return 累计支付金额
     */
    Double calculateTotalPaidByOrderId(Long orderId);
}
