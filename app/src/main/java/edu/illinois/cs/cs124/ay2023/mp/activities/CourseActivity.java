package edu.illinois.cs.cs124.ay2023.mp.activities;

import static edu.illinois.cs.cs124.ay2023.mp.helpers.Helpers.OBJECT_MAPPER;

import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.fasterxml.jackson.core.JsonProcessingException;
import edu.illinois.cs.cs124.ay2023.mp.R;
import edu.illinois.cs.cs124.ay2023.mp.application.CourseableApplication;
import edu.illinois.cs.cs124.ay2023.mp.helpers.ResultMightThrow;
import edu.illinois.cs.cs124.ay2023.mp.models.Course;
import edu.illinois.cs.cs124.ay2023.mp.models.Rating;
import edu.illinois.cs.cs124.ay2023.mp.models.Summary;
import java.util.function.Consumer;

public class CourseActivity extends AppCompatActivity {
  // borrow stuff from mainactivity flow
  private Rating rating = new Rating();

  // oncreate method
  @Override
  protected void onCreate(@Nullable Bundle unused) {
    super.onCreate(unused);

    // Load this activity's layout and set the title
    setContentView(R.layout.activity_course);

    // set up our ui (make linkages between this code and ui elements, like m.a.)
    TextView titleTextView = findViewById(R.id.title);
    TextView descriptionTextView = findViewById(R.id.description);
    RatingBar ratingBar = findViewById(R.id.rating);

    runOnUiThread(
        () -> {
          titleTextView.setText("Title Test");
          descriptionTextView.setText("Testing 123");
        });

    // retrieve the intent that started this activity, get the summary, deserialize
    String summaryString = getIntent().getStringExtra("summary");
    try {
      Summary summary = OBJECT_MAPPER.readValue(summaryString, Summary.class);

      Consumer<ResultMightThrow<Course>> courseCallback =
          (result) -> {
            try {
              Course course = result.getValue();
              titleTextView.setText(course.toString());
              descriptionTextView.setText(course.getDescription());
            } catch (Exception e) {
              e.printStackTrace();
            }
          };

      // once we have summary, make the request using the client for course details (getCourse())
      // like m.a. making request to use client, use callback

      Consumer<ResultMightThrow<Rating>> ratingCallback =
          // don't need much data from the callback
          (result) -> {
            try {
              rating = result.getValue();
              ratingBar.setRating(rating.getRating());
            } catch (Exception e) {
              e.printStackTrace();
            }
          };

      CourseableApplication application = (CourseableApplication) getApplication();
      application.getClient().getCourse(summary, courseCallback);
      application.getClient().getRating(summary, ratingCallback);

      ratingBar.setOnRatingBarChangeListener(
          new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float ratings, boolean fromUser) {
              Rating temp = new Rating(summary, ratings);
              application.getClient().postRating(temp, ratingCallback);
            }
          });
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
