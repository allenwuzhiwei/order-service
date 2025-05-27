package com.nusiss.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nusiss.orderservice.config.ApiResponse;
import com.nusiss.orderservice.entity.OrderShipment;
import com.nusiss.orderservice.service.OrderShipmentService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderShipmentController.class)
public class OrderShipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderShipmentService shipmentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateShipment() throws Exception {
        OrderShipment shipment = new OrderShipment();
        Mockito.when(shipmentService.createShipment(any())).thenReturn(shipment);

        mockMvc.perform(post("/order-shipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shipment)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Shipment created"));
    }

    @Test
    void testUpdateShipment_Success() throws Exception {
        OrderShipment updated = new OrderShipment();
        Mockito.when(shipmentService.updateShipment(eq(1L), any())).thenReturn(true);

        mockMvc.perform(put("/order-shipments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Shipment updated"));
    }

    @Test
    void testUpdateShipment_NotFound() throws Exception {
        Mockito.when(shipmentService.updateShipment(eq(99L), any())).thenReturn(false);

        mockMvc.perform(put("/order-shipments/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrderShipment())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Shipment not found"));
    }

    @Test
    void testDeleteShipment() throws Exception {
        Mockito.when(shipmentService.deleteShipment(1L)).thenReturn(true);

        mockMvc.perform(delete("/order-shipments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Shipment deleted"));
    }

    @Test
    void testGetShipmentByOrderId_Found() throws Exception {
        OrderShipment shipment = new OrderShipment();
        shipment.setShipmentId(100L);
        Mockito.when(shipmentService.getShipmentByOrderId(1L)).thenReturn(Optional.of(shipment));

        mockMvc.perform(get("/order-shipments/by-order/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.shipmentId").value(100));
    }

    @Test
    void testGetShipmentByOrderId_NotFound() throws Exception {
        Mockito.when(shipmentService.getShipmentByOrderId(2L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/order-shipments/by-order/2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Shipment not found"));
    }

    @Test
    void testGetAllShipments() throws Exception {
        Mockito.when(shipmentService.getAllShipments()).thenReturn(List.of(new OrderShipment()));

        mockMvc.perform(get("/order-shipments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("All shipments retrieved"));
    }
}
