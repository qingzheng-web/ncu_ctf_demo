package cn.ncu.ctf.demo.controller;

import cn.ncu.ctf.demo.entities.Manager;
import cn.ncu.ctf.demo.entities.User;
import cn.ncu.ctf.demo.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.security.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @Author: jinge
 * @Date: 2022/10/26 23:05
 */
@Controller
@Slf4j

public class UserController {
//@RequestMapping("/user")

    @Autowired
    private UserService userService;


    //注意代码的规范，user的控制器UserController只对user表进行操作
    @RequestMapping({"/","index"})
    public String index(HttpServletRequest httpServletRequest, Model model) {
        User user = (User) httpServletRequest.getSession().getAttribute("user");
        model.addAttribute("user",user);
        return "index";
    }


    /**
     *  @author: ijnge
     *  @Date: 2022/11/10
     *  @Description:登录页
     */
    @RequestMapping(value = "/login")
    public String LoginPage(@ModelAttribute("message") String message, Model model) {
        model.addAttribute("message",message);
        return "Login";
    }

    @RequestMapping("/user/login")
    public String userLogin(
            User user,
            HttpServletRequest httpServletRequest,
            RedirectAttributes redirectAttributes,
            Model model) {
        //将页面提交的密码md5加密
        log.info("user: "+user);
        String password = user.getPassword();
//        password = DigestUtils.md5DigestAsHex(password.getBytes());
        //根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername,user.getUsername());

        //根据username查询用户user
        User userServiceOne = userService.getOne(queryWrapper);

        if (userServiceOne == null) {
            log.info("未查询到用户,请注册");
            redirectAttributes.addFlashAttribute("message","未查询到用户"+user.getUsername());
            return "redirect:/register";
        }
        //密码比对
        if(!userServiceOne.getPassword().equals(password)){
            log.info("密码错误");
            redirectAttributes.addFlashAttribute("message","密码错误");

            return  "redirect:/login";
        }
        //用户登录成功 将id存储session
        HttpSession session = httpServletRequest.getSession();
        session.setAttribute("user",userServiceOne);
        model.addAttribute("user",userServiceOne);

        return "/index";
    }


    @RequestMapping("user/register")
    public String RegisterPage(
            @ModelAttribute("message") String message, Model model
    ) {
        model.addAttribute("message",message);
        return "Register";
    }

    //用户注册
    @RequestMapping(value = "/user/register",method  = RequestMethod.POST)
    public String UserRegister(
            User user,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        //学习https://blog.csdn.net/qlzw1990/article/details/116996422
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername,user.getUsername());//user是POST接收到的内容
        //根据username查询用户user
        User readyUser = userService.getOne(queryWrapper);

        //如果用户已经注册
        if(readyUser != null) {
            log.info("用户已经存在");
            redirectAttributes.addFlashAttribute("message","用户"+user.getUsername()+"已存在，请返回登录页登录");
//            return "redirect:/Register";
            return "redirect:/Login";
        }
        //如果用户未注册
        String password = user.getPassword();

        //对密码进行加密，先不急着写
//        String encryptPassword = DigestUtils.md5DigestAsHex(password.getBytes());
//        user.setPassword(encryptPassword);
        userService.save(user);//添加一条用户数据
        user.setPassword(password);
        model.addAttribute("user",user);
        model.addAttribute("message","用户"+user.getUsername()+"注册成功！请登录");
        return "Login";
    }

    //这里大部分还未创建,需要在user目录下创建对应的html页面
    @RequestMapping("user/challenges")
    public String challenge(HttpServletRequest httpServletRequest,Model model) {
        HttpSession session = httpServletRequest.getSession();
        User user = (User)session.getAttribute("user");
        if(user == null) {
            //如果未登陆的话
            return "redirect:/Login";//重定向到Login页面
        }
        model.addAttribute("user",user);
        return "redirect:/user/challenge";
    }

    @RequestMapping("/solutions")
    public String solution() {
        return "solutions";
    }

    //    登出操作
    @RequestMapping("/logout")
    public String doLogout() throws Exception{
//        Subject currentUser = SecurityUtils.getSubject();
//        currentUser.logout();
        return "redirect:/index";
    }

    /**
     * 还需要检验是否登录，未登录的话则返回登录页面
     * @param httpServletRequest
     * @param model
     * @return
     */
    @RequestMapping("/user/profile")
    public String seeProfile(HttpServletRequest httpServletRequest,Model model){
        HttpSession session = httpServletRequest.getSession();
        User user = (User)session.getAttribute("user");
        if(user == null){
            return "redirect:/Login";
        }
        model.addAttribute("user",user);
//        model.addAttribute("message","用户"+user.getUsername()+"已经登录！请登录");
        return "redirect:/user/profile";
    }


}
