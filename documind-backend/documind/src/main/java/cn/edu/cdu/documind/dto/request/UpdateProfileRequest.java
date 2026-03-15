package cn.edu.cdu.documind.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.Data;

/**
 * 更新个人信息请求
 * 
 * @author DocuMind Team
 */
@Data
@Schema(description = "更新个人信息请求")
public class UpdateProfileRequest {

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "邮箱")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "头像URL")
    private String avatar;
}

