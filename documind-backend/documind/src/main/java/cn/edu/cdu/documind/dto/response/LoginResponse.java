package cn.edu.cdu.documind.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应 DTO
 * 
 * @author DocuMind Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    /**
     * JWT Token
     */
    private String accessToken;
    
    /**
     * Token 类型
     */
    private String tokenType;
    
    /**
     * Token 过期时间（毫秒）
     */
    private Long expiresIn;
    
    /**
     * 用户信息
     */
    private UserResponse user;
}

