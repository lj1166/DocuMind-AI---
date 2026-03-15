package cn.edu.cdu.documind.controller;

import cn.edu.cdu.documind.common.Result;
import cn.edu.cdu.documind.dto.request.PromptTemplateCreateRequest;
import cn.edu.cdu.documind.dto.request.PromptTemplateUpdateRequest;
import cn.edu.cdu.documind.dto.response.PromptTemplateResponse;
import cn.edu.cdu.documind.service.PromptTemplateService;
import cn.edu.cdu.documind.util.SecurityUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 提示词模板管理Controller
 * 
 * @author DocuMind Team
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "提示词模板管理", description = "提示词模板的CRUD操作")
public class PromptTemplateController {

    private final PromptTemplateService templateService;

    @PostMapping("/agents/{agentId}/templates")
    @Operation(summary = "创建提示词模板", description = "为指定智能体创建提示词模板")
    public Result<PromptTemplateResponse> createTemplate(
            @Parameter(description = "智能体ID") @PathVariable Long agentId,
            @RequestBody PromptTemplateCreateRequest request,
            Authentication authentication) {
        try {
            Long userId = SecurityUtil.getUserId(authentication);
            request.setAgentId(agentId);
            PromptTemplateResponse response = templateService.createTemplate(request, userId);
            return Result.success("模板创建成功", response);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (SecurityException e) {
            return Result.error(403, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "创建模板失败: " + e.getMessage());
        }
    }

    @GetMapping("/agents/{agentId}/templates")
    @Operation(summary = "获取模板列表", description = "获取智能体的提示词模板列表（分页）")
    public Result<Page<PromptTemplateResponse>> getTemplates(
            @Parameter(description = "智能体ID") @PathVariable Long agentId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "排序方式") @RequestParam(defaultValue = "priority") String sortBy,
            Authentication authentication) {
        try {
            Long userId = SecurityUtil.getUserId(authentication);
            Page<PromptTemplateResponse> result = templateService.getTemplatesByAgent(agentId, userId, page, pageSize, sortBy);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(500, "获取模板列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/templates/{id}")
    @Operation(summary = "获取模板详情", description = "获取单个提示词模板的详细信息")
    public Result<PromptTemplateResponse> getTemplate(
            @Parameter(description = "模板ID") @PathVariable Long id,
            Authentication authentication) {
        try {
            Long userId = SecurityUtil.getUserId(authentication);
            PromptTemplateResponse response = templateService.getTemplateById(id, userId);
            return Result.success(response);
        } catch (Exception e) {
            return Result.error(500, "获取模板详情失败: " + e.getMessage());
        }
    }

    @PutMapping("/templates/{id}")
    @Operation(summary = "更新模板", description = "更新提示词模板信息")
    public Result<PromptTemplateResponse> updateTemplate(
            @Parameter(description = "模板ID") @PathVariable Long id,
            @RequestBody PromptTemplateUpdateRequest request,
            Authentication authentication) {
        try {
            Long userId = SecurityUtil.getUserId(authentication);
            PromptTemplateResponse response = templateService.updateTemplate(id, request, userId);
            return Result.success("模板更新成功", response);
        } catch (Exception e) {
            return Result.error(500, "更新模板失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/templates/{id}")
    @Operation(summary = "删除模板", description = "删除指定的提示词模板")
    public Result<Void> deleteTemplate(
            @Parameter(description = "模板ID") @PathVariable Long id,
            Authentication authentication) {
        try {
            Long userId = SecurityUtil.getUserId(authentication);
            templateService.deleteTemplate(id, userId);
            return Result.success("模板删除成功", null);
        } catch (IllegalArgumentException e) {
            return Result.error(404, e.getMessage());
        } catch (SecurityException e) {
            return Result.error(403, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "删除模板失败: " + e.getMessage());
        }
    }

    @PostMapping("/templates/{id}/copy")
    @Operation(summary = "复制模板", description = "复制一个现有的提示词模板")
    public Result<PromptTemplateResponse> copyTemplate(
            @Parameter(description = "模板ID") @PathVariable Long id,
            Authentication authentication) {
        try {
            Long userId = SecurityUtil.getUserId(authentication);
            PromptTemplateResponse response = templateService.copyTemplate(id, userId);
            return Result.success("模板复制成功", response);
        } catch (Exception e) {
            return Result.error(500, "复制模板失败: " + e.getMessage());
        }
    }
}

