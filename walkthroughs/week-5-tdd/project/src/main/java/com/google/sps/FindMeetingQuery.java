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
import java.util.PriorityQueue;
import java.util.Set;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    List<TimeRange> freeTimes = new ArrayList<>();
    // Check for edge cases.
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return freeTimes;
    }
    if (events.isEmpty() || request.getAttendees().isEmpty() && request.getOptionalAttendees().isEmpty()) {
      freeTimes.add(TimeRange.WHOLE_DAY);
      return freeTimes;
    }
    
    PriorityQueue<Event> orderedEvents = new PriorityQueue<>(Event.ORDER_BY_START);
    orderedEvents.addAll(events);
    List<TimeRange> busyTimes = findBusyIntervals(orderedEvents, request.getAttendees());
    // Restore the PQ.
    orderedEvents.addAll(events);
    Map<String, List<TimeRange>> individualBusyIntervals = findIndividualBusyIntervals(orderedEvents, request.getOptionalAttendees());

    List<List<TimeRange>> optionalFreeList = makeOptionalFreeTimesList(individualBusyIntervals, request.getDuration());
    freeTimes = findFreeIntervals(busyTimes, request.getDuration());

    List<TimeRange> bothFreeTimes;
    // Iterate back to front through the list so that time ranges with the max number of attendees are returned.
    for (int i = optionalFreeList.size() - 1; i >= 0; i--) {
      if (request.getAttendees().isEmpty() && !optionalFreeList.get(i).isEmpty()) {
        Collections.sort(optionalFreeList.get(i), TimeRange.ORDER_BY_START);
        return optionalFreeList.get(i);
      }
      bothFreeTimes = findIntersectionFreeTimes(freeTimes, optionalFreeList.get(i), request.getDuration());
      if (!bothFreeTimes.isEmpty()) {
        Collections.sort(bothFreeTimes, TimeRange.ORDER_BY_START);
        return bothFreeTimes;
      }
    }
    return freeTimes;
  }

  /**
   * Determines if two collections of attendees have any attendees in common.
   *
   * @param eventAttendees    a collection of attendees for a certain event
   * @param requestAttendees  a collection of the requested attendees for the meeting
   * @return                  whether or not the two collections have at least one attendee in common
   */
  private static boolean hasRelevantAttendees(Collection<String> eventAttendees, Collection<String> requestAttendees) {
    return !Collections.disjoint(eventAttendees, requestAttendees);
  }

  /**
   * Finds all common attendees between two collections.
   *
   * @param eventAttendees    a collection of attendees for a certain event
   * @param requestAttendees  a collection of the requested attendees for the meeting
   * @return                  a collection of all the attendees that are in both collections
   */
  private static Set<String> findRelevantAttendees(Collection<String> eventAttendees, Collection<String> requestAttendees) {
    Set<String> commonAttendees = new HashSet<>();
    for (String attendee : eventAttendees) {
      if (requestAttendees.contains(attendee)) {
        commonAttendees.add(attendee);
      }
    }
    return commonAttendees;
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
   * Takes in a list of busy time ranges to find all free time ranges based on duration of the meeting.
   * 
   * @param busyTimes        list of busy time ranges
   * @param meetingDuration  length of requested meeting
   * @return                 a list containing all free time ranges for the meeting
   */
  private static List<TimeRange> findFreeIntervals(List<TimeRange> busyTimes, long meetingDuration) {
    List<TimeRange> freeTimes = new ArrayList<>();
    if (busyTimes.isEmpty()) {
      freeTimes.add(TimeRange.WHOLE_DAY);
      return freeTimes;
    }

    for (int i = 0; i < busyTimes.size(); i++) {
      TimeRange busyTime = busyTimes.get(i);
      int nextBusyTimeStart = i < busyTimes.size() - 1 ? busyTimes.get(i + 1).start() : TimeRange.END_OF_DAY;
      if (i == 0) {
        TimeRange firstTime = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, busyTime.start(), /* inclusive= */ false);
        if (firstTime.duration() >= meetingDuration) {
          freeTimes.add(firstTime);
        }
      }
      TimeRange freeTime = TimeRange.fromStartEnd(
          busyTime.end(), nextBusyTimeStart, /* inclusive= */ nextBusyTimeStart == TimeRange.END_OF_DAY);
      if (freeTime.duration() >= meetingDuration) {
        freeTimes.add(freeTime);
      }
    }
    return freeTimes;
  }

  /**
   * Creates a nested list where the index i of each list represents that 
   * i + 1 optional attendees can attend the times listed at that index.
   *
   * @param optionalBusyTimesMap  maps each optional attendee's name to a list of the time ranges when they are busy
   * @param meetingDuration       length of the requested meeting
   * @return                      a nested list containing each time range at its corresponding index
   */
  private static List<List<TimeRange>> makeOptionalFreeTimesList(
      Map<String, List<TimeRange>> busyIntervalsMap, long meetingDuration) {
    List<List<TimeRange>> freeTimesList = new ArrayList<>();
    int numOptionalAttendees = busyIntervalsMap.size();
    for (int i = 0; i < numOptionalAttendees; i++) {
      freeTimesList.add(new ArrayList<TimeRange>());
    }
    for (Map.Entry entry : busyIntervalsMap.entrySet()) {
      List<TimeRange> freeTimes = findFreeIntervals((List<TimeRange>) entry.getValue(), meetingDuration);
      for (TimeRange range : freeTimes) {
        addToFreeTimesList(freeTimesList, range, numOptionalAttendees, meetingDuration);
      }
    }
    return freeTimesList;
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
      List<List<TimeRange>> freeTimes, TimeRange freeRange, int numOptionalAttendees, long meetingDuration) {
    for (int i = numOptionalAttendees - 2; i >= 0; i--) {
      for (TimeRange existingRange : freeTimes.get(i)) {
        TimeRange intersection = freeRange.intersection(existingRange);
        if (intersection != null && intersection.duration() >= meetingDuration) {
          freeTimes.get(i + 1).add(intersection);
        }
      }
    }
    if (!freeTimes.isEmpty()) {
      freeTimes.get(0).add(freeRange);
    }
  }

  /**
   * Maps each optional attendee to a list of intervals when they are busy.
   *
   * @param orderedEvents     priority queue of events sorted by increasing start time
   * @param requestAttendees  requested attendees for the meeting
   * @return                  map containing each optional attendee and their busy intervals
   */
  private static Map<String, List<TimeRange>> findIndividualBusyIntervals(
      PriorityQueue<Event> orderedEvents, Collection<String> requestAttendees) {
    Map<String, List<TimeRange>> individualBusyIntervals = new HashMap<>();
    while (!orderedEvents.isEmpty()) {
      Event currEvent = orderedEvents.poll();
      if (hasRelevantAttendees(currEvent.getAttendees(), requestAttendees)) {
        TimeRange currEventTime = currEvent.getWhen();
        Set<String> relevantAttendees = findRelevantAttendees(currEvent.getAttendees(), requestAttendees);
        for (String attendee : relevantAttendees) {
          List<TimeRange> busyTimes = individualBusyIntervals.getOrDefault(attendee, new ArrayList<>());
          if (busyTimes.isEmpty()) {
            busyTimes.add(currEventTime);
          } else {
            int lastInd = busyTimes.size() - 1;
            TimeRange prevEventTime = busyTimes.get(lastInd);
            if (prevEventTime.end() < currEventTime.start()) {
              busyTimes.add(currEventTime);
            } else if (prevEventTime.end() < currEventTime.end()) {
              busyTimes.set(lastInd, TimeRange.fromStartEnd(prevEventTime.start(), currEventTime.end(),
                  /* inclusive= */ currEventTime.end() == TimeRange.END_OF_DAY));
            }
          }
          individualBusyIntervals.put(attendee, busyTimes);
        }
      }
    }
    return individualBusyIntervals;
  }

  /**
   * Finds busy intervals for relevant attendees from a PQ of events ordered by start time.
   *
   * @param orderedEvents     priority queue of events sorted by increasing start time
   * @param requestAttendees  requested attendees for the meeting
   * @return                  a list of busy time ranges
   */  
  private static List<TimeRange> findBusyIntervals(
      PriorityQueue<Event> orderedEvents, Collection<String> requestAttendees) {
    List<TimeRange> busyTimes = new ArrayList<>();
    int busyTimesInd = 0;

    while (!orderedEvents.isEmpty()) {
      Event currEvent = orderedEvents.poll();
      if (hasRelevantAttendees(currEvent.getAttendees(), requestAttendees)) {
        if (busyTimesInd == 0) {
          busyTimes.add(currEvent.getWhen());
          busyTimesInd++;
        } else {
          TimeRange prevEventTime = busyTimes.get(busyTimesInd - 1);
          TimeRange currEventTime = currEvent.getWhen();
          if (prevEventTime.end() < currEventTime.start()) {
            busyTimes.add(currEventTime);
            busyTimesInd++;
          } else if (prevEventTime.end() < currEventTime.end()) {
            busyTimes.set(busyTimesInd - 1, TimeRange.fromStartEnd(
                prevEventTime.start(), currEventTime.end(), /* inclusive= */ false));
          }
        }
      }
    }
    return busyTimes;
  }
}