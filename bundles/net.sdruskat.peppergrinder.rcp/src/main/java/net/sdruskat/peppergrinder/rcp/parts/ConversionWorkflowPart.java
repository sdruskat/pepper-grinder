package net.sdruskat.peppergrinder.rcp.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import net.sdruskat.peppergrinder.rcp.conversion.ConversionRunner;
import net.sdruskat.peppergrinder.rcp.conversion.ConversionRunnerBuilder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;

public class ConversionWorkflowPart {
	
	private String corpusDirectoryPath = null;

	@Inject
	private MDirtyable dirty;

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
		
		Button btnBrowse = new Button(composite, SWT.NONE);
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
				runner.run();
			}
		});
	}

	@Focus
	public void setFocus() {
//		tableViewer.getTable().setFocus();
	}

	@Persist
	public void save() {
		dirty.setDirty(false);
	}
	
}