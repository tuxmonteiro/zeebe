/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zeebe.model.bpmn.validation;

import static io.zeebe.model.bpmn.validation.ExpectedValidationResult.expect;
import static java.util.Collections.singletonList;

import io.zeebe.model.bpmn.Bpmn;
import io.zeebe.model.bpmn.instance.TimerEventDefinition;
import org.junit.runners.Parameterized.Parameters;

public class ZeebeTimerValidationTest extends AbstractZeebeValidationTest {

  @Parameters(name = "{index}: {1}")
  public static Object[][] parameters() {
    return new Object[][] {
      {
        Bpmn.createExecutableProcess("process")
            .startEvent()
            .intermediateCatchEvent("catch", c -> c.timerWithDuration(""))
            .endEvent()
            .done(),
        singletonList(expect(TimerEventDefinition.class, "Time duration is invalid"))
      },
      {
        Bpmn.createExecutableProcess("process")
            .startEvent()
            .intermediateCatchEvent("catch", c -> c.timerWithDuration("R/PT01S"))
            .endEvent()
            .done(),
        singletonList(expect(TimerEventDefinition.class, "Time duration is invalid"))
      },
      {
        Bpmn.createExecutableProcess("process")
            .startEvent()
            .intermediateCatchEvent("catch", c -> c.timerWithCycle("R5/PT05S"))
            .endEvent()
            .done(),
        singletonList(
            expect(
                TimerEventDefinition.class, "Time cycles must be non-interrupting boundary events"))
      },
      {
        Bpmn.createExecutableProcess("process")
            .startEvent()
            .serviceTask("task", b -> b.zeebeTaskType("type"))
            .boundaryEvent("catch")
            .timerWithCycle("R5/PT05S")
            .endEvent()
            .done(),
        singletonList(
            expect(
                TimerEventDefinition.class, "Time cycles must be non-interrupting boundary events"))
      },
      {
        Bpmn.createExecutableProcess("process")
            .startEvent()
            .serviceTask("task", b -> b.zeebeTaskType("type"))
            .boundaryEvent("catch")
            .cancelActivity(false)
            .timerWithCycle("R5/")
            .endEvent()
            .done(),
        singletonList(expect(TimerEventDefinition.class, "Time cycle is invalid"))
      },
      {
        Bpmn.createExecutableProcess("process")
            .startEvent()
            .intermediateCatchEvent("catch", c -> c.timerWithDuration("foo"))
            .endEvent()
            .done(),
        singletonList(expect(TimerEventDefinition.class, "Time duration is invalid"))
      },
      {
        Bpmn.createExecutableProcess("process")
            .startEvent()
            .intermediateCatchEvent("catch", b -> b.timerWithDate("2017-01-01"))
            .endEvent()
            .done(),
        singletonList(
            expect(
                TimerEventDefinition.class,
                "Timer event definitions with timeDate are not supported"))
      },
    };
  }
}
