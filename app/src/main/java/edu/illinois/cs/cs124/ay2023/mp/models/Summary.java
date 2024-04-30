package edu.illinois.cs.cs124.ay2023.mp.models;

import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Model holding the course summary information shown in the summary list.
 *
 * @noinspection unused
 */
public class Summary implements Comparable<Summary> { // type can be compared to instances of itself
  private String subject;

  /**
   * Get the subject for this Summary.
   *
   * @return the subject for this Summary
   */
  @NotNull
  public final String getSubject() {
    return subject;
  }

  private String number;

  /**
   * Get the number for this Summary.
   *
   * @return the number for this Summary
   */
  @NotNull
  public final String getNumber() {
    return number;
  }

  private String label;

  /**
   * Get the label for this Summary.
   *
   * @return the label for this Summary
   */
  @NotNull
  public final String getLabel() {
    return label;
  }

  /** Create an empty Summary. */
  public Summary() {}

  /**
   * Create a Summary with the provided fields.
   *
   * @param setSubject the department for this Summary
   * @param setNumber the number for this Summary
   * @param setLabel the label for this Summary
   */
  public Summary(@NonNull String setSubject, @NonNull String setNumber, @NotNull String setLabel) {
    subject = setSubject;
    number = setNumber;
    label = setLabel;
  }

  /** {@inheritDoc} */
  @NonNull
  @Override
  public String toString() {
    return subject + " " + number + ": " + label;
  }

  @Override
  public int compareTo(Summary o) {
    // sort first by number then by subject
    if (number.compareTo(o.number) > 0) {
      return 1;
    } else if (number.compareTo(o.number) < 0) {
      return -1;
    } else {
      return subject.compareTo(o.subject);
    }
  }

  public static List<Summary> filter(List<Summary> summaryList, String str) {
    String filter = str.trim().toLowerCase();
    List<Summary> list = new ArrayList<>();
    for (int i = 0; i < summaryList.size(); i++) {
      String summary = summaryList.get(i).toString().trim().toLowerCase();
      if (summary.contains(filter)) {
        list.add(summaryList.get(i));
      }
    }
    Collections.sort(list);
    // sort summaries by position of the search term, with earlier matches appearing first
    list.sort(
        (summary1, summary2) -> {
          if (summary1.toString().trim().toLowerCase().indexOf(filter)
                  - summary2.toString().trim().toLowerCase().indexOf(filter)
              == 0) {
            return summary1.compareTo(summary2);
          } else {
            return summary1.toString().trim().toLowerCase().indexOf(filter)
                - summary2.toString().trim().toLowerCase().indexOf(filter);
          }
        });
    return list;
  }
}
