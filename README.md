# hotfix

动态加载 Dex/Jar/APK 功能实现热更新优化
 
# 类概述
 
 MyApp  继承自  Application ，是一个用于实现安卓动态加载功能的自定义应用类。通过在应用初始化阶段加载指定目录下的  dex 、 jar 、 apk  文件，实现类的动态加载与热更新能力。
 
# 核心功能
 
1. 初始化动态加载环境
 
在应用启动的  attachBaseContext  阶段完成初始化，指定：
 
- 优化目录：应用私有目录下的  /dex_opt/ （用于存放 dex 优化后的 oat 文件）
- 目标文件目录：应用私有目录下的  /dex_files/ （用于存放待加载的动态文件）
 
2. 扫描可加载文件
 
通过  getDexPaths()  方法遍历目标目录，筛选出符合条件的文件：
 
- 仅处理文件（不包含目录）
- 支持的文件格式： .dex 、 .jar 、 .apk （不区分大小写）
- 返回所有符合条件文件的绝对路径列表
 
3. 动态加载文件
 
通过  loadDexFiles()  方法实现核心加载逻辑，步骤包括：
 
- 检查待加载文件列表是否为空
- 为每个文件创建  DexClassLoader  进行加载
- 提取加载后的  dexElements （dex 元素数组，存储类信息）
- 合并新增 dex 元素与原有主程序的 dex 元素
- 将合并后的元素注入到主类加载器（ PathClassLoader ），使动态类生效
 
# 关键方法解析
 
1.  attachBaseContext(Context context) 
 
- 重写应用初始化方法，在上下文附加阶段执行动态加载
- 初始化优化目录和目标文件目录（均为应用私有目录，路径格式： /data/user/0/包名/files/子目录/ ）
- 调用  getDexPaths()  获取文件列表，再通过  loadDexFiles()  加载
 
2.  getDexPaths() 
 
- 功能：扫描目标目录，返回所有可动态加载的文件路径
- 逻辑：
- 检查目录是否存在、是否为有效目录且可读
- 遍历目录下所有文件，过滤出  .dex 、 .jar 、 .apk  格式的文件
- 返回文件绝对路径列表
 
3.  loadDexFiles(Context context, List<String> dexPaths) 
 
- 核心加载方法，通过反射修改类加载器的 dex 元素实现动态加载
- 关键步骤：
- 获取主类加载器（ PathClassLoader ）
- 为每个待加载文件创建  DexClassLoader ，并提取其  dexElements 
- 提取主类加载器的  dexElements （原有类信息）
- 合并所有  dexElements （新增文件的元素优先于原有元素）
- 通过反射将合并后的元素设置回主类加载器，完成注入
 
4. 反射相关辅助方法
 
-  createDexClassLoader() ：创建  DexClassLoader ，指定 dex 路径、优化目录和父类加载器
-  getPathList(ClassLoader classLoader) ：通过反射获取类加载器中的  pathList  字段（存储 dex 相关信息）
-  getDexElements(Object pathList) ：通过反射从  pathList  中获取  dexElements  数组
-  combineDexElements(List<Object> elementArrays) ：合并多个  dexElements  数组为一个
-  setDexElements(Object pathList, Object elements) ：通过反射将合并后的  dexElements  设置回  pathList 
 
# 实现原理
 
通过反射修改安卓类加载器（ PathClassLoader ）的内部结构，将动态加载的 dex/jar/apk 中的类信息合并到主类加载器中。由于类加载器采用双亲委派模型，新增的类会优先被加载，从而实现不重启应用即可更新类逻辑（热更新）。
 
# 注意事项
 
- 所有目录均使用应用私有目录，无需额外存储权限
- 优化目录（ dex_opt ）用于存放 dex 优化后的 oat 文件，提升加载效率
- 支持的文件格式为  .dex 、 .jar 、 .apk （安卓中 jar 和 apk 可包含 dex 资源）
- 反射操作可能因安卓版本不同而存在兼容性问题（需测试不同 API 版本）
- 异常处理：代码中捕获了反射及文件操作可能出现的异常，避免应用崩溃
