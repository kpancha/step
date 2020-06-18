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
    if (events.isEmpty() || request.getAttendees().isEmpty()) {
      freeTimes.add(TimeRange.WHOLE_DAY);
      return freeTimes;
    }
    
    PriorityQueue<Event> orderedEvents = new PriorityQueue<>(Event.ORDER_BY_START);
    orderedEvents.addAll(events);
    List<TimeRange> busyTimes = findBusyIntervals(orderedEvents, request.getAttendees());

    return findFreeIntervals(busyTimes, request.getDuration());
  }

  // Determines if the two collections of attendees have any in common.
  private static boolean hasRelevantAttendees(Collection<String> eventAttendees, Collection<String> requestAttendees) {
    return !Collections.disjoint(eventAttendees, requestAttendees);
  }

  // Finds free intervals that are long enough for the meeting from an ordered list of busy intervals.
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

  // Finds busy intervals for relevant attendees from a PQ of events ordered by start time.
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