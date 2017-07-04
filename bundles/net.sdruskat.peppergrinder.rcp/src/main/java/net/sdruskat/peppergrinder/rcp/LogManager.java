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
package net.sdruskat.peppergrinder.rcp;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.internal.workbench.WorkbenchLogger;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * Provides simple logging by patching messages through to the default Workbench
 * logger.
 *
 * @author Stephan Druskat <[mail@sdruskat.net](mailto:mail@sdruskat.net)>
 * 
 */
@SuppressWarnings("restriction")
public class LogManager {

	public static final LogManager INSTANCE = new LogManager();
	private Logger logger;

	private LogManager() {
		Bundle bundle = FrameworkUtil.getBundle(LogManager.class);
		IEclipseContext context = EclipseContextFactory.getServiceContext(bundle.getBundleContext());
		this.logger = ContextInjectionFactory.make(WorkbenchLogger.class, context);
	}

	/**
	 * Logs the message at log level INFO.
	 * 
	 * @param message The message to log
	 */
	public void info(String message) {
		logger.info(message);
	}

	/**
	 * Logs the message at log level WARN.
	 * 
	 * @param message The message to log
	 */
	public void warn(String message) {
		logger.warn(message);
	}

	/**
	 * Logs the message at log level ERROR.
	 * 
	 * @param message The message to log
	 */
	public void error(String message) {
		logger.error(message);
	}

}
