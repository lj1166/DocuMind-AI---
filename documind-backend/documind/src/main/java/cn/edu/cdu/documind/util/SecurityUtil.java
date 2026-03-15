package cn.edu.cdu.documind.util;

import cn.edu.cdu.documind.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Security工具类
 * 方便获取当前登录用户信息
 * 
 * @author DocuMind Team
 */
public class SecurityUtil {
    
    /**
     * 从Authentication中获取userId
     */
    public static Long getUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("未登录");
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUserId();
        }
        
        throw new RuntimeException("无法获取用户ID");
    }
    
    /**
     * 从SecurityContext中获取当前用户ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return getUserId(authentication);
    }
    
    /**
     * 从Authentication中获取username
     */
    public static String getUsername(Authentication authentication) {
        if (authentication == null) {
            throw new RuntimeException("未登录");
        }
        return authentication.getName();
    }
    
    /**
     * 从SecurityContext中获取当前用户名
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return getUsername(authentication);
    }
    
    /**
     * 从Authentication中获取CustomUserDetails
     */
    public static CustomUserDetails getUserDetails(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("未登录");
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return (CustomUserDetails) principal;
        }
        
        throw new RuntimeException("无法获取用户信息");
    }
}

