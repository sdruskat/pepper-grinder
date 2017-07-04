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
/**
 * 
 */
package net.sdruskat.peppergrinder.rcp.conversion;

/**
 * A builder class for configuring and building a {@link ConversionRunner}.
 *
 * @author Stephan Druskat <[mail@sdruskat.net](mailto:mail@sdruskat.net)>
 * 
 */
public class ConversionRunnerBuilder {

	private String corpusDirectoryPath;

	/**
	 * Creates a new instance of {@link ConversionRunnerBuilder},
	 * configures it with the import path of the corpus and 
	 * returns it.
	 * 
	 * @param corpusDirectoryPath {@link String} representation of the path where the corpus to import is located. 
	 * @return An instance of {@link ConversionRunnerBuilder} configured with the import path.
	 */
	public static ConversionRunnerBuilder withCorpusImportPath(String corpusDirectoryPath) {
		ConversionRunnerBuilder self = new ConversionRunnerBuilder();
		self.corpusDirectoryPath = corpusDirectoryPath;
		return self;
	}

	/**
	 * Builds a new instance of {@link ConversionRunner}, configured
	 * with the configurations of this instance of {@link ConversionRunnerBuilder}.
	 * 
	 * @return A readily configured {@link ConversionRunner}.
	 */
	public ConversionRunner build() {
		return new ConversionRunner(this.corpusDirectoryPath);
	}

}
