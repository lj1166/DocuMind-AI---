package cn.edu.cdu.documind.controller;

import cn.edu.cdu.documind.common.Result;
import cn.edu.cdu.documind.dto.request.LoginRequest;
import cn.edu.cdu.documind.dto.request.RegisterRequest;
import cn.edu.cdu.documind.dto.request.UpdateProfileRequest;
import cn.edu.cdu.documind.dto.request.UpdatePasswordRequest;
import cn.edu.cdu.documind.dto.response.LoginResponse;
import cn.edu.cdu.documind.dto.response.UserResponse;
import cn.edu.cdu.documind.service.AuthService;
import cn.edu.cdu.documind.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 
 * @author DocuMind Team
 */
@Tag(name = "1. 用户认证", description = "用户注册、登录、登出等认证相关接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    
    /**
     * 用户注册
     */
    @Operation(summary = "用户注册", description = "注册新用户账号")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "注册成功"),
            @ApiResponse(responseCode = "500", description = "注册失败（用户名已存在、邮箱已被使用等）")
    })
    @PostMapping("/register")
    public Result<UserResponse> register(
            @Parameter(description = "注册信息", required = true) 
            @Valid @RequestBody RegisterRequest request) {
        try {
            UserResponse user = authService.register(request);
            return Result.success("注册成功", user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "用户登录获取JWT Token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "登录成功"),
            @ApiResponse(responseCode = "500", description = "登录失败（用户名或密码错误、账号已禁用等）")
    })
    @PostMapping("/login")
    public Result<LoginResponse> login(
            @Parameter(description = "登录信息", required = true)
            @Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return Result.success("登录成功", response);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "获取已登录用户的详细信息（需要JWT Token）")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "401", description = "未登录或Token无效"),
            @ApiResponse(responseCode = "500", description = "获取失败")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/profile")
    public Result<UserResponse> getProfile() {
        try {
            // 从 Security Context 中获取当前用户名
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // 查询用户信息
            UserResponse user = authService.getUserByUsername(username);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 用户登出（前端清除Token即可，后端无需处理）
     */
    @Operation(summary = "用户登出", description = "用户登出（前端清除Token即可）")
    @ApiResponse(responseCode = "200", description = "登出成功")
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success("登出成功", null);
    }

    /**
     * 更新个人信息
     */
    @Operation(summary = "更新个人信息", description = "更新用户的昵称、邮箱、头像")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/profile")
    public Result<UserResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            UserResponse user = authService.updateProfile(username, request);
            return Result.success("更新成功", user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @Operation(summary = "修改密码", description = "修改当前用户的登录密码")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/password")
    public Result<Void> updatePassword(
            @Valid @RequestBody UpdatePasswordRequest request,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            authService.updatePassword(username, request);
            return Result.success("密码修改成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

