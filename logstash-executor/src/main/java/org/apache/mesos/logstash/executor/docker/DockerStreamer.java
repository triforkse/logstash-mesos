package org.apache.mesos.logstash.executor.docker;

import org.apache.mesos.logstash.executor.logging.HeartbeatFilterOutputStream;
import org.apache.mesos.logstash.executor.logging.LogStream;
import org.apache.mesos.logstash.executor.logging.LogStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DockerStreamer {

    private static final String BASH_COMMAND = "touch %s; while sleep 3; do echo '%c HEARTBEAT'; done & tail -F %s";
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerStreamer.class);

    private final LogStreamWriter writer;
    private final ContainerizerClient client;

    public DockerStreamer(LogStreamWriter writer, ContainerizerClient client) {

        this.writer = writer;
        this.client = client;
    }

    public LogStream startStreaming(DockerLogPath path) {
        LOGGER
            .info("Stream from " + path.getContainerLogPath() + " to " + path.getExecutorLogPath());

        LogStream logStream = createContainerLogStream(path.getContainerId(),
            path.getContainerLogPath());
        try {
            writer.write(path.getExecutorLogPath(), logStream);

            LOGGER.info(String.format("Logging file: %s", path.getExecutorLogPath()));

            return logStream;

        } catch (IOException e) {
            LOGGER.error("Failed", e);
            logStream.close();
            return null;
        }
    }

    private LogStream createContainerLogStream(String containerId, String logLocation) {
        String MONITOR_CMD = getMonitorCmd(logLocation);
        LOGGER.info("Running: " + MONITOR_CMD);
        return client.exec(containerId, "sh", "-c", MONITOR_CMD);
    }

    String getMonitorCmd(String logLocation) {
        return String
            .format(BASH_COMMAND, logLocation, HeartbeatFilterOutputStream.MAGIC_CHARACTER,
                logLocation);
    }

    public void stopStreaming(LogStream logStream) {
        logStream.close();
    }
}