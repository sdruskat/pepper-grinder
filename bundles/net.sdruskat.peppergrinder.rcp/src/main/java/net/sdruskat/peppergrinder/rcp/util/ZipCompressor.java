package net.sdruskat.peppergrinder.rcp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipCompressor {

	List<String> filesListInDir = new ArrayList<String>();

	public void zipDirectory(File dir, String zipDirName) throws IOException {
        	/* 
        	 * Populate a complete list of files in the 
        	 * directory and its subdirectories
        	 */
            populateFilesList(dir);
            // Create tmp dir and zip file to do compression
            File tmpZip = new File("./tmp/tmpZip.zip");
            tmpZip.getParentFile().mkdirs();
            tmpZip.createNewFile();
            //now zip files one by one
            //create ZipOutputStream to write to the zip file
            FileOutputStream fos = new FileOutputStream(tmpZip);
            ZipOutputStream zos = new ZipOutputStream(fos);
            for(String filePath : filesListInDir){
                System.out.println("Zipping "+filePath);
                //for ZipEntry we need to keep only relative file path, so we used substring on absolute path
                ZipEntry ze = new ZipEntry(filePath.substring(dir.getAbsolutePath().length()+1, filePath.length()));
                zos.putNextEntry(ze);
                //read the file and write to ZipOutputStream
                FileInputStream fis = new FileInputStream(filePath);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
                zos.closeEntry();
                fis.close();
            }
            zos.close();
            fos.close();
        // Copy tmp zip and remove tmp
        // Create zip file
        File targetZip = new File(zipDirName);
        // Not nececssary, but in case target path is changed, created parent dirs anyway
        targetZip.getParentFile().mkdirs();
        targetZip.createNewFile();
        String targetPath = targetZip.getAbsolutePath();
        String tmpPath = tmpZip.getAbsolutePath();
        Files.copy(Paths.get(tmpPath), Paths.get(targetPath), StandardCopyOption.REPLACE_EXISTING);
        Files.delete(Paths.get(tmpPath));
        Files.delete(Paths.get("./tmp"));
    }

	private void populateFilesList(File dir) throws IOException {
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isFile())
				filesListInDir.add(file.getAbsolutePath());
			else
				populateFilesList(file);
		}
	}

	// /**
	// *
	// * FIXME Write up differently This method creates the zip archive and then
	// * goes through each file in the chosen directory, adding each one to the
	// * archive. Note the use of the try with resource to avoid any finally
	// * blocks.
	// */
	// public void createZip(String sourceDirectoryPath, String zipFilePath) {
	// Path directory = Paths.get(sourceDirectoryPath);
	//
	// File zipFile = Paths.get(zipFilePath).toFile();
	//
	// try (ZipOutputStream zipStream = new ZipOutputStream(new
	// FileOutputStream(zipFile))) {
	//
	// // FIXME Change comment traverse every file in the selected
	// // directory and add them
	// // to the zip file by calling addToZipFile(..)
	// DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory);
	// dirStream.forEach(path -> addToZip(path, zipStream));
	//
	// }
	// catch (IOException e) {
	// e.printStackTrace();
	// }
	// }

	/**
	 * FIXME Change
	 * 
	 * Adds an extra file to the zip archive, copying in the created date and a
	 * comment.
	 * 
	 * @param file
	 *            file to be archived
	 * @param zipStream
	 *            archive to contain the file.
	 */
	// private void addToZip(Path file, ZipOutputStream zipStream) {
	// File inputFile = file.toFile();
	// if (inputFile.isDirectory()) {
	// createZip(inputFile.getAbsolutePath(), zipStream.g);
	// }
	// String inputFilePath = inputFile.getPath();
	//
	// // file.toFile().getp
	// // try (FileInputStream inputStream = new
	// // FileInputStream(inputFileName)) {
	// //
	// // // FIXME create a new ZipEntry, which is basically another file
	// // // within the archive. We omit the path from the filename
	// // ZipEntry entry = new ZipEntry(file.toFile().getName());
	// //
	// entry.setCreationTime(FileTime.fromMillis(file.toFile().lastModified()));
	// // zipStream.putNextEntry(entry);
	// //
	// //
	// // // Now we copy the existing file into the zip archive. To do
	// // // this we write into the zip stream, the call to putNextEntry
	// // // above prepared the stream, we now write the bytes for this
	// // // entry. For another source such as an in memory array, you'd
	// // // just change where you read the information from.
	// // byte[] readBuffer = new byte[2048];
	// // int amountRead;
	// // int written = 0;
	// //
	// // while ((amountRead = inputStream.read(readBuffer)) > 0) {
	// // zipStream.write(readBuffer, 0, amountRead);
	// // written += amountRead;
	// // }
	// //
	// //
	// //
	// // }
	// // catch(IOException e) {
	// // e.printStackTrace();
	// // }
	// }

}
