package cn.huanzi.qch.baseadmin.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 自动生成代码
 */
@Slf4j
public class CodeDOM {

    public static void main(String[] args) {
        CodeDOM codeDOM = new CodeDOM("tb_user_role", "sevice", "userRole", "role_name");
        // 后台代码生成
        String msg = codeDOM.create();
        System.out.println(msg);
        // 前台代码生成
        msg = codeDOM.createFront();
        System.out.println(msg);
    }

    /**
     * 构造参数，出入表名
     */
    private CodeDOM(String tableName, String moduleName, String menuName, String queryColName) {
        this.tableName = tableName;
        this.moduleName = moduleName;
        this.menuName = menuName;
        this.queryColName = queryColName;
        this.queryColNameToFeng = StringUtil.camelCaseName(queryColName);


        basePackage_ = "cn\\huanzi\\qch\\baseadmin\\" + moduleName + "\\";
        package_ = basePackage_ + menuName + "\\";
        //System.getProperty("user.dir") 获取的是项目所在路径，如果我们是子项目，则需要添加一层路径
        basePath = System.getProperty("user.dir") + "\\src\\main\\java\\" + package_;
        basePackage_ = "cn\\huanzi\\qch\\baseadmin\\";

        // js css路径
        frontJSBasePath = System.getProperty("user.dir") + "\\src\\main\\resources\\static\\" + moduleName + "\\";
        // html路径
        frontHtmlBasePath = System.getProperty("user.dir") + "\\src\\main\\resources\\view\\" + moduleName + "\\" + menuName + "\\";
    }

    /**
     * 数据连接相关
     */
    private static final String URL = "jdbc:mysql://10.66.196.40:33306/base_admin?serverTimezone=GMT%2B8&characterEncoding=utf-8";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "v3_123456";
    private static final String DRIVER_CLASSNAME = "com.mysql.cj.jdbc.Driver";
    /**
     * 表名
     */
    private String tableName;

    private String moduleName;

    private String menuName;

    private String queryColName;

    private String queryColNameToFeng;

    List<ColumnInfo> columnInfos;

    /**
     * 基础路径
     */
    private String basePackage_;
    private String package_;
    private String basePath;

    private String frontJSBasePath;
    private String frontHtmlBasePath;

    /**
     * 创建pojo实体类
     */
    private void createPojo() {
        File file = FileUtil.createFile(basePath + "pojo\\" + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + ".java");
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(
                "package " + package_.replaceAll("\\\\", ".") + "pojo;\n" +
                        "\n" +
                        "import lombok.Data;\n" +
                        "import javax.persistence.*;\n" +
                        "import java.io.Serializable;\n" +
                        "import java.util.Date;\n" +
                        "\n" +
                        "@Entity\n" +
                        "@Table(name = \"" + tableName + "\")\n" +
                        "@Data\n" +
                        "public class " + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + " implements Serializable {\n"
        );
        //遍历设置属性
        for (ColumnInfo columnInfo : columnInfos) {
            //主键
            if ("PRI".equals(columnInfo.getColumnKey())) {
                stringBuffer.append("    @Id\n");
            }
            //自增
            if ("auto_increment".equals(columnInfo.getExtra())) {
                stringBuffer.append("    @GeneratedValue(strategy= GenerationType.IDENTITY)\n");
            }
            stringBuffer.append("    private " + StringUtil.typeMapping(columnInfo.getDataType()) + " " + StringUtil.camelCaseName(columnInfo.getColumnName()) + ";//" + columnInfo.getColumnComment() + "\n\n");
        }
        stringBuffer.append("}");
        FileUtil.fileWriter(file, stringBuffer);
    }

    /**
     * 创建vo类
     */
    private void createVo() {
        File file = FileUtil.createFile(basePath + "vo\\" + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Vo.java");
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(
                "package " + package_.replaceAll("\\\\", ".") + "vo;\n" +
                        "\n" +
                        "import " + basePackage_.replaceAll("\\\\", ".") + "common.pojo.PageCondition;\n" +
                        "import lombok.Data;\n" +
                        "import java.io.Serializable;\n" +
                        "import java.util.Date;\n" +
                        "\n" +
                        "@Data\n" +
                        "public class " + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Vo extends PageCondition implements Serializable {\n"
        );
        //遍历设置属性
        for (ColumnInfo columnInfo : columnInfos) {
            stringBuffer.append("    private " + StringUtil.typeMapping(columnInfo.getDataType()) + " " + StringUtil.camelCaseName(columnInfo.getColumnName()) + ";//" + columnInfo.getColumnComment() + "\n\n");
        }
        stringBuffer.append("}");
        FileUtil.fileWriter(file, stringBuffer);
    }

    /**
     * 创建repository类
     */
    private void createRepository() {
        File file = FileUtil.createFile(basePath + "repository\\" + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Repository.java");
        StringBuffer stringBuffer = new StringBuffer();
        String t = "String";
        //遍历属性
        for (ColumnInfo columnInfo : columnInfos) {
            //主键
            if ("PRI".equals(columnInfo.getColumnKey())) {
                t = StringUtil.typeMapping(columnInfo.getDataType());
            }
        }
        stringBuffer.append(
                "package " + package_.replaceAll("\\\\", ".") + "repository;\n" +
                        "\n" +
                        "import " + basePackage_.replaceAll("\\\\", ".") + "common.repository.*;\n" +
                        "import " + package_.replaceAll("\\\\", ".") + "pojo." + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + ";\n" +
                        "import org.springframework.stereotype.Repository;\n" +
                        "\n" +
                        "@Repository\n" +
                        "public interface " + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Repository extends CommonRepository<" + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + ", " + t + "> {"
        );
        stringBuffer.append("\n");
        stringBuffer.append("}");
        FileUtil.fileWriter(file, stringBuffer);
    }

    /**
     * 创建service类
     */
    private void createService() {
        File file = FileUtil.createFile(basePath + "service\\" + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Service.java");
        StringBuffer stringBuffer = new StringBuffer();
        String t = "String";
        //遍历属性
        for (ColumnInfo columnInfo : columnInfos) {
            //主键
            if ("PRI".equals(columnInfo.getColumnKey())) {
                t = StringUtil.typeMapping(columnInfo.getDataType());
            }
        }
        stringBuffer.append(
                "package " + package_.replaceAll("\\\\", ".") + "service;\n" +
                        "\n" +
                        "import " + basePackage_.replaceAll("\\\\", ".") + "common.service.*;\n" +
                        "import " + package_.replaceAll("\\\\", ".") + "pojo." + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + ";\n" +
                        "import " + package_.replaceAll("\\\\", ".") + "vo." + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Vo;\n" +
                        "\n" +
                        "public interface " + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Service extends CommonService<" + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Vo, " + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + ", " + t + "> {"
        );
        stringBuffer.append("\n");
        stringBuffer.append("}");
        FileUtil.fileWriter(file, stringBuffer);

        //Impl
        File file1 = FileUtil.createFile(basePath + "service\\" + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "ServiceImpl.java");
        StringBuffer stringBuffer1 = new StringBuffer();
        stringBuffer1.append(
                "package " + package_.replaceAll("\\\\", ".") + "service;\n" +
                        "\n" +
                        "import " + basePackage_.replaceAll("\\\\", ".") + "common.service.*;\n" +
                        "import " + package_.replaceAll("\\\\", ".") + "pojo." + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + ";\n" +
                        "import " + package_.replaceAll("\\\\", ".") + "vo." + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Vo;\n" +
                        "import " + package_.replaceAll("\\\\", ".") + "repository." + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Repository;\n" +
                        "import org.springframework.beans.factory.annotation.Autowired;\n" +
                        "import org.springframework.stereotype.Service;\n" +
                        "import org.springframework.transaction.annotation.Transactional;\n" +
                        "import javax.persistence.EntityManager;\n" +
                        "import javax.persistence.PersistenceContext;\n" +
                        "\n" +
                        "@Service\n" +
                        "@Transactional\n" +
                        "public class " + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "ServiceImpl extends CommonServiceImpl<" + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Vo, " + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + ", " + t + "> implements " + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Service{"
        );
        stringBuffer1.append("\n\n");
        stringBuffer1.append(
                "    @PersistenceContext\n" +
                        "    private EntityManager em;\n");

        stringBuffer1.append("" +
                "    @Autowired\n" +
                "    private " + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Repository " + StringUtil.camelCaseName(tableName) + "Repository;\n");
        stringBuffer1.append("}");
        FileUtil.fileWriter(file1, stringBuffer1);
    }

    /**
     * 创建controller类
     */
    private void createController() {
        File file = FileUtil.createFile(basePath + "controller\\" + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Controller.java");
        StringBuffer stringBuffer = new StringBuffer();
        String t = "String";
        //遍历属性
        for (ColumnInfo columnInfo : columnInfos) {
            //主键
            if ("PRI".equals(columnInfo.getColumnKey())) {
                t = StringUtil.typeMapping(columnInfo.getDataType());
            }
        }
        stringBuffer.append(
                "package " + package_.replaceAll("\\\\", ".") + "controller;\n" +
                        "\n" +
                        "import " + basePackage_.replaceAll("\\\\", ".") + "common.controller.*;\n" +
                        "import " + package_.replaceAll("\\\\", ".") + "pojo." + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + ";\n" +
                        "import " + package_.replaceAll("\\\\", ".") + "vo." + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Vo;\n" +
                        "import " + package_.replaceAll("\\\\", ".") + "service." + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Service;\n" +
                        "import org.springframework.beans.factory.annotation.Autowired;\n" +
                        "import org.springframework.web.servlet.ModelAndView;\n" +
                        "import org.springframework.web.bind.annotation.*;\n" +
                        "\n" +
                        "@RestController\n" +
                        "@RequestMapping(\"/" + moduleName + "/" + StringUtil.camelCaseName(tableName) + "/\")\n" +
                        "public class " + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Controller extends CommonController<" + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Vo, " + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + ", " + t + "> {"
        );
        stringBuffer.append("\n");
        stringBuffer.append("" +
                "    @Autowired\n" +
                "    private " + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Service " + StringUtil.camelCaseName(tableName) + "Service;\n");
        stringBuffer.append("\n");
        stringBuffer.append("    @GetMapping(\"index\")\n" +
                "    public ModelAndView index(){\n" +
                "        return new ModelAndView(\"" + moduleName + "/" + menuName + "/" + menuName + "\");\n" +
                "    }\n");

        stringBuffer.append("}");
        FileUtil.fileWriter(file, stringBuffer);
    }

    /**
     * 获取表结构信息
     */
    private List<ColumnInfo> getTableInfo() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<ColumnInfo> list = new ArrayList<>();
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "select column_name,data_type,column_comment,column_key,extra from information_schema.columns where table_schema = (select database()) and table_name=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, tableName);
            rs = ps.executeQuery();
            while (rs.next()) {
                ColumnInfo columnInfo = new ColumnInfo();
                //列名，全部转为小写
                columnInfo.setColumnName(rs.getString("column_name").toLowerCase());
                //列类型
                columnInfo.setDataType(rs.getString("data_type"));
                //列注释
                columnInfo.setColumnComment(rs.getString("column_comment"));
                //主键
                columnInfo.setColumnKey(rs.getString("column_key"));
                //主键类型
                columnInfo.setExtra(rs.getString("extra"));
                list.add(columnInfo);
            }
        } catch (SQLException e) {
            //输出到日志文件中
            log.error(ErrorUtil.errorInfoToString(e));
        } finally {
            assert rs != null;
            DBConnectionUtil.close(conn, ps, rs);
        }
        return list;
    }

    /**
     * file工具类
     */
    private static class FileUtil {
        /**
         * 创建文件
         *
         * @param pathNameAndFileName 路径跟文件名
         * @return File对象
         */
        private static File createFile(String pathNameAndFileName) {
            File file = new File(pathNameAndFileName);
            try {
                //获取父目录
                File fileParent = file.getParentFile();
                if (!fileParent.exists()) {
                    fileParent.mkdirs();
                }
                //创建文件
                if (!file.exists()) {
                    file.createNewFile();
                }
            } catch (Exception e) {
                file = null;
                System.err.println("新建文件操作出错");
                //输出到日志文件中
                log.error(ErrorUtil.errorInfoToString(e));
            }
            return file;
        }

        /**
         * 字符流写入文件
         *
         * @param file         file对象
         * @param stringBuffer 要写入的数据
         */
        private static void fileWriter(File file, StringBuffer stringBuffer) {
            //字符流
            try {
                FileWriter resultFile = new FileWriter(file, false);//true,则追加写入 false,则覆盖写入
                PrintWriter myFile = new PrintWriter(resultFile);
                //写入
                myFile.println(stringBuffer.toString());

                myFile.close();
                resultFile.close();
            } catch (Exception e) {
                System.err.println("写入操作出错");
                //输出到日志文件中
                log.error(ErrorUtil.errorInfoToString(e));
            }
        }
    }

    /**
     * 字符串处理工具类
     */
    private static class StringUtil {
        /**
         * 数据库类型->JAVA类型
         *
         * @param dbType 数据库类型
         * @return JAVA类型
         */
        private static String typeMapping(String dbType) {
            String javaType;
            if ("int|integer".contains(dbType)) {
                javaType = "Integer";
            } else if ("float|double|decimal|real".contains(dbType)) {
                javaType = "Double";
            } else if ("date|time|datetime|timestamp".contains(dbType)) {
                javaType = "Date";
            } else {
                javaType = "String";
            }
            return javaType;
        }

        /**
         * 驼峰转换为下划线
         */
        public static String underscoreName(String camelCaseName) {
            StringBuilder result = new StringBuilder();
            if (camelCaseName != null && camelCaseName.length() > 0) {
                result.append(camelCaseName.substring(0, 1).toLowerCase());
                for (int i = 1; i < camelCaseName.length(); i++) {
                    char ch = camelCaseName.charAt(i);
                    if (Character.isUpperCase(ch)) {
                        result.append("_");
                        result.append(Character.toLowerCase(ch));
                    } else {
                        result.append(ch);
                    }
                }
            }
            return result.toString();
        }

        /**
         * 首字母大写
         */
        static String captureName(String name) {
            char[] cs = name.toCharArray();
            cs[0] -= 32;
            return String.valueOf(cs);

        }

        /**
         * 下划线转换为驼峰
         */
        static String camelCaseName(String underscoreName) {
            StringBuilder result = new StringBuilder();
            if (underscoreName != null && underscoreName.length() > 0) {
                boolean flag = false;
                for (int i = 0; i < underscoreName.length(); i++) {
                    char ch = underscoreName.charAt(i);
                    if ("_".charAt(0) == ch) {
                        flag = true;
                    } else {
                        if (flag) {
                            result.append(Character.toUpperCase(ch));
                            flag = false;
                        } else {
                            result.append(ch);
                        }
                    }
                }
            }
            return result.toString();
        }
    }

    /**
     * JDBC连接数据库工具类
     */
    private static class DBConnectionUtil {

        static {
            // 1、加载驱动
            try {
                Class.forName(DRIVER_CLASSNAME);
            } catch (ClassNotFoundException e) {
                //输出到日志文件中
                log.error(ErrorUtil.errorInfoToString(e));
            }
        }

        /**
         * 返回一个Connection连接
         */
        static Connection getConnection() {
            Connection conn = null;
            // 2、连接数据库
            try {
                conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            } catch (SQLException e) {
                //输出到日志文件中
                log.error(ErrorUtil.errorInfoToString(e));
            }
            return conn;
        }

        /**
         * 关闭Connection，Statement连接
         */
        public static void close(Connection conn, Statement stmt) {
            try {
                conn.close();
                stmt.close();
            } catch (SQLException e) {
                //输出到日志文件中
                log.error(ErrorUtil.errorInfoToString(e));
            }
        }

        /**
         * 关闭Connection，Statement，ResultSet连接
         */
        public static void close(Connection conn, Statement stmt, ResultSet rs) {
            try {
                close(conn, stmt);
                rs.close();
            } catch (SQLException e) {
                //输出到日志文件中
                log.error(ErrorUtil.errorInfoToString(e));
            }
        }

    }

    /**
     * 表结构行信息实体类
     */
    private class ColumnInfo {
        private String columnName;
        private String dataType;
        private String columnComment;
        private String columnKey;
        private String extra;

        ColumnInfo() {
        }

        String getColumnName() {
            return columnName;
        }

        void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        String getDataType() {
            return dataType;
        }

        void setDataType(String dataType) {
            this.dataType = dataType;
        }

        String getColumnComment() {
            return columnComment;
        }

        void setColumnComment(String columnComment) {
            this.columnComment = columnComment;
        }

        String getColumnKey() {
            return columnKey;
        }

        void setColumnKey(String columnKey) {
            this.columnKey = columnKey;
        }

        String getExtra() {
            return extra;
        }

        void setExtra(String extra) {
            this.extra = extra;
        }
    }

    /**
     * 快速创建，供外部调用，调用之前先设置一下项目的基础路径
     */
    private String create() {
        columnInfos = getTableInfo();
        createPojo();
        createVo();
        createRepository();
        createService();
        createController();
        System.out.println("生成java路径位置：" + basePath);
        return tableName + " 后台代码生成完毕！";
    }

    private String createFront() {
        createCss();
        createJs();
        System.out.println("生成css ,js路径位置：" + frontJSBasePath);
        createHtml();
        System.out.println("生成html路径位置：" + frontHtmlBasePath);
        return tableName + " 前台代码生成完毕！";
    }

    private void createCss() {
        File file = FileUtil.createFile(frontJSBasePath + menuName + "\\css\\" + menuName + ".css");
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("body {\n" +
                "    padding: 20px;\n" +
                "    background-color: #F2F2F2;\n" +
                "}\n" +
                "\n" +
                ".layui-form-label {\n" +
                "    width: 280px !important;\n" +
                "}\n" +
                "\n" +
                ".layui-input-block {\n" +
                "    display: -webkit-box !important;\n" +
                "}\n" +
                "\n" +
                ".layui-table-tool-temp {\n" +
                "    padding-right: 0;\n" +
                "}\n" +
                "\n" +
                "#queryBy" + queryColNameToFeng + " {\n" +
                "    width: 100px;\n" +
                "    display: unset;\n" +
                "    margin-right: 10px;\n" +
                "    margin-bottom: 10px;\n" +
                "}");

        FileUtil.fileWriter(file, stringBuffer);
    }

    private void createJs() {
        File file = FileUtil.createFile(frontJSBasePath + menuName + "\\js\\" + menuName + ".js");
        String tbName = StringUtil.camelCaseName(tableName);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("let tableIns;\n" +
                "layui.use(['element', 'form', 'table', 'layer', 'laydate', 'util'], function () {\n" +
                "    let table = layui.table;\n" +
                "    let form = layui.form;//select、单选、复选等依赖form\n" +
                "    let height = document.documentElement.clientHeight - 160;\n" +
                "\n" +
                "    tableIns = table.render({\n" +
                "        elem: '#" + menuName + "Table'\n" +
                "        , url: ctx + '/" + moduleName + "/" + tbName + "/page'\n" +
                "        , method: 'POST'\n" +
                "        //请求前参数处理\n" +
                "        , request: {\n" +
                "            pageName: 'page' //页码的参数名称，默认：page\n" +
                "            , limitName: 'rows' //每页数据量的参数名，默认：limit\n" +
                "        }\n" +
                "        , response: {\n" +
                "            statusName: 'flag' //规定数据状态的字段名称，默认：code\n" +
                "            , statusCode: true //规定成功的状态码，默认：0\n" +
                "            , msgName: 'msg' //规定状态信息的字段名称，默认：msg\n" +
                "            , countName: 'records' //规定数据总数的字段名称，默认：count\n" +
                "            , dataName: 'rows' //规定数据列表的字段名称，默认：data\n" +
                "        }\n" +
                "        //响应后数据处理\n" +
                "        , parseData: function (res) { //res 即为原始返回的数据\n" +
                "            var data = res.data;\n" +
                "            return {\n" +
                "                \"flag\": res.flag, //解析接口状态\n" +
                "                \"msg\": res.msg, //解析提示文本\n" +
                "                \"records\": data.records, //解析数据长度\n" +
                "                \"rows\": data.rows //解析数据列表\n" +
                "            };\n" +
                "        }\n" +
                "        , toolbar: '#" + menuName + "TableToolbar'\n" +
                "        , title: '列表'\n" +
                "        , cols: [[\n" +
                "            {field: 'id', title: 'ID'}\n");
        columnInfos.forEach(columnInfo -> {
            if (!"PRI".equals(columnInfo.getColumnKey())) {
                stringBuffer.append("            , {field: '" + StringUtil.camelCaseName(columnInfo.getColumnName()) + "', title: '" + columnInfo.getColumnComment() + "'}\n");
            }
        });


        stringBuffer.append("            , {fixed: 'right', title: '操作', toolbar: '#" + menuName + "TableBar'}\n" +
                "        ]]\n" +
                "        , defaultToolbar: ['', '', '']\n" +
                "        , page: true\n" +
                "        , height: height\n" +
                "        , cellMinWidth: 80\n" +
                "    });\n" +
                "\n" +
                "    //头工具栏事件\n" +
                "    table.on('toolbar(test)', function (obj) {\n" +
                "        switch (obj.event) {\n" +
                "            case 'addData':\n" +
                "                //重置操作表单\n" +
                "                $(\"#" + menuName + "Form\")[0].reset();\n" +
                "                form.render();\n" +
                "                layer.msg(\"请填写右边的表单并保存！\");\n" +
                "                break;\n" +
                "            case 'query':\n" +
                "                let queryBy" + queryColNameToFeng + " = $(\"#queryBy" + queryColNameToFeng + "\").val();\n" +
                "                let query = {\n" +
                "                    page: {\n" +
                "                        curr: 1 //重新从第 1 页开始\n" +
                "                    }\n" +
                "                    , done: function (res, curr, count) {\n" +
                "                        //完成后重置where，解决下一次请求携带旧数据\n" +
                "                        this.where = {};\n" +
                "                    }\n" +
                "                };\n" +
                "                if (queryBy" + queryColNameToFeng + ") {\n" +
                "                    //设定异步数据接口的额外参数\n" +
                "                    query.where = {name: queryBy" + queryColNameToFeng + "};\n" +
                "                }\n" +
                "                tableIns.reload(query);\n" +
                "                $(\"#queryBy" + queryColNameToFeng + "\").val(queryBy" + queryColNameToFeng + ");\n" +
                "                break;\n" +
                "        }\n" +
                "    });\n" +
                "\n" +
                "    //监听行工具事件\n" +
                "    table.on('tool(test)', function (obj) {\n" +
                "        let data = obj.data;\n" +
                "        //删除\n" +
                "        if (obj.event === 'del') {\n" +
                "            layer.confirm('确认删除吗？', function (index) {\n" +
                "                //向服务端发送删除指令\n" +
                "                $.delete(ctx + \"/" + moduleName + "/" + tbName + "/delete/\" + data.id, {}, function (data) {\n" +
                "                    obj.del();\n" +
                "                    layer.close(index);\n" +
                "                })\n" +
                "            });\n" +
                "        }\n" +
                "        //编辑\n" +
                "        else if (obj.event === 'edit') {\n" +
                "            //回显操作表单\n" +
                "            $(\"#" + menuName + "Form\").form(data);\n" +
                "            form.render();\n" +
                "        }\n" +
                "    });\n" +
                "});\n" +
                "\n" +
                "/**\n" +
                " * 提交保存\n" +
                " */\n" +
                "function " + menuName + "FormSave() {\n" +
                "    let " + menuName + "Form = $(\"#" + menuName + "Form\").serializeObject();\n" +
                "    $.post(ctx + \"/" + moduleName + "/" + tbName + "/save\", " + menuName + "Form, function (data) {\n" +
                "        layer.msg(\"保存成功\", {icon: 1,time: 2000}, function () {});\n" +
                "        tableIns.reload();\n" +
                "    });\n" +
                "}");

        FileUtil.fileWriter(file, stringBuffer);
    }

    private void createHtml() {
        File file = FileUtil.createFile(frontHtmlBasePath + menuName + ".html");
        String queryColComment = queryColName;
        for (ColumnInfo columnInfo : columnInfos) {
            if (queryColName.equals(columnInfo.getColumnName())) {
                queryColComment = columnInfo.getColumnComment();
                break;
            }
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("<!DOCTYPE html>\n" +
                "<html xmlns:th=\"http://www.thymeleaf.org\">\n" +
                "<head>\n" +
                "    <!-- 引入公用部分 -->\n" +
                "    <script th:replace=\"common/head::static\"></script>\n" +
                "    <!-- 样式 -->\n" +
                "    <link th:href=\"@{/" + moduleName + "/" + menuName + "/css/" + menuName + ".css}\" rel=\"stylesheet\" type=\"text/css\"/>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div class=\"layui-row layui-col-space15\">\n" +
                "    <div class=\"layui-col-md8\">\n" +
                "        <div class=\"layui-card\">\n" +
                "            <div class=\"layui-card-header\">列表</div>\n" +
                "            <div class=\"layui-card-body\">\n" +
                "                <!-- 表格主体 -->\n" +
                "                <table class=\"layui-hide\" id=\"" + menuName + "Table\" lay-filter=\"test\"></table>\n" +
                "                <!-- 模板 -->\n" +
                "                <script type=\"text/html\" id=\"" + menuName + "TableToolbar\">\n" +
                "                    <div class=\"layui-btn-container\">\n" +
                "                        <button class=\"layui-btn layui-btn-sm\" lay-event=\"addData\">新增</button>\n" +
                "                        <input type=\"text\" id=\"queryBy" + queryColNameToFeng + "\" name=\"queryBy" + queryColNameToFeng + "\" autocomplete=\"off\"\n" +
                "                               placeholder=\"请输入" + queryColComment + "\" class=\"layui-input layui-btn-sm\">\n" +
                "                        <button class=\"layui-btn layui-btn-sm\" lay-event=\"query\">查询</button>\n" +
                "                    </div>\n" +
                "                </script>\n" +
                "                <script type=\"text/html\" id=\"" + menuName + "TableBar\">\n" +
                "                    <a class=\"layui-btn layui-btn-xs\" lay-event=\"edit\">编辑</a>\n" +
                "                    <a class=\"layui-btn layui-btn-danger layui-btn-xs\" lay-event=\"del\">删除</a>\n" +
                "                </script>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "    <!-- 操作表单 -->\n" +
                "    <div class=\"layui-col-md4\">\n" +
                "        <div class=\"layui-card\">\n" +
                "            <div class=\"layui-card-header\">操作表单</div>\n" +
                "            <div class=\"layui-card-body\">\n" +
                "                <form id=\"" + menuName + "Form\" class=\"layui-form layui-form-pane\">\n");

        columnInfos.forEach(columnInfo -> {
            if (!"PRI".equals(columnInfo.getColumnKey())) {
                stringBuffer.append("                    <div class=\"layui-form-item\">\n" +
                        "                        <label class=\"layui-form-label\">" + columnInfo.getColumnComment() + "</label>\n" +
                        "                        <div class=\"layui-input-block\">\n" +
                        "                            <input type=\"text\" name=\"" + StringUtil.camelCaseName(columnInfo.getColumnName()) + "\" autocomplete=\"off\" placeholder=\"" + columnInfo.getColumnComment() + "\"\n" +
                        "                                   class=\"layui-input\">\n" +
                        "                        </div>\n" +
                        "                    </div>\n");
            }
        });


        stringBuffer.append("                    <div class=\"layui-form-item\">\n" +
                "                        <div class=\"layui-input-block\">\n" +
                "                            <a class=\"layui-btn\" onclick=\"" + menuName + "FormSave()\">保存</a>\n" +
                "                        </div>\n" +
                "                    </div>\n" +
                "                    <!-- 隐藏域 -->\n" +
                "                    <input type=\"text\" name=\"id\" hidden=\"hidden\"/>\n" +
                "                </form>\n" +
                "\n" +
                "            </div>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</div>\n" +
                "</body>\n" +
                "<!-- js -->\n" +
                "<script th:src=\"@{/" + moduleName + "/" + menuName + "/js/" + menuName + ".js}\"></script>\n" +
                "</html>");

        FileUtil.fileWriter(file, stringBuffer);
    }


}
