package cn.edu.cdu.documind.controller;

import cn.edu.cdu.documind.common.Result;
import cn.edu.cdu.documind.dto.request.ChatRequest;
import cn.edu.cdu.documind.entity.ChatHistory;
import cn.edu.cdu.documind.service.ChatService;
import cn.edu.cdu.documind.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 聊天Controller
 * 
 * @author DocuMind Team
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "聊天功能", description = "发送消息和获取对话历史")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/send")
    @Operation(summary = "发送消息（同步）", description = "发送消息给智能体并获取完整回复")
    public Result<String> sendMessage(
            @Valid @RequestBody ChatRequest request,
            Authentication authentication) {
        try {
            Long userId = SecurityUtil.getUserId(authentication);
            String reply = chatService.chat(
                Long.parseLong(request.getAgentId()),
                request.getSessionId(),
                request.getMessage(),
                userId
            );
            return Result.success("发送成功", reply);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (SecurityException e) {
            return Result.error(403, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "发送消息失败: " + e.getMessage());
        }
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "发送消息（流式）", description = "发送消息并以SSE方式实时返回AI回复")
    public Flux<String> sendMessageStream(
            @Valid @RequestBody ChatRequest request,
            Authentication authentication) {
        try {
            Long userId = SecurityUtil.getUserId(authentication);
            return chatService.chatStream(
                Long.parseLong(request.getAgentId()),
                request.getSessionId(),
                request.getMessage(),
                userId
            );
        } catch (Exception e) {
            return Flux.error(e);
        }
    }

    @GetMapping("/history/{sessionId}")
    @Operation(summary = "获取对话历史", description = "获取指定会话的对话历史")
    public Result<List<ChatHistory>> getHistory(
            @PathVariable String sessionId,
            Authentication authentication) {
        try {
            Long userId = SecurityUtil.getUserId(authentication);
            List<ChatHistory> history = chatService.getHistory(sessionId, userId);
            return Result.success(history);
        } catch (Exception e) {
            return Result.error(500, "获取对话历史失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/history/{messageId}")
    @Operation(summary = "删除单条消息", description = "删除指定的对话消息")
    public Result<Void> deleteMessage(
            @PathVariable Long messageId,
            Authentication authentication) {
        try {
            Long userId = SecurityUtil.getUserId(authentication);
            chatService.deleteMessage(messageId, userId);
            return Result.success(null);
        } catch (SecurityException e) {
            return Result.error(403, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "删除消息失败: " + e.getMessage());
        }
    }

    @PostMapping("/regenerate")
    @Operation(summary = "重新生成回答", description = "删除最后一条AI回复并重新生成")
    public Flux<String> regenerate(
            @Valid @RequestBody ChatRequest request,
            Authentication authentication) {
        try {
            Long userId = SecurityUtil.getUserId(authentication);
            return chatService.regenerateResponse(
                Long.parseLong(request.getAgentId()),
                request.getSessionId(),
                userId
            );
        } catch (Exception e) {
            return Flux.error(e);
        }
    }
}

