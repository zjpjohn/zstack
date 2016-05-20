package org.zstack.storage.backup.sftp;

import org.zstack.header.message.MessageReply;

public class GetSftpBackupStorageDownloadCredentialReply extends MessageReply {
    private String sshKey;
    private String hostname;
    private int sshPort;
    public int getSshPort() {
        return sshPort;
    }
    public void setSshPort(int sshPort) {
        this.sshPort = sshPort;
    }
    public String getSshKey() {
        return sshKey;
    }
    public void setSshKey(String sshKey) {
        this.sshKey = sshKey;
    }
    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
}
