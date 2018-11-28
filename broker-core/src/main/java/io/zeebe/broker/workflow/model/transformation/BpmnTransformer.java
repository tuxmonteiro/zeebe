/*
 * Zeebe Broker Core
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.zeebe.broker.workflow.model.transformation;

import io.zeebe.broker.workflow.model.element.ExecutableWorkflow;
import io.zeebe.broker.workflow.model.transformation.transformer.ActivityTransformer;
import io.zeebe.broker.workflow.model.transformation.transformer.BoundaryEventTransformer;
import io.zeebe.broker.workflow.model.transformation.transformer.CatchEventTransformer;
import io.zeebe.broker.workflow.model.transformation.transformer.ContextProcessTransformer;
import io.zeebe.broker.workflow.model.transformation.transformer.EndEventTransformer;
import io.zeebe.broker.workflow.model.transformation.transformer.EventBasedGatewayTransformer;
import io.zeebe.broker.workflow.model.transformation.transformer.ExclusiveGatewayTransformer;
import io.zeebe.broker.workflow.model.transformation.transformer.FlowElementInstantiationTransformer;
import io.zeebe.broker.workflow.model.transformation.transformer.FlowNodeTransformer;
import io.zeebe.broker.workflow.model.transformation.transformer.IntermediateCatchEventTransformer;
import io.zeebe.broker.workflow.model.transformation.transformer.MessageTransformer;
import io.zeebe.broker.workflow.model.transformation.transformer.ParallelGatewayTransformer;
import io.zeebe.broker.workflow.model.transformation.transformer.ProcessTransformer;
import io.zeebe.broker.workflow.model.transformation.transformer.ReceiveTaskTransformer;
import io.zeebe.broker.workflow.model.transformation.transformer.SequenceFlowTransformer;
import io.zeebe.broker.workflow.model.transformation.transformer.ServiceTaskTransformer;
import io.zeebe.broker.workflow.model.transformation.transformer.StartEventTransformer;
import io.zeebe.broker.workflow.model.transformation.transformer.SubProcessTransformer;
import io.zeebe.model.bpmn.BpmnModelInstance;
import io.zeebe.model.bpmn.traversal.ModelWalker;
import io.zeebe.msgpack.jsonpath.JsonPathQueryCompiler;
import java.util.List;

public class BpmnTransformer {

  /*
   * Step 1: Instantiate all elements in the workflow
   */
  private final TransformationVisitor step1Visitor;

  /*
   * Step 2: Transform all attributes, cross-link elements, etc.
   */
  private final TransformationVisitor step2Visitor;

  /*
   * Step 3: Modify elements based on the context
   */
  private final TransformationVisitor step3Visitor;

  private final JsonPathQueryCompiler jsonPathQueryCompiler = new JsonPathQueryCompiler();

  public BpmnTransformer() {
    this.step1Visitor = new TransformationVisitor();
    step1Visitor.registerHandler(new FlowElementInstantiationTransformer());
    step1Visitor.registerHandler(new MessageTransformer());
    step1Visitor.registerHandler(new ProcessTransformer());

    this.step2Visitor = new TransformationVisitor();
    step2Visitor.registerHandler(new ActivityTransformer());
    step2Visitor.registerHandler(new BoundaryEventTransformer());
    step2Visitor.registerHandler(new CatchEventTransformer());
    step2Visitor.registerHandler(new ContextProcessTransformer());
    step2Visitor.registerHandler(new EndEventTransformer());
    step2Visitor.registerHandler(new ExclusiveGatewayTransformer());
    step2Visitor.registerHandler(new FlowNodeTransformer());
    step2Visitor.registerHandler(new IntermediateCatchEventTransformer());
    step2Visitor.registerHandler(new ParallelGatewayTransformer());
    step2Visitor.registerHandler(new SequenceFlowTransformer());
    step2Visitor.registerHandler(new ServiceTaskTransformer());
    step2Visitor.registerHandler(new ReceiveTaskTransformer());
    step2Visitor.registerHandler(new StartEventTransformer());
    step2Visitor.registerHandler(new SubProcessTransformer());

    this.step3Visitor = new TransformationVisitor();
    step3Visitor.registerHandler(new ContextProcessTransformer());
    step3Visitor.registerHandler(new EventBasedGatewayTransformer());
  }

  public List<ExecutableWorkflow> transformDefinitions(BpmnModelInstance modelInstance) {
    final TransformContext context = new TransformContext();
    context.setJsonPathQueryCompiler(jsonPathQueryCompiler);

    final ModelWalker walker = new ModelWalker(modelInstance);
    step1Visitor.setContext(context);
    walker.walk(step1Visitor);

    step2Visitor.setContext(context);
    walker.walk(step2Visitor);

    step3Visitor.setContext(context);
    walker.walk(step3Visitor);

    return context.getWorkflows();
  }
}
