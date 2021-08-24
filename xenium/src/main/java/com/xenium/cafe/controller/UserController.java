package com.xenium.cafe.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class UserController {
	
	@RequestMapping("/")
	public ModelAndView SelectAllFromUser() {
		ModelAndView mav = new ModelAndView("main");
		return mav;
	}
}
