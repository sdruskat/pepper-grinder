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
package net.sdruskat.peppergrinder.rcp.conversion;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.corpus_tools.pepper.common.DOCUMENT_STATUS;
import org.corpus_tools.pepper.common.Pepper;
import org.corpus_tools.pepper.common.PepperJob;
import org.corpus_tools.pepper.common.StepDesc;
import org.corpus_tools.pepper.connectors.PepperConnector;
import org.corpus_tools.pepper.core.PepperJobImpl;
import org.corpus_tools.pepper.modules.DocumentController;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Runnable} for Pepper modules.
 *
 * @author Stephan Druskat <[mail@sdruskat.net](mailto:mail@sdruskat.net)>
 * 
 */
public abstract class PepperModuleRunnable implements IRunnableWithProgress, Future<Boolean> {
	
	private static final Logger log = LoggerFactory.getLogger(PepperModuleRunnable.class);
	
	protected static final AtomicInteger threadCounter = new AtomicInteger();

	protected final boolean cancelable;

	protected final Object cancelLock = new Object();

	protected Semaphore semaphore = new Semaphore(0);
	protected Boolean outcome = Boolean.TRUE;
	protected Throwable throwable = null;
	protected volatile boolean cancelled = false;
	protected volatile boolean done = false;
	protected volatile Thread controlThread = null;
	protected volatile Thread moduleThread = null;

	private PepperConnector pepper;

	protected String corpusDirectoryPath;

	private List<String> failedDocuments;

	/**
	 * @param pepperWizard
	 * @param project
	 * @param cancelable
	 * @param pepperConnector 
	 * @param corpusDirectoryPath 
	 */
	public PepperModuleRunnable(/*AbstractPepperWizard pepperWizard, IProject project, */boolean cancelable, PepperConnector pepperConnector, String corpusDirectoryPath) {
//		this.pepperWizard = pepperWizard;
		this.pepper = pepperConnector;
		this.cancelable = cancelable;
		this.corpusDirectoryPath = corpusDirectoryPath;
	}

	protected abstract StepDesc createImporterParams();
	
	protected abstract StepDesc createManipulatorParams();

	protected abstract StepDesc createExporterParams();

	/**
	 * Creates and starts a Pepper job. The job is created via {@link AbstractPepperWizard#getPepper()}.
	 * 
	 * @throws IOException
	 * @throws CoreException
	 */
	protected void runPepper() throws IOException, CoreException {
		Pepper pepper = this.pepper;
		String jobId = pepper.createJob();
		PepperJob pepperJob = pepper.getJob(jobId);
		pepperJob.addStepDesc(createImporterParams());
		pepperJob.addStepDesc(createManipulatorParams());
		pepperJob.addStepDesc(createExporterParams());
		pepperJob.convert(); // TODO: CONVERT FROM
		if (pepperJob instanceof PepperJobImpl) {
			PepperJobImpl pji = (PepperJobImpl) pepperJob;
			List<DocumentController> docCtrls = pji.getDocumentControllers();
			failedDocuments = new ArrayList<>();
			for (DocumentController c : pji.getDocumentControllers()) {
				if (c.getGlobalStatus() == DOCUMENT_STATUS.DELETED) {
					outcome = false;
					failedDocuments.add(c.getDocument().getName());
					log.error("Document {} failed to convert successfully.", c.getDocument().getName());
				}
			}
				
		}
	}

	/*
	 * @copydoc @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		try {
			synchronized (cancelLock) {
				// Check if run has been cancelled before start
				if (cancelled) {
					throw new InterruptedException();
				}
				else {
					controlThread = Thread.currentThread();
					// Thread cancellable via input in the ProgressMonitorDialog
					moduleThread = new Thread("Pepper Module Thread #" + threadCounter.incrementAndGet()) {
						@Override
						public void run() {
							try {
								runPepper();
							}
							catch (IOException X) {
								throw new RuntimeException(X);
							}
							catch (CoreException X) {
								throw new RuntimeException(X);
							}
						}
					};
				}
			}

			// Asynchronously monitor ProgressMonitor for cancellations
			ScheduledFuture<?> cancellationCheck;
			if (cancelable) {
				// Monitoring thread
				cancellationCheck = Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new Runnable() {
					@Override
					public void run() {
						if (monitor.isCanceled()) {
							cancel(true);
						}
					}
				}, 500, 500, TimeUnit.MILLISECONDS);
			}
			else {
				cancellationCheck = null;
			}

			// Start monitor
			monitor.beginTask("Converting corpus ...", IProgressMonitor.UNKNOWN);
//			outcome = Boolean.FALSE;
			try {
				// Run module
				moduleThread.start();

				Display display = Display.findDisplay(Thread.currentThread());
				if (display == null) {
					moduleThread.join();
				}
				else {
					// Current thread is the UI Thread
					while (!isCancelled() && moduleThread.isAlive()) {
						if (controlThread.isInterrupted()) {
							throw new InterruptedException();
						}

						if (!display.readAndDispatch()) {
							display.sleep();
						}
					}
				}
//				outcome = Boolean.TRUE;
			}
			finally {
				// Stop monitor
				monitor.done();

				// Stop monitor thread
				if (cancellationCheck != null) {
					cancellationCheck.cancel(true);
				}

				// Signal cancellation
				if (Thread.currentThread().isInterrupted()) {
					throw new InterruptedException();
				}
			}
		}
		catch (InterruptedException X) {
			new MessageDialog(Display.getCurrent().getActiveShell(), "Error", null, "Some error occured while running Pepper and unfortunately Pepper is not stoppable anymore. Letting it run wild.", MessageDialog.ERROR, new String[] { IDialogConstants.OK_LABEL }, 0).open();
		}
		catch (Throwable T) {
			throw new InvocationTargetException(throwable = T);
		}
		finally {
			moduleThread = null;
			controlThread = null;
			done = true;
			semaphore.release(Integer.MAX_VALUE);
		}
	}

	/* 
	 * @copydoc @see java.util.concurrent.Future#cancel(boolean)
	 */
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		if (cancelable) {
			synchronized (cancelLock) {
				if (!cancelled) {
					Thread thread = controlThread;
					if (thread != null) {
						if (mayInterruptIfRunning) {
							thread.interrupt();
							cancelled = true;
						}
					}
					else {
						cancelled = true;
					}
				}

				return cancelled;
			}
		}
		else {
			return false;
		}
	}

	/* 
	 * @copydoc @see java.util.concurrent.Future#isCancelled()
	 */
	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	/* 
	 * @copydoc @see java.util.concurrent.Future#isDone()
	 */
	@Override
	public boolean isDone() {
		return done;
	}

	/**
	 * Returns the result as boolean.
	 *
	 * @return the result
	 * @throws ExecutionException
	 */
	protected Boolean getOutcome() throws ExecutionException {
		if (throwable != null) {
			throw new ExecutionException(throwable);
		}
		else {
			return outcome;
		}
	}

	/* 
	 * @copydoc @see java.util.concurrent.Future#get()
	 */
	@Override
	public Boolean get() throws InterruptedException, CancellationException, ExecutionException {
		if (cancelled) {
			throw new CancellationException();
		}
		else {
			semaphore.acquire();
			try {
				return getOutcome();
			}
			finally {
				semaphore.release();
			}
		}
	}

	/* 
	 * @copydoc @see java.util.concurrent.Future#get(long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, CancellationException, ExecutionException, TimeoutException {
		if (cancelled) {
			throw new CancellationException();
		}
		else {
			if (semaphore.tryAcquire(timeout, unit)) {
				try {
					return getOutcome();
				}
				finally {
					semaphore.release();
				}
			}
			else {
				return null;
			}
		}
	}

	/**
	 * @return the failedDocuments
	 */
	public final List<String> getFailedDocuments() {
		return failedDocuments;
	}

}
