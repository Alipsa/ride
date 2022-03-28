package se.alipsa.ride.utils.git;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;
import org.eclipse.jgit.util.FS;
import se.alipsa.ride.utils.FileUtils;

import java.io.File;

/** If the DefaultSshSessionFactory is not working, we can roll our own and
 * insert it into the command (like below for a pullCommand):
 * <code>
 * SshSessionFactory sshSessionFactory = new RideSshSessionFactory();
 *       pullCommand.setTransportConfigCallback( new TransportConfigCallback() {
 *         public void configure( Transport transport ) {
 *           SshTransport sshTransport = ( SshTransport )transport;
 *           sshTransport.setSshSessionFactory( sshSessionFactory );
 *         }
 *       } );
 * </code>
 */
public class RideSshSessionFactory extends JschConfigSessionFactory {

  @Override
  protected void configure(OpenSshConfig.Host hc, Session session) {
    // Nothing
  }

  @Override
  public synchronized RemoteSession getSession(URIish uri,
                                               CredentialsProvider credentialsProvider, FS fs, int tms)
      throws TransportException {
    return super.getSession(uri, credentialsProvider, fs, tms);
  }

  @Override
  protected JSch createDefaultJSch(FS fs) throws JSchException {
    File sshDir = new File(FileUtils.getUserHome(), ".ssh");
    File knownHosts = new File(sshDir, "known_hosts");
    JSch defaultJSch = super.createDefaultJSch(fs);
    if (knownHosts.exists()) {
      defaultJSch.setKnownHosts(knownHosts.getAbsolutePath());
    }
    return defaultJSch;
  }
}
