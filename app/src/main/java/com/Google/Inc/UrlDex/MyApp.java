package com.Google.Inc.UrlDex;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;
import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
/*
 By:LeoChen

 MyApp热更新动态加载Dex/Jar/APK
 */

public class MyApp extends Application {
	// private static final String TAG = "MultiDexLoader";
	private String optimizedDirPath = ""; // 优化目录
	String targetDirPath = "";

	@Override
	protected void attachBaseContext(Context context) {
		super.attachBaseContext(context); // 必须先调用父类
		optimizedDirPath = context.getFilesDir() + "/dex_opt/"; // 私有优化目录
		File filesDir = getFilesDir();
		String dexPath = filesDir.getAbsolutePath()+"/dex_files/";//程序的私有目录/data/user/0/包名/files/dex_files/
		//在这里存放动态加载文件
		targetDirPath = dexPath;
		List<String> dexPaths = getDexPaths();
		loadDexFiles(context, dexPaths);//动态加载

	}
	//遍历所有关于可动态加载的文件
	private List<String> getDexPaths() {
		List<String> dexPaths = new ArrayList<>();
		File targetDir = new File(targetDirPath);


		if (!targetDir.exists() || !targetDir.isDirectory() || !targetDir.canRead()) {
			return dexPaths;
		}


		File[] files = targetDir.listFiles();
		if (files == null) {
			return dexPaths;
		}


		for (File file : files) {
			String fileName = file.getName().toLowerCase();
			if (file.isFile() && (fileName.endsWith(".dex")
				|| fileName.endsWith(".jar")
				|| fileName.endsWith(".apk"))) {
				dexPaths.add(file.getAbsolutePath());
			}
		}

		return dexPaths;
	}

	//热更新动态加载

	private void loadDexFiles(Context context, List<String> dexPaths) {
		if (dexPaths == null || dexPaths.isEmpty()) {
			// Log.i(TAG, "No dex files to load.");
			return;
		}
		try {
			PathClassLoader mainClassLoader = (PathClassLoader) getClassLoader();
			List<Object> allDexElements = new ArrayList<>();

			// 1. 加载所有新增 dex 的 elements（优先级更高，放前面）
			for (String dexPath : dexPaths) {
				if (!new File(dexPath).exists()) {
					//     Log.e(TAG, "Dex file not found: " + dexPath);
					continue;
				}
				DexClassLoader dexClassLoader = createDexClassLoader(dexPath, mainClassLoader);
				Object dexElements = getDexElementsFromClassLoader(dexClassLoader);
				if (dexElements != null) {
					allDexElements.add(dexElements);
				}
			}

			// 2. 加载主ClassLoader的 elements（原有 dex，放后面）
			Object mainDexElements = getDexElementsFromClassLoader(mainClassLoader);
			if (mainDexElements != null) {
				allDexElements.add(mainDexElements);
			}

			// 3. 合并所有 elements
			Object combinedElements = combineDexElements(allDexElements);
			if (combinedElements == null) {
				//    Log.e(TAG, "Failed to combine dex elements.");
				return;
			}

			// 4. 注入主ClassLoader
			Object mainPathList = getPathList(mainClassLoader);
			setDexElements(mainPathList, combinedElements);
			//   Log.i(TAG, "Multi dex loaded successfully.");
		} catch (Exception e) {
			//   Log.e(TAG, "Dex load failed: " + e.getMessage(), e);
		}
	}

	private DexClassLoader createDexClassLoader(String dexPath, PathClassLoader parent) {
		return new DexClassLoader(
			dexPath,
			optimizedDirPath, // 优化目录
			null,             // 本地库路径（无则 null）
			parent            // 父 ClassLoader
		);
	}

	private Object getDexElementsFromClassLoader(ClassLoader classLoader) throws Exception {
		Object pathList = getPathList(classLoader);
		return getDexElements(pathList);
	}

	private Object getPathList(ClassLoader classLoader) throws Exception {
		Field pathListField = classLoader.getClass().getSuperclass().getDeclaredField("pathList");
		pathListField.setAccessible(true);
		return pathListField.get(classLoader);
	}

	private Object getDexElements(Object pathList) throws Exception {
		Field dexElementsField = pathList.getClass().getDeclaredField("dexElements");
		dexElementsField.setAccessible(true);
		return dexElementsField.get(pathList);
	}

	private Object combineDexElements(List<Object> elementArrays) {
		if (elementArrays.isEmpty()) return null;
		Class<?> componentType = elementArrays.get(0).getClass().getComponentType();
		int totalLength = 0;
		for (Object array : elementArrays) {
			totalLength += Array.getLength(array);
		}
		Object combined = Array.newInstance(componentType, totalLength);
		int index = 0;
		for (Object array : elementArrays) {
			for (int i = 0; i < Array.getLength(array); i++) {
				Array.set(combined, index++, Array.get(array, i));
			}
		}
		return combined;
	}

	private void setDexElements(Object pathList, Object elements) throws Exception {
		Field dexElementsField = pathList.getClass().getDeclaredField("dexElements");
		dexElementsField.setAccessible(true);
		dexElementsField.set(pathList, elements);
	}
}
