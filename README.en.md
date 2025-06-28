# hotfix

Implementation and Optimization of Dynamic Loading of Dex/Jar/APK for Hot Updates
 
# Class Overview
 
 MyApp  extends  Application  and serves as a custom application class designed to implement dynamic loading functionality on Android. By loading  dex ,  jar , and  apk  files from a specified directory during the application's initialization phase, it enables dynamic class loading and hot update capabilities.
 
# Core Functions
 
2.1 Initializing the Dynamic Loading Environment
 
Initialization occurs during the  attachBaseContext  phase when the application starts, with the following directories specified:
 
- Optimization Directory:  /dex_opt/  within the application's private directory, used to store the optimized  oat  files generated from  dex  files.
- Target File Directory:  /dex_files/  within the application's private directory, designated for storing dynamic files to be loaded.
 
2.2 Scanning Loadable Files
 
The  getDexPaths()  method traverses the target directory and filters files based on the following criteria:
 
- Only processes files (excluding directories).
- Supports file formats:  .dex ,  .jar ,  .apk  (case-insensitive).
- Returns a list of absolute paths of all eligible files.
 
2.3 Dynamic File Loading
 
The  loadDexFiles()  method executes the core loading logic with the following steps:
 
- Checks if the list of files to be loaded is empty.
- Creates a  DexClassLoader  for each file to perform the loading operation.
- Extracts the  dexElements  (an array of dex elements that store class information) after loading.
- Merges the newly added dex elements with the existing dex elements of the main program.
- Injects the merged elements into the main class loader ( PathClassLoader ), making the dynamic classes effective.
 
# Analysis of Key Methods
 
3.1  attachBaseContext(Context context) 
 
- Overrides the application initialization method to execute dynamic loading during the context attachment phase.
- Initializes the optimization directory and the target file directory (both are private directories of the application, with the path format:  /data/user/0/package_name/files/subdirectory/ ).
- Calls  getDexPaths()  to obtain the file list and then uses  loadDexFiles()  to perform the loading.
 
3.2  getDexPaths() 
 
- Function: Scans the target directory and returns the paths of all dynamically loadable files.
- Logic:
- Checks if the directory exists, is a valid directory, and is readable.
- Iterates through all files in the directory and filters out files with the  .dex ,  .jar , and  .apk  extensions.
- Returns a list of absolute file paths.
 
3.3  loadDexFiles(Context context, List<String> dexPaths) 
 
- This is the core loading method, which achieves dynamic loading by modifying the  dex  elements of the class loader through reflection.
- Key Steps:
- Obtains the main class loader ( PathClassLoader ).
- Creates a  DexClassLoader  for each file to be loaded and extracts its  dexElements .
- Extracts the  dexElements  from the main class loader (original class information).
- Merges all  dexElements  (elements from newly added files take precedence over the original ones).
- Sets the merged elements back into the main class loader through reflection to complete the injection.
 
3.4 Reflection - Related Auxiliary Methods
 
-  createDexClassLoader() : Creates a  DexClassLoader , specifying the  dex  path, optimization directory, and parent class loader.
-  getPathList(ClassLoader classLoader) : Retrieves the  pathList  field from the class loader through reflection, which stores  dex  - related information.
-  getDexElements(Object pathList) : Fetches the  dexElements  array from the  pathList  via reflection.
-  combineDexElements(List<Object> elementArrays) : Merges multiple  dexElements  arrays into a single one.
-  setDexElements(Object pathList, Object elements) : Sets the merged  dexElements  back into the  pathList  using reflection.
 
# Implementation Principle
 
By modifying the internal structure of the Android class loader ( PathClassLoader ) through reflection, the class information from dynamically loaded  dex ,  jar , and  apk  files is merged into the main class loader. Thanks to the parent - delegation model of class loaders, newly added classes are loaded with priority, thus enabling the update of class logic without restarting the application (hot update).
 
# Precautions
 
- All directories use the application's private directory, eliminating the need for additional storage permissions.
- The optimization directory ( dex_opt ) stores the optimized  oat  files of  dex , enhancing the loading efficiency.
- Supported file formats are  .dex ,  .jar ,  .apk  (in Android,  jar  and  apk  files can contain  dex  resources).
- Reflection operations may encounter compatibility issues across different Android versions (testing on various API levels is required).
- Exception Handling: The code catches potential exceptions from reflection and file operations to prevent application crashes.
