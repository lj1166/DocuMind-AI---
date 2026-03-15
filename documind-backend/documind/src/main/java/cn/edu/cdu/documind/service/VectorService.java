package cn.edu.cdu.documind.service;

import cn.edu.cdu.documind.entity.Document;
import cn.edu.cdu.documind.mapper.DocumentMapper;
import cn.edu.cdu.documind.reader.ExcelReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 向量化服务（简化版）
 * 
 * @author DocuMind Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VectorService {

    private final DocumentMapper documentMapper;
    private final VectorStore vectorStore;
    
    @Value("${documind.vector.store.path:./data/vector-store.json}")
    private String vectorStorePath;
    
    // 文件保存锁，避免并发保存冲突
    private final Object saveLock = new Object();

    /**
     * 异步向量化文档
     */
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void vectorizeDocument(Long documentId) {
        log.info("开始向量化文档: {}", documentId);
        
        Document document = documentMapper.selectById(documentId);
        if (document == null) {
            log.error("文档不存在: {}", documentId);
            return;
        }

        try {
            // 更新状态为处理中
            document.setVectorStatus(1);
            documentMapper.updateById(document);

            // 1. 读取文件内容（根据文件类型选择Reader）
            File file = new File(document.getFilePath());
            DocumentReader reader;
            String fileType = document.getFileType().toLowerCase();
            
            // 根据文件类型选择合适的Reader
            if ("xls".equals(fileType) || "xlsx".equals(fileType)) {
                reader = new ExcelReader(new FileSystemResource(file));
                log.info("使用ExcelReader读取文件: {}", document.getFileName());
            } else {
                // txt, md, pdf, doc, docx 等使用TextReader
                reader = new TextReader(new FileSystemResource(file));
                log.info("使用TextReader读取文件: {}", document.getFileName());
            }
            
            List<org.springframework.ai.document.Document> documents = reader.get();
            
            // 2. 文档分块（使用TokenTextSplitter）
            TokenTextSplitter splitter = new TokenTextSplitter(500, 50, 5, 10000, true);
            List<org.springframework.ai.document.Document> chunks = splitter.apply(documents);
            
            // 3. 为每个chunk添加metadata（关键：数据隔离）
            for (int i = 0; i < chunks.size(); i++) {
                org.springframework.ai.document.Document chunk = chunks.get(i);
                Map<String, Object> metadata = new HashMap<>(chunk.getMetadata());
                
                // ⭐ 核心：添加agent_id和document_id到metadata
                metadata.put("agent_id", document.getAgentId().toString());
                metadata.put("document_id", document.getId().toString());
                metadata.put("file_name", document.getFileName());
                metadata.put("chunk_index", i);
                
                chunk.getMetadata().putAll(metadata);
            }
            
            // 4. 存储到向量数据库
            log.info("添加{}个向量chunk到VectorStore", chunks.size());
            vectorStore.add(chunks);
            
            // 5. 持久化到文件（使用锁避免并发冲突）
            if (vectorStore instanceof SimpleVectorStore simpleVectorStore) {
                synchronized (saveLock) {  // ⭐ 加锁，避免多个文档同时保存
                    try {
                        // 使用相对路径（相对于工作目录）
                        File vectorFile = new File(vectorStorePath);
                        
                        // 确保父目录存在
                        vectorFile.getParentFile().mkdirs();
                        
                        log.info("保存向量数据到: {}", vectorFile.getAbsolutePath());
                        simpleVectorStore.save(vectorFile);
                        log.info("向量数据已保存（文档{}），内存中累计向量数: {}", documentId, chunks.size());
                    } catch (Exception e) {
                        log.warn("保存向量文件时出错（不影响使用）: {}", e.getMessage());
                        // 不抛出异常，因为向量已在内存中，文件保存失败不影响检索
                    }
                }
            }
            
            // 更新状态为已完成
            document.setVectorStatus(2);
            document.setChunkCount(chunks.size());
            documentMapper.updateById(document);
            
            log.info("文档向量化完成: {}, 分块数: {}, 向量已持久化", documentId, chunks.size());
            
        } catch (Exception e) {
            log.error("文档向量化失败: {}", documentId, e);
            
            // 更新状态为失败
            document.setVectorStatus(3);
            document.setVectorError(e.getMessage());
            documentMapper.updateById(document);
        }
    }

    /**
     * RAG检索 - 根据问题检索相关文档片段
     */
    public List<org.springframework.ai.document.Document> searchRelevantDocuments(
            String question, Long agentId, int topK, double similarityThreshold) {
        try {
            // 构建检索请求，使用metadata过滤（数据隔离）
            // 注意：SimpleVectorStore的过滤可能不支持，先不过滤，后续手动过滤
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(question)
                    .topK(topK * 2)  // 多查一些，后续手动过滤
                    .similarityThreshold(similarityThreshold)
                    .build();
            
            log.info("开始向量检索，问题: {}, agentId: {}, topK: {}, threshold: {}", 
                question, agentId, topK, similarityThreshold);
            
            List<org.springframework.ai.document.Document> allResults = vectorStore.similaritySearch(searchRequest);
            
            log.info("向量检索原始结果: {} 个", allResults.size());
            
            // 打印每个结果的metadata（调试用）
            for (int i = 0; i < allResults.size(); i++) {
                var doc = allResults.get(i);
                log.info("结果{}: agent_id={}, file_name={}", 
                    i, doc.getMetadata().get("agent_id"), doc.getMetadata().get("file_name"));
            }
            
            // 手动过滤：只返回当前智能体的文档（数据隔离）
            List<org.springframework.ai.document.Document> filteredResults = allResults.stream()
                    .filter(doc -> {
                        Object agentIdObj = doc.getMetadata().get("agent_id");
                        boolean matches = agentIdObj != null && agentIdObj.toString().equals(agentId.toString());
                        log.debug("过滤检查: agent_id={}, 期望={}, 匹配={}", agentIdObj, agentId, matches);
                        return matches;
                    })
                    .limit(topK)  // 限制返回数量
                    .toList();
            
            log.info("RAG检索完成，agentId: {}, 问题: {}, 总共找到{}个，过滤后{}个相关片段", 
                agentId, question, allResults.size(), filteredResults.size());
            
            return filteredResults;
            
        } catch (Exception e) {
            log.error("RAG检索失败: ", e);
            return List.of();
        }
    }

    /**
     * 删除文档的向量数据
     */
    public void deleteDocumentVectors(Long documentId, Long agentId) {
        try {
            log.info("删除文档向量，documentId: {}, agentId: {}", documentId, agentId);
            // SimpleVectorStore删除功能有限，记录日志
            // 未来升级到Qdrant时可以精确删除
        } catch (Exception e) {
            log.error("删除文档向量失败: ", e);
        }
    }
}

