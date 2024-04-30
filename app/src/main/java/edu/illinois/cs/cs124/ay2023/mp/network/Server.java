package edu.illinois.cs.cs124.ay2023.mp.network;

import static edu.illinois.cs.cs124.ay2023.mp.helpers.Helpers.CHECK_SERVER_RESPONSE;
import static edu.illinois.cs.cs124.ay2023.mp.helpers.Helpers.OBJECT_MAPPER;

import androidx.annotation.NonNull;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import edu.illinois.cs.cs124.ay2023.mp.application.CourseableApplication;
import edu.illinois.cs.cs124.ay2023.mp.models.Course;
import edu.illinois.cs.cs124.ay2023.mp.models.Rating;
import edu.illinois.cs.cs124.ay2023.mp.models.Summary;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * Development course API server.
 *
 * <p>Normally you would run this server on another machine, which the client would connect to over
 * the internet. For the sake of development, we're running the server alongside the app on the same
 * device. However, all communication between the course API client and course API server is still
 * done using the HTTP protocol. Meaning that it would be straightforward to move this code to an
 * actual server, which could provide data for all course API clients.
 */
public final class Server extends Dispatcher {
  /** List of summaries as a JSON string. */
  private final String summariesJSON;

  private Map<String, String> ratings = new HashMap<>();

  /** Helper method to create a 200 HTTP response with a body. */
  private MockResponse makeOKJSONResponse(@NonNull String body) {
    return new MockResponse()
        .setResponseCode(HttpURLConnection.HTTP_OK)
        .setBody(body)
        .setHeader("Content-Type", "application/json; charset=utf-8");
  }

  /** Helper value storing a 404 Not Found response. */
  private static final MockResponse HTTP_NOT_FOUND =
      new MockResponse()
          .setResponseCode(HttpURLConnection.HTTP_NOT_FOUND)
          .setBody("404: Not Found");

  /** Helper value storing a 400 Bad Request response. */
  private static final MockResponse HTTP_BAD_REQUEST =
      new MockResponse()
          .setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)
          .setBody("400: Bad Request");

  /** GET the JSON with the list of course summaries. */
  private MockResponse getSummaries() {
    return makeOKJSONResponse(summariesJSON);
  }

  /**
   * @noinspection checkstyle:EmptyBlock, checkstyle:MagicNumber
   */
  private MockResponse getCourse(String path) throws JsonProcessingException {
    String[] pathParts = path.split("/");

    if (pathParts.length < (2 * 2)) {
      return HTTP_BAD_REQUEST;
    }

    String subject = pathParts[2];
    String number = pathParts[3];

    // calls loadCourse() function with path
    List<Course> courseList = loadCourseData();
    // if (pathParts.length <= 3)
    for (int i = 0; i < courseList.size(); i++) {
      if (courseList.get(i).getSubject().equals(subject)
          && courseList.get(i).getNumber().equals(number)) {
        String result =
            OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(courseList.get(i));
        return makeOKJSONResponse(result);
      }
    }
    return HTTP_NOT_FOUND;
    // return different info based on path (which course is being requested)
  }

  // test0 getRating in the SERVER
  // pattern after mp2 getCourse route
  // rating/CS/100 path support pattern
  // what should come back from that is a rating
  // initially everything is not_rated!
  private MockResponse getRating(String path) throws JsonProcessingException {
    String[] pathParts = path.split("/");

    if (pathParts.length < (2 * 2)) {
      return HTTP_BAD_REQUEST;
    }

    String subject = pathParts[2];
    String number = pathParts[3];

    // loaddata populated map
    if (!(ratings.containsKey(subject + number))) {
      return HTTP_NOT_FOUND;
    }
    return makeOKJSONResponse(ratings.get(subject + number));
    // return different info based on path (which course is being requested)
  }

  private MockResponse postRating(RecordedRequest request) throws JsonProcessingException {
    // you can only call this method once - save the data!
    // body is the info you want to send with request (Rating class)
    String body =
        request.getBody().readUtf8(); // the request has a serialized summary field and rating value
    // use this to update the information
    try { // if the rating in the rating is BOGUS must do SOMETHING... for now in try catch
      Rating rating = OBJECT_MAPPER.readValue(body, Rating.class);
      // save the rating so that when getRating() is called, when that course requested, returns NEW
      // rating value
      // to do this will need to make changes to go back and change getRating()
      // the rating is rating.getRating()
      // the course is rating.getSummary()
      String key = rating.getSummary().getSubject() + rating.getSummary().getNumber();
      if (!(ratings.containsKey(key))) {
        return HTTP_NOT_FOUND;
      }

      // when you serialize, need to put whole ratings object in (can't serialize a float
      ratings.put(key, OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(rating));

      // am i supposed to edit get rating somehow?

      // doing redirect
      String ratingPath =
          "/rating/" + rating.getSummary().getSubject() + "/" + rating.getSummary().getNumber();
      // code tells client when response comes back, go to this and do get request
      return new MockResponse()
          .setResponseCode(HttpURLConnection.HTTP_MOVED_TEMP)
          .setHeader("Location", ratingPath); // name and where you want client to go

    } catch (JsonProcessingException e) {
      return HTTP_BAD_REQUEST;
    }
  }

  /**
   * HTTP request dispatcher.
   *
   * <p>This method receives HTTP requests from clients and determines how to handle them, based on
   * the request path and method.
   */
  @NonNull
  @Override
  public MockResponse dispatch(
      @NonNull RecordedRequest request) { // gets info about request client is trying to make
    // server = thing client connects to to retrieve data
    // client = thing that requests the data (web browser is http client)
    try {
      // Reject requests without a path or method
      if (request.getPath() == null || request.getMethod() == null) {
        return HTTP_BAD_REQUEST;
      }

      // Normalize trailing slashes and method
      String path = request.getPath().replaceAll("/+", "/");
      String method = request.getMethod().toUpperCase();

      // Main dispatcher tree tree
      // changes the path a bit
      if (path.equals("/") && method.equals("GET")) {
        // Used by API client to validate server
        return new MockResponse()
            .setBody(
                CHECK_SERVER_RESPONSE) // checks whether connected to right web server, app runs a
            // WEB SERVER on port 8023
            .setResponseCode(HttpURLConnection.HTTP_OK);
      } else if (path.equals("/reset/") && method.equals("GET")) {
        // Used to reset the server during testing
        for (String key : ratings.keySet()) {
          // i have a map of String class, String rating where rating is serialized rating object
          // put new rating object (with default) for each value
          Summary summary = OBJECT_MAPPER.readValue(ratings.get(key), Rating.class).getSummary();
          Rating rating = new Rating(summary, Rating.NOT_RATED);
          String ratingJSON =
              OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(rating);

          ratings.put(key, ratingJSON);
        }
        return new MockResponse().setBody("200: OK").setResponseCode(HttpURLConnection.HTTP_OK);
      } else if (path.startsWith("/course/")) { // all requests for course details will do this
        return getCourse(path);
      } else if (path.startsWith("/rating/") && method.equals("GET")) {
        return getRating(path);
      } else if (path.equals("/rating/") && method.equals("POST")) {
        return postRating(request);
      } else if (path.equals("/summary/") && method.equals("GET")) {
        // returns the summary list after it gets it (from client?)
        // dataflow: main activity calls to retrieve summaries, client retrieves them
        // the summaries
        return getSummaries(); // returns from json into summary
        // /course/CS/100 should have succeeded --> with details about course but didn't (should
        // send back json object cs100
      } else { // path doesn't match others... so it falls into this else
        // this path will change based on the COURSE!!!
        // Default is not found
        return HTTP_NOT_FOUND;
      }
    } catch (Exception e) {
      // Log an error and return 500 if an exception is thrown
      e.printStackTrace();
      return new MockResponse()
          .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
          .setBody("500: Internal Error");
    }
  }

  /**
   * Start the server if has not already been started, and wait for startup to finish.
   *
   * <p>Done in a separate thread to avoid blocking the UI.
   */
  public static void start() {
    if (isRunning(false)) {
      return;
    }
    new Server();
    if (!isRunning(true)) {
      throw new IllegalStateException("Server should be running");
    }
  }

  /** Number of times to check the server before failing. */
  private static final int RETRY_COUNT = 8;

  /** Delay between retries. */
  private static final int RETRY_DELAY = 512;

  /**
   * Determine if the server is currently running.
   *
   * @param wait whether to wait or not
   * @return whether the server is running or not
   * @throws IllegalStateException if something else is running on our port
   */
  public static boolean isRunning(boolean wait) {
    return isRunning(wait, RETRY_COUNT, RETRY_DELAY);
  }

  public static boolean isRunning(boolean wait, int retryCount, long retryDelay) {
    for (int i = 0; i < retryCount; i++) {
      OkHttpClient client = new OkHttpClient();
      Request request = new Request.Builder().url(CourseableApplication.SERVER_URL).get().build();
      try (Response response = client.newCall(request).execute()) {
        if (response.isSuccessful()) {
          if (Objects.requireNonNull(response.body()).string().equals(CHECK_SERVER_RESPONSE)) {
            return true;
          } else {
            throw new IllegalStateException(
                "Another server is running on port " + CourseableApplication.DEFAULT_SERVER_PORT);
          }
        }
      } catch (IOException ignored) {
        if (!wait) {
          break;
        }
        try {
          Thread.sleep(retryDelay);
        } catch (InterruptedException ignored1) {
        }
      }
    }
    return false;
  }

  /**
   * Reset the server. Used to reset the server between tests.
   *
   * @noinspection UnusedReturnValue, unused
   */
  public static boolean reset() throws IOException {
    OkHttpClient client = new OkHttpClient();
    Request request =
        new Request.Builder().url(CourseableApplication.SERVER_URL + "/reset/").get().build();
    try (Response response = client.newCall(request).execute()) {
      return response.isSuccessful();
    }
  }

  private Server() {
    // Disable server logging, since this is a bit verbose
    Logger.getLogger(MockWebServer.class.getName()).setLevel(Level.OFF);

    // Load data used by the server
    summariesJSON = loadData();

    try {
      // This resource needs to outlive the try-catch
      //noinspection resource
      MockWebServer server = new MockWebServer();
      server.setDispatcher(this);
      server.start(CourseableApplication.DEFAULT_SERVER_PORT);
    } catch (IOException e) {
      e.printStackTrace();
      throw new IllegalStateException(e.getMessage());
    }
  }

  /**
   * Helper method to load data used by the server.
   *
   * @return the summary JSON string.
   */
  private String loadData() { // loads all the courses from the json file
    // server makes the courses AVAILABLE to the client through JSON!!! (ITS A STRING STILL)

    // Load the JSON string
    //noinspection UnresolvedClassReferenceRepair
    String json =
        new Scanner(Server.class.getResourceAsStream("/courses.json"), "UTF-8")
            .useDelimiter("\\A")
            .next();

    // Build the list of summaries
    List<Summary> summaries =
        new ArrayList<>(); // building arraylist of summaries doesn't include description but its in
    // json
    try {
      // Iterate through the list of JsonNodes returned by deserialization
      JsonNode nodes = OBJECT_MAPPER.readTree(json);
      for (JsonNode node : nodes) { // goes through all nodes in json list
        // save this info for later
        // Deserialize as Summary and add to the list
        Summary summary =
            OBJECT_MAPPER.readValue(node.toString(), Summary.class); // deserialize as course
        summaries.add(summary);

        Rating rating = new Rating(summary, Rating.NOT_RATED);
        String ratingJSON =
            OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(rating);

        ratings.put(summary.getSubject() + summary.getNumber(), ratingJSON);
      }
      // Convert the List<Summary> to a String and return it
      return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(summaries);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  private List<Course> loadCourseData() { // load all course data
    String json =
        new Scanner(Server.class.getResourceAsStream("/courses.json"), "UTF-8")
            .useDelimiter("\\A")
            .next();

    List<Course> courses = new ArrayList<>(); // building arraylist of courses includes description
    try {
      // Iterate through the list of JsonNodes returned by deserialization
      JsonNode nodes = OBJECT_MAPPER.readTree(json);
      for (JsonNode node : nodes) {
        // Deserialize as Class and see if its same as path
        Course course =
            OBJECT_MAPPER.readValue(node.toString(), Course.class); // deserialize as course
        courses.add(course);
      }
      return courses;
      // Convert the List<Summary> to a String and return it
      // I don't need this part right? --> return
      // OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(course);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }
}
