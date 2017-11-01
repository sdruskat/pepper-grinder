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
package net.sdruskat.peppergrinder.rcp.parts;

import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import net.sdruskat.peppergrinder.rcp.conversion.ConversionRunner;
import net.sdruskat.peppergrinder.rcp.conversion.ConversionRunnerBuilder;
import net.sdruskat.peppergrinder.rcp.conversion.TraCESToANNISModuleRunnable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;

/**
 * A GUI part providing access to basic functionality.
 *
 * @author Stephan Druskat <[mail@sdruskat.net](mailto:mail@sdruskat.net)>
 * 
 */
public class ConversionWorkflowPart {
	
	private String corpusDirectoryPath = null;

	private Button btnBrowse;

	/**
	 * Constructs a simple GUI that lets the user
	 * browse for a directory containing corpus
	 * files and start the conversion of these
	 * corpus files via Pepper.
	 * 
	 * @param parent
	 */
	@PostConstruct
	public void createComposite(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true, 1, 1));
		
		Label lblBrowse = new Label(composite, SWT.NONE);
		lblBrowse.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		lblBrowse.setText("Choose the directory containing the corpus data you want to convert.");
		
		Label lblSelection = new Label(composite, SWT.NONE);
		lblSelection.setText("Current selection:");
		
		Label lblSelectedPath = new Label(composite, SWT.NONE);
		lblSelectedPath.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		lblSelectedPath.setText("--");
		
		btnBrowse = new Button(composite, SWT.NONE);
		btnBrowse.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		btnBrowse.setText("Browse");
		
		Button btnRun = new Button(composite, SWT.NONE);
		btnRun.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		btnRun.setText("Run conversion");
		btnRun.setEnabled(false);
		new Label(composite, SWT.NONE);
		
		btnBrowse.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				DirectoryDialog dirDialog = new DirectoryDialog(Display.getDefault().getActiveShell());
				corpusDirectoryPath = dirDialog.open();
				if (corpusDirectoryPath != null && !corpusDirectoryPath.isEmpty()) {
					btnRun.setEnabled(true);
					lblSelectedPath.setText(corpusDirectoryPath);
					lblSelectedPath.requestLayout();
					composite.requestLayout();
					parent.requestLayout();
					composite.requestLayout();
					lblSelectedPath.requestLayout();
				}
			}
		});

		btnRun.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				ConversionRunner runner = ConversionRunnerBuilder.withCorpusImportPath(corpusDirectoryPath).build();
				boolean outcome = runner.run();
				if (outcome) {
					MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Success!", "The conversion of corpus \"" + runner.getName() + "\" finished successfully.\n\n"
							+ "The ANNIS files are located in the folder './ANNIS-OUTPUT'.");

				}
				else {
					String failedDocs = runner.getFailedDocuments().stream()
					     .map(name -> "- " + name)
					     .collect(Collectors.joining("\n"));
					MessageDialog.openError(Display.getDefault().getActiveShell(), "Conversion error", "The following documents could not be converted successfully. Please check the log files for error messages.\n\n" + failedDocs);
				}
				
			}
		});
	}

	/**
	 * Sets the focus on the *Browse* button.
	 * 
	 */
	@Focus
	public void setFocus() {
		btnBrowse.setFocus();
	}

}