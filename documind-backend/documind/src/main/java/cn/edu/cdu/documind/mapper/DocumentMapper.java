package cn.edu.cdu.documind.mapper;

import cn.edu.cdu.documind.entity.Document;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文档Mapper
 * 
 * @author DocuMind Team
 */
@Mapper
public interface DocumentMapper extends BaseMapper<Document> {
}

