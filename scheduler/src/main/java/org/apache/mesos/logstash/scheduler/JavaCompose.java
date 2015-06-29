package org.apache.mesos.logstash.scheduler;


import com.spotify.docker.client.*;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;

import org.apache.zookeeper.server.quorum.QuorumPeerMain;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import static java.util.concurrent.TimeUnit.HOURS;

/**
 * Created by peldan on 29/06/15.
 */
public class JavaCompose {

    private DockerClient dockerClient;
    private String mesosId;

    public static void main(String[] args) {
        new JavaCompose().run();
    }


    public void run() {
//        DockerClientConfig config = DockerClientConfig.createDefaultConfigBuilder()
//                .withVersion("1.16")
//                .withUri("https://192.168.59.103:2376")
//                .withDockerCertPath("/Users/peldan/.docker")
//                .build();
//
//        DockerClient docker = DockerClientBuilder.getInstance(config).build();

//        new   Thread(new Runnable(){
//
//            @Override
//            public void run() {
//                QuorumPeerMain.main(new String[]{"src/main/resources/zoo.cfg"});
//            }
//        }).start();

//        CreateContainerResponse r = docker.createContainerCmd("mesos-local").exec();
//        docker.startContainerCmd(r.getId()).exec();
//
//        InputStream is =  docker.attachContainerCmd(r.getId()).exec();
//        try {
//            for(int i = is.read(); i != -1; i = is.read()) System.out.print((char)i);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        try {
            dockerClient = DefaultDockerClient.builder()
                    .readTimeoutMillis(HOURS.toMillis(1))
                    .dockerCertificates(DockerCertificates.builder().dockerCertPath(FileSystems.getDefault().getPath("/Users/peldan/.docker")).
                            build())
            .uri(URI.create("https://192.168.59.103:2376"))
                    .build();
        } catch (DockerCertificateException e) {
            e.printStackTrace();
        }


        try {
            ContainerCreation c = dockerClient.createContainer(ContainerConfig.builder().image("mesos-local").attachStdout(true).build());
            mesosId = c.id();

            dockerClient.startContainer(mesosId);
            com.spotify.docker.client.LogStream logStream = dockerClient.attachContainer(mesosId, DockerClient.AttachParameter.STDOUT);

            logStream.attach(new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    System.out.print(b);
                }
            }, System.err);
        } catch (DockerException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    dockerClient.stopContainer(mesosId, 15);
                } catch (DockerException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }));

        try {
            throw new RuntimeException("boom");
        }
        catch(RuntimeException e) {
            e.printStackTrace();
        }
    }
}
