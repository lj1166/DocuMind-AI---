package cn.edu.cdu.documind.reader;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel文件读取器
 * 支持 .xls 和 .xlsx 格式
 * 将Excel表格转换为Markdown格式的文本，便于向量化和检索
 * 
 * @author DocuMind Team
 */
public class ExcelReader implements DocumentReader {

    private final Resource resource;
    private final boolean preserveTableFormat; // 是否保留表格格式

    /**
     * 构造函数
     * @param resource Excel文件资源
     */
    public ExcelReader(Resource resource) {
        this(resource, true);
    }

    /**
     * 构造函数
     * @param resource Excel文件资源
     * @param preserveTableFormat 是否保留Markdown表格格式（推荐true，利于检索和显示）
     */
    public ExcelReader(Resource resource, boolean preserveTableFormat) {
        this.resource = resource;
        this.preserveTableFormat = preserveTableFormat;
    }

    @Override
    public List<Document> get() {
        List<Document> documents = new ArrayList<>();
        
        try (InputStream inputStream = resource.getInputStream()) {
            Workbook workbook = createWorkbook(inputStream);
            
            // 遍历所有工作表
            int numberOfSheets = workbook.getNumberOfSheets();
            for (int i = 0; i < numberOfSheets; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String sheetContent = convertSheetToText(sheet);
                
                if (sheetContent != null && !sheetContent.trim().isEmpty()) {
                    // 创建Document对象
                    Document doc = new Document(sheetContent);
                    // 添加工作表名称到metadata
                    doc.getMetadata().put("sheet_name", sheet.getSheetName());
                    doc.getMetadata().put("sheet_index", i);
                    documents.add(doc);
                }
            }
            
            workbook.close();
            
        } catch (IOException e) {
            throw new RuntimeException("读取Excel文件失败: " + e.getMessage(), e);
        }
        
        return documents;
    }

    /**
     * 根据文件扩展名创建对应的Workbook
     */
    private Workbook createWorkbook(InputStream inputStream) throws IOException {
        String filename = resource.getFilename();
        if (filename == null) {
            throw new IOException("无法获取文件名");
        }
        
        // 根据文件扩展名选择对应的Workbook实现
        if (filename.toLowerCase().endsWith(".xlsx")) {
            return new XSSFWorkbook(inputStream); // Excel 2007+
        } else if (filename.toLowerCase().endsWith(".xls")) {
            return new HSSFWorkbook(inputStream); // Excel 97-2003
        } else {
            throw new IOException("不支持的Excel文件格式: " + filename);
        }
    }

    /**
     * 将工作表转换为文本
     */
    private String convertSheetToText(Sheet sheet) {
        if (sheet.getPhysicalNumberOfRows() == 0) {
            return "";
        }
        
        if (preserveTableFormat) {
            return convertSheetToMarkdownTable(sheet);
        } else {
            return convertSheetToPlainText(sheet);
        }
    }

    /**
     * 转换为Markdown表格格式（推荐）
     * 优点：保留表格结构，AI回复时可以直接渲染
     */
    private String convertSheetToMarkdownTable(Sheet sheet) {
        StringBuilder content = new StringBuilder();
        
        // 添加工作表标题
        content.append("## ").append(sheet.getSheetName()).append("\n\n");
        
        List<Row> rows = new ArrayList<>();
        for (Row row : sheet) {
            if (row != null && !isEmptyRow(row)) {
                rows.add(row);
            }
        }
        
        if (rows.isEmpty()) {
            return "";
        }
        
        // 获取最大列数
        int maxCols = 0;
        for (Row row : rows) {
            maxCols = Math.max(maxCols, row.getLastCellNum());
        }
        
        // 第一行作为表头
        Row headerRow = rows.get(0);
        content.append("|");
        for (int col = 0; col < maxCols; col++) {
            Cell cell = headerRow.getCell(col);
            String cellValue = getCellValue(cell);
            content.append(" ").append(cellValue.trim()).append(" |");
        }
        content.append("\n");
        
        // 添加分隔行
        content.append("|");
        for (int col = 0; col < maxCols; col++) {
            content.append("---------|");
        }
        content.append("\n");
        
        // 添加数据行
        for (int i = 1; i < rows.size(); i++) {
            Row row = rows.get(i);
            content.append("|");
            for (int col = 0; col < maxCols; col++) {
                Cell cell = row.getCell(col);
                String cellValue = getCellValue(cell);
                content.append(" ").append(cellValue.trim()).append(" |");
            }
            content.append("\n");
        }
        
        content.append("\n");
        return content.toString();
    }

    /**
     * 转换为纯文本格式（每行用逗号分隔）
     */
    private String convertSheetToPlainText(Sheet sheet) {
        StringBuilder content = new StringBuilder();
        
        // 添加工作表名称
        content.append("工作表: ").append(sheet.getSheetName()).append("\n\n");
        
        // 获取第一行作为列名
        Row headerRow = sheet.getRow(sheet.getFirstRowNum());
        List<String> headers = new ArrayList<>();
        if (headerRow != null) {
            for (Cell cell : headerRow) {
                headers.add(getCellValue(cell));
            }
        }
        
        // 遍历所有数据行
        for (int rowNum = sheet.getFirstRowNum() + 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row == null || isEmptyRow(row)) {
                continue;
            }
            
            // 将每行转换为"列名: 值"的格式
            for (int col = 0; col < headers.size(); col++) {
                Cell cell = row.getCell(col);
                String cellValue = getCellValue(cell);
                if (!cellValue.trim().isEmpty()) {
                    content.append(headers.get(col)).append(": ").append(cellValue).append(", ");
                }
            }
            content.append("\n");
        }
        
        return content.toString();
    }

    /**
     * 获取单元格的值（处理各种类型）
     */
    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                // 判断是否为日期格式
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                } else {
                    // 数字格式，避免科学计数法
                    double numericValue = cell.getNumericCellValue();
                    // 如果是整数，不显示小数点
                    if (numericValue == Math.floor(numericValue)) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    // 尝试获取公式计算结果
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getCellFormula();
                }
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    /**
     * 判断是否为空行
     */
    private boolean isEmptyRow(Row row) {
        if (row == null) {
            return true;
        }
        
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValue(cell);
                if (!value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
}

