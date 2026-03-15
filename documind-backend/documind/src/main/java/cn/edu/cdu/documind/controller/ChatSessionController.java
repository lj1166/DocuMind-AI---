package cn.edu.cdu.documind.controller;

import cn.edu.cdu.documind.common.Result;
import cn.edu.cdu.documind.dto.request.ChatSessionCreateRequest;
import cn.edu.cdu.documind.dto.response.ChatSessionResponse;
import cn.edu.cdu.documind.service.ChatSessionService;
import cn.edu.cdu.documind.util.SecurityUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 会话管理Controller
 * 
 * @author DocuMind Team
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "会话管理", description = "会话的CRUD操作")
public class ChatSessionController {

    private final ChatSessionService sessionService;

    @PostMapping("/agents/{agentId}/sessions")
    @Operation(summary = "创建会话", description = "为指定智能体创建新会话")
    public Result<ChatSessionResponse> createSession(
            @Parameter(description = "智能体ID") @PathVariable Long agentId,
            @RequestBody ChatSessionCreateRequest request,
            Authentication authentication) {
        try {
            Long userId = SecurityUtil.getUserId(authentication);
            request.setAgentId(agentId);
            ChatSessionResponse response = sessionService.createSession(request, userId);
            return Result.success("会话创建成功", response);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (SecurityException e) {
            return Result.error(403, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "创建会话失败: " + e.getMessage());
        }
    }

    @GetMapping("/agents/{agentId}/sessions")
    @Operation(summary = "获取会话列表", description = "获取智能体的会话列表（分页）")
    public Result<Page<ChatSessionResponse>> getSessions(
            @Parameter(description = "智能体ID") @PathVariable Long agentId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            Authentication authentication) {
        try {
            Long userId = SecurityUtil.getUserId(authentication);
            Page<ChatSessionResponse> result = sessionService.getSessionsByAgent(agentId, userId, page, pageSize, keyword);
            return Result.success(result);
        } catch (SecurityException e) {
            return Result.error(403, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "获取会话列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/sessions/{id}")
    @Operation(summary = "获取会话详情", description = "获取单个会话的详细信息")
    public Result<ChatSessionResponse> getSession(
            @Parameter(description = "会话ID") @PathVariable Long id,
            Authentication authentication) {
        try {
            Long userId = SecurityUtil.getUserId(authentication);
            ChatSessionResponse response = sessionService.getSessionById(id, userId);
            return Result.success(response);
        } catch (IllegalArgumentException e) {
            return Result.error(404, e.getMessage());
        } catch (SecurityException e) {
            return Result.error(403, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "获取会话详情失败: " + e.getMessage());
        }
    }

    @GetMapping("/sessions/by-session-id/{sessionId}")
    @Operation(summary = "通过sessionId获取会话", description = "通过sessionId获取会话详情")
    public Result<ChatSessionResponse> getSessionBySessionId(
            @Parameter(description = "会话SessionID") @PathVariable String sessionId,
            Authentication authentication) {
        try {
            Long userId = SecurityUtil.getUserId(authentication);
            ChatSessionResponse response = sessionService.getSessionBySessionId(sessionId, userId);
            return Result.success(response);
        } catch (IllegalArgumentException e) {
            return Result.error(404, e.getMessage());
        } catch (SecurityException e) {
            return Result.error(403, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "获取会话详情失败: " + e.getMessage());
        }
    }

    @PutMapping("/sessions/{id}")
    @Operation(summary = "更新会话标题", description = "修改会话的标题")
    public Result<ChatSessionResponse> updateSession(
            @Parameter(description = "会话ID") @PathVariable Long id,
            @Parameter(description = "新标题") @RequestParam String title,
            Authentication authentication) {
        try {
            Long userId = SecurityUtil.getUserId(authentication);
            ChatSessionResponse response = sessionService.updateSessionTitle(id, title, userId);
            return Result.success("会话更新成功", response);
        } catch (IllegalArgumentException e) {
            return Result.error(404, e.getMessage());
        } catch (SecurityException e) {
            return Result.error(403, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "更新会话失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/sessions/{id}")
    @Operation(summary = "删除会话", description = "删除指定会话及其对话历史")
    public Result<Void> deleteSession(
            @Parameter(description = "会话ID") @PathVariable Long id,
            Authentication authentication) {
        try {
            Long userId = SecurityUtil.getUserId(authentication);
            sessionService.deleteSession(id, userId);
            return Result.success("会话删除成功", null);
        } catch (IllegalArgumentException e) {
            return Result.error(404, e.getMessage());
        } catch (SecurityException e) {
            return Result.error(403, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "删除会话失败: " + e.getMessage());
        }
    }
}

