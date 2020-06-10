package com.google.sps.data;

import java.util.Date;
import java.util.List;

/** Class containing information about a comment left on the site. */
public class Comment {

  private final String email;
  private final String name;
  private final String content;
  private int numLikes;
  private final List<String> userLikes;
  private final Date timestamp;
  private final String key;

  /**
   * Constructs a Comment.
   *
   * @param name name of person who posted comment
   * @param content message left in the comment
   * @param numLikes current number of likes the comment has
   * @param timestamp time that comment was posted
   * @param key unique identifier for the comment
   */
  public Comment(String email, String name, String content, int numLikes, List<String> userLikes, Date timestamp, String key) {
    this.email = email;
    this.name = name;
    this.content = content;
    this.numLikes = numLikes;
    this.userLikes = userLikes;
    this.timestamp = timestamp;
    this.key = key;
  }
}
