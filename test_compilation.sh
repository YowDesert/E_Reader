#!/bin/bash
echo "编译 E_Reader 项目..."

# 设置 JAVA_HOME（根据你的 Java 安装路径调整）
# export JAVA_HOME=/path/to/your/java

# 进入项目目录
cd "C:/E_Reader/E_Reader"

# 使用 Maven 编译项目
echo "正在使用 Maven 编译..."
mvn clean compile

if [ $? -eq 0 ]; then
    echo "编译成功！"
    echo "主要修改内容："
    echo "1. ✅ 根据模式动态显示功能按钮"
    echo "2. ✅ 图片模式移除文字相关功能（文字搜索、字体调整、行距调整）"
    echo "3. ✅ 文字模式移除图片相关功能（图片缩放、旋转、适配）"
    echo "4. ✅ 新增返回档案管理器按钮"
    echo "5. ✅ 页码标签移至右下角显示"
    echo ""
    echo "你可以运行以下命令启动应用程式："
    echo "mvn javafx:run"
else
    echo "编译失败，请检查错误信息"
fi
