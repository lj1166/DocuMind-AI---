package cn.edu.cdu.documind.service;

import cn.edu.cdu.documind.dto.response.DocumentResponse;
import cn.edu.cdu.documind.entity.Document;
import cn.edu.cdu.documind.mapper.AgentMapper;
import cn.edu.cdu.documind.mapper.DocumentMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 文档服务
 * 
 * @author DocuMind Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentMapper documentMapper;
    private final AgentMapper agentMapper;
    private final VectorService vectorService;

    @Value("${documind.upload.path:./data/uploads}")
    private String uploadBasePath;

    @Value("${documind.upload.max-size:52428800}") // 50MB
    private Long maxFileSize;

    /**
     * 上传文档
     */
    @Transactional(rollbackFor = Exception.class)
    public DocumentResponse uploadDocument(Long agentId, Long userId, MultipartFile file) throws IOException {
        // 验证文件
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("文件大小超过限制（最大50MB）");
        }

        // 验证智能体是否存在且属于该用户
        var agent = agentMapper.selectById(agentId);
        if (agent == null) {
            throw new IllegalArgumentException("智能体不存在");
        }
        if (!agent.getUserId().equals(userId)) {
            throw new SecurityException("无权操作该智能体");
        }

        // 获取文件信息
        String originalFilename = file.getOriginalFilename();
        String fileType = getFileType(originalFilename);
        
        // 验证文件类型
        if (!isValidFileType(fileType)) {
            throw new IllegalArgumentException("不支持的文件类型，仅支持：pdf, doc, docx, txt, md, xls, xlsx");
        }

        // 生成唯一文件名
        String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFilename;
        
        // 创建目录（按智能体ID分目录）
        // 使用绝对路径，避免相对路径解析问题
        File baseDir = new File(uploadBasePath);
        if (!baseDir.isAbsolute()) {
            // 如果是相对路径，转换为项目根目录的绝对路径
            baseDir = new File(System.getProperty("user.dir"), uploadBasePath);
        }
        Path agentUploadPath = Paths.get(baseDir.getAbsolutePath(), agentId.toString());
        
        try {
            // 确保目录存在
            File uploadDir = agentUploadPath.toFile();
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                if (!created) {
                    throw new IOException("无法创建上传目录: " + agentUploadPath);
                }log.info("创建上传目录: {}", agentUploadPath);
            }
        } catch (Exception e) {
            log.error("创建目录失败: {}", agentUploadPath, e);
            throw new RuntimeException("创建上传目录失败: " + e.getMessage());
        }
        
        // 保存文件
        Path filePath = agentUploadPath.resolve(uniqueFileName);
        log.info("准备保存文件到: {}", filePath);
        
        file.transferTo(filePath.toFile());

        // 创建文档记录
        Document document = new Document();
        document.setAgentId(agentId);
        document.setFileName(originalFilename);
        document.setFilePath(filePath.toString());
        document.setFileType(fileType);
        document.setFileSize(file.getSize());
        document.setChunkCount(0);
        document.setVectorStatus(0); // 待处理
        document.setCreatedAt(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());

        documentMapper.insert(document);

        // 更新智能体文档数量统计
        agent.setDocumentCount(agent.getDocumentCount() + 1);
        agentMapper.updateById(agent);

        // 创建返回对象
        DocumentResponse response = convertToResponse(document);
        
        // 注意：向量化任务需要在事务提交后执行
        // 所以使用TransactionSynchronization确保事务提交后再启动
        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
            new org.springframework.transaction.support.TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    log.info("事务已提交，启动向量化任务，documentId: {}", document.getId());
                    try {
                        vectorService.vectorizeDocument(document.getId());
                        log.info("向量化任务已提交");
                    } catch (Exception e) {
                        log.error("启动向量化任务失败: ", e);
                    }
                }
            }
        );

        return response;
    }

    /**
     * 获取智能体的文档列表（分页）
     */
    public Page<DocumentResponse> getDocumentsByAgent(Long agentId, Long userId, int page, int pageSize, String keyword) {
        // 验证智能体权限
        var agent = agentMapper.selectById(agentId);
        if (agent == null || !agent.getUserId().equals(userId)) {
            throw new SecurityException("无权访问该智能体的文档");
        }

        LambdaQueryWrapper<Document> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Document::getAgentId, agentId);
        
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like(Document::getFileName, keyword);
        }
        
        queryWrapper.orderByDesc(Document::getCreatedAt);

        Page<Document> documentPage = documentMapper.selectPage(new Page<>(page, pageSize), queryWrapper);
        
        Page<DocumentResponse> responsePage = new Page<>(documentPage.getCurrent(), documentPage.getSize(), documentPage.getTotal());
        responsePage.setRecords(
            documentPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList())
        );
        
        return responsePage;
    }

    /**
     * 获取文档详情
     */
    public DocumentResponse getDocumentById(Long id, Long userId) {
        Document document = documentMapper.selectById(id);
        if (document == null) {
            throw new IllegalArgumentException("文档不存在");
        }

        // 验证权限
        var agent = agentMapper.selectById(document.getAgentId());
        if (agent == null || !agent.getUserId().equals(userId)) {
            throw new SecurityException("无权访问该文档");
        }

        return convertToResponse(document);
    }

    /**
     * 删除文档
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteDocument(Long id, Long userId) {
        Document document = documentMapper.selectById(id);
        if (document == null) {
            throw new IllegalArgumentException("文档不存在");
        }

        // 验证权限
        var agent = agentMapper.selectById(document.getAgentId());
        if (agent == null || !agent.getUserId().equals(userId)) {
            throw new SecurityException("无权删除该文档");
        }

        // 删除文件
        try {
            File file = new File(document.getFilePath());
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            // 记录日志但不抛出异常
            System.err.println("删除文件失败: " + e.getMessage());
        }

        // TODO: 删除向量数据

        // 删除数据库记录
        documentMapper.deleteById(id);

        // 更新智能体文档数量统计
        int newCount = Math.max(0, agent.getDocumentCount() - 1);
        agent.setDocumentCount(newCount);
        agentMapper.updateById(agent);
    }

    /**
     * 查询文档向量化状态
     */
    public DocumentResponse getDocumentStatus(Long id, Long userId) {
        return getDocumentById(id, userId);
    }

    /**
     * 获取文档文件（用于下载）
     */
    public File getDocumentFile(Long id, Long userId) {
        Document document = documentMapper.selectById(id);
        if (document == null) {
            throw new IllegalArgumentException("文档不存在");
        }

        // 验证权限
        var agent = agentMapper.selectById(document.getAgentId());
        if (agent == null || !agent.getUserId().equals(userId)) {
            throw new SecurityException("无权访问该文档");
        }

        File file = new File(document.getFilePath());
        if (!file.exists()) {
            throw new IllegalArgumentException("文件不存在");
        }

        return file;
    }

    /**
     * 预览文档
     */
    public Map<String, Object> previewDocument(Long id, Long userId) throws IOException {
        Document document = documentMapper.selectById(id);
        if (document == null) {
            throw new IllegalArgumentException("文档不存在");
        }

        // 验证权限
        var agent = agentMapper.selectById(document.getAgentId());
        if (agent == null || !agent.getUserId().equals(userId)) {
            throw new SecurityException("无权访问该文档");
        }

        File file = new File(document.getFilePath());
        if (!file.exists()) {
            throw new IllegalArgumentException("文件不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("fileName", document.getFileName());
        result.put("fileType", document.getFileType());
        result.put("fileSize", document.getFileSize());

        // 根据文件类型提供不同的预览
        String fileType = document.getFileType().toLowerCase();
        
        if ("txt".equals(fileType) || "md".equals(fileType)) {
            // 文本文件：直接读取内容
            String content = Files.readString(file.toPath(), java.nio.charset.StandardCharsets.UTF_8);
            result.put("content", content);
            result.put("previewType", "text");
        } else if ("pdf".equals(fileType)) {
            // PDF文件：返回文件URL，由前端使用PDF.js预览
            result.put("previewType", "pdf");
            result.put("downloadUrl", "/api/documents/" + id + "/download");
            result.put("message", "PDF文件，请使用下载功能或前端PDF查看器");
        } else {
            // 其他文件：只支持下载
            result.put("previewType", "unsupported");
            result.put("downloadUrl", "/api/documents/" + id + "/download");
            result.put("message", "该文件类型不支持在线预览，请下载查看");
        }

        return result;
    }

    /**
     * 重新向量化文档
     */
    @Transactional(rollbackFor = Exception.class)
    public DocumentResponse reVectorizeDocument(Long id, Long userId) {
        Document document = documentMapper.selectById(id);
        if (document == null) {
            throw new IllegalArgumentException("文档不存在");
        }

        // 验证权限
        var agent = agentMapper.selectById(document.getAgentId());
        if (agent == null || !agent.getUserId().equals(userId)) {
            throw new SecurityException("无权操作该文档");
        }

        // 重置向量化状态为待处理
        document.setVectorStatus(0);
        document.setVectorError(null);
        document.setChunkCount(0);
        documentMapper.updateById(document);

        // 触发异步向量化
        vectorService.vectorizeDocument(id);

        log.info("文档重新向量化已触发: documentId={}", id);

        return convertToResponse(document);
    }

    /**
     * 转换为响应DTO
     */
    private DocumentResponse convertToResponse(Document document) {
        return DocumentResponse.builder()
            .id(document.getId())
            .agentId(document.getAgentId())
            .fileName(document.getFileName())
            .fileType(document.getFileType())
            .fileSize(document.getFileSize())
            .chunkCount(document.getChunkCount())
            .vectorStatus(document.getVectorStatus())
            .vectorError(document.getVectorError())
            .createdAt(document.getCreatedAt())
            .updatedAt(document.getUpdatedAt())
            .build();
    }

    /**
     * 获取文件类型
     */
    private String getFileType(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "unknown";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 验证文件类型
     */
    private boolean isValidFileType(String fileType) {
        List<String> validTypes = List.of("pdf", "doc", "docx", "txt", "md", "xls", "xlsx");
        return validTypes.contains(fileType.toLowerCase());
    }
}

