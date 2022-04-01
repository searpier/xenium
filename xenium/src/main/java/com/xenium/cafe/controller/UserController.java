package com.xenium.cafe.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xenium.cafe.service.UserService;
import com.xenium.cafe.util.Criteria;
import com.xenium.cafe.util.Paging;

@Controller
public class UserController {

	@Autowired
	UserService us;

	private int maximumAmount = 14;
	private int maximumAmountPerCookies = 5;

	@RequestMapping("/")
	public ModelAndView mainPage() {
		ModelAndView mav = new ModelAndView("index");
		Map<String, String> cu = new HashMap<>();
		cu.put("status", "메인페이지팝업");
		mav.addObject("popup", us.selectProductNotice(cu));
		return mav;
	}

	@RequestMapping("/signupPage")
	public ModelAndView signupPage() {
		ModelAndView mav = new ModelAndView("user/signup");
		return mav;
	}

	@RequestMapping("/signup")
	public ModelAndView signup(@RequestParam HashMap<String, String> param) {

		int result = us.signup(param);

		if (result == 1) {
			ModelAndView mav = new ModelAndView("redirect:/");
			return mav;
		} else {
			ModelAndView mav = new ModelAndView("forward:/signupPage");
			mav.addObject("param", param);
			mav.addObject("message", "존재하는 아이디입니다.");
			return mav;
		}
	}
	
	@RequestMapping("/aboutus")
	public ModelAndView aboutus(@RequestParam HashMap<String, String> param) {

		ModelAndView mav = new ModelAndView("aboutus");
		return mav;
		
	}

	@RequestMapping("/loginPage")
	public ModelAndView loginPage(@RequestParam Map<String, String> param) {
		ModelAndView mav = new ModelAndView("user/login");
		String dir = param.get("dir");
		String search = param.get("search");
		mav.addObject("dir", dir);
		mav.addObject("search", search);
		return mav;
	}

	@RequestMapping("/login")
	public ModelAndView login(@RequestParam Map<String, String> param, HttpSession session,
			HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("redirect:/");
		Map<String, String> result = us.login(param);

		if (result == null) {
			ModelAndView back = new ModelAndView("forward:/loginPage");
			back.addObject("param", param);
			back.addObject("message", "존재하지 않는 계정입니다.");
			return back;
		} else {

			mav.addObject("result", result);
			session.setAttribute("id", result);
			Cookie cookie1 = new Cookie("user_check", result.get("ID"));
			Cookie cookie2 = new Cookie("user_pw", result.get("PW"));
			if (param.get("remember_userId") != null) {
				response.addCookie(cookie1);
				response.addCookie(cookie2);
			} else {
				cookie1.setMaxAge(0);
				cookie2.setMaxAge(0);
				response.addCookie(cookie1);
				response.addCookie(cookie2);
			}

			String dir = param.get("dir");
			String search = param.get("search");
			if (dir != null && search != null) {
				ModelAndView mav2 = new ModelAndView("forward:/" + dir + "?searchWord=" + search);
				mav2.addObject("result", result);
				return mav2;
			}
			return mav;
		}

	}

	@RequestMapping("/logout")
	public ModelAndView logout(HttpSession session) {
		session.setAttribute("id", null);
		ModelAndView mav = new ModelAndView("redirect:/");
		return mav;
	}

	@RequestMapping("/changeInfo")
	public ModelAndView changeInfo(HttpSession session) {
		if (session.getAttribute("id") == null) {
			ModelAndView mav = new ModelAndView("redirect:/");
			return mav;
		}
		Map<String, String> info = (Map<String, String>) session.getAttribute("id");
		Map<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		map.putAll(info);
		Map<String, String> result = us.login(map);
		Map<String, String> resultmap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		resultmap.putAll(result);
		ModelAndView mav = new ModelAndView("user/changeinfo");
		mav.addObject("exparam", resultmap);
		return mav;
	}

	@RequestMapping("/changeCus")
	public ModelAndView changeCus(@RequestParam HashMap<String, String> param) {
		int result = us.changeCus(param);
		if (result == 1) {
			ModelAndView mav = new ModelAndView("redirect:/");
			return mav;
		} else {
			ModelAndView mav = new ModelAndView("forward:/changeInfo");
			mav.addObject("param", param);
			mav.addObject("message", "변경에 실패하였습니다.");
			return mav;
		}
	}

	@RequestMapping("/contentsList")
	public ModelAndView contentsList(Criteria param) {
		param.setPerPageNum(6);
		int total = us.selectContentCount(param);
		Paging paging = new Paging();
		paging.setCri(param);
		paging.setTotalCount(total);

		List<Map<String, String>> list = us.selectContent(param);

		for (Map<String, String> map : list) {
			String con = map.get("CONTENT");
			String nohtml = con.toString().replaceAll("\\<.*?>", "");
			if (nohtml.length() > 30)
				nohtml = nohtml.substring(0, Math.min(nohtml.length(), 30)) + " ....";
			map.put("SUM", nohtml);

			String title = map.get("TITLE");
			String noThtml = title.toString().replaceAll("\\<.*?>", "");
			if (noThtml.length() > 20)
				noThtml = noThtml.substring(0, Math.min(noThtml.length(), 20)) + " ..";
			map.put("TITLE", noThtml);

			String writer = map.get("WRITER");
			String noWhtml = writer.toString().replaceAll("\\<.*?>", "");
			if (noWhtml.length() > 20)
				noWhtml = noWhtml.substring(0, Math.min(noWhtml.length(), 10)) + " ...";
			map.put("WRITER", noWhtml);
		}

		ModelAndView mav = new ModelAndView("/contentslist");
		mav.addObject("result", list);
		mav.addObject("paging", paging);

		return mav;
	}

	@RequestMapping("/contentsDetail")
	public ModelAndView contentsDetail(String id, Criteria param) {
		param.setPerPageNum(6);
		int total = us.selectContentCount(param);
		Paging paging = new Paging();
		paging.setCri(param);
		paging.setTotalCount(total);
		Map<String, String> result = us.selectContentDetail(id);
		ModelAndView mav = new ModelAndView("/contentsdetail");
		mav.addObject("result", result);
		mav.addObject("paging", paging);

		return mav;
	}

	@RequestMapping("/productList")
	public ModelAndView productList(Criteria param, HttpSession session) {
		List<Map<String, Object>> par = new ArrayList<Map<String, Object>>();
		String userid = null;
		Map<String, String> accParam = new HashMap<>();
		if (session.getAttribute("id") != null) {
			Map<String, String> account = (Map<String, String>) session.getAttribute("id");
			userid = account.get("ID");
			accParam.put("userid", userid);
		}
		List<Map<String, String>> cList = us.selectUserCart(accParam);
		ModelAndView mav = new ModelAndView("/productlist");
		mav.addObject("cartList", cList);

		for (Map<String, String> component : cList) {
			Map<String, Object> comMap = new HashMap<String, Object>();
			comMap.put("id", component.get("PID"));
			comMap.put("amount", component.get("AMOUNT"));
			comMap.put("name", component.get("PRODUCTNAME"));
			comMap.put("price", component.get("PRICE"));
			par.add(comMap);
		}

		ObjectMapper mapper = new ObjectMapper();
		try {
			String paramText = mapper.writeValueAsString(par);
			mav.addObject("paramText", paramText);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Map<String, String> p = new HashMap<>();
		p.put("searchWord", param.getSearchWord());
		//int check = us.selectAvailProduct(p);
		int check = 1;

		param.setPerPageNum(12);
		int total = us.selectProductCount(param);
		Paging paging = new Paging();
		paging.setCri(param);
		paging.setTotalCount(total);

		List<Map<String, String>> list = us.selectProduct(param);
		if (check > 0) {
			mav.addObject("result", list);
			mav.addObject("paging", paging);
			mav.addObject("searchWord", param.getSearchWord());
			Map<String, String> cu = new HashMap<>();
			cu.put("status", "쿠키구매페이지상단");
			mav.addObject("cookienoticeupper", us.selectProductNotice(cu));
			Map<String, String> cl = new HashMap<>();
			cl.put("status", "쿠키구매페이지하단");
			mav.addObject("cookienoticelower", us.selectProductNotice(cl));
			Map<String, String> fu = new HashMap<>();
			fu.put("status", "커피구매페이지상단");
			mav.addObject("coffeenoticeupper", us.selectProductNotice(fu));
			Map<String, String> au = new HashMap<>();
			au.put("status", "전체구매페이지상단");
			mav.addObject("allupper", us.selectProductNotice(au));
			Map<String, String> fl = new HashMap<>();
			fl.put("status", "커피구매페이지하단");
			mav.addObject("coffeenoticelower", us.selectProductNotice(fl));
			Map<String, String> gu = new HashMap<>();
			gu.put("status", "굿즈구매페이지상단");
			mav.addObject("goodsnoticeupper", us.selectProductNotice(gu));
			Map<String, String> gl = new HashMap<>();
			gl.put("status", "굿즈구매페이지하단");
			mav.addObject("goodsnoticelower", us.selectProductNotice(gl));
			Map<String, String> al = new HashMap<>();
			al.put("status", "전체구매페이지하단");
			mav.addObject("alllower", us.selectProductNotice(al));
			return mav;
		} else {
			ModelAndView mav2 = new ModelAndView("/productlist");
			mav2.addObject("message", "판매기간이 아닙니다.");
			return mav2;
		}
	}

	@RequestMapping("/checkLogin")
	public @ResponseBody String checkLogin(HttpSession session) {

		if (session.getAttribute("id") != null) {
			return "logged";
		} else {
			return "need";
		}
	}

	@RequestMapping("/selectAvailAmount")
	public @ResponseBody String selectAvailAmount(String id, HttpSession session) {
		Map<String, String> data = new HashMap<String, String>();
		data.put("id", id);
		int result = us.selectAvailAmount(data);

		String userid = null;

		if (session.getAttribute("id") != null) {
			Map<String, String> account = (Map<String, String>) session.getAttribute("id");
			userid = account.get("ID");
			data.put("userid", userid);
		} else {
			return "-1";
		}
		int cart = us.selectCartAmount(data);

		return (result - cart) + "";
	}

	@RequestMapping("/selectAvailAmountCart")
	public @ResponseBody String selectAvailAmountCart(String id, HttpSession session) {
		Map<String, String> data = new HashMap<String, String>();
		data.put("id", id);
		int result = us.selectAvailAmount(data);
		return result + "";
	}

	@RequestMapping("/insertCart")
	public @ResponseBody String insertCart(@RequestBody(required = false) Object pocket, HttpSession session) {
		String userid = null;
		int total = 0;
		List<Map<String, String>> putList = new ArrayList<Map<String, String>>();
		Map<String, String> accParam = new HashMap<>();

		if (session.getAttribute("id") != null) {
			Map<String, String> account = (Map<String, String>) session.getAttribute("id");
			userid = account.get("ID");
			accParam.put("userid", userid);
		} else {
			return "loginFail";
		}

		List<Map<String, String>> cartSaved = us.selectCart(accParam);

		Map<String, List<LinkedHashMap<String, String>>> param = (Map<String, List<LinkedHashMap<String, String>>>) pocket;
		List<LinkedHashMap<String, String>> cartList = param.get("pocket");

		for (LinkedHashMap<String, String> one : cartList) {

			String flag = "false";
			int amount = 0;
			String cate = us.selectProductCate(String.valueOf(one.get("id")));

			for (int i = 0; i < cartSaved.size(); i++) {
				if (String.valueOf(cartSaved.get(i).get("PRODUCTID")).equals(String.valueOf(one.get("id")))) {
					flag = "true";
					amount = Integer.parseInt(String.valueOf(cartSaved.get(i).get("AMOUNT")));
				}
			}

			int added = Integer.parseInt(String.valueOf(one.get("amount")));

			if (cate.equals("쿠키")) {
				Map<String, String> paramMap = new HashMap<>();
				paramMap.put("id", String.valueOf(one.get("id")));
				if (added + amount > 5 || added + amount > us.selectAvailAmount(paramMap)) {
					return "amount";
				} else {
					total += added;
					added += amount;
					// total+=added;
				}
			} else {
				Map<String, String> paramMap = new HashMap<>();
				paramMap.put("id", String.valueOf(one.get("id")));
				if (us.selectAvailAmount(paramMap) < added) {
					return "amount";
				}
			}

			Map<String, String> daoMap = new HashMap<>();
			daoMap.put("productid", String.valueOf(one.get("id")));
			daoMap.put("amount", added + "");
			daoMap.put("userid", userid);
			daoMap.put("flag", flag);
			daoMap.put("cate", cate);
			putList.add(daoMap);
		}

		for (Map<String, String> cart : putList) {
			if (cart.get("cate").equals("쿠키") && cart.get("flag").equals("false")) {
				// total+=Integer.parseInt(cart.get("amount"));
			}
		}
		for (int i = 0; i < cartSaved.size(); i++) {
			boolean flag = true;
			for (int j = 0; j < putList.size(); j++) {
				if (String.valueOf(cartSaved.get(i).get("PRODUCTID"))
						.equals(String.valueOf(putList.get(j).get("id")))) {
					flag = false;
				}
			}
			if (flag && cartSaved.get(i).get("CATENAME").equals("쿠키")) {
				total += Integer.parseInt(String.valueOf(cartSaved.get(i).get("AMOUNT")));
			}
		}

		// System.out.println("total : "+total);
		if (total > 14) {
			return "amount";
		}
		for (Map<String, String> cart : putList) {
			String flag = cart.get("flag");
			if (flag.equals("true")) {
				us.updateCartAmount(cart);
			} else {
				us.insertCart(cart);
			}
		}

		return "success";
	}

	@RequestMapping("/addCart")
	public @ResponseBody String addCart(@RequestBody(required = false) Object pocket, HttpSession session) {
		String userid = null;
		int total = 0;
		List<Map<String, String>> putList = new ArrayList<Map<String, String>>();
		Map<String, String> accParam = new HashMap<>();

		if (session.getAttribute("id") != null) {
			Map<String, String> account = (Map<String, String>) session.getAttribute("id");
			userid = account.get("ID");
			accParam.put("userid", userid);
		} else {
			return "loginFail";
		}

		List<Map<String, String>> cartSaved = us.selectCart(accParam);

		Map<String, List<LinkedHashMap<String, String>>> param = (Map<String, List<LinkedHashMap<String, String>>>) pocket;
		List<LinkedHashMap<String, String>> cartList = param.get("pocket");

		for (LinkedHashMap<String, String> one : cartList) {

			String flag = "false";
			int amount = 0;
			String cate = "";

			for (int i = 0; i < cartSaved.size(); i++) {
				if (String.valueOf(cartSaved.get(i).get("PRODUCTID")).equals(String.valueOf(one.get("id")))) {
					flag = "true";
					amount = Integer.parseInt(String.valueOf(cartSaved.get(i).get("AMOUNT")));
					cate = cartSaved.get(i).get("CATENAME");
				}
			}
			int added = Integer.parseInt(String.valueOf(one.get("amount")));

			Map<String, String> daoMap = new HashMap<>();
			daoMap.put("productid", String.valueOf(one.get("id")));
			daoMap.put("amount", added + "");
			daoMap.put("userid", userid);
			daoMap.put("flag", flag);

			if (flag.equals("false")) {
				cate = one.get("cate");
			}
			daoMap.put("cate", cate);

			if (cate.equals("쿠키")) {
				if (added > 5) {
					return "amount";
				}
			}
			putList.add(daoMap);

		}

		for (Map<String, String> cart : putList) {
			if (cart.get("cate").equals("쿠키"))
				total += Integer.parseInt(cart.get("amount"));
		}
		if (total > 14) {
			return "amount";
		}

		for (Map<String, String> cart : putList) {
			String flag = cart.get("flag");
			if (flag.equals("true")) {
				if (Integer.parseInt(String.valueOf(cart.get("amount"))) > 0)
					us.updateCartAmount(cart);
			} else {
				if (Integer.parseInt(String.valueOf(cart.get("amount"))) > 0)
					us.insertCart(cart);
			}
		}
		ObjectMapper mapper = new ObjectMapper();
		try {
			String paramText = mapper.writeValueAsString(putList);
			return paramText;
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "success";
	}

	@RequestMapping("/cartList")
	public ModelAndView cartList(HttpSession session) {
		List<Map<String, Object>> param = new ArrayList<Map<String, Object>>();
		String userid = null;
		Map<String, String> accParam = new HashMap<>();
		if (session.getAttribute("id") != null) {
			Map<String, String> account = (Map<String, String>) session.getAttribute("id");
			userid = account.get("ID");
			accParam.put("userid", userid);
		}
		List<Map<String, String>> cList = us.selectUserCart(accParam);
		ModelAndView mav = new ModelAndView("/cartlist");
		mav.addObject("cartList", cList);

		if (session.getAttribute("message") != null) {
			mav.addObject("message", session.getAttribute("message"));
			session.setAttribute("message", null);
		}

		for (Map<String, String> component : cList) {
			Map<String, Object> comMap = new HashMap<String, Object>();
			comMap.put("id", component.get("PID"));
			comMap.put("amount", component.get("AMOUNT"));
			comMap.put("name", component.get("PRODUCTNAME"));
			comMap.put("price", component.get("PRICE"));
			param.add(comMap);
		}

		ObjectMapper mapper = new ObjectMapper();
		try {
			String paramText = mapper.writeValueAsString(param);
			mav.addObject("paramText", paramText);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return mav;
	}

	@RequestMapping("/deleteCart")
	public @ResponseBody String deleteCart(String id, HttpSession session) {
		String userid = null;
		Map<String, String> accParam = new HashMap<>();
		if (session.getAttribute("id") != null) {
			Map<String, String> account = (Map<String, String>) session.getAttribute("id");
			userid = account.get("ID");
			accParam.put("userid", userid);
			accParam.put("id", id);
		} else {
			return "loginFail";
		}
		int result = us.deleteCart(accParam);
		if (result == 1) {
			return "success";
		} else {
			return "failed";
		}
	}

	@RequestMapping("/buyPage")
	public ModelAndView buyingPage(HttpSession session) {
		List<Map<String, Object>> param = new ArrayList<Map<String, Object>>();
		String userid = null;
		Map<String, String> account = null;
		Map<String, String> accParam = new HashMap<>();
		if (session.getAttribute("id") != null) {
			account = (Map<String, String>) session.getAttribute("id");
			userid = account.get("ID");
			accParam.put("userid", userid);
		} else {
			ModelAndView back = new ModelAndView("forward:/loginPage");
			back.addObject("param", param);
			back.addObject("message", "존재하지 않는 계정입니다.");
			return back;
		}
		List<Map<String, String>> cList = us.selectUserCart(accParam);
		ModelAndView mav = new ModelAndView("/buypage");

		int total = 0;
		for (Map<String, String> cart : cList) {
			int amount = Integer.parseInt(String.valueOf(cart.get("AMOUNT")));
			if (amount > 5 && cart.get("CATENAME").equals("쿠키")) {
				ModelAndView back = new ModelAndView("forward:/cartList");
				back.addObject("param", param);
				session.setAttribute("message", "쿠키는 품목당 5개 이상 구매 하실수 없습니다.");
				return back;
			}else if(cart.get("CATENAME").equals("쿠키")) {
				total += amount;
			}
		}
		if (total < 4 && total>0) {
			ModelAndView back = new ModelAndView("forward:/cartList");
			back.addObject("param", param);
			session.setAttribute("message", "쿠키는 최소 3개 이상 구매 하셔야합니다.");
			return back;
		}

		mav.addObject("cartList", cList);
		mav.addObject("user", account);

		for (Map<String, String> component : cList) {
			Map<String, Object> comMap = new HashMap<String, Object>();
			comMap.put("id", component.get("PID"));
			comMap.put("amount", component.get("AMOUNT"));
			comMap.put("name", component.get("PRODUCTNAME"));
			comMap.put("price", component.get("PRICE"));
			param.add(comMap);
		}
		ObjectMapper mapper = new ObjectMapper();
		try {
			String paramText = mapper.writeValueAsString(param);
			mav.addObject("paramText", paramText);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mav;
	}

	@RequestMapping("/confirmBuying")
	public @ResponseBody String confirmBuying(@RequestBody(required = false) Object pocket, HttpSession session) {
		String userid = null;
		int total = 0;
		int minTot = 0;
		List<Map<String, String>> putList = new ArrayList<Map<String, String>>();
		Map<String, String> accParam = new HashMap<>();

		if (session.getAttribute("id") != null) {
			Map<String, String> account = (Map<String, String>) session.getAttribute("id");
			userid = account.get("ID");
			accParam.put("userid", userid);
		} else {
			return "loginFail";
		}

		List<Map<String, String>> cartSaved = us.selectCart(accParam);

		Map<String, List<LinkedHashMap<String, String>>> param = (Map<String, List<LinkedHashMap<String, String>>>) pocket;
		List<LinkedHashMap<String, String>> cartList = param.get("pocket");

		List<Map<String, String>> idList=us.checkAvailProductId();
		for (LinkedHashMap<String, String> one : cartList) {
			boolean avail=true;
			String flag = "false";
			int amount = 0;
			String cate = "";
			
			

			for (int i = 0; i < cartSaved.size(); i++) {
				if (String.valueOf(cartSaved.get(i).get("PRODUCTID")).equals(String.valueOf(one.get("id")))) {
					flag = "true";
					amount = Integer.parseInt(String.valueOf(cartSaved.get(i).get("AMOUNT")));
					cate = cartSaved.get(i).get("CATENAME");
				}
				for( Map<String, String> id: idList) {
					String singleId=String.valueOf(id.get("ID"));
					if (String.valueOf(cartSaved.get(i).get("PRODUCTID")).equals(singleId)) {
						avail=false;
					}
				}
				if(avail) {
					return "avail";
				}
			}
			int added = Integer.parseInt(String.valueOf(one.get("amount")));
			
			if (cate.equals("쿠키"))
			minTot += added;

			Map<String, String> daoMap = new HashMap<>();
			daoMap.put("productid", String.valueOf(one.get("id")));
			daoMap.put("amount", added + "");
			daoMap.put("userid", userid);
			daoMap.put("flag", flag);

			if (flag.equals("false")) {
				cate = one.get("cate");
			}
			daoMap.put("cate", cate);

			if (cate.equals("쿠키")) {
				if (added > 5) {
					return "amount";
				}
			}
			putList.add(daoMap);
		}
		
		
		
		
		if (minTot < 4&& minTot>0) {
			return "amount";
		}

		for (Map<String, String> cart : putList) {
			if (cart.get("cate").equals("쿠키"))
				total += Integer.parseInt(cart.get("amount"));
		}
		if (total > 14) {
			return "amount";
		}

		return "success";

	}

	@RequestMapping("/confirmRest")
	public @ResponseBody Object confirmRest(@RequestBody(required = false) Object pocket, HttpSession session) {

		String userid = null;
		Map<String, String> accParam = new HashMap<>();
		if (session.getAttribute("id") != null) {
			Map<String, String> account = (Map<String, String>) session.getAttribute("id");
			userid = account.get("ID");
		}else {
			return "loginFail";
		}

		Map<String, List<LinkedHashMap<String, String>>> param = (Map<String, List<LinkedHashMap<String, String>>>) pocket;

		LinkedHashMap<String, String> user = (LinkedHashMap<String, String>) param.get("param");
		List<LinkedHashMap<String, String>> cart = param.get("pocket");

		boolean errorFlag=false;
		for (LinkedHashMap<String, String> ss : cart) {
			Map<String, String> ckcParam = new HashMap<>();
			ckcParam.put("id", String.valueOf(ss.get("id")));
			
			Map<String, String> buyWanted=null;
			try {
				buyWanted=us.checkAmountProduct(ckcParam);
			}catch(Exception e) {
				return "noneProduct";
			}
			if(buyWanted==null) {
				return "noneProduct";
			}

			int restAmt=Integer.parseInt(String.valueOf(buyWanted.get("AMOUNT")));
			
			if(restAmt<Integer.parseInt(String.valueOf(ss.get("amount")))) {
				ss.put("amount", String.valueOf(buyWanted.get("AMOUNT")));
				ss.put("flag", "true");
				errorFlag=true;
			}
		}
		
		if(errorFlag) {
			return cart;
		}
		
		user.put("userid", userid);
		int result = us.insertOrderBook(user);
		if (result == 1) {
			for (LinkedHashMap<String, String> ss : cart) {
				ss.put("userid", userid);
				ss.put("pid", String.valueOf(ss.get("id")));
				us.updateAmountProduct(ss);
				ss.put("id", String.valueOf(user.get("preid")));
				us.insertOrderDetail(ss);
			}
			if (session.getAttribute("id") != null) {
				Map<String, String> account = (Map<String, String>) session.getAttribute("id");
				userid = account.get("ID");
				account.put("userid", userid);
				
				us.deleteCartAll(account);
				
			}
			
			return "success";
		}else {
			return "failed";
		}
		
	}

	@RequestMapping("/orderList")
	public ModelAndView orderList(HttpSession session) {
		List<Map<String, Object>> param = new ArrayList<Map<String, Object>>();
		String userid = null;
		Map<String, String> accParam = new HashMap<>();
		if (session.getAttribute("id") != null) {
			Map<String, String> account = (Map<String, String>) session.getAttribute("id");
			userid = account.get("ID");
			accParam.put("userid", userid);
		}
		List<Map<String, String>> cList = us.selectOrderBook(accParam);
		ModelAndView mav = new ModelAndView("/orderhistory");
		mav.addObject("orderList", cList);

		/*
		 * for (Map<String, String> component : cList) { Map<String, Object> comMap =
		 * new HashMap<String, Object>(); comMap.put("id", component.get("PID"));
		 * comMap.put("amount", component.get("AMOUNT")); comMap.put("name",
		 * component.get("PRODUCTNAME")); comMap.put("price", component.get("PRICE"));
		 * param.add(comMap); }
		 * 
		 * ObjectMapper mapper = new ObjectMapper(); try { String paramText =
		 * mapper.writeValueAsString(param); mav.addObject("paramText", paramText); }
		 * catch (JsonProcessingException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */

		return mav;
	}
	
	@RequestMapping("/checkOrder")
	public @ResponseBody Object checkOrder(String id, HttpSession session) {

		String userid = null;
		Map<String, String> accParam = new HashMap<>();
		if (session.getAttribute("id") != null) {
			Map<String, String> account = (Map<String, String>) session.getAttribute("id");
			userid = account.get("ID");
		}else {
			return "loginFail";
		}
		Map<String, String> account =new HashMap<>();
		account.put("userid", userid);
		account.put("id",  id);
		List<Map<String, String>> cList = us.selectOrderDetail(account);
		return cList;
		
	}
}
