package com.blogsite.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.blogsite.entity.MyOrder;

public interface MyOrderRepository extends JpaRepository<MyOrder, Long> {
	public MyOrder findByOrderId(String orderId);
//	@Query("select u from User u where u.email = :email")
	@Query ("SELECT o.flag FROM MyOrder o WHERE o.user.id = :id AND o.blogid = :blogid")
	public boolean getFlag(@Param("id") int id, 
							@Param("blogid") int blogid);
}
