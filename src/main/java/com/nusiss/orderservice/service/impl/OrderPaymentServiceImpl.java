package com.nusiss.orderservice.service.impl;

import com.nusiss.orderservice.dao.OrderPaymentRepository;
import com.nusiss.orderservice.entity.OrderPayment;
import com.nusiss.orderservice.service.OrderPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/*
 OrderPaymentServiceImpl - 订单支付模块业务逻辑实现类
 */
@Service
public class OrderPaymentServiceImpl implements OrderPaymentService {

    @Autowired
    private OrderPaymentRepository paymentRepository;

    // 基础功能
    // 创建支付记录
    @Override
    public OrderPayment createPayment(OrderPayment payment) {
        payment.setCreateDatetime(LocalDateTime.now()); // 设置创建时间
        return paymentRepository.save(payment);
    }

    // 根据支付ID查询
    @Override
    public Optional<OrderPayment> getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId);
    }

    // 获取全部支付记录
    @Override
    public List<OrderPayment> getAllPayments() {
        return paymentRepository.findAll();
    }

    // 更新支付记录
    @Override
    public boolean updatePayment(OrderPayment payment) {
        if (paymentRepository.existsById(payment.getPaymentId())) {
            payment.setUpdateDatetime(LocalDateTime.now()); // 设置更新时间
            paymentRepository.save(payment);
            return true;
        }
        return false;
    }

    // 删除支付记录
    @Override
    public boolean deletePayment(Long paymentId) {
        if (paymentRepository.existsById(paymentId)) {
            paymentRepository.deleteById(paymentId);
            return true;
        }
        return false; //若不存在，则返回false
    }

    //扩展功能
    // 扩展功能1: 根据订单ID查询该订单所有支付记录
    @Override
    public List<OrderPayment> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    // 扩展功能2: 多条件筛选（状态、方式、时间）
    @Override
    public List<OrderPayment> filterPayments(String status, String method, Date startDate, Date endDate) {
        //从数据库中获取所有订单支付记录
        List<OrderPayment> all = paymentRepository.findAll();
        //将开始日期转换为LocalDateTime，如果为空则为null
        LocalDateTime startDateTime = startDate != null ? startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
        // 将结束日期转换为LocalDateTime，如果为空则为null
        LocalDateTime endDateTime = endDate != null ? endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;

        // 使用流式处理过滤支付记录
        return all.stream().filter(p -> {
            //初始化匹配标志为true
            boolean match = true;
            // 如果状态参数不为空且不匹配，标记为不匹配
            if (status != null && !status.equalsIgnoreCase(p.getPaymentStatus())) {
                match = false;
            }
            if (method != null && !method.equalsIgnoreCase(p.getPaymentMethod())) {
                match = false;
            }
            if (startDateTime != null && (p.getPaymentDate() == null || p.getPaymentDate().isBefore(startDateTime))) {
                match = false;
            }
            if (endDateTime != null && (p.getPaymentDate() == null || p.getPaymentDate().isAfter(endDateTime))) {
                match = false;
            }
            return match;
        }).collect(Collectors.toList());
    }

    // 扩展功能3: 获取某订单的累计支付金额
    @Override
    public Double calculateTotalPaidByOrderId(Long orderId) {
        List<OrderPayment> payments = paymentRepository.findByOrderId(orderId);
        return payments.stream().mapToDouble(OrderPayment::getAmountPaid).sum();
    }
}
