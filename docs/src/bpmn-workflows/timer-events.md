# Timer Events

Timer events are events which are triggered by a defined timer. 

## Timer Intermediate Catch Events

![workflow](/bpmn-workflows/timer-intermediate-catch-event.png)

A timer intermediate event acts as a stopwatch. When a token arrives at the timer intermediate catch event then a timer is started. The timer fires after the specified duration is over and the event is left.

The duration must be defined in the [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601#Durations) format. For example:

* `P1Y2M3DT1H2M3S` - 1 year, two months, 3 days, 1 hour, 2 minutes, and 1 second.
* `PT15S` - 15 seconds
* `PT1H30M` - 1 hour and 30 minutes
* `P14D` - 14 days

Durations can also be negative; a negative timer will fire immediately, and can be expressed as `-PT5S`, or minus 5 seconds.

XML representation:

```xml
<bpmn:intermediateCatchEvent id="wait-for-coffee" name="4 minutes">
  <bpmn:timerEventDefinition>
    <bpmn:timeDuration>PT4M</bpmn:timeDuration>
  </bpmn:timerEventDefinition>
</bpmn:intermediateCatchEvent>
```

### Timer Boundary Events

![workflow](/bpmn-workflows/timer-interrupting-boundary-event.png)

As boundary events, timer catch events can be marked as non-interrupting; as a simple duration, however,
a non-interrupting timer event isn't particularly useful. As such, it is possible to define repeating timers,
that is, timers that are fired every `X` amount of time, where `X` is a duration as specified above.

The notation to express the timer is changed slightly from the above to denote how often a timer should be repeated:

* `R/PT5S` - every 5 seconds, infinitely.
* `R5/PT1S` - every second, up to 5 times.

XML representation:

```xml
<bpmn:intermediateCatchEvent id="wait-for-coffee" name="4 minutes">
  <bpmn:timerEventDefinition>
    <bpmn:timeCycle>R/PT4M</bpmn:timeCycle>
  </bpmn:timerEventDefinition>
</bpmn:intermediateCatchEvent>
```
