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
    Set<TimeRange> busyTimesSet = new HashSet<>();
    for (Event event : events) {
      if (hasCommonAttendees(event.getAttendees(), request.getAttendees())) {
        TimeRange eventTime = event.getWhen();
        boolean added = addToBusySet(busyTimesSet, eventTime);
        if (!added) {
          busyTimesSet.add(eventTime);
        }
      }
    }
    return getFreeTimesFromBusySet(busyTimesSet, request.getDuration());
  }

  private boolean hasCommonAttendees(Collection<String> eventAttendees, Collection<String> requestAttendees) {
    return !Collections.disjoint(eventAttendees, requestAttendees);
  }

  private boolean addToBusySet(Set<TimeRange> busyTimesSet, TimeRange eventTime) {
    Iterator iterator = busyTimesSet.iterator();
    while (iterator.hasNext()) {
      TimeRange currRange = (TimeRange) iterator.next();
      if (currRange.contains(eventTime)) {
        return true;
      } else if (eventTime.contains(currRange)) {
        busyTimesSet.remove(currRange);
        busyTimesSet.add(eventTime);
        return true;
      } else if (currRange.overlaps(eventTime)) {
        if (currRange.contains(eventTime.start())) {
          busyTimesSet.remove(currRange);
          busyTimesSet.add(TimeRange.fromStartEnd(currRange.start(), eventTime.end(), false));
        } else {
          busyTimesSet.remove(currRange);
          busyTimesSet.add(TimeRange.fromStartEnd(eventTime.start(), currRange.end(), true));
        }
        return true;
      }
    }
    return false;
  }

  private Collection<TimeRange> getFreeTimesFromBusySet(Set<TimeRange> busyTimesSet, long duration) {
    List<TimeRange> busyTimesList = new ArrayList<>(busyTimesSet);
    Collections.sort(busyTimesList, TimeRange.ORDER_BY_START);
    Collection<TimeRange> freeTimes = new ArrayList<>();
    int startTime = TimeRange.START_OF_DAY;
    int endTime;
    for (TimeRange busyTime : busyTimesList) {
      if (busyTime.start() - startTime >= duration) {
        endTime = busyTime.start();
        freeTimes.add(TimeRange.fromStartEnd(startTime, endTime, false));
      }
      startTime = busyTime.end();
    }
    if (TimeRange.END_OF_DAY - startTime >= duration) {
      endTime = TimeRange.END_OF_DAY;
      freeTimes.add(TimeRange.fromStartEnd(startTime, endTime, true));
    }
    return freeTimes;
  }
}
