// package com.goconnect.cabservice;

// import java.io.File;
// import java.util.ArrayList;
// import java.util.List;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.stereotype.Controller; // <-- IMPORTANT: Add this import
// import org.springframework.util.ResourceUtils;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestParam;

// import com.fasterxml.jackson.core.type.TypeReference;
// import com.fasterxml.jackson.databind.ObjectMapper;

// @Controller
// public class AuthController {

//     @Autowired
//     private PasswordEncoder passwordEncoder;

//     @GetMapping("/login")
//     public String login() {
//         return "login";
//     }

//     @GetMapping("/signup")
//     public String signup() {
//         return "signup";
//     }

//     @PostMapping("/signup")
//     public String registerUser(@RequestParam String username, @RequestParam String password) {
//         String encodedPassword = passwordEncoder.encode(password);
        
//         ObjectMapper mapper = new ObjectMapper();
//         try {
//             // THE FIX IS HERE: Use ResourceUtils to find the file reliably
//             File userFile = ResourceUtils.getFile("classpath:users.json");

//             List<User> users = new ArrayList<>();
//             if (userFile.exists() && userFile.length() > 0) {
//                  users.addAll(mapper.readValue(userFile, new TypeReference<List<User>>() {}));
//             }
            
//             User newUser = new User(users.size() + 1, username, encodedPassword, "ROLE_USER");
//             users.add(newUser);
            
//             mapper.writerWithDefaultPrettyPrinter().writeValue(userFile, users);

//         } catch (Exception e) {
//             e.printStackTrace();
//             return "redirect:/signup?error"; 
//         }

//         return "redirect:/login?success";
//     }
// }














































































package com.goconnect.cabservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public String registerUser(@RequestParam String username, @RequestParam String password) {
        // Check if username already exists
        if (userService.existsByUsername(username)) {
            return "redirect:/signup?error=username_exists";
        }
        
        String encodedPassword = passwordEncoder.encode(password);
        
        try {
            // Create new user with database
            User newUser = userService.createUser(username, encodedPassword, "ROLE_USER", 0.0);
            userService.saveUser(newUser);
            
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/signup?error"; 
        }

        return "redirect:/login?success";
    }
}
