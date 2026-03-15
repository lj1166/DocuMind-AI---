package cn.edu.cdu.documind.controller;

import cn.edu.cdu.documind.common.Result;
import cn.edu.cdu.documind.dto.response.DocumentResponse;
import cn.edu.cdu.documind.service.DocumentService;
import cn.edu.cdu.documind.util.SecurityUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 文档管理Controller
 * 
 * @author DocuMind Team
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "文档管理", description = "文档上传、查询、删除等操作")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/agents/{agentId}/documents")
    @Operation(summary = "上传文档", description = "上传文档到指定智能体")
    public Result<DocumentResponse> uploadDocument(
            @Parameter(description = "智能体ID") @PathVariable Long agentId,
            @Parameter(description = "文件") @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            Long userId = SecurityUtil.getUserId(authentication);
            DocumentResponse response = documentService.uploadDocument(agentId, userId, file);
            return Result.success("文档上传成功", response);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (SecurityException e) {
            return Result.error(403, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "文档上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/agents/{agentId}/documents")
    @Operation(summary = "获取文档列表", description = "获取智能体的文档列表（分页）")
    public Result<Page<DocumentResponse>> getDocuments(
            @Parameter(description = "智能体ID") @PathVariable Long agentId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            Authentication authentication) {
        try {
            Long userId = SecurityUtil.getUserId(authentication);
            Page<DocumentResponse> result = documentService.getDocumentsByAgent(agentId, userId, page, pageSize, keyword);
            return Result.success(result);
        } catch (SecurityException e) {
            return Result.error(403, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "获取文档列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/documents/{id}")
    @Operation(summary = "获取文档详情", description = "获取单个文档的详细信息")
    public Result<DocumentResponse> getDocument(
            @Parameter(description = "文档ID") @PathVariable Long id,
            Authentication authentication) {
        try {
            Long userId = SecurityUtil.getUserId(authentication);
            DocumentResponse response = documentService.getDocumentById(id, userId);
            return Result.success(response);
        } catch (Exception e) {
            return Result.error(500, "获取文档详情失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/documents/{id}")
    @Operation(summary = "删除文档", description = "删除指定文档（包括文件和向量数据）")
    public Result<Void> deleteDocument(
            @Parameter(description = "文档ID") @PathVariable Long id,
            Authentication authentication) {
        try {
            Long userId = SecurityUtil.getUserId(authentication);
            documentService.deleteDocument(id, userId);
            return Result.success("文档删除成功", null);
        } catch (IllegalArgumentException e) {
            return Result.error(404, e.getMessage());
        } catch (SecurityException e) {
            return Result.error(403, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "删除文档失败: " + e.getMessage());
        }
    }

    @GetMapping("/documents/{id}/status")
    @Operation(summary = "查询文档向量化状态", description = "查询文档的向量化处理状态")
    public Result<DocumentResponse> getDocumentStatus(
            @Parameter(description = "文档ID") @PathVariable Long id,
            Authentication authentication) {
        try {
            Long userId = SecurityUtil.getUserId(authentication);
            DocumentResponse response = documentService.getDocumentStatus(id, userId);
            return Result.success(response);
        } catch (Exception e) {
            return Result.error(500, "查询文档状态失败: " + e.getMessage());
        }
    }

    @GetMapping("/documents/{id}/download")
    @Operation(summary = "下载文档", description = "下载指定的文档文件")
    public void downloadDocument(
            @Parameter(description = "文档ID") @PathVariable Long id,
            Authentication authentication,
            jakarta.servlet.http.HttpServletResponse response) {
        try {
            Long userId = SecurityUtil.getUserId(authentication);
            DocumentResponse doc = documentService.getDocumentById(id, userId);
            java.io.File file = documentService.getDocumentFile(id, userId);

            // 设置响应头
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", 
                "attachment; filename=" + java.net.URLEncoder.encode(doc.getFileName(), "UTF-8"));
            response.setContentLengthLong(file.length());

            // 输出文件
            try (java.io.FileInputStream fis = new java.io.FileInputStream(file);
                 java.io.OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }
        } catch (Exception e) {
            throw new RuntimeException("文档下载失败: " + e.getMessage());
        }
    }

    @GetMapping("/documents/{id}/preview")
    @Operation(summary = "预览文档", description = "获取文档内容用于预览")
    public Result<Map<String, Object>> previewDocument(
            @Parameter(description = "文档ID") @PathVariable Long id,
            Authentication authentication) {
        try {
            Long userId = SecurityUtil.getUserId(authentication);
            Map<String, Object> preview = documentService.previewDocument(id, userId);
            return Result.success(preview);
        } catch (SecurityException e) {
            return Result.error(403, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "文档预览失败: " + e.getMessage());
        }
    }

    @PostMapping("/documents/{id}/re-vectorize")
    @Operation(summary = "重新向量化", description = "重新对文档进行向量化处理")
    public Result<DocumentResponse> reVectorizeDocument(
            @Parameter(description = "文档ID") @PathVariable Long id,
            Authentication authentication) {
        try {
            Long userId = SecurityUtil.getUserId(authentication);
            DocumentResponse response = documentService.reVectorizeDocument(id, userId);
            return Result.success(response);
        } catch (SecurityException e) {
            return Result.error(403, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "重新向量化失败: " + e.getMessage());
        }
    }
}

