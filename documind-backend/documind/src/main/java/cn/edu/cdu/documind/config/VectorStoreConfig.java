package cn.edu.cdu.documind.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * 向量存储配置
 * 使用SimpleVectorStore（简单版，适合开发和小规模应用）
 * 
 * @author DocuMind Team
 */
@Configuration
public class VectorStoreConfig {

    @Value("${documind.vector.store.path:./data/vector-store.json}")
    private String vectorStorePath;

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        
        // 使用相对路径（相对于工作目录）
        File vectorFile = new File(vectorStorePath);
        
        if (vectorFile.exists()) {
            System.out.println("========== 向量存储加载诊断 ==========");
            System.out.println("加载向量数据从: " + vectorFile.getAbsolutePath());
            System.out.println("文件大小: " + vectorFile.length() + " bytes");
            
            vectorStore.load(vectorFile);
            
            // ⭐ 测试：手动检索验证是否真的加载了数据
            try {
                var testResults = vectorStore.similaritySearch(
                    SearchRequest.builder()
                        .query("测试")
                        .topK(20)
                        .similarityThreshold(0.0)  // 设为0确保能返回所有
                        .build()
                );
                System.out.println("向量数据加载验证：内存中有 " + testResults.size() + " 条向量");
                if (testResults.size() > 0) {
                    System.out.println("示例向量metadata: " + testResults.get(0).getMetadata());
                }
            } catch (Exception e) {
                System.out.println("向量加载验证失败: " + e.getMessage());
                e.printStackTrace();
            }
            System.out.println("=====================================");
        } else {
            System.out.println("向量文件不存在: " + vectorFile.getAbsolutePath() + "，将创建新的向量存储");
        }
        
        return vectorStore;
    }
}

