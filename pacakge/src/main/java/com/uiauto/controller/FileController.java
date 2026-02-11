package com.uiauto.controller;

import com.uiauto.common.ApiResponse;
import com.uiauto.testcase.dto.TestCaseCreateRequest;
import com.uiauto.testcase.service.TestCaseService;
import com.uiauto.testcase.repository.ProjectRepository;
import com.uiauto.testcase.entity.ProjectEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件管理Controller
 */
@Slf4j
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    private final TestCaseService testCaseService;
    private final ProjectRepository projectRepository;

    /**
     * 下载测试用例导入模板
     */
    @GetMapping("/download/test-case-template")
    public void downloadTestCaseTemplate(HttpServletResponse response) {
        try (Workbook workbook = new XSSFWorkbook()) {
            // 创建工作表
            Sheet sheet = workbook.createSheet("测试用例模板");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("用例编号*");
            headerRow.createCell(1).setCellValue("用例名称*");
            headerRow.createCell(2).setCellValue("用例描述");
            headerRow.createCell(3).setCellValue("前置条件");
            headerRow.createCell(4).setCellValue("测试步骤");
            headerRow.createCell(5).setCellValue("预期结果");

            // 设置列宽
            sheet.setColumnWidth(0, 15 * 256);  // 用例编号
            sheet.setColumnWidth(1, 20 * 256);  // 用例名称
            sheet.setColumnWidth(2, 30 * 256);  // 用例描述
            sheet.setColumnWidth(3, 30 * 256);  // 前置条件
            sheet.setColumnWidth(4, 30 * 256);  // 测试步骤
            sheet.setColumnWidth(5, 30 * 256);  // 预期结果

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");

            String fileName = URLEncoder.encode("测试用例导入模板.xlsx", "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName);

            // 写入响应流
            try (OutputStream os = response.getOutputStream()) {
                workbook.write(os);
                os.flush();
            }

            log.info("测试用例模板下载成功");
        } catch (IOException e) {
            log.error("模板下载失败", e);
            throw new RuntimeException("模板下载失败", e);
        }
    }

    /**
     * 导入测试用例（Excel文件）
     */
    @PostMapping("/import/test-cases")
    public ApiResponse<String> importTestCases(
            @RequestParam("file") MultipartFile file,
            @RequestParam("projectId") Long projectId) {
        log.info("接收到测试用例导入请求: 文件名={}, 大小={}, 项目ID={}",
                file.getOriginalFilename(), file.getSize(), projectId);

        try {
            // 1. 验证文件格式（必须是.xlsx或.xls）
            String fileName = file.getOriginalFilename();
            if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
                return ApiResponse.error(400, "文件格式错误，请上传.xlsx或.xls格式的Excel文件");
            }

            // 2. 验证文件是否为空
            if (file.isEmpty()) {
                return ApiResponse.error(400, "文件为空，请重新选择文件");
            }

            // 3. 解析Excel内容
            List<String> caseNumbers = new ArrayList<>();
            List<String> testCaseNames = new ArrayList<>();
            List<String> descriptions = new ArrayList<>();
            List<String> prerequisites = new ArrayList<>();
            List<String> stepsTexts = new ArrayList<>();
            List<String> expectedResults = new ArrayList<>();

            try (InputStream inputStream = file.getInputStream();
                 Workbook workbook = new XSSFWorkbook(inputStream)) {

                Sheet sheet = workbook.getSheetAt(0);

                // 从第2行开始读取数据（跳过标题行）
                log.info("开始解析Excel，总行数: {}, 最后一行索引: {}", sheet.getPhysicalNumberOfRows(), sheet.getLastRowNum());

                for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    Row row = sheet.getRow(rowIndex);

                    // 跳过空行
                    if (row == null) {
                        log.warn("第{}行为空，跳过", rowIndex + 1);
                        continue;
                    }

                    // 读取单元格数据
                    String caseNumber = getCellValueAsString(row.getCell(0));
                    String name = getCellValueAsString(row.getCell(1));
                    String description = getCellValueAsString(row.getCell(2));
                    String prerequisite = getCellValueAsString(row.getCell(3));
                    String stepsText = getCellValueAsString(row.getCell(4));
                    String expectedResult = getCellValueAsString(row.getCell(5));

                    log.info("读取第{}行: 用例编号={}, 用例名称={}, 描述={}, 测试步骤={}",
                            rowIndex + 1, caseNumber, name, description, stepsText != null ? stepsText.substring(0, Math.min(20, stepsText.length())) : "null");

                    // 跳过完全空的行
                    if (caseNumber == null && name == null && description == null && prerequisite == null &&
                        stepsText == null && expectedResult == null) {
                        log.warn("第{}行所有字段都为空，跳过", rowIndex + 1);
                        continue;
                    }

                    caseNumbers.add(caseNumber);
                    testCaseNames.add(name);
                    descriptions.add(description);
                    prerequisites.add(prerequisite);
                    stepsTexts.add(stepsText);
                    expectedResults.add(expectedResult);
                }

                log.info("Excel解析完成，共读取{}行有效数据", testCaseNames.size());
            }

            // 4. 验证是否有数据
            if (testCaseNames.isEmpty()) {
                return ApiResponse.error(400, "Excel文件中没有有效的测试用例数据");
            }

            // 5. 逐个调用创建方法导入
            int successCount = 0;
            int failCount = 0;
            int skipCount = 0;  // 跳过的数量（用例已存在）

            for (int i = 0; i < testCaseNames.size(); i++) {
                try {
                    String caseNumber = caseNumbers != null && i < caseNumbers.size() ? caseNumbers.get(i) : null;
                    String name = testCaseNames.get(i);
                    String description = descriptions != null && i < descriptions.size() ? descriptions.get(i) : null;
                    String prerequisite = prerequisites != null && i < prerequisites.size() ? prerequisites.get(i) : null;
                    String stepsText = stepsTexts != null && i < stepsTexts.size() ? stepsTexts.get(i) : null;
                    String expectedResult = expectedResults != null && i < expectedResults.size() ? expectedResults.get(i) : null;

                    // 验证必填字段
                    if (name == null || name.trim().isEmpty()) {
                        log.warn("第{}行：用例名称为空，跳过", i + 2); // +2 因为有标题行(0)，从数据行(1)开始
                        failCount++;
                        continue;
                    }

                    // 构建创建请求
                    TestCaseCreateRequest request = TestCaseCreateRequest.builder()
                            .caseNumber(caseNumber)
                            .name(name.trim())
                            .description(description)
                            .projectId(projectId)
                            .stepsText(stepsText != null ? stepsText : "")
                            .expectedResult(expectedResult)
                            .priority("P2")
                            .status("NOT_EXECUTED")
                            .automationStatus("MANUAL")
                            .build();

                    // 调用创建方法
                    Long id = testCaseService.create(request);
                    log.info("成功导入第{}行测试用例，ID: {}, 用例编号: {}, 名称: {}", i + 2, id, caseNumber, name);
                    successCount++;

                } catch (RuntimeException e) {
                    String errorMsg = e.getMessage();
                    // 判断是否是"用例编号已存在"的错误
                    if (errorMsg != null && errorMsg.contains("在该项目中已存在")) {
                        log.info("第{}行：用例编号已存在，跳过。错误信息: {}", i + 2, errorMsg);
                        skipCount++;
                    } else {
                        log.error("导入第{}行测试用例失败: {}", i + 2, errorMsg, e);
                        failCount++;
                    }
                } catch (Exception e) {
                    log.error("导入第{}行测试用例失败: {}", i + 2, e.getMessage(), e);
                    failCount++;
                }
            }

            // 6. 返回导入结果
            // 查询项目名称
            String projectName = "项目[" + projectId + "]";
            try {
                ProjectEntity project = projectRepository.findById(projectId).orElse(null);
                if (project != null && project.getName() != null) {
                    projectName = project.getName();
                }
            } catch (Exception e) {
                log.warn("查询项目名称失败: {}", e.getMessage());
            }

            StringBuilder message = new StringBuilder();
            message.append(String.format("成功导入%d条", successCount));
            if (skipCount > 0) {
                message.append(String.format("，跳过%d条（用例已存在）", skipCount));
            }
            if (failCount > 0) {
                message.append(String.format("，失败%d条", failCount));
            }
            message.append(String.format("到项目[%s]", projectName));

            return ApiResponse.success("导入成功", message.toString());

        } catch (Exception e) {
            log.error("测试用例导入失败", e);
            return ApiResponse.error(500, "导入失败: " + e.getMessage());
        }
    }

    /**
     * 获取单元格值作为字符串
     */
    private String getCellValueAsString(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getStringCellValue();
                }
            default:
                return null;
        }
    }

    /**
     * 文件上传
     */
    @PostMapping("/upload")
    public ApiResponse<String> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("接收到文件上传请求: 文件名={}", file.getOriginalFilename());

        try {
            // TODO: 实现文件上传逻辑
            // 1. 验证文件大小和类型
            // 2. 生成文件存储路径
            // 3. 保存文件到磁盘或OSS
            // 4. 记录文件信息到file_management表
            // 5. 返回文件ID

            return ApiResponse.success("上传成功", "file-id-123");
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return ApiResponse.error(500, "上传失败: " + e.getMessage());
        }
    }
}
