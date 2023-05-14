 package com.blogsite.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.blogsite.Service.EmailService;
import com.blogsite.dao.UserRepository;
import com.blogsite.entity.User;

import jakarta.servlet.http.HttpSession;

import java.util.*;
@Controller
public class ForgotPasswordController {
	@Autowired
	private EmailService emailService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@GetMapping("/forgot-password")
	public String forgetPassword()
	{
		return "forget_password";
	}
	@PostMapping("/otp")
	public String sendOTP(@RequestParam("registerEmail") String email,
			HttpSession session) 
	{
		Random random = new Random();
		int otp = random.nextInt(999999-100000+1)+100000;
		System.out.println("OTP--> "+otp);
		String subject ="OTP TO RESET PASSWORD FOR BLOG SITE";
		String message = ""+
		"<div style='border: 2px solid red; padding:20px'>"+
				"<h2>" +"OTP is "+ "<b>"+otp +"</b>"+"</h1>"+
		"</div>";
		String to =  email;
		boolean flag = this.emailService.sendEmail(subject, message, to);
		if(flag)
		{
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			return "verify_otp";
		}
		else
		{
			session.setAttribute("message","check your email");
			return "forget_password";
		}
		
	}
	
	// verify otp
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp")int otp, HttpSession session)
	{
		 int myOtp = (int)session.getAttribute("myotp");
		 String email = (String)session.getAttribute("email");
		 if(myOtp==otp)
		 {
			 User user = this.userRepository.getUserByUserName(email);
			 System.out.println(" otp is "+otp+ " myotp"+myOtp);
			 if (user==null)
			 {
				 // user doesn't exist in our database
				 session.setAttribute("message", "User doesn't exist");
				 System.out.println("User doesn't exist");
				 return "forget_password";
			 }
			 else
			 {
				 
				 return "password_change_form";
			 }
		 }
		 else
		 {
			 session.setAttribute("message", "OTP is wrong");
			 return "verify_otp";
		 }
	}
	@PostMapping("/new-password")
	public String newPassword(@RequestParam("newPassword")String newPassword,
			@RequestParam("confirmNewPassword") String confirmNewPassword,
			HttpSession session) {
		String email = (String)session.getAttribute("email");
		User user = this.userRepository.getUserByUserName(email);
		System.out.println("Inside newPassword..."+user.getId());
		if (newPassword.equals(confirmNewPassword)) {
			user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(user);
			return "redirect:/signin?change=password changed successfully";
		}
		else
		{
			System.out.println("Both Passwords aren't same");
			return "redirect:/forgot-password";
		}
		
	}
	
}
