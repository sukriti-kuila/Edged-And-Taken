package com.blogsite.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import com.blogsite.dao.BlogRepository;
import com.blogsite.dao.MyOrderRepository;
import com.blogsite.dao.UserRepository;
import com.blogsite.entity.Blog;
import com.blogsite.entity.User;
import com.blogsite.entity.MyOrder;
import com.blogsite.helper.Message;

import java.io.*;
import java.nio.file.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import com.razorpay.*;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private BlogRepository blogRepository;
	@Autowired
	private MyOrderRepository myOrderRepository;
	
	static HashMap<String,ArrayList<Integer>> map = new HashMap();
	// This method will run everytime
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String username = principal.getName();
		System.out.println("USERNAME "+username);
//		get the user using user name(in this project email is user name)
		User user = userRepository.getUserByUserName(username);
//		System.out.println("User "+user);
		model.addAttribute("user",user);
		
	}
	
	@GetMapping("/add-blog")
	public String openAddBlogForm(Model model)
	{
		model.addAttribute("title","Add Blog | User Dashboard");
		model.addAttribute("blog",new Blog());
		
		return "normal/add_blog_form";
	}
	
	@PostMapping("/process-blog")
	public String processBlog(@ModelAttribute("blog") @Valid Blog blog, 
			BindingResult bindingResult, 
			@RequestParam("image") MultipartFile file,
			Principal principal, Model model) {
//		System.out.println(blog);
		model.addAttribute("title","Add Blog | User Dashboard");
		try {
			
		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);
		
//		processing and uploading file
		if(file.isEmpty())
		{
			System.out.println("Empty Image File");
			blog.setImage("thumbnail.jpg"); //default img will be set if user doesn't upload any image
		}
		else
		{
//			 update the file to folder and update the name to blog
			blog.setImage(file.getOriginalFilename());
			File saveFile = new ClassPathResource("static/img").getFile();
			
			Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			
			System.out.println("Image is Uploaded");
		}
		
		blog.setUser(user);
		user.getBlogs().add(blog);
		
		this.userRepository.save(user);
		System.out.println("Added to DB");
		}
		catch(Exception e)
		{
			System.out.println("ERROR "+e.getMessage());
		}
		
		
		return "normal/add_blog_form";
	}
	
// 	show blog handler
	@GetMapping("/show-blogs/{page}")
	public String showBlogs(@PathVariable("page") Integer page, Model model, Principal principal)
	{
		model.addAttribute("title","View Blogs");
		String username = principal.getName(); // email will be stored in username
		User user = this.userRepository.getUserByUserName(username);
//		List<Blog> blogs = user.getBlogs();
		PageRequest pageable = PageRequest.of(page, 3); //(currentPage, content per page)
		Page<Blog> blogs = this.blogRepository.findBlogsByUser(user.getId(), pageable);
		model.addAttribute("blogs",blogs);
		model.addAttribute("currentpage",page);
		model.addAttribute("totalpage",blogs.getTotalPages()); // if we have 15 contents then getTotalPages() will return 15/3 = 5 pages
		return "normal/show_blogs";
	}
	
// showing a single blog content
	@RequestMapping("/blog/{id}")
	public String showBlogDetail(@PathVariable("id") Integer id, Model model, Principal principal) {
		System.out.println("Blog id = "+id);
		Optional <Blog> blogId = this.blogRepository.findById(id);
		Blog blog = blogId.get();
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==blog.getUser().getId())
		{
			model.addAttribute("blog", blog);			
		}
		
		return "normal/blog_detail";
	}
	
//  delete blog handler
	@GetMapping("/delete/{id}")
	public String deleteBlog(@PathVariable ("id") Integer id, Principal principal, HttpSession session) throws IOException
	{
		Optional <Blog> blogId = this.blogRepository.findById(id);
		Blog blog = blogId.get();
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==blog.getUser().getId())
		{
			// delete old photo
			Blog oldBlog = this.blogRepository.findById(blog.getId()).get();
			File deleteFile = new ClassPathResource("static/img").getFile();
			File file1 = new File(deleteFile, oldBlog.getImage());
			file1.delete();
			
			this.blogRepository.delete(blog);
		}
		
		session.setAttribute("message", new Message("Blog deleted successfully","success"));
		return "redirect:/user/show-blogs/0";
	}
	
//	open update form handler
	@PostMapping("/update-blog/{id}")
	public String updateForm(@PathVariable ("id") Integer id, Model m) {
		m.addAttribute("title","Update Blog");
		Blog blog = this.blogRepository.findById(id).get();
		m.addAttribute("blog",blog);
		m.addAttribute("privacy",blog.getPrivacy());
		m.addAttribute("paid",blog.getPaid());
		return "normal/update_form";
	}
	
//	update blog handler
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute("blog") @Valid Blog blog, 
			BindingResult bindingResult, 
			@RequestParam("image") MultipartFile file,
			Principal principal, Model model) {
			try { 
				// old bolg details
				Blog oldBlog = this.blogRepository.findById(blog.getId()).get();
				if(!file.isEmpty()) {
					// delete old photo
					File deleteFile = new ClassPathResource("static/img").getFile();
					File file1 = new File(deleteFile, oldBlog.getImage());
					file1.delete();
					
					// update new photo
					File saveFile = new ClassPathResource("static/img").getFile();
					Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
					Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
					blog.setImage(file.getOriginalFilename());
				} 
				else {
					blog.setImage(oldBlog.getImage());
				}
				
				User user = this.userRepository.getUserByUserName(principal.getName());
				blog.setUser(user); 
				this.blogRepository.save(blog); 
			} 
			catch(Exception e) {
		 
			}
			System.out.println("Output " +blog.getId()+" "+blog.getTitle());
			return "redirect:/user/blog/"+blog.getId();
//			return "hi";
	}
// my profile handler
	
	@GetMapping("/profile")
	public String myprofile(Model model)
	{
		model.addAttribute("title","Profile");
		return "normal/profile";
	}
// settings change password
	
	@GetMapping("/settings")
	public String settings() {
		return "normal/settings";
	}
// change password -- POST
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword,
			@RequestParam("confirmNewPassword") String confirmNewPassword,
			Principal principal,
			HttpSession session
			) 
	{
		System.out.println("Old: "+oldPassword+" New: "+newPassword);
		
		String username = principal.getName();
		User currentUser = this.userRepository.getUserByUserName(username);
		
		if (this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword()))
		{
			//change the password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message", new Message("your password has been updated", "success"));
		}
		else
		{
			//error
			session.setAttribute("message", new Message("old password is wrong", "danger"));
			return "redirect:/user/settings";
		}
		return "redirect:/user/index";
	}
	
// payment integration
	
//	@PostMapping("/create_order")
//	@ResponseBody
//	public String createOrder(@RequestBody Map<String, Object> data,Principal principal) throws RazorpayException
//	{
////		System.out.println("Order Funtion is working....");
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
////saving the payment details in the database
//		MyOrder myorder = new MyOrder(); //com.blogsite.entity.MyOrder
//		myorder.setAmount(order.get("amount")+"");
//		myorder.setOrderId(order.get("id"));
//		myorder.setPaymentId(null);
//		myorder.setStatus("created");
//		String username = principal.getName();
//		User currentUser = this.userRepository.getUserByUserName(username);
//		myorder.setUser(currentUser);
//		this.myOrderRepository.save(myorder);
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
	
	@GetMapping("/home_page")
	public String homePage(Model model,Principal principal) {
		model.addAttribute("title", "All Blogs | Sukriti Kuila Blog");

		List<String> usernames = this.userRepository.findAllEmail();
//		System.out.println(usernames);
		List<Blog> blogs = new ArrayList<Blog>();
		
		for (String user:usernames)
		{
			System.out.println(user);
			List<Blog> userBlog = this.userRepository.getUserByUserName(user).getBlogs();
			for(Blog b:userBlog)
			{
				blogs.add(b);
			}	
		}
		
		model.addAttribute("blogs", blogs);
		// to check whether the user has ALREADY paid for the article
		model.addAttribute("map",map);  
		System.out.println("Purchase History -- "+map);
		return "home_page";
	}

	
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String, Object> data,Principal principal) throws RazorpayException
	{
//		System.out.println("create order fun called");
//		System.out.println(data);
//		JSON.stringify({ amount: amount, info: "order_request" })
		int amt = Integer.parseInt(data.get("amount").toString()); // amount is the key in JSON.stringinfy
		int bId = Integer.parseInt(data.get("bid").toString());
		var client = new RazorpayClient("rzp_test_zCFwgvXFCB7zim", "pnHfDvWP93MmMRZdR1J0Ijnm");
		
		JSONObject ob = new JSONObject();
		ob.put("amount", amt*100);
		ob.put("currency", "INR");
		ob.put("receipt", "txn_235425");
		
//creating new order (Razorpay Server)
		Order order =client.orders.create(ob);
//		System.out.println(order);
		
//saving the payment details in the database
		MyOrder myorder = new MyOrder(); //com.blogsite.entity.MyOrder
	    myorder.setAmount(order.get("amount")+"");
	    myorder.setOrderId(order.get("id"));
		myorder.setPaymentId(null);
		myorder.setStatus("created");
		myorder.setBlogid(bId);
		String username = principal.getName();
		User currentUser = this.userRepository.getUserByUserName(username);
		myorder.setUser(currentUser);
		this.myOrderRepository.save(myorder);		
		
		return order.toString();
	}
	
	@PostMapping("/update_order")
	public ResponseEntity<?> updateOrder(@RequestBody Map<String,Object> data, Principal principal){
		MyOrder myorder = this.myOrderRepository.findByOrderId(data.get("order_id").toString());
		myorder.setPaymentId(data.get("payment_id").toString());
		myorder.setStatus(data.get("status").toString());
		myorder.setFlag(true);
		
		int blogid = Integer.parseInt(data.get("blogid").toString());
		String username =principal.getName();
		if (!map.containsKey(username))
		{	
			ArrayList l= new ArrayList<>();
			l.add(blogid);
			map.put(username, l);
		}
		else
		{
			ArrayList l = map.get(username);
			l.add(blogid);
			map.put(username, l);
		}
		
		
		System.out.println(map);
		
		this.myOrderRepository.save(myorder);
//		System.out.println("DATA "+data);
		return ResponseEntity.ok(Map.of("msg","updated"));
	}
	@GetMapping("/premium/read/{blogid}")
	public String showPaidBlog(
			@PathVariable int blogid,
			Model model,
			Principal principal)
	{
		String username =principal.getName();
		User user = this.userRepository.getUserByUserName(username);
		System.out.println(user.getId());
		boolean flag1 = this.myOrderRepository.getFlag(user.getId(), blogid);
		System.out.println(flag1);
		boolean flag = false;
		try {
			flag = this.myOrderRepository.getFlag(user.getId(), blogid);
			System.out.println("flag "+flag+" user"+user.getId());
		}
		catch (Exception e) {
			return "normal/Notpaid";
		}
		
		if(!flag)
			return "normal/Notpaid";
		Optional <Blog> blogId = this.blogRepository.findById(blogid);
		Blog blog = blogId.get();
		model.addAttribute("blog", blog);	
//		return "view_blog";
		return "normal/blog_detail";
	}	
}
