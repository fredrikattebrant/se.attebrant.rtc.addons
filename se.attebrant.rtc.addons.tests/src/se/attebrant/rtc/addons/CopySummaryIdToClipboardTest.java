package se.attebrant.rtc.addons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import se.attebrant.rtc.addons.actions.CopySummaryIdToClipboardAction;

public class CopySummaryIdToClipboardTest {

  @Test
  public void testGetSummaryId() {

    String summary1 = "This is a short summary";
    int id1 = 12345;
    doTestGetSummaryId(summary1, id1);

    String summary2 = "This is a longer text which will be truncated since it "
        + "extends beyong the allowed max length";
    int id2 = 654321;
    doTestGetSummaryId(summary2, id2);
  }

  private void doTestGetSummaryId(String summary, int id) {
    String simple = summary + " : " + id;
    System.out.println("[" + simple + "] => " + simple.length());

    CopySummaryIdToClipboardAction action = new CopySummaryIdToClipboardAction();
    String result = action.truncateSummaryId(summary, id);

    System.out.println("Result: [" + result + "] => " + result.length());

    if (simple.length() <= CopySummaryIdToClipboardAction.MAX_LENGTH) {
      assertEquals(simple, result);
    } else {
      assertTrue(result.length() <= CopySummaryIdToClipboardAction.MAX_LENGTH);
    }
  }

}
