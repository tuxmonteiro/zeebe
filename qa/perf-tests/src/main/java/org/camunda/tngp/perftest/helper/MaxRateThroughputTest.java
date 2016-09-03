package org.camunda.tngp.perftest.helper;

import static org.camunda.tngp.client.ClientProperties.CLIENT_MAXREQUESTS;
import static org.camunda.tngp.perftest.helper.TestHelper.printProperties;

import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.camunda.tngp.client.ClientProperties;
import org.camunda.tngp.client.TngpClient;
import org.camunda.tngp.perftest.reporter.FileReportWriter;
import org.camunda.tngp.perftest.reporter.RateReporter;
import org.camunda.tngp.perftest.reporter.SysoutRateReportFn;
import org.camunda.tngp.transport.requestresponse.client.TransportConnection;

public abstract class MaxRateThroughputTest
{
    public static final String TEST_WARMUP_TIMEMS = "test.warmup.timems";
    public static final String TEST_TIMEMS = "test.timems";
    public static final String TEST_MAX_CONCURRENT_REQUESTS = "2048";
    public static final String TEST_OUTPUT_FILE_NAME = "test.outputFileName";

    public void run()
    {
        final Properties properties = System.getProperties();

        setDefaultProperties(properties);
        ClientProperties.setDefaults(properties);

        printProperties(properties);

        TngpClient client = null;

        try
        {
            client = TngpClient.create(properties);
            client.connect();

            executeSetup(properties, client);
            executeWarmup(properties, client);
            executeTest(properties, client);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            client.close();
        }

    }

    protected void setDefaultProperties(final Properties properties)
    {
        properties.putIfAbsent(TEST_WARMUP_TIMEMS, "30000");
        properties.putIfAbsent(TEST_TIMEMS, "30000");
        properties.putIfAbsent(TEST_MAX_CONCURRENT_REQUESTS, "2048");
        properties.putIfAbsent(TEST_OUTPUT_FILE_NAME, "data/output.txt");
        properties.putIfAbsent(CLIENT_MAXREQUESTS, "2048");
    }

    protected void executeSetup(Properties properties, TngpClient client)
    {
        // noop
    }

    @SuppressWarnings("rawtypes")
    protected void executeWarmup(Properties properties, TngpClient client)
    {
        try (final TransportConnection conection = client.getConnectionPool().openConnection())
        {
            System.out.format("Executing warmup\n");

            final int warmupTimeMs = Integer.parseInt(properties.getProperty(TEST_WARMUP_TIMEMS));
            final int maxConcurrentRequests = Integer.parseInt(properties.getProperty(TEST_MAX_CONCURRENT_REQUESTS));

            final Supplier<Future> requestFn = requestFn(client, conection);

            final RateReporter rateReporter = new RateReporter(1, TimeUnit.SECONDS, new SysoutRateReportFn());

            new Thread()
            {
                @Override
                public void run()
                {
                    rateReporter.doReport();
                }

            }.start();

            TestHelper.executeAtMaxRate(requestFn, rateReporter, warmupTimeMs, maxConcurrentRequests);

            rateReporter.exit();

            System.out.format("Finished warmup.\n");
        }

        TestHelper.gc();
    }

    @SuppressWarnings("rawtypes")
    protected void executeTest(Properties properties, TngpClient client)
    {
        try (final TransportConnection conection = client.getConnectionPool().openConnection())
        {
            System.out.format("Executing test\n");

            final int warmupTimeMs = Integer.parseInt(properties.getProperty(TEST_TIMEMS));
            final int maxConcurrentRequests = Integer.parseInt(properties.getProperty(TEST_MAX_CONCURRENT_REQUESTS));
            final String outputFileName = properties.getProperty(TEST_OUTPUT_FILE_NAME);

            final FileReportWriter fileReportWriter = new FileReportWriter();
            final RateReporter rateReporter = new RateReporter(1, TimeUnit.SECONDS, fileReportWriter);

            new Thread()
            {
                @Override
                public void run()
                {
                    rateReporter.doReport();
                }

            }.start();

            final Supplier<Future> requestFn = requestFn(client, conection);

            TestHelper.executeAtMaxRate(requestFn, rateReporter, warmupTimeMs, maxConcurrentRequests);

            System.out.format("Finished test.\n");

            rateReporter.exit();

            fileReportWriter.writeToFile(outputFileName);
        }

        TestHelper.gc();
    }

    protected abstract Supplier<Future> requestFn(TngpClient client, TransportConnection conection);
}
