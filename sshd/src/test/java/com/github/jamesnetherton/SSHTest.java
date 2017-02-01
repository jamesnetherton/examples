package com.github.jamesnetherton;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Paths;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.sshd.ClientChannel;
import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.future.OpenFuture;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SSHTest {

    private EmbeddedSSHServer sshServer;

    @Before
    public void before() throws Exception {
        sshServer = new EmbeddedSSHServer(Paths.get("target/sshd"));
        sshServer.start();
    }

    @After
    public void after() throws Exception {
        if (sshServer != null) {
            sshServer.stop();
        }
    }

    @Test
    public void testEcho() throws Exception {
        SshClient client = SshClient.setUpDefaultClient();
        ClientSession session = null;
        ClientChannel channel = null;

        try {
            client.start();

            ConnectFuture connectFuture = client.connect("admin", "localhost", 1100);
            connectFuture.await(5000);

            if (!connectFuture.isDone() || !connectFuture.isConnected()) {
                throw new IllegalStateException("Connect timeout period expired");
            }

            session = connectFuture.getSession();

            AuthFuture authFuture = session.authPassword("admin", "admin");
            authFuture.await(5000);

            if (!authFuture.isDone() || authFuture.isFailure()) {
                throw new IllegalStateException("Authentication Failed");
            }

//            session.addPasswordIdentity("admin");
//            session.auth().verify();

            ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{0});
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();

            channel = session.createChannel(ClientChannel.CHANNEL_EXEC, "echo Hello Kermit");
//            channel = session.createExecChannel("echo Hello Kermit");
            channel.setIn(in);
            channel.setOut(out);
            channel.setErr(err);

            OpenFuture openFuture = channel.open();
            openFuture.await(5000);
            if (openFuture.isOpened()) {
                channel.waitFor(ClientChannel.CLOSED, 0);
            }

            Assert.assertEquals("Hello Kermit\n", new String(out.toByteArray()));
        } finally {
            if (channel != null) {
                channel.close(true);
            }
            // need to make sure the session is closed
            if (session != null) {
                session.close(false);
            }

            client.stop();
        }
    }

    @Test
    public void testCamelSSH() throws Exception {
        DefaultCamelContext camelContext = new DefaultCamelContext();
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start")
                .to("ssh://admin@localhost:1100?username=admin&password=admin");
            }
        });

        camelContext.start();
        try {
            ProducerTemplate template = camelContext.createProducerTemplate();
            String result = template.requestBody("direct:start", "echo Hello Kermit", String.class);
            Assert.assertEquals("Hello Kermit" + System.lineSeparator(), result);
        } finally {
            camelContext.stop();
        }
    }
}
