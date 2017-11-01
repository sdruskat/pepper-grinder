/*******************************************************************************
 * Copyright (c) 2016, 2017 Stephan Druskat
 * Exploitation rights for this version belong exclusively to Universität Hamburg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Stephan Druskat - initial API and implementation
 *******************************************************************************/
package net.sdruskat.peppergrinder.rcp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class providing a method to recursively compress
 * a directory into a zip archive.
 *
 * @author Stephan Druskat <[mail@sdruskat.net](mailto:mail@sdruskat.net)>
 * 
 */
public class ZipCompressor {
	
	private static final Logger log = LoggerFactory.getLogger(ZipCompressor.class);
	
	/**
	 * Static method compiling a cross-platform zip file 
	 * from the contents of a {@link File} - usually a 
	 * source directory (including the source directory 
	 * file itself). The method works recursively, i.e., 
	 * all subdirectories of the source directory and 
	 * their contents are also compressed and added to 
	 * the zip file.
	 * 
	 * If the {@link File} parameter is a file rather
	 * than a directory, the file will be compressed
	 * and added to the zip file.
	 * 
	 * @param sourceDirectory The directory (or file) which will be compressed and added to the zip file.
	 * @param zipFilePath A {@link String} representation of the target zip file path.
	 * @throws IOException
	 */
	public static void createZipFile(File sourceDirectory, String zipFilePath) throws IOException {
		List<String> fileList = traverseDirectory(sourceDirectory);
		ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFilePath));

		zipOut.setLevel(9);
		zipOut.setComment("Pepper Grinder");

		for (String fileName : fileList) {
			File file = new File(sourceDirectory.getParent(), fileName);
			String fileNameForZip = fileName;
			/*
			 * If the file separator is NOT the literal
			 * '/', replace it with '/'. This is necessary
			 * to make the zip file cross-platform
			 * compatible, as if this is run on Windows,
			 * the file separator is usually '\' which
			 * can lead to nasty side-effects when unzipping
			 * a Windows-created zip file on Unix-based
			 * systems.
			 */
			if (File.separatorChar != '/') {
				fileNameForZip = fileName.replace(File.separatorChar, '/');
			}
			ZipEntry zipEntry;
			if (!file.getName().endsWith(".zip") && file.isFile()) {
				zipEntry = new ZipEntry(fileNameForZip);
				zipEntry.setTime(file.lastModified());
				zipOut.putNextEntry(zipEntry);
				FileInputStream fileIn = new FileInputStream(file);
				byte[] buffer = new byte[4096];
				for (int n; (n = fileIn.read(buffer)) > 0; /* Do nothing with n as it is not an iterator value */) {
					zipOut.write(buffer, 0, n);
				}
				fileIn.close();
			}
			else {
				zipEntry = new ZipEntry(fileNameForZip + '/');
				zipEntry.setTime(file.lastModified());
				zipOut.putNextEntry(zipEntry);
			}
		}
		zipOut.close();
	}

	private static List<String> traverseDirectory(File directory) throws IOException {

		Stack<String> stack = new Stack<String>();
		List<String> list = new ArrayList<String>();

		/* 
		 * If the parameter is a file, add it to
		 * the list as the only entry and return
		 * the list. 
		 */
		if (directory.isFile()) {
			if (directory.canRead()) {
				list.add(directory.getName());
			}
			return list;
		}

		/* 
		 * Traverse the directory and add the files to the list.
		 */
		String root = directory.getParent();
		stack.push(directory.getName());
		while (!stack.empty()) {
			String current = (String) stack.pop();
			File currentDirectory = new File(root, current);
			String[] fileList = currentDirectory.list();
			if (fileList != null) {
				for (String fileString : fileList) {
					File file = new File(currentDirectory, fileString);
					if (file.isFile()) {
						if (file.canRead()) {
							list.add(current + File.separator + fileString);
						}
						else {
							log.error("Cannot read the file '" + file.getPath() + "'.");
							throw new IOException();
						}
					}
					else if (file.isDirectory()) {
						list.add(current + File.separator + fileString);
						stack.push(current + File.separator + file.getName());
					}
					else {
						log.error("Entry '" + file.getPath() + "' is neither file nor directory!");
						throw new IOException();
					}
				}
			}
		}
		return list;
	}

}
