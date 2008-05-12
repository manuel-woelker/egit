/*
 *  Copyright (C) 2008  Roger C. Soares
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License, version 2.1, as published by the Free Software Foundation.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 */
package org.spearce.egit.ui.internal.clone;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.spearce.egit.core.op.CloneOperation;
import org.spearce.egit.core.project.GitProjectData;
import org.spearce.egit.ui.Activator;
import org.spearce.egit.ui.UIText;
import org.spearce.jgit.lib.Constants;
import org.spearce.jgit.lib.Ref;
import org.spearce.jgit.lib.Repository;
import org.spearce.jgit.transport.RefSpec;
import org.spearce.jgit.transport.RemoteConfig;
import org.spearce.jgit.transport.URIish;

/**
 * Import Git Repository Wizard. A front end to a git clone operation.
 */
public class GitCloneWizard extends Wizard implements IImportWizard {
	private static final String HEADS_PREFIX = Constants.HEADS_PREFIX;

	private static final String REMOTES_PREFIX_S = Constants.REMOTES_PREFIX
			+ "/";

	private CloneSourcePage cloneSource;

	private SourceBranchPage validSource;

	private CloneDestinationPage cloneDestination;

	public void init(IWorkbench arg0, IStructuredSelection arg1) {
		setWindowTitle(UIText.GitCloneWizard_title);
		cloneSource = new CloneSourcePage();
		validSource = new SourceBranchPage(cloneSource);
		cloneDestination = new CloneDestinationPage(cloneSource, validSource);
	}

	@Override
	public void addPages() {
		addPage(cloneSource);
		addPage(validSource);
		addPage(cloneDestination);
	}

	@Override
	public boolean performFinish() {
		final URIish uri;
		final Repository db;
		final RemoteConfig origin;

		try {
			uri = cloneSource.getURI();
		} catch (URISyntaxException e) {
			return false;
		}

		final File workdir = cloneDestination.getDestinationFile();
		final String branch = cloneDestination.getInitialBranch();
		final File gitdir = new File(workdir, ".git");
		try {
			db = new Repository(GitProjectData.getWindowCache(), gitdir);
			db.create();
			db.writeSymref(Constants.HEAD, branch);

			final String rn = cloneDestination.getRemote();
			origin = new RemoteConfig(db.getConfig(), rn);
			origin.addURI(uri);

			final String dst = REMOTES_PREFIX_S + origin.getName();
			RefSpec wcrs = new RefSpec();
			wcrs = wcrs.setForceUpdate(true);
			wcrs = wcrs.setSourceDestination(HEADS_PREFIX + "/*", dst + "/*");

			if (validSource.isAllSelected()) {
				origin.addFetchRefSpec(wcrs);
			} else {
				for (final Ref ref : validSource.getSelectedBranches())
					if (wcrs.matchSource(ref))
						origin.addFetchRefSpec(wcrs.expandFromSource(ref));
			}

			origin.update(db.getConfig());
			db.getConfig().save();
		} catch (IOException err) {
			Activator.logError(UIText.GitCloneWizard_failed, err);
			ErrorDialog.openError(getShell(), getWindowTitle(),
					UIText.GitCloneWizard_failed, new Status(IStatus.ERROR,
							Activator.getPluginId(), 0, err.getMessage(), err));
			return false;
		} catch (URISyntaxException e) {
			return false;
		}

		final CloneOperation op = new CloneOperation(db, origin, branch);
		final Job job = new Job(NLS.bind(UIText.GitCloneWizard_jobName, uri
				.toString())) {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				try {
					op.run(monitor);
					if (monitor.isCanceled()) {
						db.close();
						delete(workdir);
						return Status.CANCEL_STATUS;
					}
					return Status.OK_STATUS;
				} catch (InvocationTargetException e) {
					Throwable thr = e.getCause();
					return new Status(IStatus.ERROR, Activator.getPluginId(),
							0, thr.getMessage(), thr);
				}
			}
		};
		job.setUser(true);
		job.schedule();
		return true;
	}

	private static void delete(final File d) {
		if (d.isDirectory()) {
			final File[] items = d.listFiles();
			if (items != null) {
				for (final File c : items)
					delete(c);
			}
		}
		d.delete();
	}
}