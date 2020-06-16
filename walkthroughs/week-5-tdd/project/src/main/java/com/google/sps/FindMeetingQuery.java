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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Set<TimeRange> mandatoryBusyTimes = new HashSet<>();
    Set<TimeRange> optionalBusyTimes = new HashSet<>();
    for (Event event : events) {
      TimeRange eventTime = event.getWhen();
      if (hasCommonAttendees(event.getAttendees(), request.getAttendees())) {
        boolean added = addToBusySet(mandatoryBusyTimes, eventTime);
        if (!added) {
          mandatoryBusyTimes.add(eventTime);
        }
      } else if (hasCommonAttendees(event.getAttendees(), request.getOptionalAttendees())) {
        boolean added = addToBusySet(optionalBusyTimes, eventTime);
        if (!added) {
          optionalBusyTimes.add(eventTime);
        }
      }
    }

    List<TimeRange> optionalFreeTimes = findFreeTimes(optionalBusyTimes, request.getDuration());
    List<TimeRange> mandatoryFreeTimes = findFreeTimes(mandatoryBusyTimes, request.getDuration());

    if (request.getAttendees().isEmpty()) {
      return optionalFreeTimes;
    } else if (request.getOptionalAttendees().isEmpty()) {
      return mandatoryFreeTimes;
    }

    Collection<TimeRange> bothFreeTimes = findIntersectionFreeTimes(mandatoryFreeTimes, optionalFreeTimes, request.getDuration());
    return bothFreeTimes.isEmpty() ? mandatoryFreeTimes : bothFreeTimes;
  }

  private static boolean hasCommonAttendees(Collection<String> eventAttendees, Collection<String> requestAttendees) {
    return !Collections.disjoint(eventAttendees, requestAttendees);
  }

  // Finds all intersections of time ranges that are at least as long as the meeting duration.
  private static Collection<TimeRange> findIntersectionFreeTimes(
    List<TimeRange> mandatoryFreeTimes, List<TimeRange> optionalFreeTimes, long meetingDuration) {
    Collection<TimeRange> bothFreeTimes = new ArrayList<>();
    for (TimeRange optionalTime : optionalFreeTimes) {
      Iterator iterator = mandatoryFreeTimes.iterator();
      boolean keepGoing = true;
      while (keepGoing && iterator.hasNext()) {
        TimeRange mandatoryTime = (TimeRange) iterator.next();
        if (mandatoryTime.end() > optionalTime.end()) {
          keepGoing = false;
        } else {
          TimeRange intersection = optionalTime.intersection(mandatoryTime);
          if (intersection != null && intersection.duration() >= meetingDuration) {
            bothFreeTimes.add(intersection);
          }
        }
      }
    }
    return bothFreeTimes;
  }

  // Merges a new time range with existing time range in set, if necessary.
  // Returns whether or not the time range was merged.
  private static boolean addToBusySet(Set<TimeRange> busyTimes, TimeRange eventTime) {
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
          busyTimes.add(TimeRange.fromStartEnd(eventTime.start(), currRange.end(), /* inclusive= */ true));
        }
        return true;
      }
    }
    return false;
  }

  // Takes in a set of busy time ranges to find all free time ranges based on duration of the meeting.
  // Returns all possibilities as a list.
  private static List<TimeRange> findFreeTimes(Set<TimeRange> busyTimes, long meetingDuration) {
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