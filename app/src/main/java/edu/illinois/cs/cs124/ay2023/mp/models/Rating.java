package edu.illinois.cs.cs124.ay2023.mp.models;

public class Rating {
  public static final float NOT_RATED = (float) -1.0; // is this supposed to have an f?
  private float rating = NOT_RATED; // float to be compatible with rating bar used
  private Summary summary;

  // empty constructor for compatibility with jackson
  public Rating() {}

  public Rating(Summary setSummary, float setRating) {
    rating = setRating;
    summary = setSummary;
  }

  public float getRating() {
    return rating;
  }

  public Summary getSummary() {
    return summary;
  }
}
