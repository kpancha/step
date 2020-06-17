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
    
    PriorityQueue<Event> orderedEvents = new PriorityQueue<>(Event.ORDER_BY_START);
    orderedEvents.addAll(events);
    List<TimeRange> busyTimes = new ArrayList<>();
    int busyTimesInd = 0;

    while (!orderedEvents.isEmpty()) {
      Event currEvent = orderedEvents.poll();
      System.out.println(currEvent.getWhen());
      if (hasRelevantAttendees(currEvent.getAttendees(), request.getAttendees())) {
        if (busyTimesInd == 0) {
          busyTimes.add(currEvent.getWhen());
          busyTimesInd++;
        } else {
          TimeRange prevEventTime = busyTimes.get(busyTimesInd - 1);
          TimeRange currEventTime = currEvent.getWhen();
          if (prevEventTime.end() < currEventTime.start()) {
            busyTimes.add(currEventTime);
            busyTimesInd++;
          } else {
            busyTimes.set(busyTimesInd - 1, TimeRange.fromStartEnd(
                prevEventTime.start(), currEventTime.end(), /* inclusive= */ false));
          }
        }
      }
    }

    List<TimeRange> freeTimes = new ArrayList<>();
    for (int i = 0; i < busyTimes.size() - 1; i++) {
      TimeRange busyTime = busyTimes.get(i);
      TimeRange nextBusyTime = busyTimes.get(i + 1);
      if (i == 0 && busyTime.start() != TimeRange.START_OF_DAY) {
        TimeRange firstTime = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, busyTime.start(), /* inclusive= */ false);
        if (firstTime.duration() >= request.getDuration()) {
          freeTimes.add(firstTime);
        }
      }
      TimeRange freeTime = TimeRange.fromStartEnd(busyTime.end(), nextBusyTime.start(), /* inclusive= */ false);
      if (freeTime.duration() >= request.getDuration()) {
        freeTimes.add(freeTime);
      }

      if (i == busyTimes.size() - 2) {
        TimeRange lastTime = TimeRange.fromStartEnd(nextBusyTime.end(), TimeRange.END_OF_DAY, /* inclusive= */ true);
        if (lastTime.duration() >= request.getDuration()) {
          freeTimes.add(lastTime);
        }
      }
    }
    return freeTimes;
  }

  private static boolean hasRelevantAttendees(Collection<String> eventAttendees, Collection<String> requestAttendees) {
    return !Collections.disjoint(eventAttendees, eventAttendees);
  }
}