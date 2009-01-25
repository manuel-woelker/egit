/*******************************************************************************
 * Copyright (C) 2008, Manuel Woelker <manuel.woelker@gmail.com>
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * See LICENSE for the full license text, also available.
 *******************************************************************************/
package org.spearce.egit.core.op;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.revisions.Revision;
import org.eclipse.jface.text.revisions.RevisionInformation;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.team.ui.history.IHistoryView;
import org.eclipse.team.ui.history.RevisionAnnotationController;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.spearce.egit.core.project.RepositoryMapping;
import org.spearce.jgit.blame.BlameEngine;
import org.spearce.jgit.blame.BlameEntry;
import org.spearce.jgit.lib.AnyObjectId;
import org.spearce.jgit.lib.Repository;
import org.spearce.jgit.revwalk.RevCommit;

/** 
 * Displays blame annotations for a file
 * 
 * <p>
 * For each line a given file, computes which commit introduced the line. This information is
 * then presented via the eclipse annotation mechanism. These annotations are connected with the
 * history view via their respective selection providers. 
 * </p> 
 */
public class BlameOperation implements IWorkspaceRunnable {
	private static final String HISTORY_VIEW_ID = IHistoryView.VIEW_ID;

	private final DateFormat iso8601dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	/**
	 * Revision class for Git
	 * 
	 */
	class GitRevision extends Revision {

		protected final RevCommit revCommit;

		private RGB color;

		public void setColor(RGB color) {
			this.color = color;
		}

		public GitRevision(RevCommit revCommit) {
			this.revCommit = revCommit;
		}

		@Override
		public RGB getColor() {
			return color;
		}

		@Override
		public Date getDate() {
			return new Date(revCommit.getCommitTime());
		}

		@Override
		public String getAuthor() {
			return revCommit.getAuthorIdent().getName();
		}

		@Override
		public Object getHoverInfo() {
			StringBuilder sb = new StringBuilder();
			sb.append(MessageFormat.format("Commit: <b>{0}</b><br>", revCommit
					.name()));
			sb.append(MessageFormat.format(
					"Author: <b>{0}</b>&nbsp;&nbsp;&nbsp;", revCommit
							.getAuthorIdent().getName()));
			sb.append(MessageFormat.format("({0})<br>", iso8601dateFormat
					.format(revCommit.getAuthorIdent().getWhen())));
			sb.append(MessageFormat.format("{0}", revCommit.getShortMessage()));
			return sb.toString();
		}

		@Override
		public String getId() {
			return revCommit.name();
		}

	}

	/**
	 * Translate between history view and annotation ruler revisions
	 * 
	 */
	class GitRevisionAnnotationController extends RevisionAnnotationController {

		public GitRevisionAnnotationController(IWorkbenchPage page, IFile file,
				ISelectionProvider historyList) {
			super(page, file, historyList);
		}

		@Override
		protected Object getHistoryEntry(Revision selected) {
			if (selected instanceof GitRevision) {
				return ((GitRevision) selected).revCommit;
			}
			return null;
		}

		@Override
		protected String getRevisionId(Object historyEntry) {
			if (historyEntry instanceof RevCommit) {
				return ((RevCommit) historyEntry).name();
			}
			return null;
		}

	}

	private final IFile file;

	private final IWorkbenchPage workbenchPage;

	private HashMap<String, RGB> authorColorMap = new HashMap<String, RGB>();

	/**
	 * @param file
	 *            the file to compute and display blame annotations for
	 * @param workbenchPage
	 *            the current workbench page
	 */
	public BlameOperation(final IFile file, IWorkbenchPage workbenchPage) {
		this.file = file;
		this.workbenchPage = workbenchPage;
	}

	public void run(IProgressMonitor progressMonitor) throws CoreException {
		IProgressMonitor monitor = progressMonitor;
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("Determining blame", 15);
		IProject project = file.getProject();
		RepositoryMapping mapping = RepositoryMapping.getMapping(project);
		if (mapping == null)
			return;
		final String gitPath = mapping.getRepoRelativePath(file);
		final Repository repository = getRepository(file);
		BlameEngine blameEngine = new BlameEngine(repository);
		monitor.worked(5);
		// Perform actual blame
		List<BlameEntry> blame = blameEngine.blame(gitPath);
		monitor.worked(9);

		// Convert blame data
		final RevisionInformation revisionInformation = createRevisionInformation(blame);
		setAuthorColors(revisionInformation);
		IViewPart historyViewPart = workbenchPage.findView(HISTORY_VIEW_ID);
		final IHistoryView historyView = historyViewPart instanceof IHistoryView ? (IHistoryView) historyViewPart
				: null;
		// open editor in ui thread
		workbenchPage.getWorkbenchWindow().getShell().getDisplay().asyncExec(
				new Runnable() {

					public void run() {
						openBlameEditor(revisionInformation, historyView);
					}
				});
		monitor.done();

	}

	private RevisionInformation createRevisionInformation(List<BlameEntry> blame) {
		final RevisionInformation revisionInformation = new RevisionInformation();
		HashMap<AnyObjectId, GitRevision> commitMap = new HashMap<AnyObjectId, GitRevision>();
		for (BlameEntry blameEntry : blame) {
			GitRevision gitRevision = commitMap.get(blameEntry.suspect
					.getCommit().getId());
			if (gitRevision == null) {
				gitRevision = new GitRevision(blameEntry.suspect.getCommit());
				revisionInformation.addRevision(gitRevision);
				commitMap.put(blameEntry.suspect.getCommit().getId(),
						gitRevision);
				authorColorMap.put(gitRevision.getAuthor(), null);
			}
			gitRevision.addRange(new LineRange(blameEntry.originalRange
					.getStart(), blameEntry.originalRange.getLength()));

		}
		return revisionInformation;
	}

	private void setAuthorColors(final RevisionInformation revisionInformation) {
		// colors evenly spaced on color wheel
		float hueIncrement = 360.0f / authorColorMap.size();
		float hue = 0.0f;
		for (Entry<String, RGB> entry : authorColorMap.entrySet()) {
			entry.setValue(new RGB(hue, .4f, 0.9f));
			hue += hueIncrement;
		}
		for (Object revision : revisionInformation.getRevisions()) {
			GitRevision gitRevision = (GitRevision) revision;
			RGB rgb = authorColorMap.get(gitRevision.getAuthor());
			gitRevision.setColor(rgb);
		}
	}

	private Repository getRepository(IResource resource) {
		IProject project = resource.getProject();
		RepositoryMapping mapping = RepositoryMapping.getMapping(project);
		if (mapping != null)
			return mapping.getRepository();
		return null;
	}

	private void openBlameEditor(final RevisionInformation revisionInformation,
			final IHistoryView historyView) {
		try {
			AbstractDecoratedTextEditor editor = RevisionAnnotationController
					.openEditor(workbenchPage, file);
			editor
					.showRevisionInformation(revisionInformation,
							"org.spearce.egit.ui.internal.decorators.GitQuickDiffProvider");
			if (historyView != null) {
				new GitRevisionAnnotationController(workbenchPage, file,
						historyView.getHistoryPage().getHistoryPageSite()
								.getSelectionProvider());
			}
		} catch (PartInitException e) {
			throw new RuntimeException("Unable to open blame editor", e);
		}
	}

}
