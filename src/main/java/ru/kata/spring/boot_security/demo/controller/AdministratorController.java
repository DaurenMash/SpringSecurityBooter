package ru.kata.spring.boot_security.demo.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.services.RoleService;
import ru.kata.spring.boot_security.demo.services.UserService;

import java.security.Principal;
import java.util.List;


@Controller
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/auth")
public class AdministratorController {

    private final UserService userService;
    private final RoleService roleService;


    public AdministratorController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping("/admin")

    public String adminPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User user = (User) userService.loadUserByUsername(username);
        model.addAttribute("id", user.getId());
        model.addAttribute("username", username);
        model.addAttribute("roles", user.getRoles());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("allRole", roleService.getAllRoles());
        model.addAttribute("newUser", new User());
        return "admin";
    }

    @GetMapping("/admin/add")
    public String showAddUserForm(Model model, Principal principal) {
        String name = principal.getName();
        User user = userService.findByUserName(name);
        model.addAttribute("user", user);
        model.addAttribute("newUser", new User());
        model.addAttribute("allRole", roleService.getAllRoles());
        return "new_user";
    }

    @PostMapping("/admin/add")
    public String addUser(@ModelAttribute User user, @RequestParam("roles") List<Long> roleIds) {
        List<Role> roles = roleService.getRolesById(roleIds);
        user.setRoles(roles);
        userService.saveUser(user, roles);
        return "redirect:/auth/admin";
    }


    @PostMapping("/admin/user/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        User user = userService.findUserById(id);
        userService.delete(user.getId());
        return "redirect:/auth/admin";
    }

    @GetMapping("/admin/user/{id}/edit")
    public String editUser(@PathVariable Long id, Model model) {
        User user = userService.findUserById(id);
        model.addAttribute("user", user);
        model.addAttribute("allRole", roleService.getAllRoles());
        return "edit_user";
    }

    @PostMapping("/admin/user/{id}/edit")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute User user,
                             @RequestParam("roles") List<Long> roleIds, Model model) {
        try {
            User existingUser = userService.findUserById(id);
            existingUser.setUsername(user.getUsername());
            existingUser.setEmail(user.getEmail());
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                existingUser.setPassword(user.getPassword());
            }
            List<Role> roles = roleService.getRolesById(roleIds);
            existingUser.setRoles(roles);
            userService.update(existingUser);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("allRole", roleService.getAllRoles());
        }
        return "redirect:/auth/admin";
    }


}