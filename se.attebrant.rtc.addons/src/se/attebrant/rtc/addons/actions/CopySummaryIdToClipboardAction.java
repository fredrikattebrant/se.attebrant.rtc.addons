package se.attebrant.rtc.addons.actions;

import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.workitem.client.IAuditableClient;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.IWorkItemHandle;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import java.util.Iterator;

public class CopySummaryIdToClipboardAction implements IObjectActionDelegate {

  private ISelection selection;
  private IWorkbenchPart workbenchPart;
  private static final String NEWLINE = System.getProperty("line.separator"); 

  public CopySummaryIdToClipboardAction() {
    super();
  }

  @Override
  public void run(IAction action) {
    if (selection instanceof IStructuredSelection) {
      IStructuredSelection ssel = (IStructuredSelection) selection;
      @SuppressWarnings("rawtypes")
      Iterator iterator = ssel.iterator();
      StringBuffer summaryIds = new StringBuffer();
      while (iterator.hasNext()) {
        Object firstElement = iterator.next();
        if (firstElement instanceof IWorkItemHandle) {
          IWorkItemHandle workItemHandle = (IWorkItemHandle) firstElement;
          ITeamRepository teamRepository = (ITeamRepository) workItemHandle.getOrigin();
          IAuditableClient auditableClient =
              (IAuditableClient) teamRepository.getClientLibrary(IAuditableClient.class);
          IWorkItem workItem =
              auditableClient.findCachedAuditable(workItemHandle, IWorkItem.SMALL_PROFILE);
          int id = workItem.getId();
          String summaryAndId = workItem.getHTMLSummary().getPlainText() + " : " + id;
          if (summaryIds.length() > 0) {
            summaryIds.append(NEWLINE);
          }
          summaryIds.append(summaryAndId);
        }
      }
      if (summaryIds.length() > 0) {
        Display display;
        if (workbenchPart != null) {
          display = workbenchPart.getSite().getShell().getDisplay();
        } else {
          display = PlatformUI.getWorkbench().getDisplay();
        }
        Clipboard clipboard = new Clipboard(display);
        try {
          clipboard.setContents(new Object[] {summaryIds.toString()},
              new Transfer[] {TextTransfer.getInstance()});
        } finally {
          clipboard.dispose();
        }
      }
    }
  }

  @Override
  public void selectionChanged(IAction action, ISelection selection) {
    this.selection = selection;
    if (action != null) {
      action.setEnabled(isEnabled(selection));
    }
  }

  private boolean isEnabled(ISelection selection) {
    if (selection instanceof IStructuredSelection) {
      IStructuredSelection structuredSelection = ((IStructuredSelection) selection);
      for (@SuppressWarnings("rawtypes")
      Iterator iterator = structuredSelection.iterator(); iterator.hasNext();) {
        Object next = iterator.next();
        if (next instanceof IWorkItemHandle) {
          IWorkItemHandle handle = (IWorkItemHandle) next;
          IWorkItem workitem;
          if (handle.hasFullState()) {
            // new unsaved have always full state
            workitem = (IWorkItem) handle.getFullState();
            if (workitem.isNewItem()) {
              return false;
            }

          }
        } else {
          // selection contains something else than workitems
          return false;
        }
      }
      return !structuredSelection.isEmpty();
    }
    return false;
  }

  @Override
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    workbenchPart = targetPart;
  }

}
