package org.camunda.tngp.client.task.impl;

import java.time.Duration;

import org.camunda.tngp.client.event.impl.EventAcquisition;
import org.camunda.tngp.client.impl.TaskTopicClientImpl;
import org.camunda.tngp.client.impl.data.MsgPackMapper;
import org.camunda.tngp.client.task.PollableTaskSubscription;
import org.camunda.tngp.client.task.PollableTaskSubscriptionBuilder;
import org.camunda.tngp.util.EnsureUtil;

public class PollableTaskSubscriptionBuilderImpl implements PollableTaskSubscriptionBuilder
{

    protected int taskFetchSize = TaskSubscriptionBuilderImpl.DEFAULT_TASK_FETCH_SIZE;
    protected String taskType;
    protected long lockTime = Duration.ofMinutes(1).toMillis();
    protected int lockOwner = -1;

    protected final TaskTopicClientImpl taskClient;
    protected final EventAcquisition<TaskSubscriptionImpl> taskAcquisition;
    protected final boolean autoCompleteTasks;
    protected final MsgPackMapper msgPackMapper;

    public PollableTaskSubscriptionBuilderImpl(
            TaskTopicClientImpl taskClient,
            EventAcquisition<TaskSubscriptionImpl> taskAcquisition,
            boolean autoCompleteTasks,
            MsgPackMapper msgPackMapper)
    {
        this.taskClient = taskClient;
        this.taskAcquisition = taskAcquisition;
        this.autoCompleteTasks = autoCompleteTasks;
        this.msgPackMapper = msgPackMapper;
    }

    @Override
    public PollableTaskSubscriptionBuilder taskType(String taskType)
    {
        this.taskType = taskType;
        return this;
    }

    @Override
    public PollableTaskSubscriptionBuilder lockTime(long lockDuration)
    {
        this.lockTime = lockDuration;
        return this;
    }

    @Override
    public PollableTaskSubscriptionBuilder lockTime(Duration lockDuration)
    {
        return lockTime(lockDuration.toMillis());
    }

    @Override
    public PollableTaskSubscriptionBuilder lockOwner(int lockOwner)
    {
        this.lockOwner = lockOwner;
        return this;
    }

    @Override
    public PollableTaskSubscriptionBuilderImpl taskFetchSize(int numTasks)
    {
        this.taskFetchSize = numTasks;
        return this;
    }

    @Override
    public PollableTaskSubscription open()
    {
        EnsureUtil.ensureNotNullOrEmpty("taskType", taskType);
        EnsureUtil.ensureGreaterThan("lockTime", lockTime, 0L);
        EnsureUtil.ensureGreaterThanOrEqual("lockOwner", lockOwner, 0);
        EnsureUtil.ensureGreaterThan("taskFetchSize", taskFetchSize, 0);

        final TaskSubscriptionImpl subscription =
                new TaskSubscriptionImpl(
                        taskClient,
                        null,
                        taskType,
                        lockTime,
                        lockOwner,
                        taskFetchSize,
                        taskAcquisition,
                        msgPackMapper,
                        autoCompleteTasks);

        subscription.open();
        return subscription;
    }
}
