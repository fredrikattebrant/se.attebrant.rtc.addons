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

/**
 * Action which copies Summary : ID to the {@link Clipboard} for selected {@link IWorkItemHandle}s.
 * The resulting text is truncated to a length of 72 characters (the summary part is shortened).
 *
 */
public class CopySummaryIdToClipboardAction implements IObjectActionDelegate {

  private ISelection selection;
  private IWorkbenchPart workbenchPart;
  private static final String NEWLINE = System.getProperty("line.separator");
  private static final String SEPARATOR = " : ";
  public static final String TRUNCATE_SEPARATOR = "..." + SEPARATOR;
  public static final int TRUNCATE_SEPARATOR_LENGTH = TRUNCATE_SEPARATOR.length();
  public static final int MAX_LENGTH = 72;

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
          String summary = workItem.getHTMLSummary().getPlainText();
          String summaryAndId = truncateSummaryId(summary, id);
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
  
  /**
   * Returns the a "summary : id" string for the given {@link IWorkItemHandle}.
   * Truncates the summary if the resulting string is longer that MAX_LENGTH. 
   */
  public String truncateSummaryId(String summary, int id) {
    String summaryAndId = summary + SEPARATOR + id;
    int length = summaryAndId.length();
    if (length > MAX_LENGTH) {
      String idSuffix = TRUNCATE_SEPARATOR + id;
      int remove = MAX_LENGTH - idSuffix.length();
      summaryAndId = summary.substring(0, remove) + idSuffix; 
    }
    return summaryAndId;
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
