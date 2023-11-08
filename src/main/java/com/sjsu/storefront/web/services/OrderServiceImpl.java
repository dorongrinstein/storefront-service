package com.sjsu.storefront.web.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sjsu.storefront.common.OrderStatus;
import com.sjsu.storefront.common.ResourceNotFoundException;
import com.sjsu.storefront.common.WorkflowException;
import com.sjsu.storefront.data.model.Order;
import com.sjsu.storefront.data.model.ShoppingCart;
import com.sjsu.storefront.data.model.User;
import com.sjsu.storefront.data.respository.OrderRepository;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public List<Order> getAllOrdersForUser( User user) {
        return orderRepository.findByUser(user);
    }
    
    @Override
    public List<Order> getAllOrdersForUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    public Order getOderById(Long id) throws ResourceNotFoundException {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + id));
    }

    @Override
    public Long createOder(User user, ShoppingCart cart) {
    	Order order = new Order();
    	order.setItems(cart.getItems());
    	order.setPaymentInfo(user.getPayment_info());
    	order.setShippingAddress(user.getAddress());
    	order.setTotalCost(cart.getTotalCost());
    	order.setTotalProductCost(cart.getTotalProductCost());
    	order.setTotalShipping(cart.getTotalShipping());
    	order.setTotalWeight(cart.getTotalWeight());
    	order.setUser(user);
    	
    	Order newOrder = orderRepository.save(order);
    	return newOrder.getId();
    	
    }

    @Override
    public Long cancelOrder(Long orderId) throws ResourceNotFoundException, WorkflowException {
    	Optional<Order> orderTemp = orderRepository.findById(orderId);
    	if(orderTemp.isPresent()) {
    		Order order = orderTemp.get();
    		//Order can be cancelled only if its still in RECIEVED status, means not yet SHIPPED
    		if(order.getStatus() == OrderStatus.RECIEVED) {
	            order.setStatus(OrderStatus.CANCELLED); 
	            orderRepository.save(order);
	            return order.getId();
    		}
    		else {
    			throw new WorkflowException("Order can not be Cancelled, already shipped " + orderId);
    		}
    	}
    	throw new ResourceNotFoundException("Order Not found for {Id} " + orderId);
    }
    
    @Override
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        orderRepository.updateStatus(orderId, status);
    }

}
