/**
 * 
 */
package net.sdruskat.peppergrinder.rcp.util;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * // TODO Add description
 *
 * @author Stephan Druskat <[mail@sdruskat.net](mailto:mail@sdruskat.net)>
 * 
 */
public class FileCopier {

	private static final Logger log = LoggerFactory.getLogger(FileCopier.class);

	/**
	 * // TODO Add description
	 * 
	 * @param source
	 * @param target
	 * @throws IOException
	 */
	public static void copyDirectoryRecursively(Path source, Path target) throws IOException {
		String targetString = target.toString();
		if (!targetString.endsWith(File.separator)) {
			targetString = target.toString() + File.separator;
		}
		Path suffixedTarget = Paths
				.get(targetString + source.getFileName() + File.separator);

		// java nio folder copy
		EnumSet<FileVisitOption> options = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
		// check first if source is a directory
		if (Files.isDirectory(source)) {
			log.info("source is a directory");

			Files.walkFileTree(source, options, Integer.MAX_VALUE, new FileVisitor<Path>() {

				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					CopyOption[] opt = new CopyOption[] { COPY_ATTRIBUTES, REPLACE_EXISTING };
					log.info("Source Directory " + dir);
					Path newDirectory = null;
					if (dir instanceof Path)
						newDirectory = suffixedTarget.resolve(source.relativize((Path) dir));
					log.info("Target Directory " + newDirectory);
					try {
						if (dir instanceof Path)
							log.info("creating directory tree " + Files.copy((Path) dir, newDirectory, opt));
						else
							System.err.println("DIR IS NOT A PATH.");
					}
					catch (FileAlreadyExistsException x) {
					}
					catch (IOException x) {
						return FileVisitResult.SKIP_SUBTREE;
					}

					return CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					log.info("Copying file:" + file);
					if (file instanceof Path)
						copy((Path) file, suffixedTarget.resolve(source.relativize((Path) file)));
					else
						System.err.println("FILE IS NOT A PATH!");
					return CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
					// TODO Auto-generated method stub
					return CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					// TODO Auto-generated method stub
					return FileVisitResult.CONTINUE;
				}
			});
		}
	}

	private static void copy(Path source, Path target) throws IOException {
		CopyOption[] options = new CopyOption[] { REPLACE_EXISTING, COPY_ATTRIBUTES };
		log.info("Copied file " + Files.copy(source, target, options));
	}

}