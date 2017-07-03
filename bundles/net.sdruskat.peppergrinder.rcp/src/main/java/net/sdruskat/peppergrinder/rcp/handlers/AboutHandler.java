package net.sdruskat.peppergrinder.rcp.handlers;

import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Bundle;

public class AboutHandler {
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
