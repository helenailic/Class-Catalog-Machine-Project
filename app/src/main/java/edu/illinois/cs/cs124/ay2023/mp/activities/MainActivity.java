package edu.illinois.cs.cs124.ay2023.mp.activities;

import static edu.illinois.cs.cs124.ay2023.mp.helpers.Helpers.OBJECT_MAPPER;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.fasterxml.jackson.core.JsonProcessingException;
import edu.illinois.cs.cs124.ay2023.mp.R;
import edu.illinois.cs.cs124.ay2023.mp.adapters.SummaryListAdapter;
import edu.illinois.cs.cs124.ay2023.mp.application.CourseableApplication;
import edu.illinois.cs.cs124.ay2023.mp.helpers.ResultMightThrow;
import edu.illinois.cs.cs124.ay2023.mp.models.Summary;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/** Main activity showing the course summary list. */
public final class MainActivity extends AppCompatActivity
    implements SearchView.OnQueryTextListener {
  /** Tag to identify the MainActivity in the logs. */
  @SuppressWarnings("unused")
  private static final String TAG = MainActivity.class.getSimpleName();

  /** List of summaries received from the server, initially empty. */
  private List<Summary> summaries = Collections.emptyList();

  /** Adapter that connects our list of summaries with the list displayed on the display. */
  private SummaryListAdapter listAdapter;

  /**
   * Called when this activity is created.
   *
   * <p>This method is called when the activity is first launched, and at points later if the app is
   * terminated to save memory. For more details, see consult the Android activity lifecycle
   * documentation.
   *
   * @param unused saved instance state, currently unused and always empty or null
   */
  @Override
  protected void onCreate(@Nullable Bundle unused) {
    super.onCreate(unused);

    // Load this activity's layout and set the title
    setContentView(R.layout.activity_main); // R is reference into the directory
    setTitle("Search Courses");

    // Setup the list adapter for the list of summaries
    // when a course is clicked.. this method (summary) runs
    // do something with app after they click
    listAdapter =
        new SummaryListAdapter(
            summaries,
            this,
            summary -> {
              // context where activity launched, the activity to launch
              // serializing the summary object into a string
              try {
                Intent courseIntent = new Intent(this, CourseActivity.class);
                String summaryString = OBJECT_MAPPER.writeValueAsString(summary);
                courseIntent.putExtra("summary", summaryString);
                startActivity(courseIntent);
              } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
              }
              // add information to the intent (with extras)
              // add string extra to intent with key summary
              // courseactivity is blank... so open that up
              // goal: display a course when clicked (courseactivity activity)
              // add into to the intent so courseactivity knows what to do (pass main --> course
              // activity)
            });
    // where actual list of summaries is stored

    // Add the list to the layout
    // layout file describes components and their position
    // activity file makes behavior
    // populating list of summaries
    // responding to searchview user changes
    RecyclerView recyclerView = findViewById(R.id.recycler_view);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    recyclerView.setAdapter(listAdapter);

    // Initiate a request for the summary list
    CourseableApplication application =
        (CourseableApplication) getApplication(); // application hosts multiple activites
    application.getClient().getSummary(summaryCallback); // populating the UI list
    // mainactivity doesn't wait for server - callback pattern
    // asynch: when summaries ready, call this method (summaryCallback)

    // Register this component as a callback for changes to the search view component shown above
    // the summary list. We'll use these events to initiate summary list filtering.
    SearchView searchView = findViewById(R.id.search); // uses id to find it in layout

    // tells android when text changes, you call callback method
    // interface
    searchView.setOnQueryTextListener(this);

    // layout file describes how screen should look
    // activity grabs references for parts of ui to make them do things

    // Register our toolbar
    setSupportActionBar(findViewById(R.id.toolbar));
  }

  /** Callback used to update the list of summaries during onCreate. */
  private final Consumer<ResultMightThrow<List<Summary>>> summaryCallback =
      (result) -> {
        try {
          summaries = result.getValue();
          // good place to sort this list
          // can use comparable sort method since we implemented comparable with compareTo() method
          Collections.sort(summaries);
          listAdapter.setSummaries(summaries); // this is the actual list shown in ui (setting them)
        } catch (Exception e) {
          e.printStackTrace();
          Log.e(TAG, "getSummary threw an exception: " + e);
        }
      };

  // interface methods
  // event --> event handler for ui

  @Override
  public boolean onQueryTextSubmit(String query) {
    return false;
  }

  /**
   * Callback fired when the user edits the text in the search query box.
   *
   * <p>This fires every time the text in the search bar changes. We'll handle this by updating the
   * list of visible summaries.
   *
   * @param query the text to use to filter the summary list
   * @return true because we handled the action
   */
  @Override // called when searchbar text changes
  public boolean onQueryTextChange(@NonNull String query) {
    // update the list of courses shown
    // summaries is copy of list
    // when query... use filter method to filter list and rearrange
    List newSummaries = Summary.filter(summaries, query);
    // call to update the ui
    listAdapter.setSummaries(newSummaries);
    return true;
  }

  /**
   * Callback fired when the user submits a search query.
   *
   * <p>This would correspond to them hitting enter or a submit button. Because we update the list
   * on each change to the search value, we do not handle this callback.
   *
   * @param unused current query text
   * @return false because we did not handle this action
   */
}
