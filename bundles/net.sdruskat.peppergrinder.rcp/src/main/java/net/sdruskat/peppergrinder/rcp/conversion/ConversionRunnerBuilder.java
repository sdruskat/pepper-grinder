/**
 * 
 */
package net.sdruskat.peppergrinder.rcp.conversion;

/**
 * // TODO Add description
 *
 * @author Stephan Druskat <[mail@sdruskat.net](mailto:mail@sdruskat.net)>
 * 
 */
public class ConversionRunnerBuilder {

	private String corpusDirectoryPath;

	public static ConversionRunnerBuilder withCorpusImportPath(String corpusDirectoryPath) {
		ConversionRunnerBuilder self = new ConversionRunnerBuilder();
		self.corpusDirectoryPath = corpusDirectoryPath;
		return self;
	}

	public ConversionRunner build() {
		return new ConversionRunner(this.corpusDirectoryPath);
	}

}
