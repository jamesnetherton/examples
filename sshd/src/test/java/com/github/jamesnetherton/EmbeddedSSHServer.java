/*
 * #%L
 * Wildfly Camel :: Testsuite :: Common
 * %%
 * Copyright (C) 2013 - 2016 RedHat
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.github.jamesnetherton;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.ProcessShellFactory;

public class EmbeddedSSHServer {

    private SshServer sshServer;
    private Path homeDir;

    public EmbeddedSSHServer(Path homeDir) {
        this(homeDir,1100);
    }

    public EmbeddedSSHServer(Path homeDir, int port) {
        this.sshServer = SshServer.setUpDefaultServer();
        this.sshServer.setPort(port);
        this.sshServer.setCommandFactory(new ScpCommandFactory(command -> new ProcessShellFactory(command.split(" ")).create()));
        this.sshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        this.sshServer.setPasswordAuthenticator((username, password, serverSession) -> username.equals("admin") && password.equals("admin"));
        this.sshServer.setPublickeyAuthenticator((s, publicKey, serverSession) -> true);
        this.homeDir = homeDir;
        this.sshServer.setShellFactory(new ProcessShellFactory(new String[] { "/bin/sh", "-i", "-s" }));
    }

    public void start() throws Exception {
        sshServer.start();
        setupKnownHosts();
    }

    public void stop() throws Exception {
        sshServer.stop();
    }

    public String getConnection() {
        return String.format("localhost:%d", sshServer.getPort());
    }

    public void setSubsystemFactories(List<NamedFactory<Command>> factories) {
        sshServer.setSubsystemFactories(factories);
    }

    private void setupKnownHosts() {
        // Add a localhost entry for the relevant host / port combination to known_hosts
        File knownHostsFile = homeDir.resolve("known_hosts").toFile();

        JSch jsch = new JSch();
        try {
            homeDir.toFile().mkdirs();
            knownHostsFile.createNewFile();

            jsch.setKnownHosts(knownHostsFile.getPath());
            Session s = jsch.getSession("admin", "localhost", sshServer.getPort());
            s.setConfig("StrictHostKeyChecking",  "ask");

            s.setConfig("HashKnownHosts",  "no");
            s.setUserInfo(new UserInfo() {
                @Override
                public String getPassphrase() {
                    return null;
                }
                @Override
                public String getPassword() {
                    return "admin";
                }
                @Override
                public boolean promptPassword(String message) {
                    return true;
                }
                @Override
                public boolean promptPassphrase(String message) {
                    return false;
                }
                @Override
                public boolean promptYesNo(String message) {
                    return true;
                }
                @Override
                public void showMessage(String message) {
                }
            });

            s.connect();
            s.disconnect();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to configure known_hosts file", e);
        }
    }
}
