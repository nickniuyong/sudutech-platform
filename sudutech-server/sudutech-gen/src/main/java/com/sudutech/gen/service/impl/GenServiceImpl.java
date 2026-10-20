package com.sudutech.gen.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.sudutech.common.exception.ServiceException;
import com.sudutech.common.utils.JsonUtils;
import com.sudutech.common.utils.StringUtils;
import com.sudutech.gen.domain.GenTable;
import com.sudutech.gen.domain.GenTableColumn;
import com.sudutech.gen.mapper.GenTableColumnMapper;
import com.sudutech.gen.mapper.GenTableMapper;
import com.sudutech.gen.service.GenService;
import com.sudutech.gen.util.GenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.sudutech.gen.domain.table.GenTableTableDef.GEN_TABLE;

/**
 * 代码生成服务实现：Velocity 模板渲染 + Zip 打包
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GenServiceImpl implements GenService {

    private final GenTableMapper genTableMapper;
    private final GenTableColumnMapper columnMapper;
    private final JdbcTemplate jdbcTemplate;

    private VelocityEngine velocityEngine;

    @PostConstruct
    public void initVelocity() {
        velocityEngine = new VelocityEngine();
        velocityEngine.setProperty("resource.loaders", "class");
        velocityEngine.setProperty("resource.loader.class.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        velocityEngine.setProperty("input.encoding", "UTF-8");
        velocityEngine.setProperty("output.encoding", "UTF-8");
        velocityEngine.init();
    }

    @Override
    public List<Map<String, Object>> selectDbTableList(String tableName) {
        String sql = "select table_name, table_comment, create_time, update_time " +
                "from information_schema.tables where table_schema = database() and table_type = 'BASE TABLE'";
        Object[] args = new Object[0];
        if (StringUtils.isNotBlank(tableName)) {
            sql += " and table_name like ?";
            args = new Object[]{"%" + tableName + "%"};
        }
        sql += " order by table_name";
        return jdbcTemplate.queryForList(sql, args);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int importTables(String[] tableNames) {
        int count = 0;
        for (String tableName : tableNames) {
            if (genTableMapper.selectOneByCondition(GEN_TABLE.TABLE_NAME.eq(tableName)) != null) {
                continue;
            }
            GenTable table = readTableMeta(tableName);
            if (table == null) {
                throw new ServiceException("表不存在: " + tableName);
            }
            GenUtils.initTable(table, "sudutech");
            genTableMapper.insert(table);
            List<GenTableColumn> columns = readColumnMeta(tableName);
            for (GenTableColumn column : columns) {
                GenUtils.initColumn(column, table);
                column.setTableId(table.getTableId());
                columnMapper.insert(column);
            }
            count++;
        }
        return count;
    }

    private GenTable readTableMeta(String tableName) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "select table_name, table_comment from information_schema.tables " +
                        "where table_schema = database() and table_name = ?", tableName);
        if (rows.isEmpty()) {
            return null;
        }
        GenTable table = new GenTable();
        table.setTableName(String.valueOf(rows.get(0).get("table_name")));
        table.setTableComment(String.valueOf(rows.get(0).get("table_comment")));
        return table;
    }

    private List<GenTableColumn> readColumnMeta(String tableName) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "select column_name, data_type, column_comment, column_key, extra, is_nullable " +
                        "from information_schema.columns where table_schema = database() and table_name = ? " +
                        "order by ordinal_position", tableName);
        List<GenTableColumn> columns = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            GenTableColumn column = new GenTableColumn();
            column.setColumnName(String.valueOf(row.get("column_name")));
            column.setColumnComment(String.valueOf(row.get("column_comment")));
            column.setColumnType(String.valueOf(row.get("data_type")));
            String key = String.valueOf(row.get("column_key"));
            column.setIsPk("PRI".equalsIgnoreCase(key) ? "1" : "0");
            String extra = String.valueOf(row.get("extra"));
            column.setIsIncrement(extra != null && extra.toLowerCase().contains("auto_increment") ? "1" : "0");
            column.setIsNullable("NO".equalsIgnoreCase(String.valueOf(row.get("is_nullable"))) ? "1" : "0");
            columns.add(column);
        }
        return columns;
    }

    @Override
    public List<GenTable> pageList(GenTable query, int pageNum, int pageSize) {
        QueryWrapper wrapper = QueryWrapper.create();
        if (query != null && StringUtils.isNotBlank(query.getTableName())) {
            wrapper.and(GEN_TABLE.TABLE_NAME.like(query.getTableName()));
        }
        wrapper.orderBy(GEN_TABLE.TABLE_ID.desc());
        return genTableMapper.paginate(Page.of(pageNum, pageSize), wrapper).getRecords();
    }

    @Override
    public GenTable selectById(Long tableId) {
        GenTable table = genTableMapper.selectOneById(tableId);
        if (table != null) {
            List<GenTableColumn> columns = columnMapper.selectListByCondition(
                    com.sudutech.gen.domain.table.GenTableColumnTableDef.GEN_TABLE_COLUMN.TABLE_ID.eq(tableId));
            columns.sort(java.util.Comparator.comparing(c -> c.getSortNum() == null ? 0 : c.getSortNum()));
            table.setColumns(columns);
        }
        return table;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(GenTable table) {
        GenTable exist = genTableMapper.selectOneById(table.getTableId());
        if (exist == null) {
            throw new ServiceException("生成配置不存在");
        }
        boolean ok = genTableMapper.update(table) > 0;
        if (ok && table.getColumns() != null) {
            for (GenTableColumn column : table.getColumns()) {
                column.setTableId(table.getTableId());
                columnMapper.update(column);
            }
        }
        return ok;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(Long[] tableIds) {
        int count = 0;
        for (Long tableId : tableIds) {
            genTableMapper.deleteColumnsByTableId(tableId);
            count += genTableMapper.deleteById(tableId);
        }
        return count;
    }

    @Override
    public Map<String, String> preview(Long tableId) {
        GenTable table = requireTable(tableId);
        Map<String, VelocityContext> contexts = prepareContexts(table);
        Map<String, String> files = new LinkedHashMap<>();
        for (Map.Entry<String, VelocityContext> entry : contexts.entrySet()) {
            String templatePath = entry.getKey();
            String file = templateFileMap.get(templatePath);
            files.put(file, render(templatePath, entry.getValue()));
        }
        return files;
    }

    @Override
    public byte[] batchGenCode(Long[] tableIds) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bos)) {
            for (Long tableId : tableIds) {
                GenTable table = requireTable(tableId);
                Map<String, VelocityContext> contexts = prepareContexts(table);
                for (Map.Entry<String, VelocityContext> entry : contexts.entrySet()) {
                    String content = render(entry.getKey(), entry.getValue());
                    String file = templateFileMap.get(entry.getKey());
                    String fullPath = getGenPath(table) + file;
                    zip.putNextEntry(new ZipEntry(fullPath));
                    zip.write(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    zip.closeEntry();
                }
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("打包生成代码失败: " + e.getMessage());
        }
        return bos.toByteArray();
    }

    private String getGenPath(GenTable table) {
        // 以模块名作为 zip 根目录
        String moduleName = StringUtils.isBlank(table.getModuleName()) ? "main" : table.getModuleName();
        return moduleName + "/";
    }

    private GenTable requireTable(Long tableId) {
        GenTable table = selectById(tableId);
        if (table == null || table.getColumns() == null || table.getColumns().isEmpty()) {
            throw new ServiceException("请先导入表结构并配置字段");
        }
        return table;
    }

    /** 模板路径 -> 输出文件路径 */
    private static final Map<String, String> templateFileMap = new LinkedHashMap<>();

    static {
        templateFileMap.put("vm/java/domain.java.vm", "placeholder-domain");
        templateFileMap.put("vm/java/mapper.java.vm", "placeholder-mapper");
        templateFileMap.put("vm/java/mapperXml.vm", "placeholder-mapperXml");
        templateFileMap.put("vm/java/service.java.vm", "placeholder-service");
        templateFileMap.put("vm/java/serviceImpl.java.vm", "placeholder-serviceImpl");
        templateFileMap.put("vm/java/controller.java.vm", "placeholder-controller");
        templateFileMap.put("vm/vue/api.js.vm", "placeholder-api");
        templateFileMap.put("vm/vue/index.vue.vm", "placeholder-index");
        templateFileMap.put("vm/sql/menu.sql.vm", "placeholder-menu.sql");
    }

    private Map<String, VelocityContext> prepareContexts(GenTable table) {
        // 计算生成辅助字段：TableDef 常量名、首字母大写字段名
        String tableDefVar = StringUtils.toUnderScoreCase(table.getClassName()).toUpperCase();
        for (GenTableColumn column : table.getColumns()) {
            column.setDefVar(StringUtils.toUnderScoreCase(column.getJavaField()).toUpperCase());
            column.setCapField(StringUtils.capitalize(column.getJavaField()));
        }
        Map<String, Object> extra = table.getOptions() == null ? Map.of()
                : JsonUtils.parseMap(table.getOptions());
        VelocityContext context = new VelocityContext();
        context.put("table", table);
        context.put("columns", table.getColumns());
        context.put("pk", table.getColumns().stream()
                .filter(c -> "1".equals(c.getIsPk())).findFirst().orElse(null));
        context.put("packageName", StringUtils.isBlank(table.getPackageName())
                ? "com.sudutech.business" : table.getPackageName());
        context.put("moduleName", StringUtils.isBlank(table.getModuleName()) ? "business" : table.getModuleName());
        context.put("businessName", StringUtils.isBlank(table.getBusinessName())
                ? table.getTableName() : table.getBusinessName());
        context.put("functionName", StringUtils.isBlank(table.getFunctionName())
                ? table.getTableComment() : table.getFunctionName());
        context.put("author", StringUtils.isBlank(table.getAuthorName()) ? "sudutech" : table.getAuthorName());
        context.put("datetime", java.time.LocalDate.now().toString());
        context.put("extra", extra);
        context.put("tableDefVar", tableDefVar);

        Map<String, VelocityContext> contexts = new LinkedHashMap<>();
        List<String> templates = new ArrayList<>(templateFileMap.keySet());
        for (String template : templates) {
            contexts.put(template, context);
        }
        computeFileNames(table);
        return contexts;
    }

    /** 依据表配置计算每个模板的输出文件名 */
    private void computeFileNames(GenTable table) {
        String className = table.getClassName();
        String pkgPath = (StringUtils.isBlank(table.getPackageName()) ? "com/sudutech/business"
                : table.getPackageName().replace('.', '/'));
        String module = StringUtils.isBlank(table.getModuleName()) ? "business" : table.getModuleName();
        String business = StringUtils.isBlank(table.getBusinessName()) ? table.getTableName() : table.getBusinessName();

        templateFileMap.put("vm/java/domain.java.vm", "src/main/java/" + pkgPath + "/domain/" + className + ".java");
        templateFileMap.put("vm/java/mapper.java.vm", "src/main/java/" + pkgPath + "/mapper/" + className + "Mapper.java");
        templateFileMap.put("vm/java/mapperXml.vm", "src/main/resources/mapper/" + className + "Mapper.xml");
        templateFileMap.put("vm/java/service.java.vm", "src/main/java/" + pkgPath + "/service/" + className + "Service.java");
        templateFileMap.put("vm/java/serviceImpl.java.vm", "src/main/java/" + pkgPath + "/service/impl/" + className + "ServiceImpl.java");
        templateFileMap.put("vm/java/controller.java.vm", "src/main/java/" + pkgPath + "/controller/" + className + "Controller.java");
        templateFileMap.put("vm/vue/api.js.vm", "src/api/" + module + "/" + business + ".js");
        templateFileMap.put("vm/vue/index.vue.vm", "src/views/" + module + "/" + business + "/index.vue");
        templateFileMap.put("vm/sql/menu.sql.vm", "sql/" + table.getTableName() + "_menu.sql");
    }

    private String render(String templatePath, VelocityContext context) {
        Template template = velocityEngine.getTemplate(templatePath, "UTF-8");
        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        return writer.toString();
    }
}
