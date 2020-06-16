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
    Set<TimeRange> mandatoryBusyTimes = new HashSet<>();
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

    Map<String, List<TimeRange>> optionalFreeTimesMap = makeFreeTimesMap(optionalBusyTimes, request.getDuration());

    List<List<TimeRange>> optionalFreeTimes = makeOptionalFreeTimesList(optionalFreeTimesMap, request.getDuration());
    
    Collection<TimeRange> bothFreeTimes;
    for (int i = optionalFreeTimes.size() - 1; i >= 0; i--) {
      for (TimeRange time : optionalFreeTimes.get(i)) {
        System.out.println("free: " + time);
      }
      if (request.getAttendees().size() == 0 && optionalFreeTimes.get(i).size() != 0) {
        return optionalFreeTimes.get(i);
      }
      bothFreeTimes = findIntersectionFreeTimes(mandatoryFreeTimes, optionalFreeTimes.get(i), request.getDuration());
      if (bothFreeTimes.size() > 0) {
        return bothFreeTimes;
      }
    }
    return mandatoryFreeTimes;
  }

  private static boolean hasCommonAttendees(Collection<String> eventAttendees, Collection<String> requestAttendees) {
    return !Collections.disjoint(eventAttendees, requestAttendees);
  }

  private static Collection<String> getCommonAttendees(Collection<String> eventAttendees, Collection<String> requestAttendees) {
    Collection<String> commonAttendees = new HashSet<>();
    for (String person : eventAttendees) {
      if (requestAttendees.contains(person)) {
        commonAttendees.add(person);
      }
    }
    return commonAttendees;
  }

  // At index i of optionalFreeTimes, i + 1 optional attendees can attend any of the given time ranges.
  private static List<List<TimeRange>> makeOptionalFreeTimesList(
      Map<String, List<TimeRange>> optionalFreeTimesMap, long meetingDuration) {
    List<List<TimeRange>> optionalFreeTimes = new ArrayList<>();
    int numOptionalAttendees = optionalFreeTimesMap.size();
    for (int i = 0; i < numOptionalAttendees; i++) {
      optionalFreeTimes.add(new ArrayList<TimeRange>());
    }
    for (Map.Entry entry : optionalFreeTimesMap.entrySet()) {
      List<TimeRange> currRanges = (List<TimeRange>) entry.getValue();
      for (TimeRange range : currRanges) {
        for (int i = numOptionalAttendees - 1; i >= 0; i--) {
          for (TimeRange existingRange : optionalFreeTimes.get(i)) {
            TimeRange intersection = range.intersection(existingRange);
            if (i < numOptionalAttendees - 1 && 
                intersection != null && intersection.duration() >= meetingDuration) {
              optionalFreeTimes.get(i + 1).add(intersection);
            }
          }
          if (i == 0) {
            optionalFreeTimes.get(i).add(range);
          }
        }
      }
    }
    return optionalFreeTimes;
  }

  // Maps each optional attendee to a list of their free times.
  private static Map<String, List<TimeRange>> makeFreeTimesMap(
      Map<String, Set<TimeRange>> optionalBusyTimes, long meetingDuration) {
    HashMap<String, List<TimeRange>> optionalFreeTimesMap = new HashMap<>();
    for (Map.Entry entry : optionalBusyTimes.entrySet()) {
      String attendeeName = (String) entry.getKey();

      System.out.println(attendeeName);

      Set<TimeRange> attendeeBusyTimes = (Set<TimeRange>) entry.getValue();

      for (TimeRange time : attendeeBusyTimes) {
        System.out.println("busy: " + time);
      }

      optionalFreeTimesMap.put(attendeeName, findFreeTimes(attendeeBusyTimes, meetingDuration));
    }
    return optionalFreeTimesMap;
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
          busyTimes.add(TimeRange.fromStartEnd(eventTime.start(), currRange.end(), /* inclusive= */ false));
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