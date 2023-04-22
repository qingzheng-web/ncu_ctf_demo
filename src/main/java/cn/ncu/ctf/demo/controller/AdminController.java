package cn.ncu.ctf.demo.controller;

import cn.ncu.ctf.demo.entities.Manager;
import cn.ncu.ctf.demo.entities.User;
import cn.ncu.ctf.demo.service.ManagerService;
import cn.ncu.ctf.demo.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Controller
public class AdminController {

    @Autowired
    private ManagerService managerService;

    @Autowired
    private UserService userService;

    //跳转到后台登录页
    @RequestMapping("/admin")
    public String loginPage(@ModelAttribute("message") String message, Model model) {
        model.addAttribute("message",message);
        return "admin/login";
    }

    /**
     *  @author: ijnge
     *  @Date: 2022/10/26
     *  @Description: 处理用户登录请求
     *  这段代码中存在以下不足之处：
     *
     * 密码加密方式使用了MD5，但是MD5已经被认为是一种不安全的加密方式，容易被暴力破解。建议使用更强大的加密算法，如SHA256、bcrypt等。
     *
     * 在查询管理员信息时，使用了getOne方法，如果没有查询到结果，会抛出异常。建议改为使用list或者lambdaQuery方法，并且在查询结果为空时，做相应的处理。
     *
     * 在登录失败时，直接将错误信息返回给用户，存在安全风险。建议使用日志记录错误信息，避免敏感信息泄露。
     *
     * session中存储了管理员信息，存在安全隐患。建议对管理员信息进行加密或者使用JWT等状态无关的鉴权方式来保证安全性。
     *
     * 登录成功后，直接返回管理员首页，没有对用户角色进行校验。建议增加角色校验逻辑，确保管理员只能访问自己有权限的页面。
     */
    @RequestMapping("/admin/login")
    public String login(
            Manager manager,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes,
            Model model)
    {
        //将页面提交的密码md5加密
        String password = manager.getPassword();

        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Manager> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Manager::getUsername,manager.getUsername());

        //根据username查询用户user
        Manager ManagerByName = managerService.getOne(queryWrapper);

        if (ManagerByName == null) {
            log.info("未查询到用户");
            redirectAttributes.addFlashAttribute("message","未查询到用户"+manager.getUsername());

            return "redirect:/admin";
        }
        //密码比对
        if(!ManagerByName.getPassword().equals(password)){
            log.info("密码错误");
            redirectAttributes.addFlashAttribute("message","密码错误");

            return  "redirect:/admin";
        }
        //用户登录成功 将id存储session
        HttpSession session = request.getSession();
        session.setAttribute("manager",ManagerByName);

        model.addAttribute("manager",ManagerByName);
        return "/admin/index";
    }

    /**
     *  @author: ijnge
     *  @Date: 2022/11/9
     *  @Description: 登出
     */
    @RequestMapping("/admin/layout")
    public String layout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.removeAttribute("manager");
        return "redirect:/admin";
    }

    /**
     *  @author: ijnge
     *  @Date: 2022/10/31
     *  @Description:返回用户列表未做分页
     */
//    @RequestMapping("/admin/UserManage/UserList")
//    public String UserList(Model model,HttpServletRequest httpServletRequest) {
//        Manager manager =(Manager) httpServletRequest.getSession().getAttribute("manager");
//        List<User> list = userService.list();
//        model.addAttribute("userList",list);
//        model.addAttribute("manager",manager);
//        return "/admin/userList";
//    }

    //做了分页，但没有查看效果
    @RequestMapping("/admin/UserManage/UserList")
    public String UserList(Model model,HttpServletRequest httpServletRequest,
                           @RequestParam(defaultValue = "1") Integer pageNum,
                           @RequestParam(defaultValue = "10") Integer pageSize){
        Manager manager = (Manager) httpServletRequest.getSession().getAttribute("manage");
        PageInfo<User> pageInfo = userService.listWithPage(pageNum,pageSize);
        model.addAttribute("pageInfo",pageInfo);
        model.addAttribute("manager",manager);
        return "/admin/userList";
    }

    @RequestMapping("/admin/UserManager/deleteUser")
    public String deleteUser(String id){
        log.info("删除用户 {}",id);
        userService.removeById(id);
        return "redirect:/admin/UserManage";
    }



    /**
     *  @author: ijnge
     *  @Date: 2022/11/14
     *  @Description: 相应异步查询用户信息，便于更新操作
     */
    @ResponseBody
    @RequestMapping("/admin/Manager/Profile")
    public HashMap<Object,Object> showManagerProfile(String id) {
        HashMap<Object, Object> map = new HashMap<>();
        map.put("Manager",managerService.getById(id));
        return map;
    }



    /**
     *  @author: ijnge
     *  @Date: 2022/11/8
     *  @Description: 返回管理员列表
     */
    @RequestMapping("/admin/Manager/ManagerList")
    public String ManagerList(Model model,HttpServletRequest httpServletRequest) {
        Manager manager =(Manager) httpServletRequest.getSession().getAttribute("manager");
        List<Manager> list = managerService.list();
        ArrayList<String> primary_levelList = new ArrayList<String>(){{
            add("超级管理员");
            add("用户管理员");
            add("题库管理员");
            add("社区管理员");
        }};

        ArrayList<String> levelList = new ArrayList<>();
        for(int i=0;i< list.size();i++) {
            levelList.add(i, primary_levelList.get(manager.getLevel()));
        }

        model.addAttribute("levelList",levelList);
        model.addAttribute("ManagerList",list);
        model.addAttribute("manager",manager);

        return "/admin/managerList";
    }

    /**
     *  @author: ijnge
     *  @Date: 2022/11/13
     *  @Description:  添加管理员
     */
    @PostMapping("/admin/Manager/AddManager")
    public String addManager(
            Manager manager,
            Model model
    ) {
        log.info("添加管理员 {}",manager.toString());
        //md5加密
        manager.setPassword(DigestUtils.md5DigestAsHex(manager.getPassword().getBytes()));
        managerService.save(manager);
        return "redirect:/admin/Manager/ManagerList";
    }

    /**
     *  @author: ijnge
     *  @Date: 2022/11/13
     *  @Description: 删除管理员
     */
    @RequestMapping("/admin/Manager/deleteManager")
    public String deleteManager(String id) {
        log.info("删除管理员 {}",id);
        managerService.removeById(id);
        return "redirect:/admin/Manager/ManagerList";
    }

    @RequestMapping("/admin/Manager/updateManager")
    public String updateManager(Manager manager) {
        log.info("管理员{} ，更新成功",manager.getUsername());
        managerService.saveOrUpdate(manager);
        return "redirect:/admin/Manager/ManagerList";
    }


    /**
     *  @author: ijnge
     *  @Date: 2022/11/20
     *  @Description: 返回管理员详细
     */

    @RequestMapping("/admin/profile")
    public String Profile() {
        return "/admin/profile";
    }


}
