// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<TimeRange> mandatoryBusyTimes = new HashSet<>();
    Map<String, Set<TimeRange>> optionalBusyTimes = new HashMap<>();
    for (Event event : events) {
      TimeRange eventTime = event.getWhen();
      if (hasCommonAttendees(event.getAttendees(), request.getAttendees())) {
        boolean added = addToBusySet(mandatoryBusyTimes, eventTime);
        if (!added) {
          mandatoryBusyTimes.add(eventTime);
        }
      } else if (hasCommonAttendees(event.getAttendees(), request.getOptionalAttendees())) {
        Collection<String> commonAttendees = getCommonAttendees(event.getAttendees(), request.getOptionalAttendees());
        // Maps each optional attendee to a set of their busy times.
        for (String attendee : commonAttendees) {
          Set<TimeRange> times = optionalBusyTimes.getOrDefault(attendee, new HashSet<TimeRange>());
          boolean added = addToBusySet(times, eventTime);
          if (!added) {
            times.add(eventTime);
          }
          optionalBusyTimes.put(attendee, times);
        }
      }
    }

    List<TimeRange> mandatoryFreeTimes = findFreeTimes(mandatoryBusyTimes, request.getDuration());
    if (optionalBusyTimes.isEmpty()) {
      return mandatoryFreeTimes;
    }

    // At index i of optionalFreeTimes, i + 1 optional attendees can attend any of the given time ranges.
    List<List<TimeRange>> optionalFreeTimes = makeOptionalFreeTimesList(optionalBusyTimes, request.getDuration());
    
    List<TimeRange> bothFreeTimes;
    // Iterate back to front through the list so that time ranges with the max number of attendees are returned.
    for (int i = optionalFreeTimes.size() - 1; i >= 0; i--) {
      if (request.getAttendees().isEmpty() && !optionalFreeTimes.get(i).isEmpty()) {
        Collections.sort(optionalFreeTimes.get(i), TimeRange.ORDER_BY_START);
        return optionalFreeTimes.get(i);
      }
      bothFreeTimes = findIntersectionFreeTimes(mandatoryFreeTimes, optionalFreeTimes.get(i), request.getDuration());
      if (!bothFreeTimes.isEmpty()) {
        Collections.sort(bothFreeTimes, TimeRange.ORDER_BY_START);
        return bothFreeTimes;
      }
    }
    return mandatoryFreeTimes;
  }

  /**
   * Determines if two collections of attendees have any attendees in common.
   *
   * @param eventAttendees    a collection of attendees for a certain event
   * @param requestAttendees  a collection of the requested attendees for the meeting
   * @return                  whether or not the two collections have at least one attendee in common
   */
  private static boolean hasCommonAttendees(Collection<String> eventAttendees, Collection<String> requestAttendees) {
    return !Collections.disjoint(eventAttendees, requestAttendees);
  }

  /**
   * Finds all common attendees between two collections.
   *
   * @param eventAttendees    a collection of attendees for a certain event
   * @param requestAttendees  a collection of the requested attendees for the meeting
   * @return                  a collection of all the attendees that are in both collections
   */
  private static Collection<String> getCommonAttendees(Collection<String> eventAttendees, Collection<String> requestAttendees) {
    Collection<String> commonAttendees = new HashSet<>();
    for (String person : eventAttendees) {
      if (requestAttendees.contains(person)) {
        commonAttendees.add(person);
      }
    }
    return commonAttendees;
  }

  /**
   * Creates a nested list where the index i of each list represents that 
   * i + 1 optional attendees can attend the times listed at that index.
   *
   * @param optionalBusyTimesMap  maps each optional attendee's name to a set of the time ranges when they are busy
   * @param meetingDuration       length of the requested meeting
   * @return                      a nested list containing each time range at its corresponding index
   */
  private static List<List<TimeRange>> makeOptionalFreeTimesList(
      Map<String, Set<TimeRange>> optionalBusyTimesMap, long meetingDuration) {
    List<List<TimeRange>> optionalFreeTimes = new ArrayList<>();
    int numOptionalAttendees = optionalBusyTimesMap.size();
    for (int i = 0; i < numOptionalAttendees; i++) {
      optionalFreeTimes.add(new ArrayList<TimeRange>());
    }
    for (Map.Entry entry : optionalBusyTimesMap.entrySet()) {
      List<TimeRange> freeTimes = findFreeTimes((Collection<TimeRange>) entry.getValue(), meetingDuration);
      for (TimeRange freeRange : freeTimes) {
        addToFreeTimesList(optionalFreeTimes, freeRange, numOptionalAttendees, meetingDuration);
      }
    }
    return optionalFreeTimes;
  }

  /**
   * Adds a free time range to a nested list at the appropriate index.
   *
   * @param optionalFreeTimes     nested list to add the time range to
   * @param freeRange             time range to be added
   * @param numOptionalAttendees  total number of optional attendees
   * @param meetingDuration       length of the requested meeting
   */
  private static void addToFreeTimesList(
      List<List<TimeRange>> optionalFreeTimes, TimeRange freeRange, int numOptionalAttendees, long meetingDuration) {
    for (int i = numOptionalAttendees - 2; i >= 0; i--) {
      for (TimeRange existingRange : optionalFreeTimes.get(i)) {
        TimeRange intersection = freeRange.intersection(existingRange);
        if (intersection != null && intersection.duration() >= meetingDuration) {
          optionalFreeTimes.get(i + 1).add(intersection);
        }
      }
    }
    if (!optionalFreeTimes.isEmpty()) {
      optionalFreeTimes.get(0).add(freeRange);
    }
  }

  /**
   * Finds all intersections of time ranges that are at least as long as the requested meeting duration.
   *
   * @param mandatoryFreeTimes  list of time ranges when all mandatory attendees are free
   * @param optionalFreeTimes   list of times when optional attendees are free
   * @param meetingDuration     length of requested meeting
   * @return                    a list containing the intersections between the two lists
   */
  private static List<TimeRange> findIntersectionFreeTimes(
      List<TimeRange> mandatoryFreeTimes, List<TimeRange> optionalFreeTimes, long meetingDuration) {
    List<TimeRange> bothFreeTimes = new ArrayList<>();
    for (TimeRange optionalTime : optionalFreeTimes) {
      Iterator iterator = mandatoryFreeTimes.iterator();
      boolean keepGoing = true;
      while (keepGoing && iterator.hasNext()) {
        TimeRange mandatoryTime = (TimeRange) iterator.next();
        TimeRange intersection = optionalTime.intersection(mandatoryTime);
        if (intersection != null && intersection.duration() >= meetingDuration) {
          bothFreeTimes.add(intersection);
        }
        if (mandatoryTime.end() > optionalTime.end()) {
          keepGoing = false;
        }
      }
    }
    return bothFreeTimes;
  }

  /**
   * Merges a new time range with an overlapping time range in the collection, if possible.
   * 
   * @param busyTimes   collection to be altered that contains time intervals that can be merged
   * @param eventTime   new time range to be merged
   * @return            whether or not the time range was merged
   */
  private static boolean addToBusySet(Collection<TimeRange> busyTimes, TimeRange eventTime) {
    Iterator iterator = busyTimes.iterator();
    while (iterator.hasNext()) {
      TimeRange currRange = (TimeRange) iterator.next();
      if (currRange.contains(eventTime)) {
        return true;
      } else if (eventTime.contains(currRange)) {
        busyTimes.remove(currRange);
        busyTimes.add(eventTime);
        return true;
      } else if (currRange.overlaps(eventTime)) {
        if (currRange.contains(eventTime.start())) {
          busyTimes.remove(currRange);
          busyTimes.add(TimeRange.fromStartEnd(currRange.start(), eventTime.end(), /* inclusive= */ false));
        } else {
          busyTimes.remove(currRange);
          busyTimes.add(TimeRange.fromStartEnd(eventTime.start(), currRange.end(), /* inclusive= */ false));
        }
        return true;
      }
    }
    return false;
  }

  /**
   * Takes in a collection of busy time ranges to find all free time ranges based on duration of the meeting.
   * 
   * @param busyTimes        collection of busy time ranges
   * @param meetingDuration  length of requested meeting
   * @return                 a list containing all free time ranges for the meeting
   */
  private static List<TimeRange> findFreeTimes(Collection<TimeRange> busyTimes, long meetingDuration) {
    List<TimeRange> busyTimesList = new ArrayList<>(busyTimes);
    Collections.sort(busyTimesList, TimeRange.ORDER_BY_START);
    List<TimeRange> freeTimes = new ArrayList<>();
    int startTime = TimeRange.START_OF_DAY;
    int endTime;
    for (TimeRange busyTime : busyTimesList) {
      if (busyTime.start() - startTime >= meetingDuration) {
        endTime = busyTime.start();
        freeTimes.add(TimeRange.fromStartEnd(startTime, endTime, /* inclusive= */ false));
      }
      startTime = busyTime.end();
    }
    if (TimeRange.END_OF_DAY - startTime >= meetingDuration) {
      endTime = TimeRange.END_OF_DAY;
      freeTimes.add(TimeRange.fromStartEnd(startTime, endTime, /* inclusive= */ true));
    }
    return freeTimes;
  }
}