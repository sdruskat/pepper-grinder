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

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles closing of the workbench.
 *
 * @author Stephan Druskat <[mail@sdruskat.net](mailto:mail@sdruskat.net)>
 * 
 */
public class QuitHandler {
	
	private static final Logger log = LoggerFactory.getLogger(QuitHandler.class);
	
	/**
	 * Closes the workbench if prompted user agrees via dialog.
	 * 
	 * @param workbench
	 * @param shell
	 */
	@Execute
	public void execute(IWorkbench workbench, Shell shell) {
		if (MessageDialog.openConfirm(shell, "Confirmation", "Do you want to exit?")) {
			workbench.close();
			log.info("Goodbye.");
		}
	}
}
