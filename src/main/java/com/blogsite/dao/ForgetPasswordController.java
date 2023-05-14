package com.blogsite.dao;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
@Controller
public class ForgetPasswordController {
	@GetMapping("/forget-password")
	public String forgetPasswordForm()
	{
		return "forget_password";
	}
	@PostMapping("/send-otp")
	@ResponseBody
	public String sendOTP(@RequestParam("registerEmail") String email)
	{
		Random random  = new Random();
		int otp = random.nextInt(999999-100000+1)+100000;
		System.out.println("mail "+email+" otp"+otp);
		return "verify_otp";
	}
}
