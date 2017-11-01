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
package net.sdruskat.peppergrinder.rcp.handlers;

import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Handles the About menu entry.
 *
 * @author Stephan Druskat <[mail@sdruskat.net](mailto:mail@sdruskat.net)>
 * 
 */
public class AboutHandler {
	/**
	 * Shows a simple about message in a {@link MessageDialog}.
	 * 
	 * @param shell
	 */
	@Execute
	public void execute(Shell shell) {
		String version = Platform.getProduct().getDefiningBundle().getHeaders().get("Bundle-Version");
		MessageDialog.openInformation(shell, "About", "Pepper Grinder (TraCES Edition) version " + version + "\n\nCopyright (c) 2017 Stephan Druskat\n"
				+ "Exploitation rights for this version belong exclusively to Universität Hamburg\n\n"
				+ "Licensed under the Apache License, Version 2.0 (the \"License\");\n"
				+ "you may not use this file except in compliance with the License.\n"
				+ "You may obtain a copy of the License at\n\n"
				+ "    http://www.apache.org/licenses/LICENSE-2.0\n\n"
				+ "Unless required by applicable law or agreed to in writing, software\n"
				+ "distributed under the License is distributed on an \"AS IS\" BASIS,\n"
				+ "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"
				+ "See the License for the specific language governing permissions and\n"
				+ "limitations under the License.");
	}
}
