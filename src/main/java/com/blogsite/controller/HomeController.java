package com.blogsite.controller;

import java.security.Principal;
import java.util.*;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;

import com.blogsite.dao.BlogRepository;
import com.blogsite.dao.MyOrderRepository;
import com.blogsite.dao.UserRepository;
import com.blogsite.entity.*;
import com.blogsite.helper.Message;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private BlogRepository blogRepository;
	@Autowired
	private MyOrderRepository myOrderRepository;
	static HashMap<String,Boolean> map =new HashMap();
	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Edged & Taken");
		return "home";
	}

	
	@GetMapping("/read/{id}")
	public String viewBlog(@PathVariable("id") Integer id,
			Model model,Principal principal) {
		
		Optional <Blog> blogId = this.blogRepository.findById(id);
		Blog blog = blogId.get();
		
		String username = principal.getName();
		User user = userRepository.getUserByUserName(username);
//		System.out.println("User "+user);
		model.addAttribute("user",user);
		if(blog.getPaid().equals("Yes"))
		{
			model.addAttribute("user",user);
			return "normal/Notpaid";
		}
		else
		{
			model.addAttribute("blog", blog);	
			return "view_blog";
		}
	
	}
	
	@GetMapping("/about")
	public String about(Model model) {
		model.addAttribute("title", "About | Edged & Taken");
		return "about";
	}

	@GetMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title", "Register | Edged & Taken");
		model.addAttribute("user", new User());

		return "signup";
	}

//  SignUp  --- POST
	@RequestMapping(value = "/do_register", method = RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result1,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model,
			HttpSession session) {
		try {
			if (!agreement) {
				System.out.println("You haven't agreed T&C");
				throw new Exception("You haven't agreed T&C");
			}
			if (result1.hasErrors()) {
				System.out.println("ERROR " + result1.toString());
				model.addAttribute("user", user);
				return "signup";
			}
			user.setEnabled(true);
			user.setRole("ROLE_USER");
			user.setImage("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));

			User result = this.userRepository.save(user);

			model.addAttribute("user", new User());
			session.setAttribute("message", new Message("Successfully Registered", "alert-success"));

			System.out.println("Agree " + agreement);
			System.out.println(user);

		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something Went Wrong" + e.getMessage(), "alert-danger"));
			return "signup";
		}
		return "signup";
	}

	// handler for custom login
	@GetMapping("/signin")
	public String customLogIn(Model model) {
		model.addAttribute("title", "Log In");
		return "login";
	}


//	@PostMapping("/user/create_order")
//	@ResponseBody
//	public String createOrder(@RequestBody Map<String, Object> data,
//			Principal principal) throws RazorpayException
//	{
//		System.out.println(data);
//		System.out.println(principal);
//		if (principal == null)
//		{
//			return "login_premium";
//		}
//		else
//		{
//			if (!map.get(principal.getName()))
//			{	
//				map.put(principal.getName(),true);
////JSON.stringify({ amount: amount, info: "order_request" })
//				int amt = Integer.parseInt(data.get("amount").toString()); // amount is the key in JSON.stringinfy
//				var client = new RazorpayClient("rzp_test_zCFwgvXFCB7zim", "pnHfDvWP93MmMRZdR1J0Ijnm");
//				
//				JSONObject ob = new JSONObject();
//				ob.put("amount", amt*100);
//				ob.put("currency", "INR");
//				ob.put("receipt", "txn_235425");
//				
//				//creating new order (Razorpay Server)
//				Order order =client.orders.create(ob);
//				System.out.println(order);
//				return order.toString();
//			}
//		}
//		return "";
//
//	}	
	
	
	
// payment integration old
	
//	@PostMapping("/create_order")
//	@ResponseBody
//	public String createOrder(@RequestBody Map<String, Object> data,Principal principal) throws RazorpayException
//	{
//		System.out.println("create order fun called");
//		System.out.println(data);
////		JSON.stringify({ amount: amount, info: "order_request" })
//		int amt = Integer.parseInt(data.get("amount").toString()); // amount is the key in JSON.stringinfy
//		var client = new RazorpayClient("rzp_test_zCFwgvXFCB7zim", "pnHfDvWP93MmMRZdR1J0Ijnm");
//		
//		JSONObject ob = new JSONObject();
//		ob.put("amount", amt*100);
//		ob.put("currency", "INR");
//		ob.put("receipt", "txn_235425");
//		
////creating new order (Razorpay Server)
//		Order order =client.orders.create(ob);
//		System.out.println(order);
//		
//		return order.toString();
//	}
		
//	@PostMapping("/update_order")
//	public ResponseEntity<?> updateOrder(@RequestBody Map<String,Object> data){
//		MyOrder myorder = this.myOrderRepository.findByOrderId(data.get("order_id").toString());
//		myorder.setPaymentId(data.get("payment_id").toString());
//		myorder.setStatus(data.get("status").toString());
//		
//		this.myOrderRepository.save(myorder);
//		System.out.println(data);
//		return ResponseEntity.ok(Map.of("msg","updated"));
//	}
	
//		@GetMapping("/premium/read/{blogid}")
//		public String showPaidBlog(
//				@PathVariable int blogid,
//				Model model)
//		{
//			Optional <Blog> blogId = this.blogRepository.findById(blogid);
//			Blog blog = blogId.get();
//			model.addAttribute("blog", blog);	
//			return "view_blog";
//		}
		
}
