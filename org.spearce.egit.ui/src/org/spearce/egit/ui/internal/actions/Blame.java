/*******************************************************************************
 * Copyright (C) 2008, Manuel Woelker <manuel.woelker@gmail.com>
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * See LICENSE for the full license text, also available.
 *******************************************************************************/
package org.spearce.egit.ui.internal.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.spearce.egit.core.op.BlameOperation;

/**
 * An action to show blame information for a tracked file.
 * 
 * @see BlameOperation
 */
@SuppressWarnings("restriction")
public class Blame extends RepositoryAction {

	@Override
	public void run(IAction action) {
		try {
			IResource[] selectedResources = getSelectedResources();
			System.out.println(selectedResources);
			IResource resource = selectedResources[0];
			if (resource instanceof IFile) {
				IFile file = (IFile) resource;
				IWorkbenchPage activePage = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();

				final BlameOperation op = new BlameOperation(file, activePage);
				getTargetPart().getSite().getWorkbenchWindow().run(true, false,
						new IRunnableWithProgress() {
							public void run(IProgressMonitor progressMonitor)
									throws InvocationTargetException,
									InterruptedException {
								try {
									op.run(progressMonitor);
								} catch (CoreException e) {
									MessageDialog.openError(getShell(),
											"Blame failed", e.getMessage());
								}
							}
						});
			}
		} catch (Throwable e) {
			MessageDialog.openError(getShell(), "Blame failed", e.getMessage());
		}
	}

	@Override
	public boolean isEnabled() {
		return getSelectedAdaptables(getSelection(), IFile.class).length == 1;

	}

}
